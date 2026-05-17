import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import App from './App';

describe('quote-and-bind wizard', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('quotes then binds', async () => {
    const user = userEvent.setup();

    const fetchMock = vi.fn()
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ quoteId: 'q1', premium: '700.00' }),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          policyId: 'p1',
          quoteId: 'q1',
          premium: '700.00',
          effectiveFrom: '2026-05-17',
          effectiveTo: '2027-05-17',
        }),
      });

    vi.stubGlobal('fetch', fetchMock);

    render(<App />);

    await user.type(screen.getByLabelText(/driver name/i), 'Jane Doe');
    await user.type(screen.getByLabelText(/date of birth/i), '1990-06-12');
    await user.type(screen.getByLabelText(/licence number/i), 'DOEJ906120JA9AB');
    await user.type(screen.getByLabelText(/postcode/i), 'E14 5AB');
    await user.type(screen.getByLabelText(/vehicle ref/i), 'AB12CDE');

    await user.click(screen.getByRole('button', { name: /get quote/i }));

    expect(await screen.findByText(/£700\.00/)).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /bind policy/i }));

    expect(await screen.findByText(/p1/)).toBeInTheDocument();
    expect(await screen.findByText(/2026-05-17 → 2027-05-17/)).toBeInTheDocument();
  });

  it('surfaces 400 from quote', async () => {
    const user = userEvent.setup();

    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      json: async () => ({ message: 'Postcode not supported' }),
    }));

    render(<App />);

    await user.type(screen.getByLabelText(/driver name/i), 'Jane Doe');
    await user.type(screen.getByLabelText(/date of birth/i), '1990-06-12');
    await user.type(screen.getByLabelText(/licence number/i), 'DOEJ906120JA9AB');
    await user.type(screen.getByLabelText(/postcode/i), 'W1 1AA');
    await user.type(screen.getByLabelText(/vehicle ref/i), 'AB12CDE');

    await user.click(screen.getByRole('button', { name: /get quote/i }));

    expect(await screen.findByText('Postcode not supported')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /bind policy/i })).not.toBeInTheDocument();
  });

  it('surfaces bind error beneath the quote panel and leaves bind retry available', async () => {
    const user = userEvent.setup();

    const fetchMock = vi.fn()
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ quoteId: 'q1', premium: '550.00' }),
      })
      .mockResolvedValueOnce({
        ok: false,
        json: async () => ({ message: 'Quote not found: q1' }),
      });

    vi.stubGlobal('fetch', fetchMock);

    render(<App />);

    await user.type(screen.getByLabelText(/driver name/i), 'Jane Doe');
    await user.type(screen.getByLabelText(/date of birth/i), '1990-06-12');
    await user.type(screen.getByLabelText(/licence number/i), 'DOEJ906120JA9AB');
    await user.type(screen.getByLabelText(/postcode/i), 'E14 5AB');
    await user.type(screen.getByLabelText(/vehicle ref/i), 'AB12CDE');

    await user.click(screen.getByRole('button', { name: /get quote/i }));
    expect(await screen.findByText(/£550\.00/)).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /bind policy/i }));

    expect(await screen.findByText('Quote not found: q1')).toBeInTheDocument();
    // Bind button still present so the user can retry without re-quoting.
    expect(screen.getByRole('button', { name: /bind policy/i })).toBeInTheDocument();
    // The successful-policy panel must not have rendered.
    expect(screen.queryByText(/Policy ID:/i)).not.toBeInTheDocument();
  });
});
