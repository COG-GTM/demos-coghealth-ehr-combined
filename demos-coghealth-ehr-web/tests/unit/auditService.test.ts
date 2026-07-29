import {
  clearAuditLog,
  getAuditLog,
  getPatientAccessLog,
  logAuditEvent,
  logLogout,
  logPatientAccess,
  logPatientSearch,
  logPHIView,
  logPrescription,
} from '../../src/services/auditService';

const AUDIT_LOG_KEY = 'coghealth_audit_log';

describe('auditService', () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
  });

  test('logAuditEvent persists an event with default metadata', () => {
    logAuditEvent('LOGIN');

    const [event] = getAuditLog();
    expect(event).toMatchObject({
      eventType: 'LOGIN',
      userId: 'USR001',
      userRole: 'Physician',
      success: true,
    });
    expect(event.id).toBeTruthy();
    expect(Date.parse(event.timestamp)).not.toBeNaN();
  });

  test('logAuditEvent honours an explicit failure flag', () => {
    logAuditEvent('FAILED_LOGIN', { success: false, details: 'bad password' });

    expect(getAuditLog()[0]).toMatchObject({ success: false, details: 'bad password' });
  });

  test('events are stored newest first', () => {
    logAuditEvent('LOGIN');
    logAuditEvent('LOGOUT');

    expect(getAuditLog().map(e => e.eventType)).toEqual(['LOGOUT', 'LOGIN']);
  });

  test('log is capped at 1000 entries, dropping the oldest', () => {
    const entries = Array.from({ length: 1000 }, (_, i) => ({ id: `old-${i}`, eventType: 'LOGIN' }));
    localStorage.setItem(AUDIT_LOG_KEY, JSON.stringify(entries));

    logAuditEvent('PHI_VIEW');

    const log = getAuditLog();
    expect(log).toHaveLength(1000);
    expect(log[0].eventType).toBe('PHI_VIEW');
    expect(log[log.length - 1].id).toBe('old-998');
  });

  test('all events in a session share one session id', () => {
    logAuditEvent('LOGIN');
    logAuditEvent('LOGOUT');

    const [first, second] = getAuditLog();
    expect(first.sessionId).toBe(second.sessionId);
    expect(sessionStorage.getItem('coghealth_session_id')).toBe(first.sessionId);
  });

  test('getAuditLog returns an empty log when storage holds invalid JSON', () => {
    localStorage.setItem(AUDIT_LOG_KEY, 'not-json');

    expect(getAuditLog()).toEqual([]);
  });

  test('clearAuditLog removes all entries', () => {
    logAuditEvent('LOGIN');

    clearAuditLog();

    expect(getAuditLog()).toEqual([]);
  });

  test('getPatientAccessLog filters by patient and event type', () => {
    logPatientAccess('1', 'MRN-2019-00001', 'John Smith');
    logPatientAccess('2', 'MRN-2019-00002', 'Jane Doe');
    logPHIView('1', 'LabResult', '55');

    const accessLog = getPatientAccessLog('1');

    expect(accessLog).toHaveLength(1);
    expect(accessLog[0]).toMatchObject({
      eventType: 'PATIENT_ACCESS',
      patientMrn: 'MRN-2019-00001',
      patientName: 'John Smith',
      action: 'Opened patient chart',
    });
  });

  test('logPatientSearch records the query and result count', () => {
    logPatientSearch('smith', 3);

    expect(getAuditLog()[0]).toMatchObject({
      eventType: 'PATIENT_SEARCH',
      details: 'Query: "smith" - 3 results',
    });
  });

  test('logPHIView records the viewed resource', () => {
    logPHIView('1', 'LabResult', '55');

    expect(getAuditLog()[0]).toMatchObject({
      eventType: 'PHI_VIEW',
      resourceType: 'LabResult',
      resourceId: '55',
      action: 'Viewed LabResult',
    });
  });

  test('logPrescription records the medication as details', () => {
    logPrescription('1', 'Lisinopril 10mg');

    expect(getAuditLog()[0]).toMatchObject({
      eventType: 'PRESCRIPTION_CREATE',
      details: 'Lisinopril 10mg',
    });
  });

  test('logLogout distinguishes manual logout from session timeout', () => {
    logLogout();
    logLogout('timeout');

    const [timeout, manual] = getAuditLog();
    expect(timeout).toMatchObject({ eventType: 'SESSION_TIMEOUT', action: 'Session timed out' });
    expect(manual).toMatchObject({ eventType: 'LOGOUT', action: 'User logged out' });
  });
});
