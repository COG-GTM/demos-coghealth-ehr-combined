package com.medchart.ehr.mapper;

import com.medchart.ehr.domain.patient.Address;
import com.medchart.ehr.domain.patient.Gender;
import com.medchart.ehr.domain.patient.MaritalStatus;
import com.medchart.ehr.domain.patient.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FhirPatientMapperTest {

    private static final String MRN_SYSTEM = "http://hospital.example.org/mrn";
    private static final String SSN_SYSTEM = "http://hl7.org/fhir/sid/us-ssn";

    private FhirPatientMapper mapper;
    private Patient patient;

    @BeforeEach
    void setUp() {
        mapper = new FhirPatientMapper();

        patient = new Patient();
        patient.setId(1L);
        patient.setMrn("MRN-2019-00001");
        patient.setSsn("123-45-6789");
        patient.setFirstName("John");
        patient.setLastName("Smith");
        patient.setGender(Gender.MALE);
        patient.setDateOfBirth(LocalDate.of(1980, 5, 17));
        patient.setActive(true);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listOfMaps(Object value) {
        return (List<Map<String, Object>>) value;
    }

    @Test
    void toFhirResource_mapsCoreDemographics() {
        Map<String, Object> fhir = mapper.toFhirResource(patient);

        assertThat(fhir).containsEntry("resourceType", "Patient")
                .containsEntry("id", "1")
                .containsEntry("gender", "male")
                .containsEntry("birthDate", "1980-05-17")
                .containsEntry("active", true);
        assertThat(fhir).doesNotContainKey("deceasedBoolean");
    }

    @Test
    void toFhirResource_mapsMrnAndSsnIdentifiers() {
        List<Map<String, Object>> identifiers = listOfMaps(mapper.toFhirResource(patient).get("identifier"));

        assertThat(identifiers).anySatisfy(id -> assertThat(id)
                .containsEntry("system", MRN_SYSTEM)
                .containsEntry("value", "MRN-2019-00001"));
        assertThat(identifiers).anySatisfy(id -> assertThat(id)
                .containsEntry("system", SSN_SYSTEM)
                .containsEntry("value", "123-45-6789"));
    }

    @Test
    void toFhirResource_putsMiddleNameInGivenNames() {
        patient.setMiddleName("Quincy");

        Map<String, Object> name = listOfMaps(mapper.toFhirResource(patient).get("name")).get(0);

        assertThat(name).containsEntry("family", "Smith");
        assertThat(name.get("given")).isEqualTo(List.of("John", "Quincy"));
    }

    @Test
    void toFhirResource_omitsMissingTelecomEntries() {
        patient.setPhoneMobile("555-0100");
        patient.setEmail("john.smith@example.com");

        List<Map<String, Object>> telecom = listOfMaps(mapper.toFhirResource(patient).get("telecom"));

        assertThat(telecom).hasSize(2);
        assertThat(telecom).anySatisfy(entry -> assertThat(entry)
                .containsEntry("system", "phone")
                .containsEntry("use", "mobile"));
        assertThat(telecom).anySatisfy(entry -> assertThat(entry).containsEntry("system", "email"));
    }

    @Test
    void toFhirResource_mapsAddressLinesAndCountry() {
        patient.setAddress(Address.builder()
                .street1("100 Main St")
                .city("Boston")
                .state("MA")
                .zipCode("02118")
                .build());

        Map<String, Object> address = listOfMaps(mapper.toFhirResource(patient).get("address")).get(0);

        assertThat(address).containsEntry("city", "Boston")
                .containsEntry("state", "MA")
                .containsEntry("postalCode", "02118")
                .containsEntry("country", "US");
        assertThat(address.get("line")).isEqualTo(List.of("100 Main St"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void toFhirResource_mapsDeceasedAndMaritalStatus() {
        patient.setDeceased(true);
        patient.setMaritalStatus(MaritalStatus.MARRIED);

        Map<String, Object> fhir = mapper.toFhirResource(patient);

        assertThat(fhir).containsEntry("deceasedBoolean", true);
        Map<String, Object> maritalStatus = (Map<String, Object>) fhir.get("maritalStatus");
        assertThat(listOfMaps(maritalStatus.get("coding")).get(0)).containsEntry("code", "M");
    }

    @Test
    void toFhirResource_mapsUnknownGenderWhenAbsent() {
        patient.setGender(null);

        assertThat(mapper.toFhirResource(patient)).containsEntry("gender", "unknown");
    }

    @Test
    void fromFhirResource_readsIdentifiersNameGenderAndBirthDate() {
        Map<String, Object> fhir = new HashMap<>();
        fhir.put("identifier", List.of(
                Map.of("system", MRN_SYSTEM, "value", "MRN-2019-00001"),
                Map.of("system", SSN_SYSTEM, "value", "123-45-6789")));
        fhir.put("name", List.of(Map.of("family", "Smith", "given", List.of("John", "Quincy"))));
        fhir.put("gender", "FEMALE");
        fhir.put("birthDate", "1980-05-17");
        fhir.put("active", false);

        Patient result = mapper.fromFhirResource(fhir);

        assertThat(result.getMrn()).isEqualTo("MRN-2019-00001");
        assertThat(result.getSsn()).isEqualTo("123-45-6789");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getMiddleName()).isEqualTo("Quincy");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1980, 5, 17));
        assertThat(result.getActive()).isFalse();
    }

    @Test
    void fromFhirResource_defaultsActiveToTrueWhenMissing() {
        Patient result = mapper.fromFhirResource(new HashMap<>());

        assertThat(result.getActive()).isTrue();
    }

    @Test
    void roundTrip_preservesIdentifiersAndName() {
        Patient result = mapper.fromFhirResource(mapper.toFhirResource(patient));

        assertThat(result.getMrn()).isEqualTo(patient.getMrn());
        assertThat(result.getSsn()).isEqualTo(patient.getSsn());
        assertThat(result.getFullName()).isEqualTo(patient.getFullName());
        assertThat(result.getGender()).isEqualTo(Gender.MALE);
        assertThat(result.getDateOfBirth()).isEqualTo(patient.getDateOfBirth());
    }
}
