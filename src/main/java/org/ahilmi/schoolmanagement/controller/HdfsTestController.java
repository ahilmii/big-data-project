
package org.ahilmi.schoolmanagement.controller;

import lombok.RequiredArgsConstructor;
import org.ahilmi.schoolmanagement.service.HdfsStorageService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/hdfs-test")
@RequiredArgsConstructor
public class HdfsTestController {

    private final HdfsStorageService hdfsStorageService;

    @GetMapping("/read")
    public String read() throws IOException {
        return hdfsStorageService.readFile(
                "/professor-images/test.txt"
        );
    }

    @PostMapping("/write")
    public String write() throws IOException {
        String content = "Spring Boot Java 17 -> Hadoop 3.3.6";

        hdfsStorageService.writeFile(
                "/professor-images/spring-test.txt",
                content.getBytes(StandardCharsets.UTF_8)
        );

        return "File uploaded successfully";
    }
}
