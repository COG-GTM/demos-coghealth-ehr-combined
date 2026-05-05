import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { patientsApi } from '../api';
import type { Patient, Problem, Allergy, Medication, Vital } from '../types';

export function usePatient(id: number) {
  return useQuery({
    queryKey: ['patient', id],
    queryFn: () => patientsApi.getById(id),
    enabled: !!id,
  });
}

export function usePatientSearch(query: string) {
  return useQuery({
    queryKey: ['patients', 'search', query],
    queryFn: () => patientsApi.search(query),
    enabled: query.length >= 2,
  });
}

export function usePatientProblems(patientId: number) {
  return useQuery({
    queryKey: ['patient', patientId, 'problems'],
    queryFn: () => patientsApi.getProblems(patientId),
    enabled: !!patientId,
  });
}

export function usePatientAllergies(patientId: number) {
  return useQuery({
    queryKey: ['patient', patientId, 'allergies'],
    queryFn: () => patientsApi.getAllergies(patientId),
    enabled: !!patientId,
  });
}

export function usePatientMedications(patientId: number) {
  return useQuery({
    queryKey: ['patient', patientId, 'medications'],
    queryFn: () => patientsApi.getMedications(patientId),
    enabled: !!patientId,
  });
}

export function usePatientVitals(patientId: number) {
  return useQuery({
    queryKey: ['patient', patientId, 'vitals'],
    queryFn: () => patientsApi.getVitals(patientId),
    enabled: !!patientId,
  });
}

export function usePatientImmunizations(patientId: number) {
  return useQuery({
    queryKey: ['patient', patientId, 'immunizations'],
    queryFn: () => patientsApi.getImmunizations(patientId),
    enabled: !!patientId,
  });
}

export function useAddProblem(patientId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (problem: Partial<Problem>) => patientsApi.addProblem(patientId, problem),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['patient', patientId, 'problems'] });
    },
  });
}

export function useAddAllergy(patientId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (allergy: Partial<Allergy>) => patientsApi.addAllergy(patientId, allergy),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['patient', patientId, 'allergies'] });
    },
  });
}

export function useAddMedication(patientId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (medication: Partial<Medication>) => patientsApi.addMedication(patientId, medication),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['patient', patientId, 'medications'] });
    },
  });
}

export function useAddVitals(patientId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (vitals: Partial<Vital>) => patientsApi.addVitals(patientId, vitals),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['patient', patientId, 'vitals'] });
    },
  });
}
