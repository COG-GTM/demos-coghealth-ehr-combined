package com.medchart.ehr.repository;

import com.medchart.ehr.domain.chronic.MedicationFill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicationFillRepository extends JpaRepository<MedicationFill, Long> {

    List<MedicationFill> findByPatientId(Long patientId);

    List<MedicationFill> findByPatientIdAndMedicationOrderId(Long patientId, Long medicationOrderId);

    List<MedicationFill> findByPatientIdAndMedicationOrderIdAndFillDateBetween(
            Long patientId, Long medicationOrderId, LocalDate startDate, LocalDate endDate);

    List<MedicationFill> findByNdc(String ndc);

    @Query("SELECT mf FROM MedicationFill mf WHERE mf.patientId = :patientId " +
           "AND mf.medicationOrderId = :medicationOrderId " +
           "AND mf.fillDate >= :startDate " +
           "ORDER BY mf.fillDate DESC")
    List<MedicationFill> findFillsForPdcCalculation(Long patientId, Long medicationOrderId, LocalDate startDate);

    @Query("SELECT mf FROM MedicationFill mf WHERE mf.fillDate BETWEEN :startDate AND :endDate " +
           "ORDER BY mf.fillDate")
    List<MedicationFill> findFillsInPeriod(LocalDate startDate, LocalDate endDate);

    @Query("SELECT mf FROM MedicationFill mf WHERE mf.patientId = :patientId " +
           "AND mf.fillDate = :fillDate")
    List<MedicationFill> findByPatientIdAndFillDate(Long patientId, LocalDate fillDate);

    @Query("SELECT COUNT(mf) FROM MedicationFill mf WHERE mf.patientId = :patientId " +
           "AND mf.medicationOrderId = :medicationOrderId " +
           "AND mf.onTime = false")
    long countLateFills(Long patientId, Long medicationOrderId);
}