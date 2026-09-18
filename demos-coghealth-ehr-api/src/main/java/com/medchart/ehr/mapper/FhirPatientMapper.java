package com.medchart.ehr.mapper;

import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.domain.patient.Gender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * FHIR R4 Patient resource mapper.
 * 
 * PATTERN: FHIR Resource Mapping
 * - Map internal domain objects to FHIR R4 resources
 * - Handle identifier systems (MRN, SSN, etc.)
 * - Support both JSON and XML serialization
 * - Validate required FHIR elements
 * 
 * Reference: https://www.hl7.org/fhir/patient.html
 */
@Component
@Slf4j
public class FhirPatientMapper {

    private static final String FHIR_DATE_FORMAT = "yyyy-MM-dd";
    private static final String MRN_SYSTEM = "http://hospital.example.org/mrn";
    private static final String SSN_SYSTEM = "http://hl7.org/fhir/sid/us-ssn";

    private static final Pattern MRN_PATTERN = Pattern.compile("[A-Za-z0-9-]{1,20}");
    private static final Pattern SSN_PATTERN = Pattern.compile("\\d{3}-\\d{2}-\\d{4}");
    private static final int MAX_NAME_LENGTH = 100;
    private static final DateTimeFormatter FHIR_DATE_PARSER = DateTimeFormatter.ofPattern(FHIR_DATE_FORMAT);

    /**
     * PATTERN: Convert internal Patient to FHIR Patient resource
     * 
     * Maps all relevant fields to FHIR R4 Patient structure.
     * Returns a Map that can be serialized to JSON.
     */
    public Map<String, Object> toFhirResource(Patient patient) {
        Map<String, Object> fhirPatient = new HashMap<>();
        
        // Resource metadata
        fhirPatient.put("resourceType", "Patient");
        fhirPatient.put("id", patient.getId().toString());
        
        // Identifiers (MRN, SSN)
        fhirPatient.put("identifier", buildIdentifiers(patient));
        
        // Name
        fhirPatient.put("name", List.of(buildName(patient)));
        
        // Gender
        fhirPatient.put("gender", mapGender(patient.getGender()));
        
        // Birth date
        if (patient.getDateOfBirth() != null) {
            fhirPatient.put("birthDate", patient.getDateOfBirth().format(DateTimeFormatter.ofPattern(FHIR_DATE_FORMAT)));
        }
        
        // Contact info
        fhirPatient.put("telecom", buildTelecom(patient));
        
        // Address
        if (patient.getAddress() != null) {
            fhirPatient.put("address", List.of(buildAddress(patient)));
        }
        
        // Active status
        fhirPatient.put("active", Boolean.TRUE.equals(patient.getActive()));
        
        // Deceased
        if (Boolean.TRUE.equals(patient.getDeceased())) {
            fhirPatient.put("deceasedBoolean", true);
            if (patient.getDeceasedDate() != null) {
                fhirPatient.put("deceasedDateTime", patient.getDeceasedDate().toString());
            }
        }
        
        // Marital status
        if (patient.getMaritalStatus() != null) {
            fhirPatient.put("maritalStatus", buildMaritalStatus(patient));
        }
        
        log.debug("Mapped patient {} to FHIR resource", patient.getMrn());
        return fhirPatient;
    }

    /**
     * PATTERN: Convert FHIR Patient resource to internal Patient
     */
    public Patient fromFhirResource(Map<String, Object> fhirPatient) {
        if (fhirPatient == null) {
            throw new IllegalArgumentException("FHIR Patient resource is required");
        }
        if (!"Patient".equals(asString(fhirPatient.get("resourceType"), "resourceType"))) {
            throw new IllegalArgumentException("Unsupported FHIR resourceType: expected 'Patient'");
        }

        Patient patient = new Patient();
        
        // Extract identifiers
        for (Map<?, ?> identifier : asMapList(fhirPatient.get("identifier"), "identifier")) {
            String system = asString(identifier.get("system"), "identifier.system");
            String value = asString(identifier.get("value"), "identifier.value");
            
            if (MRN_SYSTEM.equals(system)) {
                patient.setMrn(matching(value, MRN_PATTERN, "MRN identifier value"));
            } else if (SSN_SYSTEM.equals(system)) {
                patient.setSsn(matching(value, SSN_PATTERN, "SSN identifier value"));
            }
        }
        if (patient.getMrn() == null) {
            throw new IllegalArgumentException("FHIR Patient requires an identifier with system " + MRN_SYSTEM);
        }
        
        // Extract name
        List<Map<?, ?>> names = asMapList(fhirPatient.get("name"), "name");
        if (names.isEmpty()) {
            throw new IllegalArgumentException("FHIR Patient requires at least one name");
        }
        Map<?, ?> name = names.get(0);
        patient.setLastName(requiredName(asString(name.get("family"), "name.family"), "name.family"));
        List<String> given = asStringList(name.get("given"), "name.given");
        if (given.isEmpty()) {
            throw new IllegalArgumentException("FHIR Patient requires name.given");
        }
        patient.setFirstName(requiredName(given.get(0), "name.given[0]"));
        if (given.size() > 1) {
            patient.setMiddleName(requiredName(given.get(1), "name.given[1]"));
        }
        
        // Extract gender
        String gender = asString(fhirPatient.get("gender"), "gender");
        patient.setGender(gender != null ? mapFhirGender(gender) : Gender.UNKNOWN);
        
        // Extract birth date
        String birthDate = asString(fhirPatient.get("birthDate"), "birthDate");
        if (birthDate == null) {
            throw new IllegalArgumentException("FHIR Patient requires birthDate");
        }
        patient.setDateOfBirth(parseBirthDate(birthDate));
        
        // Active status
        Object active = fhirPatient.get("active");
        if (active != null && !(active instanceof Boolean)) {
            throw new IllegalArgumentException("FHIR Patient active must be a boolean");
        }
        patient.setActive(active == null || (Boolean) active);
        
        log.debug("Mapped FHIR resource to patient {}", patient.getMrn());
        return patient;
    }

