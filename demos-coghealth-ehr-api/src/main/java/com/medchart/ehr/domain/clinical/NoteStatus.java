package com.medchart.ehr.domain.clinical;

public enum NoteStatus {
    DRAFT,
    PENDING_SIGNATURE,
    PENDING_COSIGN,
    SIGNED,
    AMENDED,
    ENTERED_IN_ERROR
}
