package com.medchart.ehr.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InsuranceCacheTest {

    private InsuranceCache cache;

    @BeforeEach
    void setUp() {
        cache = new InsuranceCache();
    }

    private void cacheFor(String mrn, String payerId) {
        cache.cacheEligibility(mrn, "123-45-6789", payerId, "MEM-" + mrn, true,
                "Premium Health Plan", "25.00", "500.00");
    }

    @Test
    void cachedEligibilityIsRetrievableByMrnAndPayer() {
        cacheFor("MRN001", "PAYER1");

        InsuranceCache.CachedEligibility cached = cache.getEligibility("MRN001", "PAYER1");

        assertThat(cached).isNotNull();
        assertThat(cached.memberId).isEqualTo("MEM-MRN001");
        assertThat(cached.planName).isEqualTo("Premium Health Plan");
        assertThat(cached.eligible).isTrue();
        assertThat(cached.cachedAt).isNotNull();
    }

    @Test
    void entriesAreKeyedByBothMrnAndPayer() {
        cacheFor("MRN001", "PAYER1");

        assertThat(cache.getEligibility("MRN001", "PAYER2")).isNull();
        assertThat(cache.getEligibility("MRN002", "PAYER1")).isNull();
    }

    @Test
    void repeatedCachingForSameKeyDoesNotGrowCache() {
        cacheFor("MRN001", "PAYER1");
        cacheFor("MRN001", "PAYER1");

        assertThat(cache.getCacheSize()).isEqualTo(1);
    }

    @Test
    void clearPatientCacheRemovesOnlyThatPatient() {
        cacheFor("MRN001", "PAYER1");
        cacheFor("MRN001", "PAYER2");
        cacheFor("MRN002", "PAYER1");

        cache.clearPatientCache("MRN001");

        assertThat(cache.getEligibility("MRN001", "PAYER1")).isNull();
        assertThat(cache.getEligibility("MRN001", "PAYER2")).isNull();
        assertThat(cache.getEligibility("MRN002", "PAYER1")).isNotNull();
    }

    @Test
    void clearAllCacheEmptiesTheCache() {
        cacheFor("MRN001", "PAYER1");
        cacheFor("MRN002", "PAYER1");

        cache.clearAllCache();

        assertThat(cache.getCacheSize()).isZero();
    }
}
