import { apiClient } from './client';
import type { Encounter, Appointment } from '../types';

export const encountersApi = {
  getById: async (id: number): Promise<Encounter> => {
    const { data } = await apiClient.get(`/v1/encounters/${id}`);
    return data;
  },

  getByPatient: async (patientId: number): Promise<Encounter[]> => {
    const { data } = await apiClient.get(`/v1/encounters/patient/${patientId}`);
    return data;
  },

  getSchedule: async (date: string, providerId?: number): Promise<Appointment[]> => {
    const params: Record<string, string | number> = { date };
    if (providerId) params.providerId = providerId;
    const { data } = await apiClient.get('/v1/encounters/schedule', { params });
    return data;
  },

  create: async (encounter: Partial<Encounter>): Promise<Encounter> => {
    const { data } = await apiClient.post('/v1/encounters', encounter);
    return data;
  },

  update: async (id: number, encounter: Partial<Encounter>): Promise<Encounter> => {
    const { data } = await apiClient.put(`/v1/encounters/${id}`, encounter);
    return data;
  },

  checkIn: async (id: number): Promise<Encounter> => {
    const { data } = await apiClient.post(`/v1/encounters/${id}/check-in`);
    return data;
  },

  startVisit: async (id: number): Promise<Encounter> => {
    const { data } = await apiClient.post(`/v1/encounters/${id}/start`);
    return data;
  },

  completeVisit: async (id: number): Promise<Encounter> => {
    const { data } = await apiClient.post(`/v1/encounters/${id}/complete`);
    return data;
  },

  cancel: async (id: number, reason: string): Promise<Encounter> => {
    const { data } = await apiClient.post(`/v1/encounters/${id}/cancel`, { reason });
    return data;
  },

  markNoShow: async (id: number): Promise<Encounter> => {
    const { data } = await apiClient.post(`/v1/encounters/${id}/no-show`);
    return data;
  },
};
