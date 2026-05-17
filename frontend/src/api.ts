export type HealthResponse = {
  status: string;
};

export type QuoteRequest = {
  driverName: string;
  dateOfBirth: string;   // ISO date, e.g. "1995-06-12"
  licenceNumber: string;
  postcode: string;
  vehicleRef: string;
};

// premium is a decimal string (e.g. "550.00") to avoid IEEE-754 drift on money.
export type QuoteResponse = { quoteId: string; premium: string };

export type Policy = {
  policyId: string;
  quoteId: string;
  premium: string;       // decimal string, see QuoteResponse.premium
  effectiveFrom: string; // ISO date
  effectiveTo: string;   // ISO date
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

export async function postQuote(req: QuoteRequest): Promise<QuoteResponse> {
  return parseResponse<QuoteResponse>(
    await fetch(`${apiBaseUrl}/api/quotes`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req),
    })
  );
}

export async function bindPolicy(quoteId: string): Promise<Policy> {
  return parseResponse<Policy>(
    await fetch(`${apiBaseUrl}/api/policies`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ quoteId }),
    })
  );
}
