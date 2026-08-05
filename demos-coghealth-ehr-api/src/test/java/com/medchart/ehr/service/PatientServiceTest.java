package com.medchart.ehr.service;

import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.dto.PatientDTO;
import com.medchart.ehr.mapper.PatientMapper;
import com.medchart.ehr.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;
    private PatientDTO patientDTO;

    @BeforeEach
    void setUp() {
        patient = Patient.builder()
                .id(1L)
                .mrn("MRN001")
                .firstName("Ada")
                .lastName("Lovelace")
                .dateOfBirth(LocalDate.of(1985, 3, 12))
                .build();

        patientDTO = PatientDTO.builder()
                .id(1L)
                .mrn("MRN001")
                .firstName("Ada")
                .lastName("Lovelace")
                .dateOfBirth(LocalDate.of(1985, 3, 12))
                .build();
    }

    @Test
    @DisplayName("getPatientById returns the mapped DTO when the patient exists")
    void getPatientByIdReturnsDto() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        assertThat(patientService.getPatientById(1L)).isSameAs(patientDTO);
    }

    @Test
    @DisplayName("getPatientById throws EntityNotFoundException for an unknown id")
    void getPatientByIdThrowsWhenMissing() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatientById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getPatientByMrn returns the mapped DTO when the MRN exists")
    void getPatientByMrnReturnsDto() {
        when(patientRepository.findByMrn("MRN001")).thenReturn(Optional.of(patient));
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        assertThat(patientService.getPatientByMrn("MRN001")).isSameAs(patientDTO);
    }

    @Test
    @DisplayName("getPatientByMrn throws EntityNotFoundException for an unknown MRN")
    void getPatientByMrnThrowsWhenMissing() {
        when(patientRepository.findByMrn("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatientByMrn("NOPE"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("NOPE");
    }

    @Test
    @DisplayName("searchPatients maps every page entry to a DTO and keeps pagination metadata")
    void searchPatientsMapsPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Patient> page = new PageImpl<>(Collections.singletonList(patient), pageable, 1);
        when(patientRepository.searchPatients("love", pageable)).thenReturn(page);
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        Page<PatientDTO> result = patientService.searchPatients("love", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(patientDTO);
    }

    @Test
    @DisplayName("createPatient rejects an MRN that already exists")
    void createPatientRejectsDuplicateMrn() {
        when(patientRepository.findByMrn("MRN001")).thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> patientService.createPatient(patientDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MRN001");

        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("createPatient generates an MRN when the DTO does not supply one")
    void createPatientGeneratesMrn() {
        PatientDTO withoutMrn = PatientDTO.builder().firstName("Ada").lastName("Lovelace").build();
        Patient entity = Patient.builder().firstName("Ada").lastName("Lovelace").build();
        when(patientMapper.toEntity(withoutMrn)).thenReturn(entity);
        when(patientRepository.save(entity)).thenReturn(entity);
        when(patientMapper.toDto(entity)).thenReturn(patientDTO);

        patientService.createPatient(withoutMrn);

        ArgumentCaptor<Patient> saved = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(saved.capture());
        assertThat(saved.getValue().getMrn()).startsWith("MRN");
        verify(patientRepository, never()).findByMrn(any());
    }

    @Test
    @DisplayName("createPatient keeps a caller supplied MRN that is not taken yet")
    void createPatientKeepsSuppliedMrn() {
        when(patientRepository.findByMrn("MRN001")).thenReturn(Optional.empty());
        when(patientMapper.toEntity(patientDTO)).thenReturn(patient);
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        assertThat(patientService.createPatient(patientDTO)).isSameAs(patientDTO);
        assertThat(patient.getMrn()).isEqualTo("MRN001");
    }

    @Test
    @DisplayName("updatePatient merges the DTO into the loaded entity before saving")
    void updatePatientMergesIntoExistingEntity() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        assertThat(patientService.updatePatient(1L, patientDTO)).isSameAs(patientDTO);

        verify(patientMapper).updateEntityFromDto(patientDTO, patient);
        verify(patientRepository).save(patient);
    }

    @Test
    @DisplayName("updatePatient throws EntityNotFoundException for an unknown id")
    void updatePatientThrowsWhenMissing() {
        when(patientRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.updatePatient(42L, patientDTO))
                .isInstanceOf(EntityNotFoundException.class);

        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("findBySsn stays disabled for HIPAA compliance")
    void findBySsnIsDisabled() {
        assertThatThrownBy(() -> patientService.findBySsn("123-45-6789"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("HIPAA");

        verify(patientRepository, never()).findBySsn(any());
    }
}
