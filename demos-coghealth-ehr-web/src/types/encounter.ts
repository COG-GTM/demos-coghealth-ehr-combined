export interface Encounter {
  id?: number;
  encounterNumber?: string;
  patientId: number;
  attendingProviderId?: number;
  encounterType: EncounterType;
  status: EncounterStatus;
  encounterDateTime: string;
  admitDateTime?: string;
  dischargeDateTime?: string;
  department?: string;
  room?: string;
  bed?: string;
  chiefComplaint?: string;
  priority?: EncounterPriority;
  visitType?: string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export type EncounterType = 
  | 'OUTPATIENT'
  | 'INPATIENT'
  | 'EMERGENCY'
  | 'OBSERVATION'
  | 'TELEHEALTH'
  | 'HOME_HEALTH'
  | 'AMBULATORY'
  | 'DAY_SURGERY'
  | 'LAB_ONLY'
  | 'RADIOLOGY_ONLY';

export type EncounterStatus = 
  | 'PLANNED'
  | 'ARRIVED'
  | 'TRIAGED'
  | 'IN_PROGRESS'
  | 'ON_HOLD'
  | 'FINISHED'
  | 'CANCELLED'
  | 'ENTERED_IN_ERROR'
  | 'UNKNOWN';

export type EncounterPriority = 'STAT' | 'URGENT' | 'ROUTINE' | 'ELECTIVE';
