package org.ahilmi.schoolmanagement.controller;

import lombok.RequiredArgsConstructor;
import org.ahilmi.schoolmanagement.service.ProfessorPhotoService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/professors")
@RequiredArgsConstructor
public class ProfessorPhotoController {

    private final ProfessorPhotoService professorPhotoService;

    @PostMapping(value = "/{id}/photo",  consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadPhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        String path = professorPhotoService.uploadPhoto(id, file);
        return ResponseEntity.ok(Map.of("photoPath", path));
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) throws IOException {

        byte[] photo = professorPhotoService.getPhoto(id);
        MediaType mediaType = MediaType.IMAGE_JPEG;

        if (photo.length >= 8
                && (photo[0] & 0xFF) == 0x89
                && photo[1] == 0x50
                && photo[2] == 0x4E
                && photo[3] == 0x47) {
            mediaType = MediaType.IMAGE_PNG;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(photo);
    }
}
