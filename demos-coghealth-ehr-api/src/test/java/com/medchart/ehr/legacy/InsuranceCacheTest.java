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

    private void cache(String mrn, String payerId) {
        cache.cacheEligibility(mrn, "123-45-6789", payerId, "MEMBER-1", true, "Plan", "25.00", "500.00");
    }

    @Test
    void getEligibility_returnsNullForUnknownKey() {
        assertThat(cache.getEligibility("MRN-1", "AETNA")).isNull();
    }

    @Test
    void getEligibility_returnsEntryCachedForSamePatientAndPayer() {
        cache("MRN-1", "AETNA");

        InsuranceCache.CachedEligibility cached = cache.getEligibility("MRN-1", "AETNA");

        assertThat(cached).isNotNull();
        assertThat(cached.memberId).isEqualTo("MEMBER-1");
        assertThat(cached.eligible).isTrue();
        assertThat(cached.cachedAt).isNotNull();
    }

    @Test
    void getEligibility_isKeyedByPayerAsWellAsPatient() {
        cache("MRN-1", "AETNA");

        assertThat(cache.getEligibility("MRN-1", "CIGNA")).isNull();
    }

    @Test
    void clearPatientCache_removesOnlyThatPatientsEntries() {
        cache("MRN-1", "AETNA");
        cache("MRN-1", "CIGNA");
        cache("MRN-2", "AETNA");

        cache.clearPatientCache("MRN-1");

        assertThat(cache.getCacheSize()).isEqualTo(1);
        assertThat(cache.getEligibility("MRN-2", "AETNA")).isNotNull();
    }

    @Test
    void clearAllCache_emptiesTheCache() {
        cache("MRN-1", "AETNA");
        cache("MRN-2", "AETNA");

        cache.clearAllCache();

        assertThat(cache.getCacheSize()).isZero();
    }

    @Test
    void cacheEligibility_overwritesExistingEntryForSameKey() {
        cache("MRN-1", "AETNA");
        cache.cacheEligibility("MRN-1", "123-45-6789", "AETNA", "MEMBER-2", false, "Plan", null, null);

        assertThat(cache.getCacheSize()).isEqualTo(1);
        assertThat(cache.getEligibility("MRN-1", "AETNA").memberId).isEqualTo("MEMBER-2");
    }
}
