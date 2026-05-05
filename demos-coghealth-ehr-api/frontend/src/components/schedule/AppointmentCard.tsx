import { Link } from 'react-router-dom';
import { Clock, MapPin, User, AlertTriangle, MoreVertical } from 'lucide-react';
import { Badge } from '../common';
import { formatTime, formatPatientName, formatGender, calculateAge, cn } from '../../utils';
import type { Appointment, EncounterStatus } from '../../types';

interface AppointmentCardProps {
  appointment: Appointment;
  onAction?: (action: string, appointment: Appointment) => void;
}

const statusConfig: Record<EncounterStatus, { label: string; color: string; bgColor: string }> = {
  SCHEDULED: { label: 'Scheduled', color: 'text-slate-600', bgColor: 'bg-slate-100' },
  CONFIRMED: { label: 'Confirmed', color: 'text-primary-600', bgColor: 'bg-primary-50' },
  CHECKED_IN: { label: 'Checked In', color: 'text-success-600', bgColor: 'bg-success-50' },
  ROOMED: { label: 'Roomed', color: 'text-primary-600', bgColor: 'bg-primary-100' },
  IN_PROGRESS: { label: 'In Progress', color: 'text-primary-700', bgColor: 'bg-primary-100' },
  COMPLETED: { label: 'Completed', color: 'text-slate-500', bgColor: 'bg-slate-50' },
  CANCELLED: { label: 'Cancelled', color: 'text-slate-400', bgColor: 'bg-slate-50' },
  NO_SHOW: { label: 'No Show', color: 'text-danger-600', bgColor: 'bg-danger-50' },
};

const encounterTypeLabels: Record<string, string> = {
  OFFICE_VISIT: 'Office Visit',
  FOLLOW_UP: 'Follow-up',
  NEW_PATIENT: 'New Patient',
  ANNUAL_PHYSICAL: 'Annual Physical',
  URGENT_CARE: 'Urgent Care',
  TELEHEALTH: 'Telehealth',
  PROCEDURE: 'Procedure',
  LAB_ONLY: 'Lab Only',
  IMAGING: 'Imaging',
};

export function AppointmentCard({ appointment, onAction }: AppointmentCardProps) {
  const status = statusConfig[appointment.status] || statusConfig.SCHEDULED;
  const patient = appointment.patient;

  return (
    <div className={cn('border rounded-lg p-4 transition-colors', status.bgColor, 'border-slate-200 hover:border-slate-300')}>
      <div className="flex items-start justify-between gap-3">
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-1">
            <span className={cn('text-sm font-medium', status.color)}>
              {formatTime(appointment.scheduledTime)}
            </span>
            <Badge variant={appointment.status === 'CHECKED_IN' ? 'success' : appointment.status === 'NO_SHOW' ? 'danger' : 'gray'}>
              {status.label}
            </Badge>
          </div>

          <Link
            to={`/patients/${patient.id}`}
            className="font-semibold text-slate-900 hover:text-primary-600 block truncate"
          >
            {formatPatientName(patient)}
          </Link>

          <div className="text-sm text-slate-500 mt-0.5">
            {formatGender(patient.gender)}, {calculateAge(patient.dateOfBirth)}y
          </div>

          <div className="flex items-center gap-3 mt-2 text-sm text-slate-600">
            <span>{encounterTypeLabels[appointment.encounterType] || appointment.encounterType}</span>
            {appointment.room && (
              <span className="flex items-center gap-1">
                <MapPin className="w-3.5 h-3.5" />
                {appointment.room}
              </span>
            )}
          </div>

          {appointment.chiefComplaint && (
            <div className="mt-2 text-sm text-slate-600 line-clamp-2">
              CC: {appointment.chiefComplaint}
            </div>
          )}
        </div>

        <div className="flex items-center gap-2">
          <Link
            to={`/patients/${patient.id}`}
            className="btn btn-secondary btn-sm"
          >
            Chart
          </Link>
          {onAction && (
            <button
              onClick={() => onAction('menu', appointment)}
              className="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded"
            >
              <MoreVertical className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
