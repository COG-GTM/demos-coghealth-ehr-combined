import { Link } from 'react-router-dom';
import { AlertTriangle, Phone, Mail, MapPin } from 'lucide-react';
import { Badge } from '../common';
import { formatPatientName, formatDate, formatGender, calculateAge, formatPhone, cn } from '../../utils';
import type { Patient, Allergy } from '../../types';

interface PatientHeaderProps {
  patient: Patient;
  allergies?: Allergy[];
  alerts?: string[];
  compact?: boolean;
}

export function PatientHeader({ patient, allergies = [], alerts = [], compact = false }: PatientHeaderProps) {
  const activeAllergies = allergies.filter((a) => a.status === 'ACTIVE');
  const severeAllergies = activeAllergies.filter((a) => a.severity === 'SEVERE' || a.severity === 'LIFE_THREATENING');

  return (
    <div className={cn('bg-white border-b border-slate-200', compact ? 'py-2 px-4' : 'py-4 px-6')}>
      <div className="flex items-start justify-between gap-4">
        <div className="flex-1">
          <div className="flex items-center gap-3">
            <Link
              to={`/patients/${patient.id}`}
              className={cn('font-semibold text-slate-900 hover:text-primary-600', compact ? 'text-base' : 'text-xl')}
            >
              {formatPatientName(patient)}
            </Link>
            {patient.deceased && <Badge variant="danger">Deceased</Badge>}
            {!patient.active && <Badge variant="gray">Inactive</Badge>}
          </div>

          <div className={cn('text-slate-500 flex flex-wrap items-center gap-x-3 gap-y-1', compact ? 'text-xs mt-0.5' : 'text-sm mt-1')}>
            <span>MRN: {patient.mrn}</span>
            <span className="text-slate-300">|</span>
            <span>DOB: {formatDate(patient.dateOfBirth)} ({calculateAge(patient.dateOfBirth)}y)</span>
            <span className="text-slate-300">|</span>
            <span>{formatGender(patient.gender)}</span>
            {patient.preferredLanguage && (
              <>
                <span className="text-slate-300">|</span>
                <span>{patient.preferredLanguage}</span>
              </>
            )}
          </div>

          {!compact && (
            <div className="flex flex-wrap items-center gap-4 mt-2 text-sm text-slate-600">
              {patient.phoneMobile && (
                <span className="flex items-center gap-1">
                  <Phone className="w-3.5 h-3.5" />
                  {formatPhone(patient.phoneMobile)}
                </span>
              )}
              {patient.email && (
                <span className="flex items-center gap-1">
                  <Mail className="w-3.5 h-3.5" />
                  {patient.email}
                </span>
              )}
              {patient.address && (
                <span className="flex items-center gap-1">
                  <MapPin className="w-3.5 h-3.5" />
                  {patient.address.city}, {patient.address.state}
                </span>
              )}
            </div>
          )}
        </div>
      </div>

      {(activeAllergies.length > 0 || alerts.length > 0) && (
        <div className={cn('flex flex-wrap gap-2', compact ? 'mt-2' : 'mt-3')}>
          {activeAllergies.length > 0 && (
            <div className={cn(
              'flex items-center gap-2 px-3 py-1.5 rounded-md text-sm',
              severeAllergies.length > 0 ? 'bg-danger-50 text-danger-700' : 'bg-warning-50 text-warning-700'
            )}>
              <AlertTriangle className="w-4 h-4" />
              <span className="font-medium">Allergies:</span>
              <span>
                {activeAllergies.map((a) => a.allergen).join(', ')}
              </span>
            </div>
          )}

          {alerts.map((alert, i) => (
            <Badge key={i} variant="danger">{alert}</Badge>
          ))}
        </div>
      )}
    </div>
  );
}
