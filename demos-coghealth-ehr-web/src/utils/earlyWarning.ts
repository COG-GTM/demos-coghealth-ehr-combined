import type { VitalReading } from '../types/vitals';

export type News2Parameter = 'respiratoryRate' | 'spo2' | 'systolic' | 'heartRate' | 'temperature';
export type News2RiskTier = 'Low' | 'Low-Medium' | 'Medium' | 'High';

export interface News2Subscore {
  value: number | undefined;
  scoredValue: number | undefined;
  score: number | null;
}

export interface News2Result {
  parameters: Record<News2Parameter, News2Subscore>;
  total: number;
  hasSingleParameterScore3: boolean;
  riskTier: News2RiskTier;
}

const scoreRespiratoryRate = (value: number | undefined): number | null => {
  if (value === undefined) return null;
  if (value <= 8 || value >= 25) return 3;
  if (value <= 11) return 1;
  if (value <= 20) return 0;
  return 2;
};

const scoreSpo2 = (value: number | undefined): number | null => {
  if (value === undefined) return null;
  if (value <= 91) return 3;
  if (value <= 93) return 2;
  if (value <= 95) return 1;
  return 0;
};

const scoreSystolic = (value: number | undefined): number | null => {
  if (value === undefined) return null;
  if (value <= 90 || value >= 220) return 3;
  if (value <= 100) return 2;
  if (value <= 110) return 1;
  return 0;
};

const scoreHeartRate = (value: number | undefined): number | null => {
  if (value === undefined) return null;
  if (value <= 40 || value >= 131) return 3;
  if (value <= 50 || value >= 91 && value <= 110) return 1;
  if (value <= 90) return 0;
  return 2;
};

const fahrenheitToCelsius = (fahrenheit: number): number =>
  Math.round(((fahrenheit - 32) * 5 / 9) * 10) / 10;

const scoreTemperature = (value: number | undefined): News2Subscore => {
  if (value === undefined) {
    return { value, scoredValue: undefined, score: null };
  }

  const celsius = fahrenheitToCelsius(value);
  let score: number;
  if (celsius <= 35.0) score = 3;
  else if (celsius <= 36.0) score = 1;
  else if (celsius <= 38.0) score = 0;
  else if (celsius <= 39.0) score = 1;
  else score = 2;

  return { value, scoredValue: celsius, score };
};

const getRiskTier = (total: number, hasSingleParameterScore3: boolean): News2RiskTier => {
  if (total === 0) return 'Low';
  if (total <= 4) return hasSingleParameterScore3 ? 'Low-Medium' : 'Low';
  if (total <= 6) return 'Medium';
  return 'High';
};

export const calculateNEWS2 = (reading: VitalReading): News2Result => {
  const parameters: Record<News2Parameter, News2Subscore> = {
    respiratoryRate: {
      value: reading.respiratoryRate,
      scoredValue: reading.respiratoryRate,
      score: scoreRespiratoryRate(reading.respiratoryRate),
    },
    spo2: {
      value: reading.spo2,
      scoredValue: reading.spo2,
      score: scoreSpo2(reading.spo2),
    },
    systolic: {
      value: reading.systolic,
      scoredValue: reading.systolic,
      score: scoreSystolic(reading.systolic),
    },
    heartRate: {
      value: reading.heartRate,
      scoredValue: reading.heartRate,
      score: scoreHeartRate(reading.heartRate),
    },
    temperature: scoreTemperature(reading.temperature),
  };
  const scores = Object.values(parameters)
    .map(parameter => parameter.score)
    .filter((score): score is number => score !== null);
  const total = scores.reduce((sum, score) => sum + score, 0);
  const hasSingleParameterScore3 = scores.includes(3);

  return {
    parameters,
    total,
    hasSingleParameterScore3,
    riskTier: getRiskTier(total, hasSingleParameterScore3),
  };
};
