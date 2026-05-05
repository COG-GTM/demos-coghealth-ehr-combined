export interface Medication {
  id?: number;
  ndcCode?: string;
  rxnormCode?: string;
  genericName: string;
  brandName?: string;
  manufacturer?: string;
  strength?: string;
  form?: string;
  route?: string;
  schedule?: DrugSchedule;
  controlled?: boolean;
  active?: boolean;
  warnings?: string;
  contraindications?: string;
}

export interface MedicationOrder {
  id?: number;
  orderNumber?: string;
  patientId: number;
  encounterId?: number;
  medicationId: number;
  medication?: Medication;
  prescriberId: number;
  orderDateTime: string;
  status: MedicationOrderStatus;
  dose: string;
  doseUnit: string;
  route: string;
  frequency: string;
  sig?: string;
  startDate?: string;
  endDate?: string;
  quantity?: number;
  refills?: number;
  refillsRemaining?: number;
  daysSupply?: number;
  dispenseAsWritten?: boolean;
  prn?: boolean;
  prnReason?: string;
  instructions?: string;
  pharmacyNotes?: string;
  pharmacy?: string;
  pharmacyNpi?: string;
  sentToPharmacyAt?: string;
  discontinuedReason?: string;
  discontinuedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export type DrugSchedule = 
  | 'SCHEDULE_I'
  | 'SCHEDULE_II'
  | 'SCHEDULE_III'
  | 'SCHEDULE_IV'
  | 'SCHEDULE_V'
  | 'NON_CONTROLLED';

export type MedicationOrderStatus = 
  | 'DRAFT'
  | 'PENDING'
  | 'ACTIVE'
  | 'ON_HOLD'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'DISCONTINUED'
  | 'ENTERED_IN_ERROR';
