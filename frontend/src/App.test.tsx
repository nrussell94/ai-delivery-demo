import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import App from './App';

const originalFetch = global.fetch;

describe('App', () => {
  beforeEach(() => {
    global.fetch = vi.fn().mockImplementation((url: string) => {
      if (typeof url === 'string' && url.includes('/api/health')) {
        return Promise.resolve({
          ok: true,
          json: async () => ({ status: 'ok' })
        } as Response);
      }
      return Promise.resolve({
        ok: true,
        json: async () => []
      } as Response);
    });
  });

  afterEach(() => {
    global.fetch = originalFetch;
    vi.restoreAllMocks();
  });

  it('shows the app title', () => {
    render(<App />);
    expect(screen.getByRole('heading', { name: 'FNOL Intake' })).toBeInTheDocument();
  });

  it('loads and displays backend health', async () => {
    render(<App />);
    expect(await screen.findByText('API status: ok')).toBeInTheDocument();
  });

  it('refreshes health on button click', async () => {
    render(<App />);
    await screen.findByText('API status: ok');

    const healthCallsBefore = (global.fetch as ReturnType<typeof vi.fn>).mock.calls
      .filter(([url]) => typeof url === 'string' && url.includes('/api/health')).length;

    await userEvent.click(screen.getByRole('button', { name: /refresh/i }));

    const healthCallsAfter = (global.fetch as ReturnType<typeof vi.fn>).mock.calls
      .filter(([url]) => typeof url === 'string' && url.includes('/api/health')).length;
    expect(healthCallsAfter).toBe(healthCallsBefore + 1);
  });
});
