package com.medchart.ehr.repository;

import com.medchart.ehr.domain.patient.Gender;
import com.medchart.ehr.domain.patient.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PatientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PatientRepository patientRepository;

    private Patient testPatient;

    @BeforeEach
    void setUp() {
        testPatient = Patient.builder()
                .mrn("MRN123456")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .gender(Gender.MALE)
                .email("john.doe@example.com")
                .phoneMobile("555-1234")
                .active(true)
                .build();

        entityManager.persist(testPatient);
        entityManager.flush();
    }

    @Test
    void testFindByMrn() {
        Optional<Patient> found = patientRepository.findByMrn("MRN123456");

        assertTrue(found.isPresent());
        assertEquals("MRN123456", found.get().getMrn());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    void testFindByMrnNotFound() {
        Optional<Patient> found = patientRepository.findByMrn("MRN999999");

        assertFalse(found.isPresent());
    }

    @Test
    void testFindBySsn() {
        testPatient.setSsn("123-45-6789");
        entityManager.persist(testPatient);
        entityManager.flush();

        Optional<Patient> found = patientRepository.findBySsn("123-45-6789");

        assertTrue(found.isPresent());
        assertEquals("123-45-6789", found.get().getSsn());
    }

    @Test
    void testFindByLastNameContainingIgnoreCase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Patient> result = patientRepository.findByLastNameContainingIgnoreCase("doe", pageable);

        assertTrue(result.hasContent());
        assertEquals("Doe", result.getContent().get(0).getLastName());
    }

    @Test
    void testSearchPatientsByFirstName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Patient> result = patientRepository.searchPatients("john", pageable);

        assertTrue(result.hasContent());
        assertEquals("John", result.getContent().get(0).getFirstName());
    }

    @Test
    void testSearchPatientsByLastName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Patient> result = patientRepository.searchPatients("doe", pageable);

        assertTrue(result.hasContent());
        assertEquals("Doe", result.getContent().get(0).getLastName());
    }

    @Test
    void testSearchPatientsByMrn() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Patient> result = patientRepository.searchPatients("MRN123", pageable);

        assertTrue(result.hasContent());
        assertEquals("MRN123456", result.getContent().get(0).getMrn());
    }

    @Test
    void testFindByDateOfBirth() {
        List<Patient> result = patientRepository.findByDateOfBirth(LocalDate.of(1980, 1, 1));

        assertFalse(result.isEmpty());
        assertEquals(LocalDate.of(1980, 1, 1), result.get(0).getDateOfBirth());
    }

    @Test
    void testFindByLastNameAndDob() {
        List<Patient> result = patientRepository.findByLastNameAndDob("Doe", LocalDate.of(1980, 1, 1));

        assertFalse(result.isEmpty());
        assertEquals("Doe", result.get(0).getLastName());
        assertEquals(LocalDate.of(1980, 1, 1), result.get(0).getDateOfBirth());
    }

    @Test
    void testFindByActiveTrue() {
        List<Patient> result = patientRepository.findByActiveTrue();

        assertFalse(result.isEmpty());
        assertTrue(result.get(0).getActive());
    }

    @Test
    void testCountActivePatients() {
        long count = patientRepository.countActivePatients();

        assertTrue(count > 0);
    }

    @Test
    void testSaveAndFindById() {
        Patient newPatient = Patient.builder()
                .mrn("MRN789012")
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.FEMALE)
                .active(true)
                .build();

        Patient saved = patientRepository.save(newPatient);
        
        Optional<Patient> found = patientRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Jane", found.get().getFirstName());
        assertEquals("MRN789012", found.get().getMrn());
    }

    @Test
    void testDeletePatient() {
        Long id = testPatient.getId();
        
        patientRepository.delete(testPatient);
        
        Optional<Patient> found = patientRepository.findById(id);
        assertFalse(found.isPresent());
    }
}