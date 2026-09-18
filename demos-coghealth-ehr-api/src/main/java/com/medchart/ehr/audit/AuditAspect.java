package com.medchart.ehr.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;

    @Around("@annotation(com.medchart.ehr.audit.AuditAccess)")
    public Object auditAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuditAccess auditAccess = method.getAnnotation(AuditAccess.class);

        Object[] args = joinPoint.getArgs();
        String[] parameterNames = signature.getParameterNames();
        Long patientId = extractPatientId(auditAccess, parameterNames, args);
        Long resourceId = extractResourceId(auditAccess, parameterNames, args);
        String userId = getCurrentUserId();

        AuditEvent.AuditEventBuilder eventBuilder = AuditEvent.builder()
                .userId(userId)
                .userName(getCurrentUserName())
                .action(auditAccess.action())
                .resourceType(auditAccess.resourceType())
                .description(auditAccess.description())
                .ipAddress(getClientIpAddress())
                .userAgent(getUserAgent());

        try {
            Object result = joinPoint.proceed();
            if (result instanceof AuditableResource) {
                AuditableResource resource = (AuditableResource) result;
                patientId = patientId != null ? patientId : resource.getAuditPatientId();
                resourceId = resourceId != null ? resourceId : resource.getAuditResourceId();
            }
            eventBuilder.success(true);
            auditService.saveAuditEventAsync(eventBuilder.patientId(patientId).resourceId(resourceId).build());
            return result;
        } catch (Exception e) {
            eventBuilder.success(false);
            eventBuilder.errorMessage(e.getMessage());
            auditService.saveAuditEventAsync(eventBuilder.patientId(patientId).resourceId(resourceId).build());
            throw e;
        }
    }

    private Long extractPatientId(AuditAccess auditAccess, String[] parameterNames, Object[] args) {
        for (Object arg : args) {
            if (arg instanceof AuditableResource && ((AuditableResource) arg).getAuditPatientId() != null) {
                return ((AuditableResource) arg).getAuditPatientId();
            }
        }
        if (parameterNames == null || parameterNames.length != args.length) {
            return null;
        }
        for (int i = 0; i < parameterNames.length; i++) {
            if ("patientId".equals(parameterNames[i]) && args[i] instanceof Long) {
                return (Long) args[i];
            }
        }
        if ("Patient".equals(auditAccess.resourceType())) {
            for (int i = 0; i < parameterNames.length; i++) {
                if ("id".equals(parameterNames[i]) && args[i] instanceof Long) {
                    return (Long) args[i];
                }
            }
        }
        return null;
    }

    private Long extractResourceId(AuditAccess auditAccess, String[] parameterNames, Object[] args) {
        for (Object arg : args) {
            if (arg instanceof AuditableResource && ((AuditableResource) arg).getAuditResourceId() != null) {
                return ((AuditableResource) arg).getAuditResourceId();
            }
        }
        if (parameterNames == null || parameterNames.length != args.length) {
            return null;
        }
        String typedName = uncapitalize(auditAccess.resourceType()) + "Id";
        for (int i = 0; i < parameterNames.length; i++) {
            if (("id".equals(parameterNames[i]) || typedName.equals(parameterNames[i])) && args[i] instanceof Long) {
                return (Long) args[i];
            }
        }
        return null;
    }

    private String uncapitalize(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    private String getCurrentUserId() {
        return "system";
    }

    private String getCurrentUserName() {
        return "System User";
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not get client IP", e);
        }
        return null;
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest().getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.debug("Could not get user agent", e);
        }
        return null;
    }
}