    private String asString(Object value, String field) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("FHIR Patient " + field + " must be a string");
        }
        String text = ((String) value).trim();
        return text.isEmpty() ? null : text;
    }

    private List<Map<?, ?>> asMapList(Object value, String field) {
        if (value == null) {
            return Collections.emptyList();
        }
        if (!(value instanceof List)) {
            throw new IllegalArgumentException("FHIR Patient " + field + " must be an array");
        }
        List<Map<?, ?>> elements = new ArrayList<>();
        for (Object element : (List<?>) value) {
            if (!(element instanceof Map)) {
                throw new IllegalArgumentException("FHIR Patient " + field + " entries must be objects");
            }
            elements.add((Map<?, ?>) element);
        }
        return elements;
    }

    private List<String> asStringList(Object value, String field) {
        if (value == null) {
            return Collections.emptyList();
        }
        if (!(value instanceof List)) {
            throw new IllegalArgumentException("FHIR Patient " + field + " must be an array");
        }
        List<String> elements = new ArrayList<>();
        for (Object element : (List<?>) value) {
            elements.add(asString(element, field + " entry"));
        }
        return elements;
    }

    private String matching(String value, Pattern pattern, String field) {
        if (value == null || !pattern.matcher(value).matches()) {
            throw new IllegalArgumentException("FHIR Patient " + field + " has an invalid format");
        }
        return value;
    }

    private String requiredName(String value, String field) {
        if (value == null) {
            throw new IllegalArgumentException("FHIR Patient " + field + " is required");
        }
        if (value.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("FHIR Patient " + field + " exceeds " + MAX_NAME_LENGTH + " characters");
        }
        return value;
    }

    private LocalDate parseBirthDate(String birthDate) {
        LocalDate parsed;
        try {
            parsed = LocalDate.parse(birthDate, FHIR_DATE_PARSER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("FHIR Patient birthDate must use the " + FHIR_DATE_FORMAT + " format");
        }
        if (parsed.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("FHIR Patient birthDate must be in the past");
        }
        return parsed;
    }

    private List<Map<String, Object>> buildIdentifiers(Patient patient) {
        return List.of(
            Map.of(
                "system", MRN_SYSTEM,
                "value", patient.getMrn(),
                "use", "official"
            ),
            Map.of(
                "system", SSN_SYSTEM,
                "value", patient.getSsn() != null ? patient.getSsn() : "",
                "use", "secondary"
            )
        );
    }

    private Map<String, Object> buildName(Patient patient) {
        Map<String, Object> name = new HashMap<>();
        name.put("use", "official");
        name.put("family", patient.getLastName());
        
        if (patient.getMiddleName() != null) {
            name.put("given", List.of(patient.getFirstName(), patient.getMiddleName()));
        } else {
            name.put("given", List.of(patient.getFirstName()));
        }
        
        return name;
    }

    private String mapGender(Gender gender) {
        if (gender == null) return "unknown";
        switch (gender) {
            case MALE: return "male";
            case FEMALE: return "female";
            case OTHER: return "other";
            default: return "unknown";
        }
    }

    private Gender mapFhirGender(String fhirGender) {
        switch (fhirGender.toLowerCase()) {
            case "male": return Gender.MALE;
            case "female": return Gender.FEMALE;
            case "other": return Gender.OTHER;
            case "unknown": return Gender.UNKNOWN;
            default: throw new IllegalArgumentException("FHIR Patient gender must be male, female, other or unknown");
        }
    }

    private List<Map<String, Object>> buildTelecom(Patient patient) {
        java.util.ArrayList<Map<String, Object>> telecom = new java.util.ArrayList<>();
        
        if (patient.getPhoneHome() != null) {
            telecom.add(Map.of("system", "phone", "value", patient.getPhoneHome(), "use", "home"));
        }
        if (patient.getPhoneMobile() != null) {
            telecom.add(Map.of("system", "phone", "value", patient.getPhoneMobile(), "use", "mobile"));
        }
        if (patient.getEmail() != null) {
            telecom.add(Map.of("system", "email", "value", patient.getEmail()));
        }
        
        return telecom;
    }

    private Map<String, Object> buildAddress(Patient patient) {
        var addr = patient.getAddress();
        Map<String, Object> address = new HashMap<>();
        address.put("use", "home");
        
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        if (addr.getStreet1() != null) lines.add(addr.getStreet1());
        if (addr.getStreet2() != null) lines.add(addr.getStreet2());
        address.put("line", lines);
        
        address.put("city", addr.getCity());
        address.put("state", addr.getState());
        address.put("postalCode", addr.getZipCode());
        address.put("country", "US");
        
        return address;
    }

    private Map<String, Object> buildMaritalStatus(Patient patient) {
        String code;
        switch (patient.getMaritalStatus()) {
            case SINGLE: code = "S"; break;
            case MARRIED: code = "M"; break;
            case DIVORCED: code = "D"; break;
            case WIDOWED: code = "W"; break;
            default: code = "UNK"; break;
        }
        
        Map<String, Object> coding = new java.util.HashMap<>();
        coding.put("system", "http://terminology.hl7.org/CodeSystem/v3-MaritalStatus");
        coding.put("code", code);
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("coding", java.util.Collections.singletonList(coding));
        return result;
    }
}
