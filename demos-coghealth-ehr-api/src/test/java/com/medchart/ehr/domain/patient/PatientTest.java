package com.medchart.ehr.domain.patient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PatientTest {

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setFirstName("John");
        patient.setLastName("Smith");
    }

    @Test
    void getFullName_omitsMiddleNameWhenAbsent() {
        assertThat(patient.getFullName()).isEqualTo("John Smith");
    }

    @Test
    void getFullName_omitsMiddleNameWhenBlank() {
        patient.setMiddleName("   ");

        assertThat(patient.getFullName()).isEqualTo("John Smith");
    }

    @Test
    void getFullName_includesMiddleNameWhenPresent() {
        patient.setMiddleName("Quincy");

        assertThat(patient.getFullName()).isEqualTo("John Quincy Smith");
    }

    @Test
    void addIdentifier_setsBothSidesOfRelationship() {
        PatientIdentifier identifier = new PatientIdentifier();

        patient.addIdentifier(identifier);

        assertThat(patient.getIdentifiers()).containsExactly(identifier);
        assertThat(identifier.getPatient()).isSameAs(patient);
    }

    @Test
    void removeIdentifier_clearsBothSidesOfRelationship() {
        PatientIdentifier identifier = new PatientIdentifier();
        patient.addIdentifier(identifier);

        patient.removeIdentifier(identifier);

        assertThat(patient.getIdentifiers()).isEmpty();
        assertThat(identifier.getPatient()).isNull();
    }

    @Test
    void addEmergencyContact_setsBothSidesOfRelationship() {
        EmergencyContact contact = new EmergencyContact();

        patient.addEmergencyContact(contact);

        assertThat(patient.getEmergencyContacts()).containsExactly(contact);
        assertThat(contact.getPatient()).isSameAs(patient);
    }
}
