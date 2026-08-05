package com.medchart.ehr.mapper;

import com.medchart.ehr.domain.patient.Address;
import com.medchart.ehr.domain.patient.Gender;
import com.medchart.ehr.domain.patient.IdentifierType;
import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.domain.patient.PatientIdentifier;
import com.medchart.ehr.dto.AddressDTO;
import com.medchart.ehr.dto.PatientDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PatientMapperTest {

    private PatientMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PatientMapperImpl();
    }

    @Test
    @DisplayName("toDto copies the demographics, address and identifiers")
    void toDtoCopiesDemographics() {
        Patient patient = Patient.builder()
                .id(1L)
                .mrn("MRN001")
                .firstName("Ada")
                .lastName("Lovelace")
                .dateOfBirth(LocalDate.of(1985, 3, 12))
                .gender(Gender.FEMALE)
                .address(Address.builder().street1("1 Analytical Way").city("London").state("KY").build())
                .build();
        PatientIdentifier identifier = PatientIdentifier.builder()
                .identifierType(IdentifierType.MRN)
                .identifierValue("MRN001")
                .build();
        patient.addIdentifier(identifier);

        PatientDTO dto = mapper.toDto(patient);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getMrn()).isEqualTo("MRN001");
        assertThat(dto.getFullName()).isEqualTo("Ada Lovelace");
        assertThat(dto.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(dto.getAddress().getCity()).isEqualTo("London");
        assertThat(dto.getIdentifiers()).hasSize(1);
        assertThat(dto.getIdentifiers().get(0).getIdentifierValue()).isEqualTo("MRN001");
    }

    @Test
    @DisplayName("toEntity ignores the server managed id and timestamps")
    void toEntityIgnoresServerManagedFields() {
        PatientDTO dto = PatientDTO.builder()
                .id(99L)
                .mrn("MRN001")
                .firstName("Ada")
                .lastName("Lovelace")
                .createdAt(LocalDateTime.of(2020, 1, 1, 0, 0))
                .updatedAt(LocalDateTime.of(2020, 1, 2, 0, 0))
                .address(AddressDTO.builder().city("London").build())
                .build();

        Patient entity = mapper.toEntity(dto);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
        assertThat(entity.getVersion()).isNull();
        assertThat(entity.getMrn()).isEqualTo("MRN001");
        assertThat(entity.getAddress().getCity()).isEqualTo("London");
    }

    @Test
    @DisplayName("updateEntityFromDto applies non-null values and keeps the rest")
    void updateEntityFromDtoIgnoresNulls() {
        Patient existing = Patient.builder()
                .id(1L)
                .mrn("MRN001")
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@example.org")
                .dateOfBirth(LocalDate.of(1985, 3, 12))
                .build();
        PatientDTO patch = PatientDTO.builder().lastName("King").build();

        mapper.updateEntityFromDto(patch, existing);

        assertThat(existing.getLastName()).isEqualTo("King");
        assertThat(existing.getFirstName()).isEqualTo("Ada");
        assertThat(existing.getEmail()).isEqualTo("ada@example.org");
        assertThat(existing.getId()).isEqualTo(1L);
        assertThat(existing.getDateOfBirth()).isEqualTo(LocalDate.of(1985, 3, 12));
    }

    @Test
    @DisplayName("updateEntityFromDto cannot clear collections because null values are ignored")
    void updateEntityFromDtoKeepsCollections() {
        Patient existing = Patient.builder().id(1L).mrn("MRN001").build();
        existing.addIdentifier(PatientIdentifier.builder()
                .identifierType(IdentifierType.MRN)
                .identifierValue("MRN001")
                .build());

        mapper.updateEntityFromDto(PatientDTO.builder().identifiers(null).build(), existing);

        assertThat(existing.getIdentifiers()).hasSize(1);
    }

    @Test
    @DisplayName("toDtoList maps every patient in the list")
    void toDtoListMapsEveryPatient() {
        List<PatientDTO> dtos = mapper.toDtoList(List.of(
                Patient.builder().mrn("MRN001").firstName("Ada").lastName("Lovelace").build(),
                Patient.builder().mrn("MRN002").firstName("Grace").lastName("Hopper").build()));

        assertThat(dtos).extracting(PatientDTO::getMrn).containsExactly("MRN001", "MRN002");
    }
}
