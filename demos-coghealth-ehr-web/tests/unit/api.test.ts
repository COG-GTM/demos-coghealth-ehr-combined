import { api } from '../../src/services/api';

const BASE = 'http://localhost:8080/api';

const jsonResponse = (body: unknown, init: Partial<Response> = {}) => ({
  ok: true,
  status: 200,
  statusText: 'OK',
  json: async () => body,
  ...init,
});

describe('api client', () => {
  let fetchMock: jest.Mock;

  beforeEach(() => {
    fetchMock = jest.fn().mockResolvedValue(jsonResponse({ ok: true }));
    global.fetch = fetchMock as unknown as typeof fetch;
  });

  const lastCall = () => fetchMock.mock.calls[0] as [string, RequestInit];

  it('issues a GET against the configured API base url', async () => {
    await api.get('/v1/patients/1');

    const [url, options] = lastCall();
    expect(url).toBe(`${BASE}/v1/patients/1`);
    expect(options.method).toBe('GET');
    expect(options.headers).toMatchObject({ 'Content-Type': 'application/json' });
  });

  it('serializes query params and coerces non-string values', async () => {
    await api.get('/v1/patients/search', { q: 'doe', page: 0, size: 20, active: true });

    expect(lastCall()[0]).toBe(`${BASE}/v1/patients/search?q=doe&page=0&size=20&active=true`);
  });

  it('omits undefined params', async () => {
    await api.get('/v1/patients/search', { q: 'doe', page: undefined });

    expect(lastCall()[0]).toBe(`${BASE}/v1/patients/search?q=doe`);
  });

  it('url-encodes param values', async () => {
    await api.get('/v1/patients/search', { q: 'doe, jane & co' });

    expect(lastCall()[0]).toBe(`${BASE}/v1/patients/search?q=doe%2C+jane+%26+co`);
  });

  it('does not append a trailing question mark for empty params', async () => {
    await api.get('/v1/patients/search', {});

    expect(lastCall()[0]).toBe(`${BASE}/v1/patients/search`);
  });

  it('sends JSON bodies for POST and PUT', async () => {
    await api.post('/v1/patients', { firstName: 'Jane' });
    expect(lastCall()[1]).toMatchObject({
      method: 'POST',
      body: JSON.stringify({ firstName: 'Jane' }),
    });

    fetchMock.mockClear();
    await api.put('/v1/patients/1', { firstName: 'Janet' });
    expect(lastCall()[1]).toMatchObject({
      method: 'PUT',
      body: JSON.stringify({ firstName: 'Janet' }),
    });
  });

  it('issues a DELETE without a body', async () => {
    await api.delete('/v1/patients/1');

    const [url, options] = lastCall();
    expect(url).toBe(`${BASE}/v1/patients/1`);
    expect(options.method).toBe('DELETE');
    expect(options.body).toBeUndefined();
  });

  it('returns the parsed JSON payload', async () => {
    fetchMock.mockResolvedValue(jsonResponse({ id: 1, mrn: 'MRN001' }));

    await expect(api.get<{ mrn: string }>('/v1/patients/1')).resolves.toEqual({
      id: 1,
      mrn: 'MRN001',
    });
  });

  it('throws with the status text on a non-2xx response', async () => {
    fetchMock.mockResolvedValue(
      jsonResponse(null, { ok: false, status: 404, statusText: 'Not Found' }),
    );

    await expect(api.get('/v1/patients/999')).rejects.toThrow('API Error: 404 Not Found');
  });

  it('propagates network failures', async () => {
    fetchMock.mockRejectedValue(new Error('Failed to fetch'));

    await expect(api.get('/v1/patients/1')).rejects.toThrow('Failed to fetch');
  });
});
