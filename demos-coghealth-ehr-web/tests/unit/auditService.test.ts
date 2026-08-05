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
const SESSION_ID_KEY = 'coghealth_session_id';

function createMemoryStorage(): Storage {
  let store: Record<string, string> = {};
  return {
    get length() {
      return Object.keys(store).length;
    },
    clear: () => {
      store = {};
    },
    getItem: (key: string) => (key in store ? store[key] : null),
    key: (index: number) => Object.keys(store)[index] ?? null,
    removeItem: (key: string) => {
      delete store[key];
    },
    setItem: (key: string, value: string) => {
      store[key] = value;
    },
  };
}

function installStorage(name: 'localStorage' | 'sessionStorage'): Storage {
  const storage = createMemoryStorage();
  Object.defineProperty(globalThis, name, { value: storage, configurable: true, writable: true });
  return storage;
}

describe('auditService', () => {
  let localStorageMock: Storage;
  let sessionStorageMock: Storage;

  beforeEach(() => {
    localStorageMock = installStorage('localStorage');
    sessionStorageMock = installStorage('sessionStorage');
  });

  describe('logAuditEvent', () => {
    it('persists an event with the acting user and defaults success to true', () => {
      logAuditEvent('PHI_VIEW', { patientId: 'P1', resourceType: 'LabResult', resourceId: 'L9' });

      const [event] = getAuditLog();
      expect(event).toMatchObject({
        eventType: 'PHI_VIEW',
        patientId: 'P1',
        resourceType: 'LabResult',
        resourceId: 'L9',
        userId: 'USR001',
        success: true,
      });
      expect(event.id).toBeTruthy();
      expect(Date.parse(event.timestamp)).not.toBeNaN();
    });

    it('keeps an explicit failure flag', () => {
      logAuditEvent('FAILED_LOGIN', { success: false, details: 'bad password' });

      expect(getAuditLog()[0]).toMatchObject({ eventType: 'FAILED_LOGIN', success: false });
    });

    it('stores the newest event first', () => {
      logAuditEvent('LOGIN');
      logAuditEvent('LOGOUT');

      expect(getAuditLog().map((event) => event.eventType)).toEqual(['LOGOUT', 'LOGIN']);
    });

    it('caps the log at 1000 entries and drops the oldest ones', () => {
      const overflowing = Array.from({ length: 1000 }, (_, index) => ({
        id: `seed-${index}`,
        eventType: 'LOGIN',
      }));
      localStorageMock.setItem(AUDIT_LOG_KEY, JSON.stringify(overflowing));

      logAuditEvent('PATIENT_ACCESS', { patientId: 'P1' });

      const log = getAuditLog();
      expect(log).toHaveLength(1000);
      expect(log[0].eventType).toBe('PATIENT_ACCESS');
      expect(log[log.length - 1].id).toBe('seed-998');
    });

    it('reuses one session id across events and creates it only once', () => {
      logAuditEvent('LOGIN');
      const sessionId = sessionStorageMock.getItem(SESSION_ID_KEY);
      logAuditEvent('PATIENT_SEARCH');

      expect(sessionId).toBeTruthy();
      expect(getAuditLog().map((event) => event.sessionId)).toEqual([sessionId, sessionId]);
    });
  });

  describe('getAuditLog', () => {
    it('returns an empty log when nothing has been written', () => {
      expect(getAuditLog()).toEqual([]);
    });

    it('recovers from a corrupted log instead of throwing', () => {
      localStorageMock.setItem(AUDIT_LOG_KEY, 'not-json');

      expect(getAuditLog()).toEqual([]);
    });
  });

  describe('clearAuditLog', () => {
    it('removes every stored event', () => {
      logAuditEvent('LOGIN');

      clearAuditLog();

      expect(getAuditLog()).toEqual([]);
    });
  });

  describe('getPatientAccessLog', () => {
    it('returns only PATIENT_ACCESS events for the requested patient', () => {
      logPatientAccess('P1', 'MRN001', 'Ada Lovelace');
      logPatientAccess('P2', 'MRN002', 'Grace Hopper');
      logPHIView('P1', 'LabResult', 'L9');

      const log = getPatientAccessLog('P1');

      expect(log).toHaveLength(1);
      expect(log[0]).toMatchObject({ patientId: 'P1', patientMrn: 'MRN001', eventType: 'PATIENT_ACCESS' });
    });
  });

  describe('helpers', () => {
    it('logPatientSearch records the query and the result count', () => {
      logPatientSearch('lovelace', 3);

      expect(getAuditLog()[0]).toMatchObject({
        eventType: 'PATIENT_SEARCH',
        details: 'Query: "lovelace" - 3 results',
      });
    });

    it('logPHIView names the resource that was viewed', () => {
      logPHIView('P1', 'Vitals', 'V4');

      expect(getAuditLog()[0]).toMatchObject({
        eventType: 'PHI_VIEW',
        resourceType: 'Vitals',
        resourceId: 'V4',
        action: 'Viewed Vitals',
      });
    });

    it('logPrint records the printed document type', () => {
      logPrint('P1', 'Discharge Summary');

      expect(getAuditLog()[0]).toMatchObject({
        eventType: 'PHI_PRINT',
        patientId: 'P1',
        resourceType: 'Discharge Summary',
      });
    });

    it('logPrescription and logOrder record their details', () => {
      logPrescription('P1', 'Lisinopril 10mg');
      logOrder('P1', 'LAB', 'CBC with differential');

      const [order, prescription] = getAuditLog();
      expect(prescription).toMatchObject({
        eventType: 'PRESCRIPTION_CREATE',
        details: 'Lisinopril 10mg',
      });
      expect(order).toMatchObject({
        eventType: 'ORDER_CREATE',
        resourceType: 'LAB',
        details: 'CBC with differential',
      });
    });

    it('logLogout distinguishes a manual logout from a session timeout', () => {
      logLogout();
      logLogout('timeout');

      const [timeout, manual] = getAuditLog();
      expect(manual.eventType).toBe('LOGOUT');
      expect(timeout.eventType).toBe('SESSION_TIMEOUT');
    });
  });
});
