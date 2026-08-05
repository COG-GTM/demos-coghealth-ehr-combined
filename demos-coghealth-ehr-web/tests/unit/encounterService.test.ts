jest.mock('../../src/services/api', () => ({
  api: {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
  },
}));

import { api } from '../../src/services/api';
import { encounterService } from '../../src/services/encounterService';

const get = api.get as jest.Mock;
const post = api.post as jest.Mock;
const put = api.put as jest.Mock;

describe('encounterService', () => {
  beforeEach(() => {
    jest.resetAllMocks();
    get.mockResolvedValue([]);
    post.mockResolvedValue(undefined);
    put.mockResolvedValue(undefined);
  });

  it('reads a single encounter by id and by encounter number', async () => {
    await encounterService.getById(7);
    await encounterService.getByNumber('ENC-2024-000101');

    expect(get).toHaveBeenNthCalledWith(1, '/v1/encounters/7');
    expect(get).toHaveBeenNthCalledWith(2, '/v1/encounters/number/ENC-2024-000101');
  });

  it('lists encounters by patient, provider and status', async () => {
    await encounterService.getByPatient(1);
    await encounterService.getByProvider(3);
    await encounterService.getByStatus('SCHEDULED');

    expect(get).toHaveBeenNthCalledWith(1, '/v1/encounters/patient/1');
    expect(get).toHaveBeenNthCalledWith(2, '/v1/encounters/provider/3');
    expect(get).toHaveBeenNthCalledWith(3, '/v1/encounters/status/SCHEDULED');
  });

  it('passes the date as a query parameter for a provider schedule', async () => {
    await encounterService.getProviderSchedule(3, '2024-05-17');

    expect(get).toHaveBeenCalledWith('/v1/encounters/provider/3/schedule', { date: '2024-05-17' });
  });

  it('passes both bounds as query parameters for a date range', async () => {
    await encounterService.getByDateRange('2024-05-01', '2024-05-31');

    expect(get).toHaveBeenCalledWith('/v1/encounters/date-range', {
      startDate: '2024-05-01',
      endDate: '2024-05-31',
    });
  });

  it('creates and updates encounters', async () => {
    await encounterService.create({ encounterType: 'OUTPATIENT' });
    await encounterService.update(7, { chiefComplaint: 'Chest pain' });

    expect(post).toHaveBeenCalledWith('/v1/encounters', { encounterType: 'OUTPATIENT' });
    expect(put).toHaveBeenCalledWith('/v1/encounters/7', { chiefComplaint: 'Chest pain' });
  });

  it('posts each workflow transition to its own endpoint', async () => {
    await encounterService.checkIn(7);
    await encounterService.start(7);
    await encounterService.complete(7, 'Discharged');
    await encounterService.cancel(7);
    await encounterService.markNoShow(7);

    expect(post.mock.calls).toEqual([
      ['/v1/encounters/7/check-in'],
      ['/v1/encounters/7/start'],
      ['/v1/encounters/7/complete', 'Discharged'],
      ['/v1/encounters/7/cancel'],
      ['/v1/encounters/7/no-show'],
    ]);
  });

  it('completes an encounter without notes', async () => {
    await encounterService.complete(7);

    expect(post).toHaveBeenCalledWith('/v1/encounters/7/complete', undefined);
  });
});
