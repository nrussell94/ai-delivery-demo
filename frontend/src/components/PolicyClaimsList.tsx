import { Alert, Box, Chip, CircularProgress, Paper, Stack, Typography } from '@mui/material';
import { useEffect, useState } from 'react';
import { type Claim, type IncidentType, listClaims } from '../api';

const INCIDENT_TYPE_LABELS: Record<IncidentType, string> = {
  COLLISION: 'Collision',
  THEFT: 'Theft',
  GLASS: 'Smashed glass',
  FIRE: 'Fire',
  OTHER: 'Other'
};

type Props = {
  policyId: string;
  refreshKey: number;
};

function formatIncidentAt(iso: string): string {
  try {
    return new Date(iso).toLocaleString();
  } catch {
    return iso;
  }
}

export default function PolicyClaimsList({ policyId, refreshKey }: Props) {
  const [loading, setLoading] = useState(false);
  const [claims, setClaims] = useState<Claim[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!policyId) {
      setClaims([]);
      setError(null);
      return;
    }
    let cancelled = false;
    setLoading(true);
    setError(null);
    listClaims(policyId)
      .then((result) => {
        if (!cancelled) setClaims(result);
      })
      .catch((caught: unknown) => {
        if (!cancelled) setError(caught instanceof Error ? caught.message : 'Unable to load claims.');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [policyId, refreshKey]);

  if (!policyId) {
    return (
      <Typography color="text.secondary">
        Enter a policy id above to see what's been logged.
      </Typography>
    );
  }

  return (
    <Stack spacing={2}>
      <Stack direction="row" spacing={1} alignItems="center">
        <Typography variant="h6" component="h3">
          Claims on file
        </Typography>
        <Chip label={loading ? '…' : String(claims.length)} size="small" />
      </Stack>

      {error && <Alert severity="error">{error}</Alert>}

      {loading && (
        <Stack direction="row" spacing={1} alignItems="center">
          <CircularProgress size={18} />
          <Typography>Loading…</Typography>
        </Stack>
      )}

      {!loading && !error && claims.length === 0 && (
        <Typography color="text.secondary">No claims logged for this policy yet.</Typography>
      )}

      <Stack spacing={1.5}>
        {claims.map((claim) => (
          <Paper key={claim.claimReference} variant="outlined" sx={{ p: 2 }}>
            <Stack spacing={0.5}>
              <Stack direction="row" justifyContent="space-between" alignItems="center" spacing={1}>
                <Typography
                  data-testid="claim-reference"
                  sx={{ fontFamily: 'monospace', fontWeight: 600 }}
                >
                  {claim.claimReference}
                </Typography>
                <Chip label={claim.status} color="primary" size="small" />
              </Stack>
              <Stack direction="row" spacing={2}>
                <Chip label={INCIDENT_TYPE_LABELS[claim.incidentType]} size="small" />
                <Typography variant="body2" color="text.secondary">
                  Incident: {formatIncidentAt(claim.incidentAt)}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Reported by: {claim.reportedBy}
                </Typography>
              </Stack>
              <Typography variant="body2">{claim.description}</Typography>
            </Stack>
          </Paper>
        ))}
      </Stack>
    </Stack>
  );
}
