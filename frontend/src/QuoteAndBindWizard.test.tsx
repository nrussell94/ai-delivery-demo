import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import QuoteAndBindWizard from './QuoteAndBindWizard';

const originalFetch = global.fetch;

describe('QuoteAndBindWizard', () => {
  beforeEach(() => {
    // fetch mock is set per-test
  });

  afterEach(() => {
    global.fetch = originalFetch;
    vi.restoreAllMocks();
  });

  it('quote-and-bind-happy-path', async () => {
    global.fetch = vi.fn()
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ quoteId: 'q1', premium: 550 }),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          policyId: 'p1',
          quoteId: 'q1',
          premium: 550,
          effectiveFrom: '2026-05-17T12:00:00Z',
          effectiveTo: '2027-05-17T12:00:00Z',
        }),
      });

    render(<QuoteAndBindWizard />);

    await userEvent.type(screen.getByLabelText(/name/i), 'Alex Driver');
    await userEvent.type(screen.getByLabelText(/date of birth/i), '1995-03-01');
    await userEvent.type(screen.getByLabelText(/driver licence number/i), 'AB1234567');
    await userEvent.type(screen.getByLabelText(/postcode/i), 'E1');
    await userEvent.type(screen.getByLabelText(/vehicle registration/i), 'AB12 CDE');

    await userEvent.click(screen.getByRole('button', { name: /get quote/i }));

    expect(await screen.findByText(/£550(\.00)?/)).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: /bind policy/i }));

    expect(await screen.findByText(/Policy ID: p1/)).toBeInTheDocument();
    expect(await screen.findByText(/2026-05-17T12:00:00Z/)).toBeInTheDocument();
    expect(await screen.findByText(/2027-05-17T12:00:00Z/)).toBeInTheDocument();

    expect(global.fetch).toHaveBeenCalledTimes(2);
    expect(global.fetch).toHaveBeenNthCalledWith(
      1,
      expect.stringContaining('/api/quotes'),
      expect.objectContaining({ method: 'POST' })
    );
    expect(global.fetch).toHaveBeenNthCalledWith(
      2,
      expect.stringContaining('/api/policies'),
      expect.objectContaining({ method: 'POST' })
    );
  });

  it('quote-rejection-shows-error', async () => {
    global.fetch = vi.fn().mockResolvedValueOnce({
      ok: false,
      status: 400,
      json: async () => ({ message: 'Driver must be 21 or older' }),
    });

    render(<QuoteAndBindWizard />);

    await userEvent.type(screen.getByLabelText(/name/i), 'Young Driver');
    await userEvent.type(screen.getByLabelText(/date of birth/i), '2008-01-01');
    await userEvent.type(screen.getByLabelText(/driver licence number/i), 'YD0000001');
    await userEvent.type(screen.getByLabelText(/postcode/i), 'E1');
    await userEvent.type(screen.getByLabelText(/vehicle registration/i), 'YD12 RVR');

    await userEvent.click(screen.getByRole('button', { name: /get quote/i }));

    expect(await screen.findByText('Driver must be 21 or older')).toBeInTheDocument();

    expect(screen.queryByRole('button', { name: /bind policy/i })).toBeNull();

    expect(global.fetch).toHaveBeenCalledTimes(1);
  });
});
