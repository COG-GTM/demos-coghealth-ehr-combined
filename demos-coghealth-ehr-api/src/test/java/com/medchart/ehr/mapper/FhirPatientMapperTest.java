package com.medchart.ehr.mapper;

import com.medchart.ehr.domain.patient.Address;
import com.medchart.ehr.domain.patient.Gender;
import com.medchart.ehr.domain.patient.MaritalStatus;
import com.medchart.ehr.domain.patient.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FhirPatientMapperTest {

    private static final String MRN_SYSTEM = "http://hospital.example.org/mrn";
    private static final String SSN_SYSTEM = "http://hl7.org/fhir/sid/us-ssn";

    private FhirPatientMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FhirPatientMapper();
    }

    private static Patient patient() {
        return Patient.builder()
                .id(1L)
                .mrn("MRN001")
                .ssn("123-45-6789")
                .firstName("Ada")
                .lastName("Lovelace")
                .dateOfBirth(LocalDate.of(1985, 3, 12))
                .gender(Gender.FEMALE)
                .active(true)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> listOfMaps(Object value) {
        return (List<Map<String, Object>>) value;
    }

    @Test
    @DisplayName("toFhirResource emits the required Patient elements")
    void toFhirResourceEmitsCoreElements() {
        Map<String, Object> fhir = mapper.toFhirResource(patient());

        assertThat(fhir)
                .containsEntry("resourceType", "Patient")
                .containsEntry("id", "1")
                .containsEntry("gender", "female")
                .containsEntry("birthDate", "1985-03-12")
                .containsEntry("active", true);
        assertThat(fhir).doesNotContainKey("deceasedBoolean");
    }

    @Test
    @DisplayName("identifiers carry the MRN and SSN systems")
    void identifiersUseFhirSystems() {
        List<Map<String, Object>> identifiers = listOfMaps(mapper.toFhirResource(patient()).get("identifier"));

        assertThat(identifiers).hasSize(2);
        assertThat(identifiers.get(0))
                .containsEntry("system", MRN_SYSTEM)
                .containsEntry("value", "MRN001")
                .containsEntry("use", "official");
        assertThat(identifiers.get(1))
                .containsEntry("system", SSN_SYSTEM)
                .containsEntry("value", "123-45-6789");
    }

    @Test
    @DisplayName("a missing SSN maps to an empty identifier value rather than null")
    void missingSsnMapsToEmptyValue() {
        Patient patient = patient();
        patient.setSsn(null);

        List<Map<String, Object>> identifiers = listOfMaps(mapper.toFhirResource(patient).get("identifier"));

        assertThat(identifiers.get(1)).containsEntry("value", "");
    }

    @Test
    @DisplayName("the middle name becomes the second given name")
    void middleNameBecomesSecondGivenName() {
        Patient patient = patient();
        patient.setMiddleName("Byron");

        Map<String, Object> name = listOfMaps(mapper.toFhirResource(patient).get("name")).get(0);

        assertThat(name).containsEntry("family", "Lovelace");
        assertThat(name.get("given")).isEqualTo(List.of("Ada", "Byron"));
    }

    @Test
    @DisplayName("only the populated contact points are emitted")
    void telecomOnlyIncludesPopulatedContacts() {
        Patient patient = patient();
        patient.setPhoneMobile("555-0101");
        patient.setEmail("ada@example.org");

        List<Map<String, Object>> telecom = listOfMaps(mapper.toFhirResource(patient).get("telecom"));

        assertThat(telecom).hasSize(2);
        assertThat(telecom.get(0)).containsEntry("system", "phone").containsEntry("use", "mobile");
        assertThat(telecom.get(1)).containsEntry("system", "email").containsEntry("value", "ada@example.org");
    }

    @Test
    @DisplayName("the address is mapped with its populated street lines")
    void addressIsMappedWithStreetLines() {
        Patient patient = patient();
        patient.setAddress(Address.builder()
                .street1("1 Analytical Way")
                .city("London")
                .state("KY")
                .zipCode("40741")
                .build());

        Map<String, Object> address = listOfMaps(mapper.toFhirResource(patient).get("address")).get(0);

        assertThat(address.get("line")).isEqualTo(List.of("1 Analytical Way"));
        assertThat(address)
                .containsEntry("city", "London")
                .containsEntry("state", "KY")
                .containsEntry("postalCode", "40741")
                .containsEntry("country", "US");
    }

    @Test
    @DisplayName("a deceased patient carries the deceased flag and date")
    void deceasedPatientCarriesDeceasedElements() {
        Patient patient = patient();
        patient.setDeceased(true);
        patient.setDeceasedDate(LocalDateTime.of(2024, 1, 2, 3, 4));

        Map<String, Object> fhir = mapper.toFhirResource(patient);

        assertThat(fhir).containsEntry("deceasedBoolean", true);
        assertThat(fhir.get("deceasedDateTime")).isEqualTo("2024-01-02T03:04");
    }

    @Test
    @DisplayName("marital status is mapped to its HL7 v3 code")
    void maritalStatusUsesHl7Code() {
        Patient patient = patient();
        patient.setMaritalStatus(MaritalStatus.MARRIED);

        Map<String, Object> maritalStatus = (Map<String, Object>) mapper.toFhirResource(patient).get("maritalStatus");
        List<Map<String, Object>> coding = listOfMaps(maritalStatus.get("coding"));

        assertThat(coding.get(0))
                .containsEntry("system", "http://terminology.hl7.org/CodeSystem/v3-MaritalStatus")
                .containsEntry("code", "M");
    }

    @Test
    @DisplayName("an unknown gender maps to the FHIR 'unknown' code")
    void unknownGenderMapsToUnknown() {
        Patient patient = patient();
        patient.setGender(null);

        assertThat(mapper.toFhirResource(patient)).containsEntry("gender", "unknown");
    }

    @Test
    @DisplayName("fromFhirResource reads identifiers, names, gender and birth date back")
    void fromFhirResourceReadsCoreElements() {
        Map<String, Object> fhir = new HashMap<>();
        fhir.put("identifier", List.of(
                Map.of("system", MRN_SYSTEM, "value", "MRN001"),
                Map.of("system", SSN_SYSTEM, "value", "123-45-6789")));
        fhir.put("name", List.of(Map.of("family", "Lovelace", "given", List.of("Ada", "Byron"))));
        fhir.put("gender", "female");
        fhir.put("birthDate", "1985-03-12");
        fhir.put("active", false);

        Patient patient = mapper.fromFhirResource(fhir);

        assertThat(patient.getMrn()).isEqualTo("MRN001");
        assertThat(patient.getSsn()).isEqualTo("123-45-6789");
        assertThat(patient.getFirstName()).isEqualTo("Ada");
        assertThat(patient.getMiddleName()).isEqualTo("Byron");
        assertThat(patient.getLastName()).isEqualTo("Lovelace");
        assertThat(patient.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(patient.getDateOfBirth()).isEqualTo(LocalDate.of(1985, 3, 12));
        assertThat(patient.getActive()).isFalse();
    }

    @Test
    @DisplayName("fromFhirResource defaults active to true when the element is absent")
    void fromFhirResourceDefaultsActive() {
        Patient patient = mapper.fromFhirResource(new HashMap<>());

        assertThat(patient.getActive()).isTrue();
        assertThat(patient.getMrn()).isNull();
    }

    @Test
    @DisplayName("a FHIR patient round-trips back to the same identifying fields")
    void roundTripPreservesIdentifyingFields() {
        Patient original = patient();

        Patient roundTripped = mapper.fromFhirResource(mapper.toFhirResource(original));

        assertThat(roundTripped.getMrn()).isEqualTo(original.getMrn());
        assertThat(roundTripped.getSsn()).isEqualTo(original.getSsn());
        assertThat(roundTripped.getFirstName()).isEqualTo(original.getFirstName());
        assertThat(roundTripped.getLastName()).isEqualTo(original.getLastName());
        assertThat(roundTripped.getGender()).isEqualTo(original.getGender());
        assertThat(roundTripped.getDateOfBirth()).isEqualTo(original.getDateOfBirth());
    }
}
