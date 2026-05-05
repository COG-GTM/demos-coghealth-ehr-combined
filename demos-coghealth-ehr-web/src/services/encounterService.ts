import { api } from './api';
import type { Encounter } from '../types';

export const encounterService = {
  getById: (id: number) => 
    api.get<Encounter>(`/v1/encounters/${id}`),

  getByNumber: (encounterNumber: string) => 
    api.get<Encounter>(`/v1/encounters/number/${encounterNumber}`),

  getByPatient: (patientId: number) => 
    api.get<Encounter[]>(`/v1/encounters/patient/${patientId}`),

  getByProvider: (providerId: number) => 
    api.get<Encounter[]>(`/v1/encounters/provider/${providerId}`),

  getProviderSchedule: (providerId: number, date: string) => 
    api.get<Encounter[]>(`/v1/encounters/provider/${providerId}/schedule`, { date }),

  getByDateRange: (startDate: string, endDate: string) => 
    api.get<Encounter[]>('/v1/encounters/date-range', { startDate, endDate }),

  getByStatus: (status: string) => 
    api.get<Encounter[]>(`/v1/encounters/status/${status}`),

  create: (encounter: Partial<Encounter>) => 
    api.post<Encounter>('/v1/encounters', encounter),

  update: (id: number, encounter: Partial<Encounter>) => 
    api.put<Encounter>(`/v1/encounters/${id}`, encounter),

  checkIn: (id: number) => 
    api.post<void>(`/v1/encounters/${id}/check-in`),

  start: (id: number) => 
    api.post<void>(`/v1/encounters/${id}/start`),

  complete: (id: number, notes?: string) => 
    api.post<void>(`/v1/encounters/${id}/complete`, notes),

  cancel: (id: number) => 
    api.post<void>(`/v1/encounters/${id}/cancel`),

  markNoShow: (id: number) => 
    api.post<void>(`/v1/encounters/${id}/no-show`),
};
