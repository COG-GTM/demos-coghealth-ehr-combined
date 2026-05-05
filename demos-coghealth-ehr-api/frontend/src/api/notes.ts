import { apiClient } from './client';
import type { ClinicalNote, NoteType } from '../types';

export const notesApi = {
  getByPatient: async (patientId: number): Promise<ClinicalNote[]> => {
    const { data } = await apiClient.get(`/v1/patients/${patientId}/notes`);
    return data;
  },

  getByEncounter: async (encounterId: number): Promise<ClinicalNote[]> => {
    const { data } = await apiClient.get(`/v1/encounters/${encounterId}/notes`);
    return data;
  },

  getById: async (id: number): Promise<ClinicalNote> => {
    const { data } = await apiClient.get(`/v1/notes/${id}`);
    return data;
  },

  create: async (note: Partial<ClinicalNote>): Promise<ClinicalNote> => {
    const { data } = await apiClient.post('/v1/notes', note);
    return data;
  },

  update: async (id: number, note: Partial<ClinicalNote>): Promise<ClinicalNote> => {
    const { data } = await apiClient.put(`/v1/notes/${id}`, note);
    return data;
  },

  sign: async (id: number): Promise<ClinicalNote> => {
    const { data } = await apiClient.post(`/v1/notes/${id}/sign`);
    return data;
  },

  addAddendum: async (id: number, content: string): Promise<ClinicalNote> => {
    const { data } = await apiClient.post(`/v1/notes/${id}/addendum`, { content });
    return data;
  },

  getTemplates: async (noteType?: NoteType): Promise<{ id: number; name: string; content: string }[]> => {
    const params = noteType ? { type: noteType } : {};
    const { data } = await apiClient.get('/v1/notes/templates', { params });
    return data;
  },
};
