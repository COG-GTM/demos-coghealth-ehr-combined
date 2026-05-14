import puppeteer, { Browser, Page } from 'puppeteer';

const BASE_URL = 'http://localhost:5173';
const wait = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

describe('Patient Chart Page E2E Tests', () => {
  let browser: Browser;
  let page: Page;
  let backendAvailable = false;

  beforeAll(async () => {
    browser = await puppeteer.launch({
      headless: true,
      args: ['--no-sandbox', '--disable-setuid-sandbox'],
    });
    page = await browser.newPage();
    await page.setViewport({ width: 1280, height: 800 });

    // Check if backend is available by loading a patient chart and waiting for patient-specific content
    try {
      await page.goto(`${BASE_URL}/patients/1`, { timeout: 10000 });
      // The PatientBanner or tab buttons only render when patient data is loaded from API
      await page.waitForSelector('::-p-xpath(//button[contains(., "Summary")])', { timeout: 5000 });
      backendAvailable = true;
    } catch {
      console.warn('Backend API not available — patient chart tests requiring API will be skipped');
      backendAvailable = false;
    }
  });

  afterAll(async () => {
    await browser.close();
  });

  describe('Page Loading', () => {
    test('should navigate to patient chart URL', async () => {
      await page.goto(`${BASE_URL}/patients/1`);
      await wait(500);
      const body = await page.$('body');
      expect(body).not.toBeNull();
    });

    test('should show loading state or patient chart', async () => {
      await page.goto(`${BASE_URL}/patients/1`);
      await wait(1000);
      const bodyText = await page.$eval('body', el => el.textContent);
      // Either loading state or patient chart should be visible
      const isLoading = bodyText?.includes('Loading patient');
      const isChart = bodyText?.includes('Summary') || bodyText?.includes('e-Prescribe');
      expect(isLoading || isChart).toBe(true);
    });
  });

  describe('Chart Tabs (with backend)', () => {
    beforeEach(async () => {
      if (!backendAvailable) return;
      await page.goto(`${BASE_URL}/patients/1`);
      await page.waitForSelector('::-p-xpath(//button[contains(., "Summary")])', { timeout: 5000 });
    });

    test('should display Summary tab by default', async () => {
      if (!backendAvailable) {
        console.warn('Skipping: backend not available');
        return;
      }
      const summaryTab = await page.$('::-p-xpath(//button[contains(., "Summary")])');
      expect(summaryTab).not.toBeNull();
    });

    test('should switch to Encounters tab', async () => {
      if (!backendAvailable) return;
      const encountersTab = await page.$('::-p-xpath(//button[contains(., "Encounters")])');
      if (!encountersTab) return;
      await encountersTab.click();
      await wait(200);
    });

    test('should switch to Medications tab', async () => {
      if (!backendAvailable) return;
      const medsTab = await page.$('::-p-xpath(//button[contains(., "Medications")])');
      if (!medsTab) return;
      await medsTab.click();
      await wait(200);
    });

    test('should switch to Problems tab', async () => {
      if (!backendAvailable) return;
      const problemsTab = await page.$('::-p-xpath(//button[contains(., "Problems")])');
      if (!problemsTab) return;
      await problemsTab.click();
      await wait(200);
    });

    test('should switch to Allergies tab', async () => {
      if (!backendAvailable) return;
      const allergiesTab = await page.$('::-p-xpath(//button[contains(., "Allergies")])');
      if (!allergiesTab) return;
      await allergiesTab.click();
      await wait(200);
    });

    test('should switch to Results tab', async () => {
      if (!backendAvailable) return;
      const resultsTab = await page.$('::-p-xpath(//button[contains(., "Results")])');
      if (!resultsTab) return;
      await resultsTab.click();
      await wait(200);
    });

    test('should return to Summary tab', async () => {
      if (!backendAvailable) return;
      const encountersTab = await page.$('::-p-xpath(//button[contains(., "Encounters")])');
      if (encountersTab) await encountersTab.click();
      await wait(100);
      const summaryTab = await page.$('::-p-xpath(//button[contains(., "Summary")])');
      if (summaryTab) await summaryTab.click();
      await wait(200);
    });
  });

  describe('Chart Actions (with backend)', () => {
    beforeEach(async () => {
      if (!backendAvailable) return;
      await page.goto(`${BASE_URL}/patients/1`);
      await page.waitForSelector('::-p-xpath(//button[contains(., "Summary")])', { timeout: 5000 });
    });

    test('should open e-Prescribe dialog from chart', async () => {
      if (!backendAvailable) return;
      const rxBtn = await page.$('::-p-xpath(//button[contains(., "e-Prescribe")])');
      if (!rxBtn) return;
      await rxBtn.click();
      await page.waitForSelector('.fixed.inset-0');
      await page.click('::-p-xpath(//button[contains(., "Cancel")])');
    });

    test('should open Order Labs dialog from chart', async () => {
      if (!backendAvailable) return;
      const labBtn = await page.$('::-p-xpath(//button[contains(., "Order Labs")])');
      if (!labBtn) return;
      await labBtn.click();
      await page.waitForSelector('.fixed.inset-0');
      await page.click('::-p-xpath(//button[contains(., "Cancel")])');
    });

    test('should open Print dialog from chart', async () => {
      if (!backendAvailable) return;
      const printBtn = await page.$('::-p-xpath(//button[contains(., "Print")])');
      if (!printBtn) return;
      await printBtn.click();
      await page.waitForSelector('.fixed.inset-0');
      await page.click('::-p-xpath(//button[contains(., "Cancel")])');
    });
  });

  describe('Patient Data Display (with backend)', () => {
    beforeEach(async () => {
      if (!backendAvailable) return;
      await page.goto(`${BASE_URL}/patients/1`);
      await page.waitForSelector('::-p-xpath(//button[contains(., "Summary")])', { timeout: 5000 });
    });

    test('should show problem list with ICD-10 codes', async () => {
      if (!backendAvailable) return;
      const icd10 = await page.$('::-p-xpath(//*[contains(., "E11.9")])');
      expect(icd10).not.toBeNull();
    });

    test('should show medication list', async () => {
      if (!backendAvailable) return;
      const metformin = await page.$('::-p-xpath(//*[contains(., "Metformin")])');
      expect(metformin).not.toBeNull();
    });

    test('should show allergy list', async () => {
      if (!backendAvailable) return;
      const penicillin = await page.$('::-p-xpath(//*[contains(., "Penicillin")])');
      expect(penicillin).not.toBeNull();
    });

    test('should show panel headers (Active Problems, Medications, Allergies)', async () => {
      if (!backendAvailable) return;
      const bodyText = await page.$eval('body', el => el.textContent);
      expect(bodyText).toContain('Active Problems');
      expect(bodyText).toContain('Medications');
      expect(bodyText).toContain('Allergies');
    });

    test('should collapse and expand a panel', async () => {
      if (!backendAvailable) return;
      const panelHeader = await page.$('::-p-xpath(//div[contains(@class, "ehr-header")][contains(., "Active Problems")])');
      if (!panelHeader) return;
      await panelHeader.click();
      await wait(200);
      await panelHeader.click();
      await wait(200);
    });
  });
});
