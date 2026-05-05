package com.medchart.ehr.domain.chronic;

/**
 * Supported chronic condition types for disease management.
 */
public enum ChronicConditionType {
    DIABETES_TYPE_1("E10", "Type 1 Diabetes Mellitus"),
    DIABETES_TYPE_2("E11", "Type 2 Diabetes Mellitus"),
    HYPERTENSION("I10", "Essential Hypertension"),
    COPD("J44", "Chronic Obstructive Pulmonary Disease"),
    CHF("I50", "Congestive Heart Failure"),
    CKD("N18", "Chronic Kidney Disease"),
    ASTHMA("J45", "Asthma"),
    OBESITY("E66", "Obesity"),
    HYPERLIPIDEMIA("E78", "Hyperlipidemia"),
    ATRIAL_FIBRILLATION("I48", "Atrial Fibrillation");

    private final String icd10Prefix;
    private final String description;

    ChronicConditionType(String icd10Prefix, String description) {
        this.icd10Prefix = icd10Prefix;
        this.description = description;
    }

    public String getIcd10Prefix() {
        return icd10Prefix;
    }

    public String getDescription() {
        return description;
    }
}
