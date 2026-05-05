import { apiClient } from './client';
import type { Patient, Problem, Allergy, Medication, Vital, Immunization, SearchResult } from '../types';

export const patientsApi = {
  search: async (query: string): Promise<Patient[]> => {
    const { data } = await apiClient.get('/v1/patients/search', { params: { q: query } });
    return data;
  },

  searchByLastName: async (lastName: string): Promise<Patient[]> => {
    const { data } = await apiClient.get('/v1/patients/search', { params: { lastName } });
    return data;
  },

  getById: async (id: number): Promise<Patient> => {
    const { data } = await apiClient.get(`/v1/patients/${id}`);
    return data;
  },

  getByMrn: async (mrn: string): Promise<Patient> => {
    const { data } = await apiClient.get(`/v1/patients/mrn/${mrn}`);
    return data;
  },

  create: async (patient: Partial<Patient>): Promise<Patient> => {
    const { data } = await apiClient.post('/v1/patients', patient);
    return data;
  },

  update: async (id: number, patient: Partial<Patient>): Promise<Patient> => {
    const { data } = await apiClient.put(`/v1/patients/${id}`, patient);
    return data;
  },

  getProblems: async (patientId: number): Promise<Problem[]> => {
    const { data } = await apiClient.get(`/v1/patients/${patientId}/problems`);
    return data;
  },

  addProblem: async (patientId: number, problem: Partial<Problem>): Promise<Problem> => {
    const { data } = await apiClient.post(`/v1/patients/${patientId}/problems`, problem);
    return data;
  },

  updateProblem: async (patientId: number, problemId: number, problem: Partial<Problem>): Promise<Problem> => {
    const { data } = await apiClient.put(`/v1/patients/${patientId}/problems/${problemId}`, problem);
    return data;
  },

  getAllergies: async (patientId: number): Promise<Allergy[]> => {
    const { data } = await apiClient.get(`/v1/patients/${patientId}/allergies`);
    return data;
  },

  addAllergy: async (patientId: number, allergy: Partial<Allergy>): Promise<Allergy> => {
    const { data } = await apiClient.post(`/v1/patients/${patientId}/allergies`, allergy);
    return data;
  },

  getMedications: async (patientId: number): Promise<Medication[]> => {
    const { data } = await apiClient.get(`/v1/patients/${patientId}/medications`);
    return data;
  },

  addMedication: async (patientId: number, medication: Partial<Medication>): Promise<Medication> => {
    const { data } = await apiClient.post(`/v1/patients/${patientId}/medications`, medication);
    return data;
  },

  getVitals: async (patientId: number): Promise<Vital[]> => {
    const { data } = await apiClient.get(`/v1/patients/${patientId}/vitals`);
    return data;
  },

  addVitals: async (patientId: number, vitals: Partial<Vital>): Promise<Vital> => {
    const { data } = await apiClient.post(`/v1/patients/${patientId}/vitals`, vitals);
    return data;
  },

  getImmunizations: async (patientId: number): Promise<Immunization[]> => {
    const { data } = await apiClient.get(`/v1/patients/${patientId}/immunizations`);
    return data;
  },
};
