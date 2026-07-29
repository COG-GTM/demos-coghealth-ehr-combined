jest.mock('../../src/services/api', () => ({
  api: {
    get: jest.fn(() => Promise.resolve('get-result')),
    post: jest.fn(() => Promise.resolve('post-result')),
    put: jest.fn(() => Promise.resolve('put-result')),
    delete: jest.fn(() => Promise.resolve('delete-result')),
  },
}));

import { api } from '../../src/services/api';
import { encounterService } from '../../src/services/encounterService';
import { patientService } from '../../src/services/patientService';
import { refillRequestService } from '../../src/services/refillRequestService';

const mockedApi = api as jest.Mocked<typeof api>;

describe('refillRequestService', () => {
  beforeEach(() => jest.clearAllMocks());

  test('getPending queries the pending refill queue', async () => {
    await expect(refillRequestService.getPending()).resolves.toBe('get-result');
    expect(mockedApi.get).toHaveBeenCalledWith('/v1/refill-requests/pending');
  });

  test('approve posts to the approve endpoint for the given id', async () => {
    await refillRequestService.approve(10);

    expect(mockedApi.post).toHaveBeenCalledWith('/v1/refill-requests/10/approve');
  });

  test('deny posts to the deny endpoint for the given id', async () => {
    await refillRequestService.deny(10);

    expect(mockedApi.post).toHaveBeenCalledWith('/v1/refill-requests/10/deny');
  });
});

describe('patientService', () => {
  beforeEach(() => jest.clearAllMocks());

  test('getById and getByMrn use their respective lookup endpoints', async () => {
    await patientService.getById(1);
    await patientService.getByMrn('MRN-2019-00001');

    expect(mockedApi.get).toHaveBeenNthCalledWith(1, '/v1/patients/1');
    expect(mockedApi.get).toHaveBeenNthCalledWith(2, '/v1/patients/mrn/MRN-2019-00001');
  });

  test('search defaults to the first page of 20 results', async () => {
    await patientService.search('smith');

    expect(mockedApi.get).toHaveBeenCalledWith('/v1/patients/search', { q: 'smith', page: 0, size: 20 });
  });

  test('search forwards explicit pagination', async () => {
    await patientService.search('smith', 2, 50);

    expect(mockedApi.get).toHaveBeenCalledWith('/v1/patients/search', { q: 'smith', page: 2, size: 50 });
  });

  test('create and update send the patient payload', async () => {
    await patientService.create({ mrn: 'MRN-2019-00001' });
    await patientService.update(1, { firstName: 'John' });

    expect(mockedApi.post).toHaveBeenCalledWith('/v1/patients', { mrn: 'MRN-2019-00001' });
    expect(mockedApi.put).toHaveBeenCalledWith('/v1/patients/1', { firstName: 'John' });
  });
});

describe('encounterService', () => {
  beforeEach(() => jest.clearAllMocks());

  test('getProviderSchedule passes the date as a query param', async () => {
    await encounterService.getProviderSchedule(3, '2024-03-15');

    expect(mockedApi.get).toHaveBeenCalledWith('/v1/encounters/provider/3/schedule', { date: '2024-03-15' });
  });

  test('getByDateRange passes both range bounds', async () => {
    await encounterService.getByDateRange('2024-03-01', '2024-03-31');

    expect(mockedApi.get).toHaveBeenCalledWith('/v1/encounters/date-range', {
      startDate: '2024-03-01',
      endDate: '2024-03-31',
    });
  });

  test('workflow transitions post to the matching encounter endpoints', async () => {
    await encounterService.checkIn(5);
    await encounterService.start(5);
    await encounterService.complete(5, 'Follow up in 3 months');
    await encounterService.cancel(5);
    await encounterService.markNoShow(5);

    expect(mockedApi.post.mock.calls).toEqual([
      ['/v1/encounters/5/check-in'],
      ['/v1/encounters/5/start'],
      ['/v1/encounters/5/complete', 'Follow up in 3 months'],
      ['/v1/encounters/5/cancel'],
      ['/v1/encounters/5/no-show'],
    ]);
  });
});
