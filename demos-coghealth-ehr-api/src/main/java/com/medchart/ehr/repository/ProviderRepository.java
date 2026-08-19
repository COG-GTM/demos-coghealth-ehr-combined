package com.medchart.ehr.repository;

import com.medchart.ehr.domain.provider.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {

    List<Provider> findByOrganizationId(Long organizationId);

    Optional<Provider> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<Provider> findByNpiAndOrganizationId(String npi, Long organizationId);

    List<Provider> findByOrganizationIdAndActiveTrue(Long organizationId);

    List<Provider> findByOrganizationIdAndDepartment(Long organizationId, String department);

    List<Provider> findByOrganizationIdAndSpecialty(Long organizationId, String specialty);

    @Query("SELECT DISTINCT p.department FROM Provider p WHERE p.organizationId = :organizationId AND p.active = true ORDER BY p.department")
    List<String> findAllDepartments(@Param("organizationId") Long organizationId);

    @Query("SELECT DISTINCT p.specialty FROM Provider p WHERE p.organizationId = :organizationId AND p.active = true ORDER BY p.specialty")
    List<String> findAllSpecialties(@Param("organizationId") Long organizationId);

    List<Provider> findByOrganizationIdAndLastNameContainingIgnoreCase(Long organizationId, String lastName);
}
