import { expect, test } from '@playwright/test';

test('quote and bind a policy for an eligible driver', async ({ page }) => {
  await page.goto('/');

  await expect(page.getByRole('heading', { name: 'Quote and bind' })).toBeVisible();

  await page.getByLabel('Name').fill('E2E Driver');
  await page.getByLabel('Date of birth').fill('1990-01-01');
  await page.getByLabel('Driver licence number').fill('TEST1234');
  await page.getByLabel('Postcode').fill('E5');
  await page.getByLabel('Vehicle registration').fill('EE12 OOE');

  await page.getByRole('button', { name: /get quote/i }).click();

  await expect(page.getByText(/Premium:\s*£\d/)).toBeVisible();

  await page.getByRole('button', { name: /bind policy/i }).click();

  await expect(page.getByText(/Policy ID:/)).toBeVisible();
  await expect(page.getByText(/Effective from:/)).toBeVisible();
  await expect(page.getByText(/Effective to:/)).toBeVisible();
});
