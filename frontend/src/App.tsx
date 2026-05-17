import { Alert, Box, Button, CircularProgress, Container, Paper, Stack, TextField, Typography } from '@mui/material';
import { useState } from 'react';
import { bindPolicy, postQuote, Policy, QuoteResponse } from './api';

export default function App() {
  const [driverName, setDriverName] = useState('');
  const [dateOfBirth, setDateOfBirth] = useState('');
  const [licenceNumber, setLicenceNumber] = useState('');
  const [postcode, setPostcode] = useState('');
  const [vehicleRef, setVehicleRef] = useState('');

  const [quoting, setQuoting] = useState(false);
  const [quote, setQuote] = useState<QuoteResponse | null>(null);
  const [quoteError, setQuoteError] = useState<string | null>(null);

  const [binding, setBinding] = useState(false);
  const [bindError, setBindError] = useState<string | null>(null);
  const [policy, setPolicy] = useState<Policy | null>(null);

  async function handleGetQuote() {
    setQuoting(true);
    setQuoteError(null);
    setBindError(null);
    setQuote(null);
    setPolicy(null);
    try {
      const result = await postQuote({ driverName, dateOfBirth, licenceNumber, postcode, vehicleRef });
      setQuote(result);
    } catch (err) {
      setQuoteError(err instanceof Error ? err.message : 'The API request failed');
    } finally {
      setQuoting(false);
    }
  }

  async function handleBindPolicy() {
    if (!quote) return;
    setBinding(true);
    setBindError(null);
    try {
      const result = await bindPolicy(quote.quoteId);
      setPolicy(result);
    } catch (err) {
      setBindError(err instanceof Error ? err.message : 'The API request failed');
    } finally {
      setBinding(false);
    }
  }

  return (
    <Box sx={{ bgcolor: '#f7f8fa', minHeight: '100vh', py: 4 }}>
      <Container maxWidth="sm">
        <Stack spacing={3}>
          <Typography variant="h4" component="h1" fontWeight={700}>
            Quote &amp; Bind
          </Typography>

          <Paper sx={{ p: 3, borderRadius: 2 }}>
            <Stack spacing={2}>
              <TextField
                label="Driver name"
                value={driverName}
                onChange={e => setDriverName(e.target.value)}
                fullWidth
              />
              <TextField
                label="Date of birth"
                type="date"
                value={dateOfBirth}
                onChange={e => setDateOfBirth(e.target.value)}
                fullWidth
                InputLabelProps={{ shrink: true }}
              />
              <TextField
                label="Licence number"
                value={licenceNumber}
                onChange={e => setLicenceNumber(e.target.value)}
                fullWidth
              />
              <TextField
                label="Postcode"
                value={postcode}
                onChange={e => setPostcode(e.target.value)}
                fullWidth
              />
              <TextField
                label="Vehicle ref"
                value={vehicleRef}
                onChange={e => setVehicleRef(e.target.value)}
                fullWidth
              />

              <Button
                variant="contained"
                onClick={handleGetQuote}
                disabled={quoting}
                startIcon={quoting ? <CircularProgress size={18} color="inherit" /> : undefined}
              >
                Get quote
              </Button>
            </Stack>
          </Paper>

          {quoteError && (
            <Alert severity="error">{quoteError}</Alert>
          )}

          {quote && !quoteError && !policy && (
            <Paper sx={{ p: 3, borderRadius: 2 }}>
              <Stack spacing={2}>
                <Typography variant="h6">Quote</Typography>
                <Typography>Premium: £{quote.premium}</Typography>
                {bindError && (
                  <Alert severity="error">{bindError}</Alert>
                )}
                <Button
                  variant="outlined"
                  onClick={handleBindPolicy}
                  disabled={binding}
                  startIcon={binding ? <CircularProgress size={18} color="inherit" /> : undefined}
                >
                  Bind policy
                </Button>
              </Stack>
            </Paper>
          )}

          {policy && (
            <Paper sx={{ p: 3, borderRadius: 2 }}>
              <Stack spacing={1}>
                <Typography variant="h6">Policy</Typography>
                <Typography>Policy ID: {policy.policyId}</Typography>
                <Typography>Premium: £{policy.premium}</Typography>
                <Typography>Effective: {policy.effectiveFrom} → {policy.effectiveTo}</Typography>
              </Stack>
            </Paper>
          )}
        </Stack>
      </Container>
    </Box>
  );
}
