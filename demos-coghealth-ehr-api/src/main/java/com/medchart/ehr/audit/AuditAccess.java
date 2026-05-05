package com.medchart.ehr.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditAccess {
    
    AuditAction action() default AuditAction.READ;
    
    String resourceType();
    
    String description() default "";
}
