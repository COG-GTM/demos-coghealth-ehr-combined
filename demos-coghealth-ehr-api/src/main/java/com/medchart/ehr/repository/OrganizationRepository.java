package com.medchart.ehr.repository;

import com.medchart.ehr.domain.organization.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByCodeAndActiveTrue(String code);
}
