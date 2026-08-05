package com.medchart.ehr.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InsuranceCacheTest {

    private InsuranceCache cache;

    @BeforeEach
    void setUp() {
        cache = new InsuranceCache();
    }

    private void cacheEntry(String mrn, String payerId) {
        cache.cacheEligibility(mrn, "123-45-6789", payerId, "INS1", true, "Plan", "25.00", "500.00");
    }

    @Test
    @DisplayName("entries are keyed by patient MRN and payer")
    void entriesAreKeyedByMrnAndPayer() {
        cacheEntry("MRN001", "PAYER1");

        InsuranceCache.CachedEligibility hit = cache.getEligibility("MRN001", "PAYER1");

        assertThat(hit).isNotNull();
        assertThat(hit.memberId).isEqualTo("INS1");
        assertThat(hit.cachedAt).isNotNull();
        assertThat(cache.getEligibility("MRN001", "PAYER2")).isNull();
        assertThat(cache.getEligibility("MRN002", "PAYER1")).isNull();
    }

    @Test
    @DisplayName("re-caching the same key overwrites the previous entry")
    void reCachingOverwritesEntry() {
        cacheEntry("MRN001", "PAYER1");
        cache.cacheEligibility("MRN001", "123-45-6789", "PAYER1", "INS2", false, "Plan B", null, null);

        assertThat(cache.getCacheSize()).isEqualTo(1);
        InsuranceCache.CachedEligibility hit = cache.getEligibility("MRN001", "PAYER1");
        assertThat(hit.memberId).isEqualTo("INS2");
        assertThat(hit.eligible).isFalse();
    }

    @Test
    @DisplayName("clearPatientCache only drops entries for that patient")
    void clearPatientCacheIsScopedToPatient() {
        cacheEntry("MRN001", "PAYER1");
        cacheEntry("MRN001", "PAYER2");
        cacheEntry("MRN002", "PAYER1");

        cache.clearPatientCache("MRN001");

        assertThat(cache.getCacheSize()).isEqualTo(1);
        assertThat(cache.getEligibility("MRN002", "PAYER1")).isNotNull();
    }

    @Test
    @DisplayName("clearAllCache empties the cache")
    void clearAllCacheEmptiesTheCache() {
        cacheEntry("MRN001", "PAYER1");
        cacheEntry("MRN002", "PAYER1");

        cache.clearAllCache();

        assertThat(cache.getCacheSize()).isZero();
    }
}
