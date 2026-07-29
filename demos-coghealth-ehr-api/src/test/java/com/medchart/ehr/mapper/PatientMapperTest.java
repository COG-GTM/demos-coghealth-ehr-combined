package com.medchart.ehr.mapper;

import com.medchart.ehr.domain.patient.Address;
import com.medchart.ehr.domain.patient.Gender;
import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.dto.AddressDTO;
import com.medchart.ehr.dto.PatientDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PatientMapperTest {

    private final PatientMapper mapper = new PatientMapperImpl();

    private Patient patient() {
        return Patient.builder()
                .id(1L)
                .mrn("MRN001")
                .ssn("123-45-6789")
                .firstName("Jane")
                .middleName("Q")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1980, 5, 12))
                .gender(Gender.FEMALE)
                .address(Address.builder().street1("100 Main St").city("Springfield").build())
                .active(true)
                .build();
    }

    @Test
    void toDtoCopiesDemographics() {
        PatientDTO dto = mapper.toDto(patient());

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getMrn()).isEqualTo("MRN001");
        assertThat(dto.getFullName()).isEqualTo("Jane Q Doe");
        assertThat(dto.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(dto.getActive()).isTrue();
        assertThat(dto.getAddress().getCity()).isEqualTo("Springfield");
    }

    @Test
    void patientDtoExposesNoSsnField() {
        assertThat(PatientDTO.class.getDeclaredFields())
                .extracting(java.lang.reflect.Field::getName)
                .doesNotContain("ssn");
    }

    @Test
    void toEntityIgnoresAuditAndIdentityFields() {
        PatientDTO dto = PatientDTO.builder()
                .id(99L)
                .mrn("MRN002")
                .firstName("John")
                .lastName("Smith")
                .createdAt(LocalDateTime.of(2020, 1, 1, 0, 0))
                .updatedAt(LocalDateTime.of(2020, 1, 1, 0, 0))
                .build();

        Patient entity = mapper.toEntity(dto);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
        assertThat(entity.getVersion()).isNull();
        assertThat(entity.getMrn()).isEqualTo("MRN002");
        assertThat(entity.getFullName()).isEqualTo("John Smith");
    }

    @Test
    void updateEntityFromDtoIgnoresNullPropertiesAndPreservesId() {
        Patient existing = patient();
        PatientDTO patch = PatientDTO.builder()
                .id(999L)
                .lastName("Married-Name")
                .address(AddressDTO.builder().street1("200 Oak Ave").city("Shelbyville").build())
                .build();

        mapper.updateEntityFromDto(patch, existing);

        assertThat(existing.getId()).isEqualTo(1L);
        assertThat(existing.getLastName()).isEqualTo("Married-Name");
        assertThat(existing.getFirstName()).isEqualTo("Jane");
        assertThat(existing.getMrn()).isEqualTo("MRN001");
        assertThat(existing.getAddress().getCity()).isEqualTo("Shelbyville");
    }

    @Test
    void toDtoListMapsEveryPatient() {
        assertThat(mapper.toDtoList(java.util.Arrays.asList(patient(), patient())))
                .hasSize(2)
                .allSatisfy(dto -> assertThat(dto.getMrn()).isEqualTo("MRN001"));
    }

    @Test
    void toDtoReturnsNullForNullEntity() {
        assertThat(mapper.toDto(null)).isNull();
    }
}
