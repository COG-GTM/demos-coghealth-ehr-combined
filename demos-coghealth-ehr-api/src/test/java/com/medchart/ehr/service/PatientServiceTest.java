package com.medchart.ehr.service;

import com.medchart.ehr.domain.patient.Gender;
import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.dto.PatientDTO;
import com.medchart.ehr.mapper.PatientMapper;
import com.medchart.ehr.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientService patientService;

    private Patient testPatient;
    private PatientDTO testPatientDTO;

    @BeforeEach
    void setUp() {
        testPatient = Patient.builder()
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
    void testGetPatientById() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(patientMapper.toDto(testPatient)).thenReturn(testPatientDTO);

        PatientDTO result = patientService.getPatientById(1L);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("MRN123456", result.getMrn());
        
        verify(patientRepository, times(1)).findById(1L);
        verify(patientMapper, times(1)).toDto(testPatient);
    }

    @Test
    void testGetPatientByIdNotFound() {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> patientService.getPatientById(1L));
        
        verify(patientRepository, times(1)).findById(1L);
        verify(patientMapper, never()).toDto(any());
    }

    @Test
    void testGetPatientByMrn() {
        when(patientRepository.findByMrn("MRN123456")).thenReturn(Optional.of(testPatient));
        when(patientMapper.toDto(testPatient)).thenReturn(testPatientDTO);

        PatientDTO result = patientService.getPatientByMrn("MRN123456");

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("MRN123456", result.getMrn());
        
        verify(patientRepository, times(1)).findByMrn("MRN123456");
        verify(patientMapper, times(1)).toDto(testPatient);
    }

    @Test
    void testGetPatientByMrnNotFound() {
        when(patientRepository.findByMrn("MRN999999")).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> patientService.getPatientByMrn("MRN999999"));
        assertFalse(exception.getMessage().contains("MRN999999"));
        
        verify(patientRepository, times(1)).findByMrn("MRN999999");
        verify(patientMapper, never()).toDto(any());
    }

    @Test
    void testSearchPatients() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Patient> patientPage = new PageImpl<>(Arrays.asList(testPatient));
        
        when(patientRepository.searchPatients("John", pageable)).thenReturn(patientPage);
        when(patientMapper.toDto(testPatient)).thenReturn(testPatientDTO);

        Page<PatientDTO> result = patientService.searchPatients("John", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("John", result.getContent().get(0).getFirstName());
        
        verify(patientRepository, times(1)).searchPatients("John", pageable);
        verify(patientMapper, times(1)).toDto(testPatient);
    }

    @Test
    void testCreatePatient() {
        PatientDTO newPatientDTO = PatientDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.FEMALE)
                .build();

        Patient newPatient = Patient.builder()
                .id(2L)
                .mrn("MRN789012")
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.FEMALE)
                .build();

        PatientDTO savedDTO = PatientDTO.builder()
                .id(2L)
                .mrn("MRN789012")
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.FEMALE)
                .build();

        when(patientRepository.findByMrn(anyString())).thenReturn(Optional.empty());
        when(patientMapper.toEntity(newPatientDTO)).thenReturn(newPatient);
        when(patientRepository.save(newPatient)).thenReturn(newPatient);
        when(patientMapper.toDto(newPatient)).thenReturn(savedDTO);

        PatientDTO result = patientService.createPatient(newPatientDTO);

        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("MRN789012", result.getMrn());
        
        verify(patientMapper, times(1)).toEntity(newPatientDTO);
        verify(patientRepository, times(1)).save(newPatient);
        verify(patientMapper, times(1)).toDto(newPatient);
    }

    @Test
    void testCreatePatientWithExistingMrn() {
        PatientDTO newPatientDTO = PatientDTO.builder()
                .mrn("MRN123456")
                .firstName("Jane")
                .lastName("Smith")
                .build();

        when(patientRepository.findByMrn("MRN123456")).thenReturn(Optional.of(testPatient));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> patientService.createPatient(newPatientDTO));
        assertFalse(exception.getMessage().contains("MRN123456"));
        
        verify(patientRepository, times(1)).findByMrn("MRN123456");
        verify(patientMapper, never()).toEntity(any());
        verify(patientRepository, never()).save(any());
    }

    @Test
    void testUpdatePatient() {
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

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(patientRepository.save(testPatient)).thenReturn(testPatient);
        when(patientMapper.toDto(testPatient)).thenReturn(updatedDTO);

        PatientDTO result = patientService.updatePatient(1L, updateDTO);

        assertNotNull(result);
        assertEquals("John Updated", result.getFirstName());
        assertEquals("Doe Updated", result.getLastName());
        
        verify(patientRepository, times(1)).findById(1L);
        verify(patientMapper, times(1)).updateEntityFromDto(updateDTO, testPatient);
        verify(patientRepository, times(1)).save(testPatient);
        verify(patientMapper, times(1)).toDto(testPatient);
    }

    @Test
    void testUpdatePatientNotFound() {
        PatientDTO updateDTO = PatientDTO.builder()
                .firstName("John Updated")
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> patientService.updatePatient(1L, updateDTO));
        
        verify(patientRepository, times(1)).findById(1L);
        verify(patientMapper, never()).updateEntityFromDto(any(), any());
        verify(patientRepository, never()).save(any());
    }

    @Test
    void testFindBySsnDeprecated() {
        assertThrows(UnsupportedOperationException.class, () -> patientService.findBySsn("123-45-6789"));
    }
}