package com.medchart.ehr.legacy;

import com.medchart.ehr.service.PhiExportStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class ReportGenerator {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PhiExportStorage exportStorage;

    public String generatePatientRoster() {
        String handle = "patient_roster_"
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        return exportStorage.store(handle, buildPatientRoster());
    }

    private byte[] buildPatientRoster() {
        String sql = "SELECT p.id, p.mrn, p.ssn, p.first_name, p.last_name, p.date_of_birth, " +
                     "p.phone_home, p.phone_mobile, p.email, " +
                     "p.street1, p.city, p.state, p.zip_code, " +
                     "ic.payer_name, ic.member_id " +
                     "FROM patients p " +
                     "LEFT JOIN insurance_coverages ic ON p.id = ic.patient_id AND ic.active = true " +
                     "WHERE p.active = true";
        
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,MRN,SSN,FirstName,LastName,DOB,PhoneHome,PhoneMobile,Email,Address,City,State,Zip,Insurance,MemberID\n");
        for (Object[] row : results) {
            for (int i = 0; i < row.length; i++) {
                if (i > 0) csv.append(",");
                csv.append(row[i] != null ? row[i].toString().replace(",", ";") : "");
            }
            csv.append("\n");
        }

        log.info("Generated patient roster for {} patients", results.size());
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public String generateEncounterSummary(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT e.id, e.encounter_number, e.encounter_date_time, e.encounter_type, e.status, " +
                     "p.mrn, p.first_name, p.last_name, p.ssn, p.date_of_birth, " +
                     "pr.first_name as provider_first, pr.last_name as provider_last " +
                     "FROM encounters e " +
                     "JOIN patients p ON e.patient_id = p.id " +
                     "LEFT JOIN providers pr ON e.attending_provider_id = pr.id " +
                     "WHERE e.encounter_date_time BETWEEN ?1 AND ?2";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        List<Object[]> results = query.getResultList();
        
        StringBuilder report = new StringBuilder();
        report.append("ENCOUNTER SUMMARY REPORT\n");
        report.append("========================\n");
        report.append("Date Range: ").append(startDate).append(" to ").append(endDate).append("\n");
        report.append("Generated: ").append(LocalDateTime.now()).append("\n");
        report.append("Total Encounters: ").append(results.size()).append("\n\n");

        for (Object[] row : results) {
            report.append("Encounter: ").append(row[1]).append("\n");
            report.append("  Date: ").append(row[2]).append("\n");
            report.append("  Type: ").append(row[3]).append(" | Status: ").append(row[4]).append("\n");
            report.append("  Patient: ").append(row[6]).append(" ").append(row[7])
                  .append(" (MRN: ").append(row[5]).append(")\n");
            report.append("  DOB: ").append(row[8]).append("\n");
            report.append("  Provider: ").append(row[9]).append(" ").append(row[10]).append("\n\n");
        }

        String handle = "encounter_summary_"
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
        log.info("Generated encounter summary for {} encounters", results.size());
        return exportStorage.store(handle, report.toString().getBytes(StandardCharsets.UTF_8));
    }

    public byte[] generateDailyReport() {
        return buildPatientRoster();
    }
}
