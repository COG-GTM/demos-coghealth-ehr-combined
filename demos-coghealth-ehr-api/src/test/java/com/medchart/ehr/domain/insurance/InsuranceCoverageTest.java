package com.medchart.ehr.domain.insurance;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class InsuranceCoverageTest {

    private InsuranceCoverage coverage(boolean active, LocalDate effectiveDate, LocalDate terminationDate) {
        return InsuranceCoverage.builder()
                .active(active)
                .effectiveDate(effectiveDate)
                .terminationDate(terminationDate)
                .build();
    }

    @Test
    void isCurrentlyActive_trueForOpenEndedCoverageStartedInThePast() {
        assertThat(coverage(true, LocalDate.now().minusYears(1), null).isCurrentlyActive()).isTrue();
    }

    @Test
    void isCurrentlyActive_trueOnEffectiveAndTerminationBoundaries() {
        LocalDate today = LocalDate.now();

        assertThat(coverage(true, today, today).isCurrentlyActive()).isTrue();
    }

    @Test
    void isCurrentlyActive_falseWhenNotYetEffective() {
        assertThat(coverage(true, LocalDate.now().plusDays(1), null).isCurrentlyActive()).isFalse();
    }

    @Test
    void isCurrentlyActive_falseWhenTerminated() {
        assertThat(coverage(true, LocalDate.now().minusYears(2), LocalDate.now().minusDays(1)).isCurrentlyActive())
                .isFalse();
    }

    @Test
    void isCurrentlyActive_falseWhenFlaggedInactive() {
        assertThat(coverage(false, LocalDate.now().minusYears(1), null).isCurrentlyActive()).isFalse();
    }
}
