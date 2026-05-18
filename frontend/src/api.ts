export type HealthResponse = {
  status: string;
};

export type IncidentType = 'COLLISION' | 'THEFT' | 'GLASS' | 'FIRE' | 'OTHER';

export type ClaimStatus = 'OPEN';

export type ClaimRequest = {
  policyId: string;
  incidentAt: string;
  incidentType: IncidentType;
  description: string;
  reportedBy: string;
};

export type Claim = ClaimRequest & {
  claimReference: string;
  status: ClaimStatus;
  createdAt: string;
  updatedAt?: string;
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

export async function createClaim(request: ClaimRequest): Promise<Claim> {
  return parseResponse<Claim>(
    await fetch(`${apiBaseUrl}/api/claims`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request)
    })
  );
}

export async function listClaims(policyId: string): Promise<Claim[]> {
  const url = `${apiBaseUrl}/api/claims?policyId=${encodeURIComponent(policyId)}`;
  return parseResponse<Claim[]>(await fetch(url));
}
