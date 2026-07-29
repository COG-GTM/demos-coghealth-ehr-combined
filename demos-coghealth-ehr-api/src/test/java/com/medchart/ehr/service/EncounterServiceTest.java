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

import java.time.LocalDate;
import java.util.Collections;
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

    @InjectMocks
    private EncounterService encounterService;

    private Encounter encounter;

    @BeforeEach
    void setUp() {
        Patient patient = new Patient();
        patient.setMrn("MRN-2019-00001");

        encounter = new Encounter();
        encounter.setId(5L);
        encounter.setEncounterNumber("ENC-2024-000101");
        encounter.setPatient(patient);
        encounter.setStatus(EncounterStatus.SCHEDULED);
    }

    @Test
    void create_assignsGeneratedNumberAndScheduledStatus() {
        Encounter newEncounter = new Encounter();
        newEncounter.setPatient(encounter.getPatient());
        when(encounterRepository.save(newEncounter)).thenReturn(newEncounter);

        Encounter created = encounterService.create(newEncounter);

        assertThat(created.getStatus()).isEqualTo(EncounterStatus.SCHEDULED);
        assertThat(created.getEncounterNumber())
                .matches("ENC-" + LocalDate.now().getYear() + "-\\d{6}");
    }

    @Test
    void create_generatesUniqueSequentialNumbers() {
        when(encounterRepository.save(any(Encounter.class))).thenAnswer(inv -> inv.getArgument(0));
        Encounter first = new Encounter();
        first.setPatient(encounter.getPatient());
        Encounter second = new Encounter();
        second.setPatient(encounter.getPatient());

        String firstNumber = encounterService.create(first).getEncounterNumber();
        String secondNumber = encounterService.create(second).getEncounterNumber();

        assertThat(firstNumber).isNotEqualTo(secondNumber);
    }

    @Test
    void checkIn_movesEncounterToCheckedIn() {
        when(encounterRepository.findById(5L)).thenReturn(Optional.of(encounter));

        encounterService.checkIn(5L);

        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.CHECKED_IN);
        verify(encounterRepository).save(encounter);
    }

    @Test
    void startEncounter_movesEncounterToInProgress() {
        when(encounterRepository.findById(5L)).thenReturn(Optional.of(encounter));

        encounterService.startEncounter(5L);

        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.IN_PROGRESS);
    }

    @Test
    void completeEncounter_setsStatusAndNotes() {
        when(encounterRepository.findById(5L)).thenReturn(Optional.of(encounter));

        encounterService.completeEncounter(5L, "Follow up in 3 months");

        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.COMPLETED);
        assertThat(encounter.getNotes()).isEqualTo("Follow up in 3 months");
    }

    @Test
    void completeEncounter_keepsExistingNotesWhenNoneProvided() {
        encounter.setNotes("Original note");
        when(encounterRepository.findById(5L)).thenReturn(Optional.of(encounter));

        encounterService.completeEncounter(5L, null);

        assertThat(encounter.getNotes()).isEqualTo("Original note");
    }

    @Test
    void cancelEncounter_setsCancelledStatus() {
        when(encounterRepository.findById(5L)).thenReturn(Optional.of(encounter));

        encounterService.cancelEncounter(5L);

        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.CANCELLED);
    }

    @Test
    void markNoShow_setsNoShowStatus() {
        when(encounterRepository.findById(5L)).thenReturn(Optional.of(encounter));

        encounterService.markNoShow(5L);

        assertThat(encounter.getStatus()).isEqualTo(EncounterStatus.NO_SHOW);
    }

    @Test
    void statusTransitions_areNoOpsWhenEncounterMissing() {
        when(encounterRepository.findById(99L)).thenReturn(Optional.empty());

        encounterService.checkIn(99L);
        encounterService.cancelEncounter(99L);

        verify(encounterRepository, never()).save(any());
    }

    @Test
    void getProviderSchedule_queriesFullDayWindow() {
        LocalDate date = LocalDate.of(2024, 3, 15);
        when(encounterRepository.findTodaysSchedule(3L, date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
                .thenReturn(Collections.singletonList(encounter));

        assertThat(encounterService.getProviderSchedule(3L, date)).containsExactly(encounter);
    }

    @Test
    void findByDateRange_includesEntireEndDate() {
        LocalDate start = LocalDate.of(2024, 3, 1);
        LocalDate end = LocalDate.of(2024, 3, 31);
        when(encounterRepository.findByDateRange(start.atStartOfDay(), end.plusDays(1).atStartOfDay()))
                .thenReturn(Collections.singletonList(encounter));

        assertThat(encounterService.findByDateRange(start, end)).containsExactly(encounter);
    }
}
