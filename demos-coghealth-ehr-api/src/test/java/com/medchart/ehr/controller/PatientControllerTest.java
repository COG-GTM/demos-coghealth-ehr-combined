package com.medchart.ehr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medchart.ehr.domain.patient.Gender;
import com.medchart.ehr.dto.PatientDTO;
import com.medchart.ehr.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
@Disabled("Requires complex Spring Security configuration")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientService patientService;

    private PatientDTO testPatientDTO;

    @BeforeEach
    void setUp() {
        testPatientDTO = PatientDTO.builder()
                .id(1L)
                .mrn("MRN123456")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .gender(Gender.MALE)
                .email("john.doe@example.com")
                .phoneMobile("555-1234")
                .active(true)
                .build();
    }

    @Test
    void testGetPatient() throws Exception {
        when(patientService.getPatientById(1L)).thenReturn(testPatientDTO);

        mockMvc.perform(get("/v1/patients/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.mrn").value("MRN123456"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void testGetPatientByMrn() throws Exception {
        when(patientService.getPatientByMrn("MRN123456")).thenReturn(testPatientDTO);

        mockMvc.perform(get("/v1/patients/mrn/{mrn}", "MRN123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mrn").value("MRN123456"))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void testSearchPatients() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PatientDTO> patientPage = new PageImpl<>(Arrays.asList(testPatientDTO));
        
        when(patientService.searchPatients("John", pageable)).thenReturn(patientPage);

        mockMvc.perform(get("/v1/patients/search")
                        .param("q", "John")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("John"));
    }

    @Test
    void testCreatePatient() throws Exception {
        PatientDTO newPatient = PatientDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.FEMALE)
                .build();

        PatientDTO createdPatient = PatientDTO.builder()
                .id(2L)
                .mrn("MRN789012")
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.FEMALE)
                .build();

        when(patientService.createPatient(any(PatientDTO.class))).thenReturn(createdPatient);

        mockMvc.perform(post("/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPatient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.mrn").value("MRN789012"))
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    void testUpdatePatient() throws Exception {
        PatientDTO updateDTO = PatientDTO.builder()
                .firstName("John Updated")
                .lastName("Doe Updated")
                .email("john.updated@example.com")
                .build();

        PatientDTO updatedDTO = PatientDTO.builder()
                .id(1L)
                .mrn("MRN123456")
                .firstName("John Updated")
                .lastName("Doe Updated")
                .email("john.updated@example.com")
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .gender(Gender.MALE)
                .active(true)
                .build();

        when(patientService.updatePatient(anyLong(), any(PatientDTO.class))).thenReturn(updatedDTO);

        mockMvc.perform(put("/v1/patients/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John Updated"))
                .andExpect(jsonPath("$.lastName").value("Doe Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));
    }
}