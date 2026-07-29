package com.medchart.ehr.controller;

import com.medchart.ehr.dto.RefillRequestDTO;
import com.medchart.ehr.service.RefillRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/refill-requests")
@RequiredArgsConstructor
@Tag(name = "Refill Request", description = "Medication refill request management endpoints")
public class RefillRequestController {

    private final RefillRequestService refillRequestService;

    @GetMapping("/pending")
    @Operation(summary = "List pending refill requests")
    public ResponseEntity<List<RefillRequestDTO>> listPending() {
        return ResponseEntity.ok(refillRequestService.listPendingRequests());
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a refill request")
    public ResponseEntity<RefillRequestDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(refillRequestService.approveRequest(id));
    }

    @PostMapping("/{id}/deny")
    @Operation(summary = "Deny a refill request")
    public ResponseEntity<RefillRequestDTO> deny(@PathVariable Long id) {
        return ResponseEntity.ok(refillRequestService.denyRequest(id));
    }
}
