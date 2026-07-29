export type RefillRequestStatus = 'PENDING' | 'APPROVED' | 'DENIED';

export interface RefillRequest {
  id: number;
  pharmacyName: string;
  status: RefillRequestStatus;
  requestedDate: string;
  patient: {
    id: number;
    mrn: string;
    fullName: string;
  };
  medication: {
    id: number;
    genericName: string;
    brandName?: string;
  };
}
