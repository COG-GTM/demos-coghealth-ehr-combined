package com.medchart.ehr.service;

import com.medchart.ehr.domain.encounter.Encounter;
import com.medchart.ehr.domain.encounter.EncounterStatus;
import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.repository.EncounterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncounterServiceTest {

    @Mock
    private EncounterRepository encounterRepository;

    private EncounterService encounterService;

    private Encounter encounter;

    @BeforeEach
    void setUp() {
        encounterService = new EncounterService(encounterRepository);
        encounter = Encounter.builder()
                .id(7L)
                .encounterNumber("ENC-2024-000101")
                .status(EncounterStatus.SCHEDULED)
                .patient(Patient.builder().id(1L).mrn("MRN001").build())
                .build();
    }

    @Test
    @DisplayName("create assigns a sequential encounter number and the SCHEDULED status")
    void createAssignsNumberAndStatus() {
        Encounter fresh = Encounter.builder().patient(Patient.builder().mrn("MRN001").build()).build();
        when(encounterRepository.save(fresh)).thenReturn(fresh);

        Encounter created = encounterService.create(fresh);

        String year = String.valueOf(LocalDate.now().getYear());
        assertThat(created.getEncounterNumber()).isEqualTo("ENC-" + year + "-000101");
        assertThat(created.getStatus()).isEqualTo(EncounterStatus.SCHEDULED);
    }

    @Test
    @DisplayName("create hands out a new encounter number for every call")
    void createIncrementsSequence() {
        when(encounterRepository.save(any(Encounter.class))).thenAnswer(inv -> inv.getArgument(0));
        Patient patient = Patient.builder().mrn("MRN001").build();

        String first = encounterService.create(Encounter.builder().patient(patient).build()).getEncounterNumber();
        String second = encounterService.create(Encounter.builder().patient(patient).build()).getEncounterNumber();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("getProviderSchedule queries the full calendar day for the provider")
    void getProviderScheduleUsesDayBoundaries() {
        LocalDate date = LocalDate.of(2024, 5, 17);
        when(encounterRepository.findTodaysSchedule(3L, date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(encounter));

        assertThat(encounterService.getProviderSchedule(3L, date)).containsExactly(encounter);
    }

    @Test
    @DisplayName("findByDateRange makes the end date inclusive")
    void findByDateRangeIsEndInclusive() {
        LocalDate start = LocalDate.of(2024, 5, 1);
        LocalDate end = LocalDate.of(2024, 5, 31);
        LocalDateTime expectedEnd = end.plusDays(1).atStartOfDay();
        when(encounterRepository.findByDateRange(start.atStartOfDay(), expectedEnd))
                .thenReturn(Arrays.asList(encounter));

        assertThat(encounterService.findByDateRange(start, end)).containsExactly(encounter);
    }

    @Test
    @DisplayName("checkIn moves the encounter to CHECKED_IN and persists it")
    void checkInUpdatesStatus() {
        when(encounterRepository.findById(7L)).thenReturn(Optional.of(encounter));

        encounterService.checkIn(7L);

        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.CHECKED_IN);
        verify(encounterRepository).save(encounter);
    }

    @Test
    @DisplayName("startEncounter moves the encounter to IN_PROGRESS")
    void startUpdatesStatus() {
        when(encounterRepository.findById(7L)).thenReturn(Optional.of(encounter));

        encounterService.startEncounter(7L);

        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.IN_PROGRESS);
        verify(encounterRepository).save(encounter);
    }

    @Test
    @DisplayName("completeEncounter stores the closing notes when they are provided")
    void completeStoresNotes() {
        when(encounterRepository.findById(7L)).thenReturn(Optional.of(encounter));

        encounterService.completeEncounter(7L, "Patient stable, discharged");

        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.COMPLETED);
        assertThat(encounter.getNotes()).isEqualTo("Patient stable, discharged");
    }

    @Test
    @DisplayName("completeEncounter leaves existing notes untouched when none are supplied")
    void completeKeepsExistingNotesWhenNull() {
        encounter.setNotes("original");
        when(encounterRepository.findById(7L)).thenReturn(Optional.of(encounter));

        encounterService.completeEncounter(7L, null);

        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.COMPLETED);
        assertThat(encounter.getNotes()).isEqualTo("original");
    }

    @Test
    @DisplayName("cancelEncounter and markNoShow set their terminal statuses")
    void cancelAndNoShowUpdateStatus() {
        when(encounterRepository.findById(7L)).thenReturn(Optional.of(encounter));

        encounterService.cancelEncounter(7L);
        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.CANCELLED);

        encounterService.markNoShow(7L);
        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.NO_SHOW);
    }

    @Test
    @DisplayName("status transitions are a no-op when the encounter does not exist")
    void transitionsIgnoreMissingEncounter() {
        when(encounterRepository.findById(404L)).thenReturn(Optional.empty());

        encounterService.checkIn(404L);
        encounterService.startEncounter(404L);
        encounterService.completeEncounter(404L, "notes");
        encounterService.cancelEncounter(404L);
        encounterService.markNoShow(404L);

        verify(encounterRepository, never()).save(any());
    }

    @Test
    @DisplayName("read-through methods delegate to the matching repository query")
    void readMethodsDelegateToRepository() {
        when(encounterRepository.findByEncounterNumber("ENC-2024-000101")).thenReturn(Optional.of(encounter));
        when(encounterRepository.findByPatientId(1L)).thenReturn(List.of(encounter));
        when(encounterRepository.findByAttendingProviderId(3L)).thenReturn(List.of(encounter));
        when(encounterRepository.findByStatus(EncounterStatus.SCHEDULED)).thenReturn(List.of(encounter));
        when(encounterRepository.countByPatientId(1L)).thenReturn(4L);

        assertThat(encounterService.findByEncounterNumber("ENC-2024-000101")).contains(encounter);
        assertThat(encounterService.findByPatientId(1L)).containsExactly(encounter);
        assertThat(encounterService.findByProviderId(3L)).containsExactly(encounter);
        assertThat(encounterService.findByStatus(EncounterStatus.SCHEDULED)).containsExactly(encounter);
        assertThat(encounterService.getPatientEncounterCount(1L)).isEqualTo(4L);
    }
}
