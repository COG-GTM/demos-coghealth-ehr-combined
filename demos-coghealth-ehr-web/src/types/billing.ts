export type ClaimStatus = 'SUBMITTED' | 'PENDING' | 'DENIED' | 'PAID' | 'APPEALED' | 'PARTIAL';

export type ClaimCategory = 'active' | 'denied' | 'paid';

export interface Claim {
  id: string;
  patientId: number;
  patientName: string;
  patientMrn: string;
  payer: string;
  payerId: string;
  cptCodes: string[];
  icdCodes: string[];
  billedAmount: number;
  allowedAmount?: number;
  paidAmount?: number;
  balance: number;
  status: ClaimStatus;
  category: ClaimCategory;
  dateOfService: string;
  dateSubmitted: string;
  datePaid?: string;
  denialReason?: string;
  authorizationNumber?: string;
  claimNumber: string;
  provider: string;
  agingDays: number;
}

export interface BillingSummary {
  totalBilled: number;
  totalCollected: number;
  totalPending: number;
  pendingClaimsCount: number;
  deniedClaimsCount: number;
  denialRate: number;
}

export interface PayerSummary {
  payer: string;
  claimCount: number;
  totalBilled: number;
}

export interface AgingBucket {
  label: string;
  amount: number;
  count: number;
}
