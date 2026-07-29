package com.medchart.ehr.domain.patient;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressTest {

    @Test
    void getFormattedAddress_joinsAllPresentParts() {
        Address address = Address.builder()
                .street1("100 Main St")
                .street2("Apt 4B")
                .city("Boston")
                .state("MA")
                .zipCode("02118")
                .country("USA")
                .build();

        assertThat(address.getFormattedAddress()).isEqualTo("100 Main St, Apt 4B, Boston, MA 02118");
    }

    @Test
    void getFormattedAddress_appendsNonUsaCountry() {
        Address address = Address.builder()
                .street1("10 King St")
                .city("Toronto")
                .state("ON")
                .zipCode("M5H 1A1")
                .country("Canada")
                .build();

        assertThat(address.getFormattedAddress()).isEqualTo("10 King St, Toronto, ON M5H 1A1, Canada");
    }

    @Test
    void getFormattedAddress_prefixesSeparatorWhenStreetMissing() {
        Address address = Address.builder().city("Boston").state("MA").build();

        assertThat(address.getFormattedAddress()).isEqualTo(", Boston, MA");
    }

    @Test
    void getFormattedAddress_isEmptyWhenNoPartsSet() {
        assertThat(Address.builder().build().getFormattedAddress()).isEmpty();
    }
}
