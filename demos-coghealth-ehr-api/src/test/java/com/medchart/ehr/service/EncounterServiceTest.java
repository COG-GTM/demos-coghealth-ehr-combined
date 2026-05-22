package com.medchart.ehr.service;

import com.medchart.ehr.domain.encounter.Encounter;
import com.medchart.ehr.domain.encounter.EncounterStatus;
import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.repository.EncounterRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EncounterServiceTest {

    @Mock
    private EncounterRepository encounterRepository;

    @InjectMocks
    private EncounterService encounterService;

    private Patient testPatient;
    private Encounter testEncounter;

    @BeforeEach
    void setUp() {
        testPatient = Patient.builder()
                .id(1L)
                .mrn("MRN123456")
                .firstName("John")
                .lastName("Doe")
                .build();

        testEncounter = Encounter.builder()
                .id(1L)
                .encounterNumber("ENC-2024-000100")
                .patient(testPatient)
                .status(EncounterStatus.SCHEDULED)
                .encounterDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .build();
    }

    @Test
    void testFindById() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(testEncounter));

        Optional<Encounter> result = encounterService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("ENC-2024-000100", result.get().getEncounterNumber());
        
        verify(encounterRepository, times(1)).findById(1L);
    }

    @Test
    void testFindByIdNotFound() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Encounter> result = encounterService.findById(1L);

        assertFalse(result.isPresent());
        
        verify(encounterRepository, times(1)).findById(1L);
    }

    @Test
    void testFindByEncounterNumber() {
        when(encounterRepository.findByEncounterNumber("ENC-2024-000100")).thenReturn(Optional.of(testEncounter));

        Optional<Encounter> result = encounterService.findByEncounterNumber("ENC-2024-000100");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        
        verify(encounterRepository, times(1)).findByEncounterNumber("ENC-2024-000100");
    }

    @Test
    void testFindByPatientId() {
        when(encounterRepository.findByPatientId(1L)).thenReturn(Arrays.asList(testEncounter));

        List<Encounter> result = encounterService.findByPatientId(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("ENC-2024-000100", result.get(0).getEncounterNumber());
        
        verify(encounterRepository, times(1)).findByPatientId(1L);
    }

    @Test
    void testFindByPatientIdPageable() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Encounter> encounterPage = new PageImpl<>(Arrays.asList(testEncounter));
        
        when(encounterRepository.findByPatientId(1L, pageable)).thenReturn(encounterPage);

        Page<Encounter> result = encounterService.findByPatientId(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        
        verify(encounterRepository, times(1)).findByPatientId(1L, pageable);
    }

    @Test
    void testFindByStatus() {
        when(encounterRepository.findByStatus(EncounterStatus.SCHEDULED)).thenReturn(Arrays.asList(testEncounter));

        List<Encounter> result = encounterService.findByStatus(EncounterStatus.SCHEDULED);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(EncounterStatus.SCHEDULED, result.get(0).getStatus());
        
        verify(encounterRepository, times(1)).findByStatus(EncounterStatus.SCHEDULED);
    }

    @Test
    void testCreateEncounter() {
        Encounter newEncounter = Encounter.builder()
                .patient(testPatient)
                .encounterDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .build();

        when(encounterRepository.save(any(Encounter.class))).thenReturn(testEncounter);

        Encounter result = encounterService.create(newEncounter);

        assertNotNull(result);
        assertNotNull(result.getEncounterNumber());
        assertEquals(EncounterStatus.SCHEDULED, result.getStatus());
        
        verify(encounterRepository, times(1)).save(any(Encounter.class));
    }

    @Test
    void testUpdateEncounter() {
        testEncounter.setNotes("Updated notes");
        
        when(encounterRepository.save(testEncounter)).thenReturn(testEncounter);

        Encounter result = encounterService.update(testEncounter);

        assertNotNull(result);
        assertEquals("Updated notes", result.getNotes());
        
        verify(encounterRepository, times(1)).save(testEncounter);
    }

    @Test
    void testCheckIn() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(testEncounter));
        when(encounterRepository.save(testEncounter)).thenReturn(testEncounter);

        encounterService.checkIn(1L);

        assertEquals(EncounterStatus.CHECKED_IN, testEncounter.getStatus());
        
        verify(encounterRepository, times(1)).findById(1L);
        verify(encounterRepository, times(1)).save(testEncounter);
    }

    @Test
    void testStartEncounter() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(testEncounter));
        when(encounterRepository.save(testEncounter)).thenReturn(testEncounter);

        encounterService.startEncounter(1L);

        assertEquals(EncounterStatus.IN_PROGRESS, testEncounter.getStatus());
        
        verify(encounterRepository, times(1)).findById(1L);
        verify(encounterRepository, times(1)).save(testEncounter);
    }

    @Test
    void testCompleteEncounter() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(testEncounter));
        when(encounterRepository.save(testEncounter)).thenReturn(testEncounter);

        encounterService.completeEncounter(1L, "Patient discharged");

        assertEquals(EncounterStatus.COMPLETED, testEncounter.getStatus());
        assertEquals("Patient discharged", testEncounter.getNotes());
        
        verify(encounterRepository, times(1)).findById(1L);
        verify(encounterRepository, times(1)).save(testEncounter);
    }

    @Test
    void testCancelEncounter() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(testEncounter));
        when(encounterRepository.save(testEncounter)).thenReturn(testEncounter);

        encounterService.cancelEncounter(1L);

        assertEquals(EncounterStatus.CANCELLED, testEncounter.getStatus());
        
        verify(encounterRepository, times(1)).findById(1L);
        verify(encounterRepository, times(1)).save(testEncounter);
    }

    @Test
    void testMarkNoShow() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(testEncounter));
        when(encounterRepository.save(testEncounter)).thenReturn(testEncounter);

        encounterService.markNoShow(1L);

        assertEquals(EncounterStatus.NO_SHOW, testEncounter.getStatus());
        
        verify(encounterRepository, times(1)).findById(1L);
        verify(encounterRepository, times(1)).save(testEncounter);
    }

    @Test
    void testGetPatientEncounterCount() {
        when(encounterRepository.countByPatientId(1L)).thenReturn(5L);

        long count = encounterService.getPatientEncounterCount(1L);

        assertEquals(5L, count);
        
        verify(encounterRepository, times(1)).countByPatientId(1L);
    }
}