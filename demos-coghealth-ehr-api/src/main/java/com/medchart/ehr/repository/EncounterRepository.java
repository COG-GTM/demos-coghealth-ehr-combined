package com.medchart.ehr.repository;

import com.medchart.ehr.domain.encounter.Encounter;
import com.medchart.ehr.domain.encounter.EncounterStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EncounterRepository extends JpaRepository<Encounter, Long> {

    Optional<Encounter> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<Encounter> findByEncounterNumberAndOrganizationId(String encounterNumber, Long organizationId);

    List<Encounter> findByOrganizationIdAndPatientId(Long organizationId, Long patientId);

    Page<Encounter> findByOrganizationIdAndPatientId(Long organizationId, Long patientId, Pageable pageable);

    List<Encounter> findByOrganizationIdAndAttendingProviderId(Long organizationId, Long providerId);

    List<Encounter> findByOrganizationIdAndStatus(Long organizationId, EncounterStatus status);

    @Query("SELECT e FROM Encounter e WHERE e.organizationId = :organizationId AND e.encounterDateTime BETWEEN :startDate AND :endDate")
    List<Encounter> findByDateRange(@Param("organizationId") Long organizationId,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM Encounter e WHERE e.organizationId = :organizationId AND e.attendingProvider.id = :providerId AND e.encounterDateTime >= :startOfDay AND e.encounterDateTime < :endOfDay AND e.status IN ('SCHEDULED', 'CHECKED_IN', 'IN_PROGRESS')")
    List<Encounter> findTodaysSchedule(@Param("organizationId") Long organizationId,
                                       @Param("providerId") Long providerId,
                                       @Param("startOfDay") LocalDateTime startOfDay,
                                       @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(e) FROM Encounter e WHERE e.organizationId = :organizationId AND e.patient.id = :patientId")
    long countByPatientId(@Param("organizationId") Long organizationId, @Param("patientId") Long patientId);

    @Query("SELECT e FROM Encounter e JOIN FETCH e.patient JOIN FETCH e.attendingProvider WHERE e.id = :id AND e.organizationId = :organizationId")
    Optional<Encounter> findByIdWithDetails(@Param("id") Long id, @Param("organizationId") Long organizationId);
}
