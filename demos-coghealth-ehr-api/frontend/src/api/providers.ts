import { apiClient } from './client';
import type { Provider } from '../types';

export const providersApi = {
  getAll: async (active?: boolean): Promise<Provider[]> => {
    const params = active !== undefined ? { active } : {};
    const { data } = await apiClient.get('/v1/providers', { params });
    return data;
  },

  getById: async (id: number): Promise<Provider> => {
    const { data } = await apiClient.get(`/v1/providers/${id}`);
    return data;
  },

  getByNpi: async (npi: string): Promise<Provider> => {
    const { data } = await apiClient.get(`/v1/providers/npi/${npi}`);
    return data;
  },

  create: async (provider: Partial<Provider>): Promise<Provider> => {
    const { data } = await apiClient.post('/v1/providers', provider);
    return data;
  },

  update: async (id: number, provider: Partial<Provider>): Promise<Provider> => {
    const { data } = await apiClient.put(`/v1/providers/${id}`, provider);
    return data;
  },
};
