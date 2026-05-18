import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import ClaimsWorkspace from './ClaimsWorkspace';

const originalFetch = global.fetch;

type FetchMock = ReturnType<typeof vi.fn>;

function setupFetch(impl: (url: string, init?: RequestInit) => Promise<Response>): FetchMock {
  const mock = vi.fn(impl);
  global.fetch = mock as unknown as typeof fetch;
  return mock;
}

async function fillForm(user: ReturnType<typeof userEvent.setup>) {
  await user.type(screen.getByLabelText(/policy id/i), 'POL-123');

  // datetime-local: use fireEvent-equivalent via userEvent.type does not work on
  // datetime-local in jsdom; use direct value assignment via the input element.
  const dateInput = screen.getByLabelText(/incident date/i) as HTMLInputElement;
  await user.click(dateInput);
  await user.type(dateInput, '2026-05-18T07:00');

  await user.selectOptions(screen.getByLabelText(/incident type/i), 'COLLISION');

  await user.type(
    screen.getByLabelText(/what did the caller say/i),
    'Side mirror clipped by van on a left turn'
  );

  await user.type(screen.getByLabelText(/reported by/i), 'Sam Quinn');
}

describe('ClaimsWorkspace', () => {
  beforeEach(() => {
    global.fetch = vi.fn(async () => ({
      ok: true,
      json: async () => []
    })) as unknown as typeof fetch;
  });

  afterEach(() => {
    global.fetch = originalFetch;
    vi.restoreAllMocks();
  });

  it('submits a claim and surfaces the reference + status to read back', async () => {
    const user = userEvent.setup();

    const successPayload = {
      claimReference: 'FNOL-7K2QH9XR',
      policyId: 'POL-123',
      incidentAt: '2026-05-18T07:00:00.000Z',
      incidentType: 'COLLISION',
      description: 'Side mirror clipped by van on a left turn',
      reportedBy: 'Sam Quinn',
      status: 'OPEN',
      createdAt: '2026-05-18T09:00:00.000Z'
    };

    const fetchMock = setupFetch(async (url: string, init?: RequestInit) => {
      if (typeof url === 'string' && url.includes('/api/claims') && init?.method === 'POST') {
        return {
          ok: true,
          status: 201,
          json: async () => successPayload
        } as Response;
      }
      return { ok: true, json: async () => [] } as Response;
    });

    render(<ClaimsWorkspace />);
    await fillForm(user);
    await user.click(screen.getByRole('button', { name: /log claim/i }));

    const banner = await screen.findByTestId('claim-success-banner');
    expect(within(banner).getByText('FNOL-7K2QH9XR')).toBeInTheDocument();
    expect(within(banner).getByTestId('claim-success-status')).toHaveTextContent('OPEN');

    await waitFor(() => {
      const postCalls = fetchMock.mock.calls.filter(([, init]) => (init as RequestInit | undefined)?.method === 'POST');
      expect(postCalls).toHaveLength(1);
      const [, postInit] = postCalls[0];
      const body = JSON.parse((postInit as RequestInit).body as string);
      expect(body).toMatchObject({
        policyId: 'POL-123',
        incidentType: 'COLLISION',
        description: 'Side mirror clipped by van on a left turn',
        reportedBy: 'Sam Quinn'
      });
      expect(body.incidentAt).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/);
    });
  });

  it('keeps the form intact and surfaces the server message on a 400', async () => {
    const user = userEvent.setup();

    setupFetch(async (url: string, init?: RequestInit) => {
      if (typeof url === 'string' && url.includes('/api/claims') && init?.method === 'POST') {
        return {
          ok: false,
          status: 400,
          json: async () => ({ message: 'Incident date is in the future' })
        } as Response;
      }
      return { ok: true, json: async () => [] } as Response;
    });

    render(<ClaimsWorkspace />);
    await fillForm(user);
    await user.click(screen.getByRole('button', { name: /log claim/i }));

    expect(await screen.findByText('Incident date is in the future')).toBeInTheDocument();
    expect(screen.queryByTestId('claim-success-banner')).not.toBeInTheDocument();

    expect((screen.getByLabelText(/policy id/i) as HTMLInputElement).value).toBe('POL-123');
    expect((screen.getByLabelText(/incident date/i) as HTMLInputElement).value).toBe('2026-05-18T07:00');
    expect((screen.getByLabelText(/incident type/i) as HTMLSelectElement).value).toBe('COLLISION');
    expect((screen.getByLabelText(/what did the caller say/i) as HTMLTextAreaElement).value).toContain('Side mirror clipped');
    expect((screen.getByLabelText(/reported by/i) as HTMLInputElement).value).toBe('Sam Quinn');
  });
});
