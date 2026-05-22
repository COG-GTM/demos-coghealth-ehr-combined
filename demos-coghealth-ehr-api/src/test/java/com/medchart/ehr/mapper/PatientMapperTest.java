package com.medchart.ehr.mapper;

import com.medchart.ehr.domain.patient.Address;
import com.medchart.ehr.domain.patient.Gender;
import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.dto.AddressDTO;
import com.medchart.ehr.dto.PatientDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatientMapperTest {

    private PatientMapper patientMapper;

    @BeforeEach
    void setUp() {
        patientMapper = Mappers.getMapper(PatientMapper.class);
    }

    @Test
    void testToDto() {
        Patient patient = Patient.builder()
                .id(1L)
                .mrn("MRN123456")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .gender(Gender.MALE)
                .email("john.doe@example.com")
                .phoneMobile("555-1234")
                .active(true)
                .build();

        PatientDTO dto = patientMapper.toDto(patient);

        assertNotNull(dto);
        assertEquals(patient.getId(), dto.getId());
        assertEquals(patient.getMrn(), dto.getMrn());
        assertEquals(patient.getFirstName(), dto.getFirstName());
        assertEquals(patient.getLastName(), dto.getLastName());
        assertEquals(patient.getDateOfBirth(), dto.getDateOfBirth());
        assertEquals(patient.getGender(), dto.getGender());
        assertEquals(patient.getEmail(), dto.getEmail());
        assertEquals(patient.getPhoneMobile(), dto.getPhoneMobile());
        assertEquals(patient.getActive(), dto.getActive());
    }

    @Test
    void testToEntity() {
        PatientDTO dto = PatientDTO.builder()
                .mrn("MRN123456")
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.FEMALE)
                .email("jane.smith@example.com")
                .phoneMobile("555-5678")
                .active(true)
                .build();

        Patient patient = patientMapper.toEntity(dto);

        assertNotNull(patient);
        assertNull(patient.getId()); // ID should be ignored
        assertNull(patient.getCreatedAt()); // createdAt should be ignored
        assertNull(patient.getUpdatedAt()); // updatedAt should be ignored
        assertNull(patient.getVersion()); // version should be ignored
        assertEquals(dto.getMrn(), patient.getMrn());
        assertEquals(dto.getFirstName(), patient.getFirstName());
        assertEquals(dto.getLastName(), patient.getLastName());
        assertEquals(dto.getDateOfBirth(), patient.getDateOfBirth());
        assertEquals(dto.getGender(), patient.getGender());
        assertEquals(dto.getEmail(), patient.getEmail());
        assertEquals(dto.getPhoneMobile(), patient.getPhoneMobile());
        assertEquals(dto.getActive(), patient.getActive());
    }

    @Test
    void testUpdateEntityFromDto() {
        Patient existingPatient = Patient.builder()
                .id(1L)
                .mrn("MRN123456")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .gender(Gender.MALE)
                .email("john.doe@example.com")
                .phoneMobile("555-1234")
                .active(true)
                .build();

        PatientDTO dto = PatientDTO.builder()
                .firstName("John Updated")
                .lastName("Doe Updated")
                .email("john.updated@example.com")
                .build();

        patientMapper.updateEntityFromDto(dto, existingPatient);

        assertEquals("John Updated", existingPatient.getFirstName());
        assertEquals("Doe Updated", existingPatient.getLastName());
        assertEquals("john.updated@example.com", existingPatient.getEmail());
        assertEquals("MRN123456", existingPatient.getMrn()); // Unchanged
    }

    @Test
    void testAddressMapping() {
        Address address = Address.builder()
                .street1("123 Main St")
                .street2("Apt 4B")
                .city("Springfield")
                .state("IL")
                .zipCode("62701")
                .country("USA")
                .build();

        AddressDTO dto = patientMapper.toAddressDto(address);

        assertNotNull(dto);
        assertEquals(address.getStreet1(), dto.getStreet1());
        assertEquals(address.getStreet2(), dto.getStreet2());
        assertEquals(address.getCity(), dto.getCity());
        assertEquals(address.getState(), dto.getState());
        assertEquals(address.getZipCode(), dto.getZipCode());
        assertEquals(address.getCountry(), dto.getCountry());
    }

    @Test
    void testAddressEntityMapping() {
        AddressDTO dto = AddressDTO.builder()
                .street1("456 Oak Ave")
                .city("Chicago")
                .state("IL")
                .zipCode("60601")
                .build();

        Address address = patientMapper.toAddressEntity(dto);

        assertNotNull(address);
        assertEquals(dto.getStreet1(), address.getStreet1());
        assertEquals(dto.getCity(), address.getCity());
        assertEquals(dto.getState(), address.getState());
        assertEquals(dto.getZipCode(), address.getZipCode());
    }

    @Test
    void testToDtoList() {
        List<Patient> patients = new ArrayList<>();
        patients.add(Patient.builder()
                .id(1L)
                .mrn("MRN123456")
                .firstName("John")
                .lastName("Doe")
                .build());
        patients.add(Patient.builder()
                .id(2L)
                .mrn("MRN789012")
                .firstName("Jane")
                .lastName("Smith")
                .build());

        List<PatientDTO> dtos = patientMapper.toDtoList(patients);

        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals("John", dtos.get(0).getFirstName());
        assertEquals("Jane", dtos.get(1).getFirstName());
    }
}