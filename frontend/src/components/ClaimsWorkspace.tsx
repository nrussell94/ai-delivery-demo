import { Alert, Paper, Stack, TextField, Typography } from '@mui/material';
import { useState } from 'react';
import { type Claim } from '../api';
import ClaimIntakeForm from './ClaimIntakeForm';
import PolicyClaimsList from './PolicyClaimsList';

export default function ClaimsWorkspace() {
  const [policyId, setPolicyId] = useState('');
  const [latestClaim, setLatestClaim] = useState<Claim | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

  function handleCreated(claim: Claim) {
    setLatestClaim(claim);
    setRefreshKey((n) => n + 1);
  }

  return (
    <Stack spacing={3}>
      <Paper sx={{ p: 3, borderRadius: 2 }}>
        <Stack spacing={2}>
          <Typography variant="h5" component="h2">
            Policy
          </Typography>
          <TextField
            label="Policy ID"
            value={policyId}
            onChange={(event) => setPolicyId(event.target.value)}
            placeholder="e.g. POL-123"
            fullWidth
          />
        </Stack>
      </Paper>

      {latestClaim && (
        <Alert severity="success" data-testid="claim-success-banner">
          <Typography component="span" sx={{ fontWeight: 600 }}>
            Read this back to the caller:{' '}
          </Typography>
          <Typography component="span" sx={{ fontFamily: 'monospace', fontWeight: 700 }}>
            {latestClaim.claimReference}
          </Typography>
          <Typography component="span"> — status </Typography>
          <Typography component="span" sx={{ fontWeight: 600 }} data-testid="claim-success-status">
            {latestClaim.status}
          </Typography>
        </Alert>
      )}

      <Paper sx={{ p: 3, borderRadius: 2 }}>
        <Stack spacing={2}>
          <Typography variant="h5" component="h2">
            Log a new claim
          </Typography>
          <ClaimIntakeForm policyId={policyId} onCreated={handleCreated} />
        </Stack>
      </Paper>

      <Paper sx={{ p: 3, borderRadius: 2 }}>
        <PolicyClaimsList policyId={policyId} refreshKey={refreshKey} />
      </Paper>
    </Stack>
  );
}
