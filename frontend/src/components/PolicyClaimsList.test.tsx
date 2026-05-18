import { render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import PolicyClaimsList from './PolicyClaimsList';

const originalFetch = global.fetch;

describe('PolicyClaimsList', () => {
  beforeEach(() => {
    global.fetch = vi.fn(async () => ({
      ok: true,
      json: async () => [
        {
          claimReference: 'FNOL-RECENT01',
          policyId: 'POL-123',
          incidentAt: '2026-05-18T08:55:00.000Z',
          incidentType: 'COLLISION',
          description: 'Just-now incident',
          reportedBy: 'Test',
          status: 'OPEN',
          createdAt: '2026-05-18T09:00:00.000Z'
        },
        {
          claimReference: 'FNOL-OLDER01',
          policyId: 'POL-123',
          incidentAt: '2026-05-17T08:00:00.000Z',
          incidentType: 'THEFT',
          description: 'Yesterday incident',
          reportedBy: 'Test',
          status: 'OPEN',
          createdAt: '2026-05-17T09:00:00.000Z'
        }
      ]
    })) as unknown as typeof fetch;
  });

  afterEach(() => {
    global.fetch = originalFetch;
    vi.restoreAllMocks();
  });

  it("renders a policy's claims newest first", async () => {
    render(<PolicyClaimsList policyId="POL-123" refreshKey={0} />);

    await waitFor(() => {
      expect(screen.getAllByTestId('claim-reference')).toHaveLength(2);
    });
    const references = screen.getAllByTestId('claim-reference').map((el) => el.textContent);
    expect(references).toEqual(['FNOL-RECENT01', 'FNOL-OLDER01']);
  });
});
