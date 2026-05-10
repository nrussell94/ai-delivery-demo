import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import App from './App';

const originalFetch = global.fetch;

describe('App', () => {
  beforeEach(() => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ status: 'ok' })
    });
  });

  afterEach(() => {
    global.fetch = originalFetch;
    vi.restoreAllMocks();
  });

  it('shows the app title', () => {
    render(<App />);
    expect(screen.getByRole('heading', { name: 'Demo App' })).toBeInTheDocument();
  });

  it('loads and displays backend health', async () => {
    render(<App />);
    expect(await screen.findByText('API status: ok')).toBeInTheDocument();
  });

  it('refreshes health on button click', async () => {
    render(<App />);
    await screen.findByText('API status: ok');

    await userEvent.click(screen.getByRole('button', { name: /refresh/i }));
    expect(global.fetch).toHaveBeenCalledTimes(2);
  });
});
