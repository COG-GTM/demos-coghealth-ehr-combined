package com.medchart.ehr.audit;

/**
 * Implemented by domain records whose reads and edits are audited, so the audit
 * trail can identify the record and the patient it belongs to.
 */
public interface AuditableResource {

    Long getAuditResourceId();

    Long getAuditPatientId();
}
