package com.medchart.ehr.mapper;

import com.medchart.ehr.domain.patient.Address;
import com.medchart.ehr.domain.patient.Gender;
import com.medchart.ehr.domain.patient.MaritalStatus;
import com.medchart.ehr.domain.patient.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FhirPatientMapperTest {

    private FhirPatientMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FhirPatientMapper();
    }

    private Patient patient() {
        return Patient.builder()
                .id(42L)
                .mrn("MRN001")
                .ssn("123-45-6789")
                .firstName("Jane")
                .middleName("Q")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1980, 5, 12))
                .gender(Gender.FEMALE)
                .maritalStatus(MaritalStatus.MARRIED)
                .email("jane.doe@example.org")
                .phoneMobile("555-0100")
                .address(Address.builder()
                        .street1("100 Main St")
                        .city("Springfield")
                        .state("IL")
                        .zipCode("62704")
                        .build())
                .active(true)
                .build();
    }

    @SuppressWarnings("unchecked")
    @Test
    void toFhirResourceMapsCoreDemographics() {
        Map<String, Object> fhir = mapper.toFhirResource(patient());

        assertThat(fhir).containsEntry("resourceType", "Patient");
        assertThat(fhir).containsEntry("id", "42");
        assertThat(fhir).containsEntry("gender", "female");
        assertThat(fhir).containsEntry("birthDate", "1980-05-12");
        assertThat(fhir).containsEntry("active", true);

        Map<String, Object> name = ((List<Map<String, Object>>) fhir.get("name")).get(0);
        assertThat(name).containsEntry("family", "Doe");
        assertThat((List<String>) name.get("given")).containsExactly("Jane", "Q");
    }

    @SuppressWarnings("unchecked")
    @Test
    void toFhirResourceMapsIdentifierSystems() {
        Map<String, Object> fhir = mapper.toFhirResource(patient());

        List<Map<String, Object>> identifiers = (List<Map<String, Object>>) fhir.get("identifier");
        assertThat(identifiers).hasSize(2);
        assertThat(identifiers.get(0))
                .containsEntry("system", "http://hospital.example.org/mrn")
                .containsEntry("value", "MRN001")
                .containsEntry("use", "official");
        assertThat(identifiers.get(1))
                .containsEntry("system", "http://hl7.org/fhir/sid/us-ssn")
                .containsEntry("use", "secondary");
    }

    @SuppressWarnings("unchecked")
    @Test
    void toFhirResourceMapsTelecomAndAddress() {
        Map<String, Object> fhir = mapper.toFhirResource(patient());

        List<Map<String, Object>> telecom = (List<Map<String, Object>>) fhir.get("telecom");
        assertThat(telecom).contains(
                Map.of("system", "phone", "value", "555-0100", "use", "mobile"),
                Map.of("system", "email", "value", "jane.doe@example.org"));

        Map<String, Object> address = ((List<Map<String, Object>>) fhir.get("address")).get(0);
        assertThat(address)
                .containsEntry("city", "Springfield")
                .containsEntry("state", "IL")
                .containsEntry("postalCode", "62704")
                .containsEntry("country", "US");
        assertThat((List<String>) address.get("line")).containsExactly("100 Main St");
    }

    @SuppressWarnings("unchecked")
    @Test
    void toFhirResourceMapsMaritalStatusCode() {
        Map<String, Object> fhir = mapper.toFhirResource(patient());

        Map<String, Object> maritalStatus = (Map<String, Object>) fhir.get("maritalStatus");
        List<Map<String, Object>> coding = (List<Map<String, Object>>) maritalStatus.get("coding");
        assertThat(coding.get(0)).containsEntry("code", "M");
    }

    @Test
    void toFhirResourceMapsUnknownGenderAndOmitsMaritalStatusWhenAbsent() {
        Patient patient = patient();
        patient.setGender(null);
        patient.setMaritalStatus(null);

        Map<String, Object> fhir = mapper.toFhirResource(patient);

        assertThat(fhir).containsEntry("gender", "unknown");
        assertThat(fhir).doesNotContainKey("maritalStatus");
    }

    @Test
    void toFhirResourceMarksDeceasedPatients() {
        Patient patient = patient();
        patient.setDeceased(true);
        patient.setDeceasedDate(LocalDate.of(2023, 1, 2).atStartOfDay());

        Map<String, Object> fhir = mapper.toFhirResource(patient);

        assertThat(fhir).containsEntry("deceasedBoolean", true);
        assertThat(fhir).containsEntry("deceasedDateTime", "2023-01-02T00:00");
    }

    @Test
    void fromFhirResourceReadsIdentifiersNameAndDemographics() {
        Map<String, Object> fhir = new HashMap<>();
        List<Map<String, Object>> identifiers = new ArrayList<>();
        identifiers.add(Map.of("system", "http://hospital.example.org/mrn", "value", "MRN777"));
        identifiers.add(Map.of("system", "http://hl7.org/fhir/sid/us-ssn", "value", "999-99-9999"));
        fhir.put("identifier", identifiers);
        fhir.put("name", List.of(Map.of("family", "Smith", "given", Arrays.asList("John", "Paul"))));
        fhir.put("gender", "MALE");
        fhir.put("birthDate", "1975-11-03");

        Patient patient = mapper.fromFhirResource(fhir);

        assertThat(patient.getMrn()).isEqualTo("MRN777");
        assertThat(patient.getSsn()).isEqualTo("999-99-9999");
        assertThat(patient.getFirstName()).isEqualTo("John");
        assertThat(patient.getMiddleName()).isEqualTo("Paul");
        assertThat(patient.getLastName()).isEqualTo("Smith");
        assertThat(patient.getGender()).isEqualTo(Gender.MALE);
        assertThat(patient.getDateOfBirth()).isEqualTo(LocalDate.of(1975, 11, 3));
        assertThat(patient.getActive()).isTrue();
    }

    @Test
    void fromFhirResourceDefaultsActiveAndToleratesSparseResource() {
        Patient patient = mapper.fromFhirResource(new HashMap<>());

        assertThat(patient.getActive()).isTrue();
        assertThat(patient.getMrn()).isNull();
    }

    @Test
    void fhirRoundTripPreservesIdentity() {
        Patient original = patient();

        Patient roundTripped = mapper.fromFhirResource(mapper.toFhirResource(original));

        assertThat(roundTripped.getMrn()).isEqualTo(original.getMrn());
        assertThat(roundTripped.getFullName()).isEqualTo(original.getFullName());
        assertThat(roundTripped.getGender()).isEqualTo(original.getGender());
        assertThat(roundTripped.getDateOfBirth()).isEqualTo(original.getDateOfBirth());
    }
}
