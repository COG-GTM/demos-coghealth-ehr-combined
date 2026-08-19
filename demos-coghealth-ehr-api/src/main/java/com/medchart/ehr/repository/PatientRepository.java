package com.medchart.ehr.repository;

import com.medchart.ehr.domain.patient.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<Patient> findByMrnAndOrganizationId(String mrn, Long organizationId);

    Page<Patient> findByOrganizationIdAndLastNameContainingIgnoreCase(Long organizationId, String lastName, Pageable pageable);

    @Query("SELECT p FROM Patient p WHERE p.organizationId = :organizationId AND (" +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "p.mrn LIKE CONCAT('%', :searchTerm, '%'))")
    Page<Patient> searchPatients(@Param("organizationId") Long organizationId,
                                 @Param("searchTerm") String searchTerm,
                                 Pageable pageable);

    List<Patient> findByOrganizationIdAndDateOfBirth(Long organizationId, LocalDate dateOfBirth);

    @Query("SELECT p FROM Patient p WHERE p.organizationId = :organizationId AND p.lastName = :lastName AND p.dateOfBirth = :dob")
    List<Patient> findByLastNameAndDob(@Param("organizationId") Long organizationId,
                                       @Param("lastName") String lastName,
                                       @Param("dob") LocalDate dob);

    List<Patient> findByOrganizationIdAndActiveTrue(Long organizationId);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.organizationId = :organizationId AND p.active = true")
    long countActivePatients(@Param("organizationId") Long organizationId);
}
