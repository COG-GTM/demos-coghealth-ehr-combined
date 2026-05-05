import { apiClient } from './client';
import type { LabResult, LabPanel, ImagingResult } from '../types';

export const resultsApi = {
  getLabResults: async (patientId: number): Promise<LabPanel[]> => {
    const { data } = await apiClient.get(`/v1/patients/${patientId}/results/labs`);
    return data;
  },

  getLabResult: async (id: number): Promise<LabResult> => {
    const { data } = await apiClient.get(`/v1/results/labs/${id}`);
    return data;
  },

  getImagingResults: async (patientId: number): Promise<ImagingResult[]> => {
    const { data } = await apiClient.get(`/v1/patients/${patientId}/results/imaging`);
    return data;
  },

  getImagingResult: async (id: number): Promise<ImagingResult> => {
    const { data } = await apiClient.get(`/v1/results/imaging/${id}`);
    return data;
  },

  markReviewed: async (type: 'lab' | 'imaging', id: number): Promise<void> => {
    await apiClient.post(`/v1/results/${type}/${id}/review`);
  },

  getPendingResults: async (providerId?: number): Promise<{ labs: LabResult[]; imaging: ImagingResult[] }> => {
    const params = providerId ? { providerId } : {};
    const { data } = await apiClient.get('/v1/results/pending', { params });
    return data;
  },
};
