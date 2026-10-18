package org.ahilmi.schoolmanagement.service;

import lombok.RequiredArgsConstructor;
import org.ahilmi.schoolmanagement.model.Professor;
import org.ahilmi.schoolmanagement.repository.ProfessorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.cache.annotation.CacheEvict;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfessorPhotoService {

    private final HdfsStorageService hdfsStorageService;
    private final ProfessorRepository professorRepository;

    @CacheEvict(value = "professors", key = "#professorId")
    @Transactional
    public String uploadPhoto(Long professorId, MultipartFile file) throws IOException {

        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Professor not found"
                ));

        if (file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "File cannot be empty"
            );
        }

        String contentType = file.getContentType();

        String extension;

        if ("image/jpeg".equals(contentType)) {
            extension = ".jpg";
        } else if ("image/png".equals(contentType)) {
            extension = ".png";
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only JPEG and PNG images are allowed"
            );
        }

        String hdfsPath = "/professor-images/" + professorId + "-" + UUID.randomUUID() + extension;

        try (InputStream input = file.getInputStream()) {
            hdfsStorageService.uploadImage(hdfsPath, input);
        }

        professor.setPhotoPath(hdfsPath);
        professorRepository.save(professor);

        return hdfsPath;
    }

    @Transactional(readOnly = true)
    public byte[] getPhoto(Long professorId) throws IOException {

        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Professor not found"
                ));

        if (professor.getPhotoPath() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Professor does not have a photo"
            );
        }

        return hdfsStorageService.readImage(professor.getPhotoPath());
    }
}
