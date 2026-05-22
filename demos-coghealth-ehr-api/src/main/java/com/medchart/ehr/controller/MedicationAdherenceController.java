package com.medchart.ehr.controller;

import com.medchart.ehr.dto.AtRiskPatientDTO;
import com.medchart.ehr.dto.MedicationAdherenceDTO;
import com.medchart.ehr.dto.MedicationFillRequest;
import com.medchart.ehr.dto.PdcCalculationRequest;
import com.medchart.ehr.dto.PdcCalculationResponse;
import com.medchart.ehr.service.chronic.MedicationAdherenceTracker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for medication adherence tracking and PDC calculation.
 */
@RestController
@RequestMapping("/v1/medication-adherence")
@RequiredArgsConstructor
@Tag(name = "Medication Adherence", description = "Medication adherence tracking and PDC calculation endpoints")
public class MedicationAdherenceController {

    private final MedicationAdherenceTracker adherenceTracker;

    @PostMapping("/calculate-pdc")
    @Operation(summary = "Calculate PDC for a medication")
    public ResponseEntity<PdcCalculationResponse> calculatePdc(@Valid @RequestBody PdcCalculationRequest request) {
        BigDecimal pdcScore = adherenceTracker.calculatePdc(
                request.getPatientId(),
                request.getMedicationOrderId(),
                request.getPeriodStart(),
                request.getPeriodEnd()
        );

        long totalDays = ChronoUnit.DAYS.between(request.getPeriodStart(), request.getPeriodEnd()) + 1;
        long daysCovered = pdcScore.multiply(new java.math.BigDecimal(totalDays))
                .longValue();

        PdcCalculationResponse response = PdcCalculationResponse.fromCalculation(
                request.getPatientId(),
                request.getMedicationOrderId(),
                request.getPeriodStart(),
                request.getPeriodEnd(),
                pdcScore,
                totalDays,
                daysCovered
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get medication adherence for a patient")
    public ResponseEntity<List<MedicationAdherenceDTO>> getPatientAdherence(@PathVariable Long patientId) {
        List<MedicationAdherenceDTO> adherenceList = adherenceTracker.getPatientAdherence(patientId)
                .stream()
                .map(MedicationAdherenceDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(adherenceList);
    }

    @GetMapping("/patient/{patientId}/medication/{medicationOrderId}")
    @Operation(summary = "Get adherence for a specific patient medication")
    public ResponseEntity<MedicationAdherenceDTO> getMedicationAdherence(
            @PathVariable Long patientId,
            @PathVariable Long medicationOrderId) {
        
        // For now, return the first matching record
        // In a full implementation, we'd have a specific method in the service
        List<MedicationAdherenceDTO> adherenceList = adherenceTracker.getPatientAdherence(patientId)
                .stream()
                .filter(a -> a.getMedicationOrderId().equals(medicationOrderId))
                .map(MedicationAdherenceDTO::fromEntity)
                .collect(Collectors.toList());

        if (adherenceList.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(adherenceList.get(0));
    }

    @PostMapping("/process-fill")
    @Operation(summary = "Process a medication fill from pharmacy")
    public ResponseEntity<String> processMedicationFill(@Valid @RequestBody MedicationFillRequest request) {
        adherenceTracker.processMedicationFill(
                request.getPatientId(),
                request.getMedicationOrderId(),
                request.getFillDate(),
                request.getDaysSupply(),
                request.getNdc(),
                request.getPharmacyNpi(),
                request.getPharmacyName(),
                request.getRxNumber(),
                request.getFillSource()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Medication fill processed successfully");
    }

    @GetMapping("/at-risk")
    @Operation(summary = "Get patients at risk of non-adherence")
    public ResponseEntity<List<AtRiskPatientDTO>> getAtRiskPatients() {
        List<AtRiskPatientDTO> atRiskPatients = adherenceTracker.getAtRiskPatients()
                .stream()
                .map(AtRiskPatientDTO::fromServiceObject)
                .collect(Collectors.toList());

        return ResponseEntity.ok(atRiskPatients);
    }

    @PostMapping("/check-adherence")
    @Operation(summary = "Run adherence check and send alerts for non-adherent patients")
    public ResponseEntity<String> checkAdherenceAndAlert() {
        adherenceTracker.checkAdherenceAndAlert();
        return ResponseEntity.ok("Adherence check completed");
    }

    @GetMapping("/calculate-pdc/quick")
    @Operation(summary = "Quick PDC calculation using query parameters")
    public ResponseEntity<PdcCalculationResponse> calculatePdcQuick(
            @RequestParam Long patientId,
            @RequestParam Long medicationOrderId,
            @RequestParam LocalDate periodStart,
            @RequestParam LocalDate periodEnd) {

        BigDecimal pdcScore = adherenceTracker.calculatePdc(
                patientId,
                medicationOrderId,
                periodStart,
                periodEnd
        );

        long totalDays = ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;
        long daysCovered = pdcScore.multiply(new java.math.BigDecimal(totalDays))
                .longValue();

        PdcCalculationResponse response = PdcCalculationResponse.fromCalculation(
                patientId,
                medicationOrderId,
                periodStart,
                periodEnd,
                pdcScore,
                totalDays,
                daysCovered
        );

        return ResponseEntity.ok(response);
    }
}