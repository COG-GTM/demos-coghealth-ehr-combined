import { apiClient } from './client';
import type { Order, MedicationOrder, LabOrder, ImagingOrder, Referral } from '../types';

export const ordersApi = {
  getByPatient: async (patientId: number): Promise<Order[]> => {
    const { data } = await apiClient.get(`/v1/patients/${patientId}/orders`);
    return data;
  },

  getById: async (id: number): Promise<Order> => {
    const { data } = await apiClient.get(`/v1/orders/${id}`);
    return data;
  },

  createMedicationOrder: async (order: Partial<MedicationOrder>): Promise<MedicationOrder> => {
    const { data } = await apiClient.post('/v1/orders/medication', order);
    return data;
  },

  createLabOrder: async (order: Partial<LabOrder>): Promise<LabOrder> => {
    const { data } = await apiClient.post('/v1/orders/lab', order);
    return data;
  },

  createImagingOrder: async (order: Partial<ImagingOrder>): Promise<ImagingOrder> => {
    const { data } = await apiClient.post('/v1/orders/imaging', order);
    return data;
  },

  createReferral: async (order: Partial<Referral>): Promise<Referral> => {
    const { data } = await apiClient.post('/v1/orders/referral', order);
    return data;
  },

  cancel: async (id: number, reason: string): Promise<Order> => {
    const { data } = await apiClient.post(`/v1/orders/${id}/cancel`, { reason });
    return data;
  },

  sign: async (id: number): Promise<Order> => {
    const { data } = await apiClient.post(`/v1/orders/${id}/sign`);
    return data;
  },
};
