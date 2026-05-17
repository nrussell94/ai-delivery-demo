import { Alert, Button, Paper, Stack, TextField, Typography } from '@mui/material';
import { useState } from 'react';
import { type BindRequest, type Policy, type QuoteRequest, type QuoteResponse, bindPolicy, createQuote } from './api';

type Status = 'idle' | 'quoting' | 'quoted' | 'binding' | 'bound';

type FormState = {
  name: string;
  dateOfBirth: string;
  driverLicenceNumber: string;
  postcode: string;
  vehicleRegistration: string;
};

const emptyForm: FormState = {
  name: '',
  dateOfBirth: '',
  driverLicenceNumber: '',
  postcode: '',
  vehicleRegistration: '',
};

export default function QuoteAndBindWizard() {
  const [form, setForm] = useState<FormState>(emptyForm);
  const [status, setStatus] = useState<Status>('idle');
  const [quote, setQuote] = useState<QuoteResponse | null>(null);
  const [policy, setPolicy] = useState<Policy | null>(null);
  const [error, setError] = useState<string | null>(null);

  function handleChange(field: keyof FormState) {
    return (e: React.ChangeEvent<HTMLInputElement>) => {
      setForm((prev) => ({ ...prev, [field]: e.target.value }));
    };
  }

  async function handleGetQuote() {
    setStatus('quoting');
    setError(null);
    setPolicy(null);
    const request: QuoteRequest = {
      name: form.name,
      dateOfBirth: form.dateOfBirth,
      driverLicenceNumber: form.driverLicenceNumber,
      postcode: form.postcode,
      vehicleRegistration: form.vehicleRegistration,
    };
    try {
      const result = await createQuote(request);
      setQuote(result);
      setStatus('quoted');
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Unable to get a quote');
      setStatus('idle');
    }
  }

  async function handleBindPolicy() {
    if (!quote) return;
    setStatus('binding');
    setError(null);
    const request: BindRequest = { quoteId: quote.quoteId };
    try {
      const result = await bindPolicy(request);
      setPolicy(result);
      setStatus('bound');
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Unable to bind policy');
      setStatus('quoted');
    }
  }

  const inFlight = status === 'quoting' || status === 'binding';

  return (
    <Paper sx={{ p: 3, borderRadius: 2 }}>
      <Stack spacing={2}>
        <Typography variant="h5" component="h2">
          Quote and bind
        </Typography>

        {error !== null && <Alert severity="error">{error}</Alert>}

        <TextField
          label="Name"
          value={form.name}
          onChange={handleChange('name')}
        />
        <TextField
          label="Date of birth"
          type="date"
          value={form.dateOfBirth}
          onChange={handleChange('dateOfBirth')}
          InputLabelProps={{ shrink: true }}
        />
        <TextField
          label="Driver licence number"
          value={form.driverLicenceNumber}
          onChange={handleChange('driverLicenceNumber')}
        />
        <TextField
          label="Postcode"
          value={form.postcode}
          onChange={handleChange('postcode')}
        />
        <TextField
          label="Vehicle registration"
          value={form.vehicleRegistration}
          onChange={handleChange('vehicleRegistration')}
        />

        <Button variant="contained" onClick={handleGetQuote} disabled={inFlight}>
          Get quote
        </Button>

        {quote !== null && (
          <Alert severity="success">
            Premium: £{quote.premium.toFixed(2)}
          </Alert>
        )}

        {quote !== null && (
          <Button variant="contained" onClick={handleBindPolicy} disabled={inFlight}>
            Bind policy
          </Button>
        )}

        {policy !== null && (
          <Alert severity="success">
            <div>Policy ID: {policy.policyId}</div>
            <div>Effective from: {policy.effectiveFrom}</div>
            <div>Effective to: {policy.effectiveTo}</div>
          </Alert>
        )}
      </Stack>
    </Paper>
  );
}
