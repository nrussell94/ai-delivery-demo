export type HealthResponse = {
  status: string;
};

export type QuoteRequest = {
  name: string;
  dateOfBirth: string; // ISO date YYYY-MM-DD
  driverLicenceNumber: string;
  postcode: string;
  vehicleRegistration: string;
};

export type QuoteResponse = { quoteId: string; premium: number };

export type BindRequest = { quoteId: string };

export type Policy = {
  policyId: string;
  quoteId: string;
  premium: number;
  effectiveFrom: string; // ISO date-time
  effectiveTo: string;
};

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '';

async function parseResponse<T>(response: Response): Promise<T> {
  const body = await response.json();
  if (!response.ok) {
    throw new Error(body.message ?? 'The API request failed');
  }
  return body as T;
}

export async function getHealth(): Promise<HealthResponse> {
  return parseResponse<HealthResponse>(await fetch(`${apiBaseUrl}/api/health`));
}

export async function createQuote(request: QuoteRequest): Promise<QuoteResponse> {
  return parseResponse<QuoteResponse>(
    await fetch(`${apiBaseUrl}/api/quotes`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    })
  );
}

export async function bindPolicy(request: BindRequest): Promise<Policy> {
  return parseResponse<Policy>(
    await fetch(`${apiBaseUrl}/api/policies`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    })
  );
}

export async function getPolicy(id: string): Promise<Policy> {
  return parseResponse<Policy>(await fetch(`${apiBaseUrl}/api/policies/${id}`));
}
