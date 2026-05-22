package com.medchart.ehr.repository;

import com.medchart.ehr.domain.chronic.MedicationAdherence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationAdherenceRepository extends JpaRepository<MedicationAdherence, Long> {

    List<MedicationAdherence> findByPatientId(Long patientId);

    Optional<MedicationAdherence> findByPatientIdAndMedicationOrderId(Long patientId, Long medicationOrderId);

    List<MedicationAdherence> findByPatientIdAndPeriodStartBetween(Long patientId, LocalDate periodStart, LocalDate periodEnd);

    @Query("SELECT ma FROM MedicationAdherence ma WHERE ma.patientId = :patientId " +
           "AND ma.adherenceStatus = 'NON_ADHERENT' AND ma.alertSent = false")
    List<MedicationAdherence> findNonAdherentWithoutAlert(Long patientId);

    @Query("SELECT ma FROM MedicationAdherence ma WHERE ma.adherenceStatus = 'NON_ADHERENT' " +
           "OR ma.adherenceStatus = 'PARTIALLY_ADHERENT' " +
           "AND ma.alertSent = false")
    List<MedicationAdherence> findPatientsNeedingAlert();

    @Query("SELECT ma FROM MedicationAdherence ma WHERE ma.patientId = :patientId " +
           "AND ma.periodEnd >= :currentDate")
    List<MedicationAdherence> findActiveAdherenceByPatient(Long patientId, LocalDate currentDate);
}