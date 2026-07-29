import {
  clearAuditLog,
  getAuditLog,
  getPatientAccessLog,
  logAuditEvent,
  logLogout,
  logOrder,
  logPHIView,
  logPatientAccess,
  logPatientSearch,
  logPrescription,
  logPrint,
} from '../../src/services/auditService';

const AUDIT_LOG_KEY = 'coghealth_audit_log';

class MemoryStorage implements Storage {
  private store = new Map<string, string>();

  get length(): number {
    return this.store.size;
  }

  clear(): void {
    this.store.clear();
  }

  getItem(key: string): string | null {
    return this.store.has(key) ? (this.store.get(key) as string) : null;
  }

  key(index: number): string | null {
    return Array.from(this.store.keys())[index] ?? null;
  }

  removeItem(key: string): void {
    this.store.delete(key);
  }

  setItem(key: string, value: string): void {
    this.store.set(key, value);
  }
}

describe('auditService', () => {
  beforeEach(() => {
    globalThis.localStorage = new MemoryStorage();
    globalThis.sessionStorage = new MemoryStorage();
  });

  it('records an event with user, session and timestamp metadata', () => {
    logAuditEvent('LOGIN');

    const [event] = getAuditLog();
    expect(event).toMatchObject({
      eventType: 'LOGIN',
      userId: 'USR001',
      userRole: 'Physician',
      success: true,
    });
    expect(event.id).toBeTruthy();
    expect(event.sessionId).toBeTruthy();
    expect(new Date(event.timestamp).toString()).not.toBe('Invalid Date');
  });

  it('marks failures when success is false', () => {
    logAuditEvent('FAILED_LOGIN', { success: false, details: 'bad password' });

    expect(getAuditLog()[0]).toMatchObject({ success: false, details: 'bad password' });
  });

  it('reuses the session id across events', () => {
    logAuditEvent('LOGIN');
    logAuditEvent('LOGOUT');

    const [logout, login] = getAuditLog();
    expect(logout.sessionId).toBe(login.sessionId);
    expect(sessionStorage.getItem('coghealth_session_id')).toBe(login.sessionId);
  });

  it('stores the newest event first', () => {
    logAuditEvent('LOGIN');
    logPatientSearch('doe', 3);

    expect(getAuditLog().map((event) => event.eventType)).toEqual(['PATIENT_SEARCH', 'LOGIN']);
  });

  it('caps the log at 1000 entries, dropping the oldest', () => {
    const seeded = Array.from({ length: 1000 }, (_, index) => ({
      id: `seed-${index}`,
      timestamp: new Date().toISOString(),
      eventType: 'PHI_VIEW',
      userId: 'USR001',
      userName: 'Dr. Sarah Anderson',
      userRole: 'Physician',
      ipAddress: '192.168.1.100',
      sessionId: 'seed-session',
      success: true,
    }));
    localStorage.setItem(AUDIT_LOG_KEY, JSON.stringify(seeded));

    logAuditEvent('LOGIN');

    const log = getAuditLog();
    expect(log).toHaveLength(1000);
    expect(log[0].eventType).toBe('LOGIN');
    expect(log.some((event) => event.id === 'seed-999')).toBe(false);
  });

  it('returns an empty log when storage holds corrupt JSON', () => {
    localStorage.setItem(AUDIT_LOG_KEY, '{not json');

    expect(getAuditLog()).toEqual([]);
  });

  it('clears the log', () => {
    logAuditEvent('LOGIN');
    clearAuditLog();

    expect(getAuditLog()).toEqual([]);
  });

  it('filters patient access events by patient', () => {
    logPatientAccess('1', 'MRN001', 'Jane Doe');
    logPatientAccess('2', 'MRN002', 'John Smith');
    logPHIView('1', 'LabResult', 'LAB-9');

    const accessLog = getPatientAccessLog('1');
    expect(accessLog).toHaveLength(1);
    expect(accessLog[0]).toMatchObject({
      patientId: '1',
      patientMrn: 'MRN001',
      patientName: 'Jane Doe',
      eventType: 'PATIENT_ACCESS',
      action: 'Opened patient chart',
    });
  });

  it('records the query and result count for searches', () => {
    logPatientSearch('doe', 3);

    expect(getAuditLog()[0].details).toBe('Query: "doe" - 3 results');
  });

  it('records PHI views, prints, prescriptions and orders', () => {
    logPHIView('1', 'LabResult', 'LAB-9');
    logPrint('1', 'Discharge Summary');
    logPrescription('1', 'Lisinopril 10mg');
    logOrder('1', 'Lab', 'CBC with differential');

    expect(getAuditLog().map((event) => event.eventType)).toEqual([
      'ORDER_CREATE',
      'PRESCRIPTION_CREATE',
      'PHI_PRINT',
      'PHI_VIEW',
    ]);
    expect(getAuditLog()[3]).toMatchObject({
      resourceType: 'LabResult',
      resourceId: 'LAB-9',
      action: 'Viewed LabResult',
    });
  });

  it('distinguishes manual logout from session timeout', () => {
    logLogout();
    logLogout('timeout');

    const [timeout, manual] = getAuditLog();
    expect(timeout.eventType).toBe('SESSION_TIMEOUT');
    expect(timeout.action).toBe('Session timed out');
    expect(manual.eventType).toBe('LOGOUT');
  });
});
