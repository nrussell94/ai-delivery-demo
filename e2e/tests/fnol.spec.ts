import { expect, test } from '@playwright/test';

test('agent logs an FNOL and reads it back from the list', async ({ page }) => {
  const policyId = `POL-E2E-${Date.now()}`;
  const description = `E2E side mirror clipped at ${new Date().toISOString()}`;

  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'FNOL Intake' })).toBeVisible();

  await page.getByLabel('Policy ID').first().fill(policyId);

  const oneHourAgo = new Date(Date.now() - 60 * 60 * 1000);
  const datetimeLocal = oneHourAgo.toISOString().slice(0, 16);
  await page.getByLabel('Incident date & time').fill(datetimeLocal);

  await page.getByLabel('Incident type').selectOption('COLLISION');
  await page.getByLabel('What did the caller say?').fill(description);
  await page.getByLabel('Reported by (caller name)').fill('E2E Agent');

  await page.getByRole('button', { name: /log claim/i }).click();

  const banner = page.getByTestId('claim-success-banner');
  await expect(banner).toBeVisible();
  const bannerText = await banner.innerText();
  const match = bannerText.match(/FNOL-[A-Z2-9]{8}/);
  expect(match, `expected FNOL reference in banner, got: ${bannerText}`).not.toBeNull();
  const reference = match![0];

  const card = page.getByTestId('claim-reference').filter({ hasText: reference });
  await expect(card).toBeVisible();
  await expect(page.getByText(description)).toBeVisible();
});
