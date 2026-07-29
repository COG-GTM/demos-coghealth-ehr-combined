import type { RefillRequest } from '../types';
import { api } from './api';

export const refillRequestService = {
  getPending: () => api.get<RefillRequest[]>('/v1/refill-requests/pending'),
  approve: (id: number) => api.post<RefillRequest>(`/v1/refill-requests/${id}/approve`),
  deny: (id: number) => api.post<RefillRequest>(`/v1/refill-requests/${id}/deny`),
};
