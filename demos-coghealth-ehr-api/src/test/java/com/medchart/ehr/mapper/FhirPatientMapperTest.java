package com.medchart.ehr.mapper;

import com.medchart.ehr.domain.patient.Address;
import com.medchart.ehr.domain.patient.Gender;
import com.medchart.ehr.domain.patient.MaritalStatus;
import com.medchart.ehr.domain.patient.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FhirPatientMapperTest {

    private FhirPatientMapper fhirPatientMapper;

    @BeforeEach
    void setUp() {
        fhirPatientMapper = new FhirPatientMapper();
    }

    @Test
    void testToFhirResource() {
        Patient patient = Patient.builder()
                .id(1L)
                .mrn("MRN123456")
                .ssn("123-45-6789")
                .firstName("John")
                .middleName("William")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .gender(Gender.MALE)
                .email("john.doe@example.com")
                .phoneHome("555-1234")
                .phoneMobile("555-5678")
                .active(true)
                .deceased(false)
                .maritalStatus(MaritalStatus.MARRIED)
                .address(Address.builder()
                        .street1("123 Main St")
                        .street2("Apt 4B")
                        .city("Springfield")
                        .state("IL")
                        .zipCode("62701")
                        .build())
                .build();

        Map<String, Object> fhirResource = fhirPatientMapper.toFhirResource(patient);

        assertNotNull(fhirResource);
        assertEquals("Patient", fhirResource.get("resourceType"));
        assertEquals("1", fhirResource.get("id"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> identifiers = (List<Map<String, Object>>) fhirResource.get("identifier");
        assertNotNull(identifiers);
        assertEquals(2, identifiers.size());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> names = (List<Map<String, Object>>) fhirResource.get("name");
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals("Doe", names.get(0).get("family"));
        
        assertEquals("male", fhirResource.get("gender"));
        assertEquals("1980-01-01", fhirResource.get("birthDate"));
        assertTrue((Boolean) fhirResource.get("active"));
    }

    @Test
    void testFromFhirResource() {
        Map<String, Object> fhirPatient = Map.of(
                "identifier", List.of(
                        Map.of("system", "http://hospital.example.org/mrn", "value", "MRN123456", "use", "official"),
                        Map.of("system", "http://hl7.org/fhir/sid/us-ssn", "value", "123-45-6789", "use", "secondary")
                ),
                "name", List.of(
                        Map.of("use", "official", "family", "Doe", "given", List.of("John", "William"))
                ),
                "gender", "male",
                "birthDate", "1980-01-01",
                "active", true
        );

        Patient patient = fhirPatientMapper.fromFhirResource(fhirPatient);

        assertNotNull(patient);
        assertEquals("MRN123456", patient.getMrn());
        assertEquals("123-45-6789", patient.getSsn());
        assertEquals("Doe", patient.getLastName());
        assertEquals("John", patient.getFirstName());
        assertEquals("William", patient.getMiddleName());
        assertEquals(Gender.MALE, patient.getGender());
        assertEquals(LocalDate.of(1980, 1, 1), patient.getDateOfBirth());
        assertTrue(patient.getActive());
    }

    @Test
    void testFromFhirResourceMinimal() {
        Map<String, Object> fhirPatient = Map.of(
                "name", List.of(
                        Map.of("family", "Smith", "given", List.of("Jane"))
                ),
                "gender", "female"
        );

        Patient patient = fhirPatientMapper.fromFhirResource(fhirPatient);

        assertNotNull(patient);
        assertEquals("Smith", patient.getLastName());
        assertEquals("Jane", patient.getFirstName());
        assertEquals(Gender.FEMALE, patient.getGender());
        assertTrue(patient.getActive()); // Default to true
    }

    @Test
    void testMapGender() {
        assertEquals("male", fhirPatientMapper.toFhirResource(
                Patient.builder().id(1L).gender(Gender.MALE).build()).get("gender"));
        assertEquals("female", fhirPatientMapper.toFhirResource(
                Patient.builder().id(2L).gender(Gender.FEMALE).build()).get("gender"));
        assertEquals("other", fhirPatientMapper.toFhirResource(
                Patient.builder().id(3L).gender(Gender.OTHER).build()).get("gender"));
        assertEquals("unknown", fhirPatientMapper.toFhirResource(
                Patient.builder().id(4L).gender(null).build()).get("gender"));
    }

    @Test
    void testMapFhirGender() {
        Map<String, Object> fhirPatient = Map.of(
                "name", List.of(Map.of("family", "Test", "given", List.of("Test")))
        );

        Patient malePatient = fhirPatientMapper.fromFhirResource(
                Map.of("name", List.of(Map.of("family", "Test", "given", List.of("Test"))), "gender", "male"));
        assertEquals(Gender.MALE, malePatient.getGender());

        Patient femalePatient = fhirPatientMapper.fromFhirResource(
                Map.of("name", List.of(Map.of("family", "Test", "given", List.of("Test"))), "gender", "female"));
        assertEquals(Gender.FEMALE, femalePatient.getGender());

        Patient otherPatient = fhirPatientMapper.fromFhirResource(
                Map.of("name", List.of(Map.of("family", "Test", "given", List.of("Test"))), "gender", "other"));
        assertEquals(Gender.OTHER, otherPatient.getGender());
    }

    @Test
    void testDeceasedPatient() {
        Patient patient = Patient.builder()
                .id(1L)
                .mrn("MRN123456")
                .firstName("John")
                .lastName("Doe")
                .deceased(true)
                .build();

        Map<String, Object> fhirResource = fhirPatientMapper.toFhirResource(patient);

        assertNotNull(fhirResource);
        assertTrue((Boolean) fhirResource.get("deceasedBoolean"));
    }
}