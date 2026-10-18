package org.ahilmi.schoolmanagement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ahilmi.schoolmanagement.dto.PendingProfessorUpdate;
import org.ahilmi.schoolmanagement.dto.ProfessorRequest;
import org.ahilmi.schoolmanagement.repository.ProfessorRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfessorStagingService {

    private final ProfessorRepository professorRepository;
    private final StringRedisTemplate pendingRedisTemplate; // Redis üzerinde anahtar ve değerleri metin olarak saklamamızı sağlar.
    private final ObjectMapper objectMapper;
    private final CacheManager cacheManager;

    public ProfessorStagingService(
            ProfessorRepository professorRepository,
            @Qualifier("pendingRedisTemplate")
            StringRedisTemplate pendingRedisTemplate,
            ObjectMapper objectMapper,
            CacheManager cacheManager
    ) {
        this.professorRepository = professorRepository;
        this.pendingRedisTemplate = pendingRedisTemplate;
        this.objectMapper = objectMapper;
        this.cacheManager = cacheManager;
    }

    public void stageUpdate(Long id, ProfessorRequest request) {

        if (!professorRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Professor not found: " + id
            );
        }

        PendingProfessorUpdate pending = new PendingProfessorUpdate(id, request.name(), request.department());

        try {
            String json = objectMapper.writeValueAsString(pending);

            pendingRedisTemplate.opsForValue().set(
                    "pending:professor:" + id,
                    json
            );

            Cache cache = cacheManager.getCache("professors");

            if (cache != null) {
                cache.evict(id);
            }

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Cannot serialize pending update",
                    exception
            );
        }

    }
}
