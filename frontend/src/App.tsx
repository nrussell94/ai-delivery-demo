import RefreshIcon from '@mui/icons-material/Refresh';
import { Alert, Box, Button, CircularProgress, Container, Paper, Stack, Typography } from '@mui/material';
import { useEffect, useState } from 'react';
import { getHealth } from './api';
import QuoteAndBindWizard from './QuoteAndBindWizard';

export default function App() {
  const [loading, setLoading] = useState(true);
  const [status, setStatus] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function refresh() {
    setLoading(true);
    setError(null);
    try {
      const response = await getHealth();
      setStatus(response.status);
    } catch (caught) {
      setStatus(null);
      setError(caught instanceof Error ? caught.message : 'Unable to reach the API');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void refresh();
  }, []);

  return (
    <Box sx={{ bgcolor: '#f7f8fa', minHeight: '100vh', py: 4 }}>
      <Container maxWidth="md">
        <Stack spacing={3}>
          <Box>
            <Typography variant="h3" component="h1" fontWeight={700}>
              Demo App
            </Typography>
            <Typography color="text.secondary">
              Full-stack bootstrap (Spring Boot + React) ready for feature work.
            </Typography>
          </Box>

          {error && <Alert severity="error">{error}</Alert>}

          <Paper sx={{ p: 3, borderRadius: 2 }}>
            <Stack spacing={2}>
              <Stack direction="row" justifyContent="space-between" alignItems="center" spacing={2}>
                <Typography variant="h5" component="h2">
                  Backend health
                </Typography>
                <Button variant="outlined" startIcon={<RefreshIcon />} onClick={refresh} disabled={loading}>
                  Refresh
                </Button>
              </Stack>

              {loading && (
                <Stack direction="row" spacing={1} alignItems="center">
                  <CircularProgress size={20} />
                  <Typography>Checking…</Typography>
                </Stack>
              )}

              {!loading && status && (
                <Alert severity="success">API status: {status}</Alert>
              )}
            </Stack>
          </Paper>

          <QuoteAndBindWizard />
        </Stack>
      </Container>
    </Box>
  );
}
