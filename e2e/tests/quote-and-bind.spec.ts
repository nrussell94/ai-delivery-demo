import { expect, test } from '@playwright/test';

test('quote and bind a policy', async ({ page }) => {
  await page.goto('/');

  // Wait for the wizard heading
  await expect(page.getByRole('heading', { name: 'Quote & Bind' })).toBeVisible();

  // Compute a DOB that makes the driver exactly 30 years old today
  const dob = new Date(Date.now() - 30 * 365.25 * 86400000).toISOString().slice(0, 10);

  // Fill the five fields
  await page.getByLabel('Driver name').fill('Alex Driver');
  await page.getByLabel('Date of birth').fill(dob);
  await page.getByLabel('Licence number').fill('DRIVE701054AB9XY');
  await page.getByLabel('Postcode').fill('SE1 9SG');
  await page.getByLabel('Vehicle ref').fill('AB12 CDE');

  // Request a quote
  await page.getByRole('button', { name: 'Get quote' }).click();

  // SE postcode factor 1.0 × age-30 factor 1.0 × base 500 → £500
  await expect(page.getByText(/Premium:\s*£500/)).toBeVisible({ timeout: 15000 });

  // Bind the policy
  await page.getByRole('button', { name: 'Bind policy' }).click();

  // Assert policy ID and effective window appear
  await expect(page.getByText(/Policy ID:/)).toBeVisible({ timeout: 15000 });
  await expect(
    page.getByText(/Effective:\s*\d{4}-\d{2}-\d{2}\s*→\s*\d{4}-\d{2}-\d{2}/)
  ).toBeVisible({ timeout: 5000 });
});
