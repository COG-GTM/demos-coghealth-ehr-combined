package com.medchart.ehr.service;

import com.medchart.ehr.domain.encounter.Encounter;
import com.medchart.ehr.domain.encounter.EncounterStatus;
import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.repository.EncounterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncounterServiceTest {

    @Mock
    private EncounterRepository encounterRepository;

    @InjectMocks
    private EncounterService encounterService;

    private Encounter encounter;

    @BeforeEach
    void setUp() {
        encounter = Encounter.builder()
                .id(1L)
                .encounterNumber("ENC-2024-000101")
                .status(EncounterStatus.SCHEDULED)
                .patient(Patient.builder().mrn("MRN001").build())
                .build();
    }

    @Test
    void createAssignsGeneratedNumberAndScheduledStatus() {
        Encounter newEncounter = Encounter.builder()
                .patient(Patient.builder().mrn("MRN002").build())
                .build();
        when(encounterRepository.save(any(Encounter.class))).thenAnswer(inv -> inv.getArgument(0));

        Encounter saved = encounterService.create(newEncounter);

        assertThat(saved.getStatus()).isEqualTo(EncounterStatus.SCHEDULED);
        assertThat(saved.getEncounterNumber())
                .matches("ENC-" + LocalDate.now().getYear() + "-\\d{6}");
    }

    @Test
    void createGeneratesUniqueSequentialNumbers() {
        when(encounterRepository.save(any(Encounter.class))).thenAnswer(inv -> inv.getArgument(0));

        String first = encounterService.create(
                Encounter.builder().patient(Patient.builder().mrn("MRN002").build()).build())
                .getEncounterNumber();
        String second = encounterService.create(
                Encounter.builder().patient(Patient.builder().mrn("MRN003").build()).build())
                .getEncounterNumber();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void checkInMovesEncounterToCheckedIn() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(encounter));

        encounterService.checkIn(1L);

        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.CHECKED_IN);
        verify(encounterRepository).save(encounter);
    }

    @Test
    void startEncounterMovesEncounterToInProgress() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(encounter));

        encounterService.startEncounter(1L);

        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.IN_PROGRESS);
    }

    @Test
    void completeEncounterStoresNotes() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(encounter));

        encounterService.completeEncounter(1L, "Patient stable");

        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.COMPLETED);
        assertThat(encounter.getNotes()).isEqualTo("Patient stable");
    }

    @Test
    void completeEncounterKeepsExistingNotesWhenNoneProvided() {
        encounter.setNotes("Existing note");
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(encounter));

        encounterService.completeEncounter(1L, null);

        assertThat(encounter.getNotes()).isEqualTo("Existing note");
    }

    @Test
    void cancelEncounterMovesEncounterToCancelled() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(encounter));

        encounterService.cancelEncounter(1L);

        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.CANCELLED);
    }

    @Test
    void markNoShowMovesEncounterToNoShow() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(encounter));

        encounterService.markNoShow(1L);

        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.NO_SHOW);
    }

    @Test
    void statusTransitionsAreNoOpsWhenEncounterIsMissing() {
        when(encounterRepository.findById(404L)).thenReturn(Optional.empty());

        encounterService.checkIn(404L);
        encounterService.startEncounter(404L);
        encounterService.completeEncounter(404L, "notes");
        encounterService.cancelEncounter(404L);
        encounterService.markNoShow(404L);

        verify(encounterRepository, never()).save(any());
    }

    @Test
    void getProviderScheduleQueriesFullCalendarDay() {
        LocalDate date = LocalDate.of(2024, 3, 15);
        when(encounterRepository.findTodaysSchedule(eq(7L), any(), any()))
                .thenReturn(Collections.singletonList(encounter));

        List<Encounter> schedule = encounterService.getProviderSchedule(7L, date);

        ArgumentCaptor<LocalDateTime> start = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> end = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(encounterRepository).findTodaysSchedule(eq(7L), start.capture(), end.capture());
        assertThat(start.getValue()).isEqualTo(LocalDateTime.of(2024, 3, 15, 0, 0));
        assertThat(end.getValue()).isEqualTo(LocalDateTime.of(2024, 3, 16, 0, 0));
        assertThat(schedule).containsExactly(encounter);
    }

    @Test
    void findByDateRangeIsEndDateInclusive() {
        LocalDate start = LocalDate.of(2024, 3, 1);
        LocalDate end = LocalDate.of(2024, 3, 31);
        when(encounterRepository.findByDateRange(any(), any())).thenReturn(Collections.emptyList());

        encounterService.findByDateRange(start, end);

        verify(encounterRepository).findByDateRange(
                LocalDateTime.of(2024, 3, 1, 0, 0),
                LocalDateTime.of(2024, 4, 1, 0, 0));
    }

    @Test
    void findByEncounterNumberDelegatesToRepository() {
        when(encounterRepository.findByEncounterNumber("ENC-2024-000101"))
                .thenReturn(Optional.of(encounter));

        assertThat(encounterService.findByEncounterNumber("ENC-2024-000101")).contains(encounter);
    }

    @Test
    void getPatientEncounterCountDelegatesToRepository() {
        when(encounterRepository.countByPatientId(1L)).thenReturn(3L);

        assertThat(encounterService.getPatientEncounterCount(1L)).isEqualTo(3L);
    }
}
