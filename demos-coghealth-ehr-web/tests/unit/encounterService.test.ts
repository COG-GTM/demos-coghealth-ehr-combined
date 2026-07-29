import { encounterService } from '../../src/services/encounterService';

const BASE = 'http://localhost:8080/api';

describe('encounterService', () => {
  let fetchMock: jest.Mock;

  beforeEach(() => {
    fetchMock = jest.fn().mockResolvedValue({
      ok: true,
      status: 200,
      statusText: 'OK',
      json: async () => ({}),
    });
    global.fetch = fetchMock as unknown as typeof fetch;
  });

  const requestedUrl = () => fetchMock.mock.calls[0][0] as string;
  const requestedOptions = () => fetchMock.mock.calls[0][1] as RequestInit;

  it('fetches encounters by patient', async () => {
    await encounterService.getByPatient(7);

    expect(requestedUrl()).toBe(`${BASE}/v1/encounters/patient/7`);
  });

  it('fetches a provider schedule for a given date', async () => {
    await encounterService.getProviderSchedule(3, '2024-03-15');

    expect(requestedUrl()).toBe(`${BASE}/v1/encounters/provider/3/schedule?date=2024-03-15`);
  });

  it('fetches encounters within a date range', async () => {
    await encounterService.getByDateRange('2024-03-01', '2024-03-31');

    expect(requestedUrl()).toBe(
      `${BASE}/v1/encounters/date-range?startDate=2024-03-01&endDate=2024-03-31`,
    );
  });

  it('fetches an encounter by its number', async () => {
    await encounterService.getByNumber('ENC-2024-000101');

    expect(requestedUrl()).toBe(`${BASE}/v1/encounters/number/ENC-2024-000101`);
  });

  it.each([
    ['checkIn', 'check-in'],
    ['start', 'start'],
    ['cancel', 'cancel'],
    ['markNoShow', 'no-show'],
  ] as const)('posts the %s workflow transition', async (method, path) => {
    await encounterService[method](12);

    expect(requestedUrl()).toBe(`${BASE}/v1/encounters/12/${path}`);
    expect(requestedOptions().method).toBe('POST');
  });

  it('posts completion notes when provided', async () => {
    await encounterService.complete(12, 'Patient stable');

    expect(requestedUrl()).toBe(`${BASE}/v1/encounters/12/complete`);
    expect(requestedOptions().body).toBe(JSON.stringify('Patient stable'));
  });

  it('completes without a body when no notes are given', async () => {
    await encounterService.complete(12);

    expect(requestedOptions().body).toBeUndefined();
  });

  it('creates and updates encounters', async () => {
    await encounterService.create({ chiefComplaint: 'Chest pain' });
    expect(requestedUrl()).toBe(`${BASE}/v1/encounters`);
    expect(requestedOptions().method).toBe('POST');

    fetchMock.mockClear();
    await encounterService.update(12, { chiefComplaint: 'Chest pain, resolved' });
    expect(requestedUrl()).toBe(`${BASE}/v1/encounters/12`);
    expect(requestedOptions().method).toBe('PUT');
  });
});
