import { patientService } from '../../src/services/patientService';

const BASE = 'http://localhost:8080/api';

describe('patientService', () => {
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

  it('fetches a patient by id', async () => {
    await patientService.getById(42);

    expect(requestedUrl()).toBe(`${BASE}/v1/patients/42`);
    expect(requestedOptions().method).toBe('GET');
  });

  it('fetches a patient by MRN', async () => {
    await patientService.getByMrn('MRN001');

    expect(requestedUrl()).toBe(`${BASE}/v1/patients/mrn/MRN001`);
  });

  it('applies default pagination when searching', async () => {
    await patientService.search('doe');

    expect(requestedUrl()).toBe(`${BASE}/v1/patients/search?q=doe&page=0&size=20`);
  });

  it('honors explicit pagination when searching', async () => {
    await patientService.search('doe', 3, 50);

    expect(requestedUrl()).toBe(`${BASE}/v1/patients/search?q=doe&page=3&size=50`);
  });

  it('posts new patients', async () => {
    await patientService.create({ firstName: 'Jane', lastName: 'Doe' });

    expect(requestedUrl()).toBe(`${BASE}/v1/patients`);
    expect(requestedOptions()).toMatchObject({
      method: 'POST',
      body: JSON.stringify({ firstName: 'Jane', lastName: 'Doe' }),
    });
  });

  it('puts patient updates', async () => {
    await patientService.update(42, { lastName: 'Smith' });

    expect(requestedUrl()).toBe(`${BASE}/v1/patients/42`);
    expect(requestedOptions()).toMatchObject({
      method: 'PUT',
      body: JSON.stringify({ lastName: 'Smith' }),
    });
  });

  it('surfaces API errors to the caller', async () => {
    fetchMock.mockResolvedValue({ ok: false, status: 500, statusText: 'Server Error' });

    await expect(patientService.getById(42)).rejects.toThrow('API Error: 500 Server Error');
  });
});
