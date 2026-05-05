import { Outlet, useParams } from 'react-router-dom';
import { usePatient, usePatientAllergies } from '../../hooks';
import { PatientHeader } from '../patient/PatientHeader';
import { ChartSidebar } from './ChartSidebar';
import { Skeleton } from '../common';

export function ChartLayout() {
  const { patientId } = useParams();
  const { data: patient, isLoading: patientLoading } = usePatient(Number(patientId));
  const { data: allergies } = usePatientAllergies(Number(patientId));

  if (patientLoading) {
    return (
      <div className="p-6 space-y-4">
        <Skeleton className="h-24 w-full" />
        <Skeleton className="h-96 w-full" />
      </div>
    );
  }

  if (!patient) {
    return (
      <div className="p-6">
        <div className="text-center py-12">
          <h2 className="text-lg font-medium text-slate-900">Patient not found</h2>
          <p className="text-sm text-slate-500 mt-1">The requested patient could not be found.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="h-[calc(100vh-3.5rem)] flex flex-col">
      <PatientHeader patient={patient} allergies={allergies} />
      <div className="flex-1 flex overflow-hidden">
        <ChartSidebar />
        <div className="flex-1 overflow-auto bg-slate-50">
          <Outlet context={{ patient, allergies }} />
        </div>
      </div>
    </div>
  );
}
