export type HealthResponse = {
  status: string;
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
