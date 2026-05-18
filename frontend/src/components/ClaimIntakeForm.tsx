import { Alert, Box, Button, Stack, TextField } from '@mui/material';
import { useState } from 'react';
import { type Claim, type IncidentType, createClaim } from '../api';

const INCIDENT_TYPE_LABELS: Record<IncidentType, string> = {
  COLLISION: 'Collision',
  THEFT: 'Theft',
  GLASS: 'Smashed glass',
  FIRE: 'Fire',
  OTHER: 'Other'
};

type Props = {
  policyId: string;
  onCreated: (claim: Claim) => void;
};

export default function ClaimIntakeForm({ policyId, onCreated }: Props) {
  const [incidentAt, setIncidentAt] = useState('');
  const [incidentType, setIncidentType] = useState<IncidentType | ''>('');
  const [description, setDescription] = useState('');
  const [reportedBy, setReportedBy] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    if (!policyId || !incidentAt || !incidentType || !description || !reportedBy) {
      setError('Fill every field before submitting.');
      return;
    }
    setSubmitting(true);
    try {
      const claim = await createClaim({
        policyId,
        incidentAt: new Date(incidentAt).toISOString(),
        incidentType,
        description,
        reportedBy
      });
      setIncidentAt('');
      setIncidentType('');
      setDescription('');
      setReportedBy('');
      onCreated(claim);
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Failed to log the claim.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Box component="form" onSubmit={handleSubmit} noValidate>
      <Stack spacing={2}>
        {error && <Alert severity="error">{error}</Alert>}
        <TextField
          label="Incident date & time"
          type="datetime-local"
          value={incidentAt}
          onChange={(event) => setIncidentAt(event.target.value)}
          InputLabelProps={{ shrink: true }}
          required
          fullWidth
        />
        <TextField
          label="Incident type"
          select
          SelectProps={{ native: true }}
          value={incidentType}
          onChange={(event) => setIncidentType(event.target.value as IncidentType)}
          InputLabelProps={{ shrink: true }}
          required
          fullWidth
        >
          <option value="" disabled>
            Choose one
          </option>
          {(Object.keys(INCIDENT_TYPE_LABELS) as IncidentType[]).map((type) => (
            <option key={type} value={type}>
              {INCIDENT_TYPE_LABELS[type]}
            </option>
          ))}
        </TextField>
        <TextField
          label="What did the caller say?"
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          multiline
          minRows={4}
          inputProps={{ maxLength: 2000 }}
          required
          fullWidth
        />
        <TextField
          label="Reported by (caller name)"
          value={reportedBy}
          onChange={(event) => setReportedBy(event.target.value)}
          required
          fullWidth
        />
        <Box>
          <Button type="submit" variant="contained" disabled={submitting}>
            {submitting ? 'Submitting…' : 'Log claim'}
          </Button>
        </Box>
      </Stack>
    </Box>
  );
}
