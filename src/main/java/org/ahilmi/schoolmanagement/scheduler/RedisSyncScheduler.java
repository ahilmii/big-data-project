package org.ahilmi.schoolmanagement.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.ahilmi.schoolmanagement.dto.PendingProfessorUpdate;
import org.ahilmi.schoolmanagement.service.ProfessorSyncService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
public class RedisSyncScheduler {

    private final StringRedisTemplate pendingRedisTemplate;
    private final ProfessorSyncService professorSyncService;
    private final ObjectMapper objectMapper;
    private final CacheManager cacheManager;

    private static final DefaultRedisScript<Long> DELETE_IF_SAME;

    static {
        DELETE_IF_SAME = new DefaultRedisScript<>();
        DELETE_IF_SAME.setScriptText("""
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            end
            return 0
            """);
        DELETE_IF_SAME.setResultType(Long.class);
    }

    public RedisSyncScheduler(
            @Qualifier("pendingRedisTemplate")
            StringRedisTemplate pendingRedisTemplate,
            ProfessorSyncService professorSyncService,
            ObjectMapper objectMapper,
            CacheManager cacheManager
    ) {
        this.pendingRedisTemplate = pendingRedisTemplate;
        this.professorSyncService = professorSyncService;
        this.objectMapper = objectMapper;
        this.cacheManager = cacheManager;
    }

    @Scheduled(fixedDelayString = "${app.redis-sync.interval-ms:15000}", initialDelayString = "${app.redis-sync.initial-delay-ms:15000}")
    public void syncPendingProfessors() {

        ScanOptions options = ScanOptions.scanOptions()
                .match("pending:professor:*")
                .count(100)
                .build();

        try (Cursor<String> keys = pendingRedisTemplate.scan(options)) {

            while (keys.hasNext()) {
                String key = keys.next();

                try {
                    String json = pendingRedisTemplate
                            .opsForValue()
                            .get(key);

                    if (json == null) {
                        continue;
                    }

                    PendingProfessorUpdate pending =
                            objectMapper.readValue(
                                    json,
                                    PendingProfessorUpdate.class
                            );

                    // Transaction tamamlandıktan sonra devam eder.
                    professorSyncService.persistUpdate(pending);

                    // Yalnızca işlenen değer hâlâ aynıysa sil.
                    Long deleted = pendingRedisTemplate.execute(
                            DELETE_IF_SAME,
                            Collections.singletonList(key),
                            json
                    );

                    Cache cache = cacheManager.getCache("professors");

                    if (cache != null) {
                        cache.evict(pending.professorId());
                    }

                    log.info(
                            "Redis -> MySQL synced professorId={}, removed={}",
                            pending.professorId(),
                            deleted
                    );

                } catch (Exception exception) {
                    log.error(
                            "Redis -> MySQL sync failed for key={}",
                            key,
                            exception
                    );
                }
            }

        } catch (Exception exception) {
            log.error("Redis synchronization scan failed", exception);
        }
    }
}
