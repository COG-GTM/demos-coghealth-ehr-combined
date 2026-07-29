package com.medchart.ehr.controller;

import com.medchart.ehr.domain.refill.RefillRequestStatus;
import com.medchart.ehr.dto.RefillRequestDTO;
import com.medchart.ehr.service.RefillRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RefillRequestControllerTest {

    @Mock
    private RefillRequestService refillRequestService;

    @InjectMocks
    private RefillRequestController refillRequestController;

    private MockMvc mockMvc;
    private RefillRequestDTO dto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(refillRequestController).build();

        dto = RefillRequestDTO.builder()
                .id(10L)
                .status(RefillRequestStatus.PENDING)
                .pharmacyName("CVS Pharmacy")
                .requestedDate(LocalDate.of(2024, 3, 15))
                .patient(RefillRequestDTO.PatientSummary.builder()
                        .id(1L).mrn("MRN-2019-00001").fullName("John Smith").build())
                .medication(RefillRequestDTO.MedicationSummary.builder()
                        .id(2L).genericName("Lisinopril").brandName("Prinivil").build())
                .build();
    }

    @Test
    void listPending_returnsPendingRequests() throws Exception {
        when(refillRequestService.listPendingRequests()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/v1/refill-requests/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].patient.mrn").value("MRN-2019-00001"))
                .andExpect(jsonPath("$[0].medication.genericName").value("Lisinopril"));
    }

    @Test
    void listPending_returnsEmptyArrayWhenNoRequests() throws Exception {
        when(refillRequestService.listPendingRequests()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/refill-requests/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void approve_delegatesToServiceWithPathId() throws Exception {
        dto.setStatus(RefillRequestStatus.APPROVED);
        when(refillRequestService.approveRequest(10L)).thenReturn(dto);

        mockMvc.perform(post("/v1/refill-requests/10/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(refillRequestService).approveRequest(10L);
    }

    @Test
    void deny_delegatesToServiceWithPathId() throws Exception {
        dto.setStatus(RefillRequestStatus.DENIED);
        when(refillRequestService.denyRequest(10L)).thenReturn(dto);

        mockMvc.perform(post("/v1/refill-requests/10/deny"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DENIED"));

        verify(refillRequestService).denyRequest(10L);
    }
}
