package org.ahilmi.schoolmanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ahilmi.schoolmanagement.dto.*;
import org.ahilmi.schoolmanagement.service.TeachingAssignmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class TeachingAssignmentController {

    private final TeachingAssignmentService service;

    @GetMapping
    public List<TeachingAssignmentResponse> getAllAssignments() {
        return service.getAllAssignments();
    }

    @PostMapping
    public ResponseEntity<TeachingAssignmentResponse> createAssignment(@Valid @RequestBody TeachingAssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAssignment(request));
    }

    @PutMapping("/{professorId}/{courseId}/{startDate}")
    public TeachingAssignmentResponse updateAssignment(
            @PathVariable Long professorId,
            @PathVariable Long courseId,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @Valid @RequestBody TeachingAssignmentUpdateRequest request
    ) {
        return service.updateAssignment(professorId, courseId, startDate, request);
    }

    @DeleteMapping("/{professorId}/{courseId}/{startDate}")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long professorId,
            @PathVariable Long courseId,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate
    ) {
        service.deleteAssignment(professorId, courseId, startDate);
        return ResponseEntity.noContent().build();
    }
}
