package com.medchart.ehr.controller;

import com.medchart.ehr.dto.PatientDTO;
import com.medchart.ehr.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@RestController
@RequestMapping("/v1/patients")
@RequiredArgsConstructor
@Validated
@Tag(name = "Patient", description = "Patient management endpoints")
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/{id}")
    @Operation(summary = "Get patient by ID")
    public ResponseEntity<PatientDTO> getPatient(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @GetMapping("/mrn/{mrn}")
    @Operation(summary = "Get patient by MRN")
    public ResponseEntity<PatientDTO> getPatientByMrn(
            @PathVariable @NotBlank @Size(max = 20, message = "MRN must not exceed 20 characters") String mrn) {
        return ResponseEntity.ok(patientService.getPatientByMrn(mrn));
    }

    @GetMapping("/search")
    @Operation(summary = "Search patients")
    public ResponseEntity<Page<PatientDTO>> searchPatients(
            @RequestParam @Size(min = 1, max = 100, message = "Search query must be between 1 and 100 characters") String q,
            Pageable pageable) {
        return ResponseEntity.ok(patientService.searchPatients(q, pageable));
    }

    @PostMapping
    @Operation(summary = "Create new patient")
    public ResponseEntity<PatientDTO> createPatient(@Valid @RequestBody PatientDTO patientDTO) {
        PatientDTO created = patientService.createPatient(patientDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update patient")
    public ResponseEntity<PatientDTO> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientDTO patientDTO) {
        return ResponseEntity.ok(patientService.updatePatient(id, patientDTO));
    }
}
