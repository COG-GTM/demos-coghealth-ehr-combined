import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { encountersApi } from '../api';
import type { Encounter } from '../types';

export function useEncounter(id: number) {
  return useQuery({
    queryKey: ['encounter', id],
    queryFn: () => encountersApi.getById(id),
    enabled: !!id,
  });
}

export function usePatientEncounters(patientId: number) {
  return useQuery({
    queryKey: ['patient', patientId, 'encounters'],
    queryFn: () => encountersApi.getByPatient(patientId),
    enabled: !!patientId,
  });
}

export function useSchedule(date: string, providerId?: number) {
  return useQuery({
    queryKey: ['schedule', date, providerId],
    queryFn: () => encountersApi.getSchedule(date, providerId),
    enabled: !!date,
  });
}

export function useCheckIn(encounterId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => encountersApi.checkIn(encounterId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['encounter', encounterId] });
      queryClient.invalidateQueries({ queryKey: ['schedule'] });
    },
  });
}

export function useStartVisit(encounterId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => encountersApi.startVisit(encounterId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['encounter', encounterId] });
      queryClient.invalidateQueries({ queryKey: ['schedule'] });
    },
  });
}

export function useCompleteVisit(encounterId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => encountersApi.completeVisit(encounterId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['encounter', encounterId] });
      queryClient.invalidateQueries({ queryKey: ['schedule'] });
    },
  });
}

export function useCancelEncounter(encounterId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (reason: string) => encountersApi.cancel(encounterId, reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['encounter', encounterId] });
      queryClient.invalidateQueries({ queryKey: ['schedule'] });
    },
  });
}

export function useMarkNoShow(encounterId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => encountersApi.markNoShow(encounterId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['encounter', encounterId] });
      queryClient.invalidateQueries({ queryKey: ['schedule'] });
    },
  });
}
