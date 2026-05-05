import { format, formatDistanceToNow, parseISO, differenceInYears } from 'date-fns';

export function formatDate(date: string | Date, formatStr = 'MM/dd/yyyy'): string {
  const d = typeof date === 'string' ? parseISO(date) : date;
  return format(d, formatStr);
}

export function formatDateTime(date: string | Date): string {
  const d = typeof date === 'string' ? parseISO(date) : date;
  return format(d, 'MM/dd/yyyy h:mm a');
}

export function formatTime(date: string | Date): string {
  const d = typeof date === 'string' ? parseISO(date) : date;
  return format(d, 'h:mm a');
}

export function formatRelative(date: string | Date): string {
  const d = typeof date === 'string' ? parseISO(date) : date;
  return formatDistanceToNow(d, { addSuffix: true });
}

export function calculateAge(dateOfBirth: string): number {
  return differenceInYears(new Date(), parseISO(dateOfBirth));
}

export function formatPatientName(patient: { firstName: string; lastName: string; middleName?: string }): string {
  const middle = patient.middleName ? ` ${patient.middleName}` : '';
  return `${patient.lastName}, ${patient.firstName}${middle}`;
}

export function formatProviderName(provider: { firstName: string; lastName: string; credentials?: string }): string {
  const creds = provider.credentials ? `, ${provider.credentials}` : '';
  return `${provider.firstName} ${provider.lastName}${creds}`;
}

export function formatGender(gender: string): string {
  const map: Record<string, string> = {
    MALE: 'M',
    FEMALE: 'F',
    OTHER: 'O',
    UNKNOWN: 'U',
  };
  return map[gender] || gender;
}

export function formatPhone(phone?: string): string {
  if (!phone) return '';
  const cleaned = phone.replace(/\D/g, '');
  if (cleaned.length === 10) {
    return `(${cleaned.slice(0, 3)}) ${cleaned.slice(3, 6)}-${cleaned.slice(6)}`;
  }
  return phone;
}

export function formatMrn(mrn: string): string {
  return mrn;
}

export function formatVitalValue(value: number | undefined, unit: string): string {
  if (value === undefined || value === null) return '--';
  return `${value} ${unit}`;
}

export function formatBloodPressure(systolic?: number, diastolic?: number): string {
  if (!systolic || !diastolic) return '--/--';
  return `${systolic}/${diastolic}`;
}

export function calculateBmi(heightInches?: number, weightLbs?: number): number | null {
  if (!heightInches || !weightLbs) return null;
  return Math.round((weightLbs / (heightInches * heightInches)) * 703 * 10) / 10;
}
