package com.medchart.ehr.service;

import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.dto.PatientDTO;
import com.medchart.ehr.mapper.PatientMapper;
import com.medchart.ehr.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityNotFoundException;
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
        patient = new Patient();
        patient.setId(1L);
        patient.setMrn("MRN-2019-00001");
        patient.setFirstName("John");
        patient.setLastName("Smith");

        patientDTO = new PatientDTO();
        patientDTO.setId(1L);
        patientDTO.setMrn("MRN-2019-00001");
    }

    @Test
    void getPatientById_returnsMappedDto() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        assertThat(patientService.getPatientById(1L)).isSameAs(patientDTO);
    }

    @Test
    void getPatientById_throwsWhenMissing() {
        when(patientRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatientById(42L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void getPatientByMrn_returnsMappedDto() {
        when(patientRepository.findByMrn("MRN-2019-00001")).thenReturn(Optional.of(patient));
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        assertThat(patientService.getPatientByMrn("MRN-2019-00001")).isSameAs(patientDTO);
    }

    @Test
    void getPatientByMrn_throwsWhenMissing() {
        when(patientRepository.findByMrn("MRN-NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatientByMrn("MRN-NOPE"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("MRN-NOPE");
    }

    @Test
    void searchPatients_mapsEachPageElement() {
        Pageable pageable = PageRequest.of(0, 20);
        when(patientRepository.searchPatients("smith", pageable))
                .thenReturn(new PageImpl<>(Collections.singletonList(patient), pageable, 1));
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        assertThat(patientService.searchPatients("smith", pageable).getContent()).containsExactly(patientDTO);
    }

    @Test
    void createPatient_generatesMrnWhenAbsent() {
        PatientDTO input = new PatientDTO();
        Patient entity = new Patient();
        when(patientMapper.toEntity(input)).thenReturn(entity);
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));
        when(patientMapper.toDto(entity)).thenReturn(patientDTO);

        patientService.createPatient(input);

        ArgumentCaptor<Patient> saved = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(saved.capture());
        assertThat(saved.getValue().getMrn()).startsWith("MRN");
        verify(patientRepository, never()).findByMrn(any());
    }

    @Test
    void createPatient_keepsProvidedMrn() {
        Patient entity = new Patient();
        entity.setMrn("MRN-2019-00001");
        when(patientRepository.findByMrn("MRN-2019-00001")).thenReturn(Optional.empty());
        when(patientMapper.toEntity(patientDTO)).thenReturn(entity);
        when(patientRepository.save(entity)).thenReturn(entity);
        when(patientMapper.toDto(entity)).thenReturn(patientDTO);

        patientService.createPatient(patientDTO);

        assertThat(entity.getMrn()).isEqualTo("MRN-2019-00001");
    }

    @Test
    void createPatient_rejectsDuplicateMrn() {
        when(patientRepository.findByMrn("MRN-2019-00001")).thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> patientService.createPatient(patientDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(patientRepository, never()).save(any());
    }

    @Test
    void updatePatient_appliesDtoOntoExistingEntity() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        assertThat(patientService.updatePatient(1L, patientDTO)).isSameAs(patientDTO);

        verify(patientMapper).updateEntityFromDto(patientDTO, patient);
    }

    @Test
    void updatePatient_throwsWhenMissing() {
        when(patientRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.updatePatient(42L, patientDTO))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @SuppressWarnings("deprecation")
    void findBySsn_isDisabledForHipaaCompliance() {
        assertThatThrownBy(() -> patientService.findBySsn("123-45-6789"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("HIPAA");

        verify(patientRepository, never()).findBySsn(any());
    }
}
