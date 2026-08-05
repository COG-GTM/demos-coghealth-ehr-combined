jest.mock('../../src/services/api', () => ({
  api: {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
  },
}));

import { api } from '../../src/services/api';
import { patientService } from '../../src/services/patientService';
import type { Patient } from '../../src/types';

const get = api.get as jest.Mock;
const post = api.post as jest.Mock;
const put = api.put as jest.Mock;

const patient: Patient = {
  id: 1,
  mrn: 'MRN001',
  firstName: 'Ada',
  lastName: 'Lovelace',
  dateOfBirth: '1985-03-12',
};

describe('patientService', () => {
  beforeEach(() => {
    jest.resetAllMocks();
  });

  it('getById requests the patient by id', async () => {
    get.mockResolvedValue(patient);

    await expect(patientService.getById(1)).resolves.toBe(patient);
    expect(get).toHaveBeenCalledWith('/v1/patients/1');
  });

  it('getByMrn requests the patient by MRN', async () => {
    get.mockResolvedValue(patient);

    await patientService.getByMrn('MRN001');

    expect(get).toHaveBeenCalledWith('/v1/patients/mrn/MRN001');
  });

  it('search defaults to the first page of twenty results', async () => {
    get.mockResolvedValue({ content: [patient], totalElements: 1, totalPages: 1, size: 20, number: 0 });

    const page = await patientService.search('lovelace');

    expect(get).toHaveBeenCalledWith('/v1/patients/search', { q: 'lovelace', page: 0, size: 20 });
    expect(page.content).toEqual([patient]);
  });

  it('search forwards explicit pagination', async () => {
    get.mockResolvedValue({ content: [], totalElements: 0, totalPages: 0, size: 50, number: 2 });

    await patientService.search('lovelace', 2, 50);

    expect(get).toHaveBeenCalledWith('/v1/patients/search', { q: 'lovelace', page: 2, size: 50 });
  });

  it('create posts the patient payload', async () => {
    post.mockResolvedValue(patient);

    await patientService.create({ firstName: 'Ada', lastName: 'Lovelace', dateOfBirth: '1985-03-12' });

    expect(post).toHaveBeenCalledWith('/v1/patients', {
      firstName: 'Ada',
      lastName: 'Lovelace',
      dateOfBirth: '1985-03-12',
    });
  });

  it('update puts the patient payload to the id scoped endpoint', async () => {
    put.mockResolvedValue(patient);

    await patientService.update(1, { lastName: 'King' });

    expect(put).toHaveBeenCalledWith('/v1/patients/1', { lastName: 'King' });
  });

  it('propagates API errors to the caller', async () => {
    get.mockRejectedValue(new Error('API Error: 404 Not Found'));

    await expect(patientService.getById(404)).rejects.toThrow('API Error: 404 Not Found');
  });
});
