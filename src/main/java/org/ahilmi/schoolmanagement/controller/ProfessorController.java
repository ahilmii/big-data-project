package org.ahilmi.schoolmanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ahilmi.schoolmanagement.dto.ProfessorRequest;
import org.ahilmi.schoolmanagement.dto.ProfessorResponse;
import org.ahilmi.schoolmanagement.service.ProfessorService;
import org.ahilmi.schoolmanagement.service.ProfessorStagingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/professors")
@RequiredArgsConstructor
public class ProfessorController {

    private final ProfessorService professorService;

    private final ProfessorStagingService professorStagingService;

    @GetMapping
    public List<ProfessorResponse> getAllProfessors() {
        return professorService.getAllProfessors();
    }

    @GetMapping("/{id}")
    public ProfessorResponse getProfessorById(@PathVariable Long id) {
        return professorService.getProfessorById(id);
    }

    @PostMapping
    public ResponseEntity<ProfessorResponse> createProfessor(@Valid @RequestBody ProfessorRequest request) {
        ProfessorResponse response = professorService.createProfessor(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ProfessorResponse updateProfessor(@PathVariable Long id, @Valid @RequestBody ProfessorRequest request) {
        return professorService.updateProfessor(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfessor(@PathVariable Long id) {
        professorService.deleteProfessor(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/staged")
    public ResponseEntity<String> stageProfessorUpdate(@PathVariable Long id, @Valid @RequestBody ProfessorRequest request) {
        professorStagingService.stageUpdate(id, request);

        return ResponseEntity
                .accepted()
                .body("Update accepted and queued in Redis");
    }
}
