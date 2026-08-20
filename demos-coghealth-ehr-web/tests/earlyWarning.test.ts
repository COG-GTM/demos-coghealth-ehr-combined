import { calculateNEWS2 } from '../src/utils/earlyWarning';
import type { VitalReading } from '../src/types/vitals';

const reading = (overrides: Partial<VitalReading> = {}): VitalReading => ({
  id: 1,
  timestamp: '2024-01-18 14:00',
  recordedBy: 'RN Smith',
  location: 'Med-Surg 4W',
  ...overrides,
});

describe('calculateNEWS2', () => {
  test.each([
    [8, 3], [9, 1], [11, 1], [12, 0], [20, 0], [21, 2], [24, 2], [25, 3],
  ])('scores respiratory rate boundary %p as %p', (value, score) => {
    expect(calculateNEWS2(reading({ respiratoryRate: value })).parameters.respiratoryRate.score).toBe(score);
  });

  test.each([
    [91, 3], [92, 2], [93, 2], [94, 1], [95, 1], [96, 0],
  ])('scores SpO2 boundary %p as %p', (value, score) => {
    expect(calculateNEWS2(reading({ spo2: value })).parameters.spo2.score).toBe(score);
  });

  test.each([
    [90, 3], [91, 2], [100, 2], [101, 1], [110, 1], [111, 0], [219, 0], [220, 3],
  ])('scores systolic BP boundary %p as %p', (value, score) => {
    expect(calculateNEWS2(reading({ systolic: value })).parameters.systolic.score).toBe(score);
  });

  test.each([
    [40, 3], [41, 1], [50, 1], [51, 0], [90, 0], [91, 1], [110, 1], [111, 2], [130, 2], [131, 3],
  ])('scores pulse boundary %p as %p', (value, score) => {
    expect(calculateNEWS2(reading({ heartRate: value })).parameters.heartRate.score).toBe(score);
  });

  test.each([
    [95, 3], [96.8, 1], [96.98, 0], [97.0, 0], [100.4, 0], [100.58, 1], [102.2, 1], [102.38, 2],
  ])('converts Fahrenheit temperature %p and scores it as %p', (value, score) => {
    expect(calculateNEWS2(reading({ temperature: value })).parameters.temperature.score).toBe(score);
  });

  test('rounds Fahrenheit conversion to one decimal before banding', () => {
    const result = calculateNEWS2(reading({ temperature: 98.6 }));
    expect(result.parameters.temperature.scoredValue).toBe(37);
    expect(result.parameters.temperature.score).toBe(0);
  });

  test('uses Low-Medium when one parameter scores 3 and total is 1-4', () => {
    const result = calculateNEWS2(reading({ spo2: 91, heartRate: 90 }));
    expect(result.total).toBe(3);
    expect(result.hasSingleParameterScore3).toBe(true);
    expect(result.riskTier).toBe('Low-Medium');
  });

  test('reports undefined parameters as not scored and excludes them from total', () => {
    const result = calculateNEWS2(reading({ spo2: 94 }));
    expect(result.parameters.respiratoryRate.score).toBeNull();
    expect(result.parameters.respiratoryRate.value).toBeUndefined();
    expect(result.parameters.systolic.score).toBeNull();
    expect(result.total).toBe(1);
    expect(result.riskTier).toBe('Low');
  });
});
