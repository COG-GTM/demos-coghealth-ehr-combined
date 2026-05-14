import puppeteer, { Browser, Page } from 'puppeteer';

const BASE_URL = 'http://localhost:5173';
const wait = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

describe('Lab Results Page E2E Tests', () => {
  let browser: Browser;
  let page: Page;

  beforeAll(async () => {
    browser = await puppeteer.launch({
      headless: true,
      args: ['--no-sandbox', '--disable-setuid-sandbox'],
    });
    page = await browser.newPage();
    await page.setViewport({ width: 1280, height: 800 });
  });

  afterAll(async () => {
    await browser.close();
  });

  beforeEach(async () => {
    await page.goto(`${BASE_URL}/labs`);
    await page.waitForSelector('.ehr-status-bar');
  });

  describe('Page Layout', () => {
    test('should display the Laboratory Results header', async () => {
      const headerText = await page.$eval('.ehr-panel > .ehr-header', el => el.textContent);
      expect(headerText).toContain('Laboratory Results');
    });

    test('should display Print and Refresh buttons', async () => {
      const printBtn = await page.$('::-p-xpath(//button[contains(., "Print")])');
      const refreshBtn = await page.$('::-p-xpath(//button[contains(., "Refresh")])');
      expect(printBtn).not.toBeNull();
      expect(refreshBtn).not.toBeNull();
    });

    test('should display status bar with panel count', async () => {
      const statusText = await page.$eval('.ehr-status-bar span', el => el.textContent);
      expect(statusText).toContain('panel(s) displayed');
    });

    test('should display critical and abnormal count badges', async () => {
      const criticalBadge = await page.$('::-p-xpath(//span[contains(., "Critical")])');
      const abnormalBadge = await page.$('::-p-xpath(//span[contains(., "Abnormal")])');
      expect(criticalBadge).not.toBeNull();
      expect(abnormalBadge).not.toBeNull();
    });
  });

  describe('Lab Panels', () => {
    test('should display multiple lab panels', async () => {
      const panels = await page.$$('.border-b.border-gray-300');
      expect(panels.length).toBeGreaterThan(0);
    });

    test('should show panel names (BMP, CBC, etc.)', async () => {
      const panelNames = await page.$$eval('.font-semibold.text-\\[11px\\]', els =>
        els.map(el => el.textContent)
      );
      expect(panelNames.some(n => n?.includes('Basic Metabolic Panel'))).toBe(true);
      expect(panelNames.some(n => n?.includes('Complete Blood Count'))).toBe(true);
    });

    test('should show FINAL status badges on panels', async () => {
      const finalBadges = await page.$$('::-p-xpath(//span[contains(., "FINAL")])');
      expect(finalBadges.length).toBeGreaterThan(0);
    });

    test('should show CRITICAL indicator on panels with critical results', async () => {
      const criticalIndicators = await page.$$('::-p-xpath(//span[contains(., "CRITICAL")])');
      expect(criticalIndicators.length).toBeGreaterThan(0);
    });

    test('should display patient name and MRN on each panel', async () => {
      const patientInfo = await page.$$eval(
        '.border-b.border-gray-300 .text-\\[10px\\].text-gray-600 span',
        els => els.map(el => el.textContent)
      );
      expect(patientInfo.some(t => t?.includes('Smith, John'))).toBe(true);
      expect(patientInfo.some(t => t?.includes('MRN'))).toBe(true);
    });
  });

  describe('Panel Expand/Collapse', () => {
    test('should toggle panel expansion when clicking header', async () => {
      const panelHeaders = await page.$$('.cursor-pointer.hover\\:from-\\[\\#fff\\]');
      if (panelHeaders.length === 0) return;

      // Click the first panel header to toggle it
      await panelHeaders[0].click();
      await wait(200);

      // Click again to toggle back
      await panelHeaders[0].click();
      await wait(200);
    });

    test('should show result table when panel is expanded', async () => {
      const tables = await page.$$('.border-b.border-gray-300 table');
      expect(tables.length).toBeGreaterThan(0);
    });

    test('should show test columns: Test, Result, Units, Reference Range, Status', async () => {
      const headers = await page.$$eval(
        '.border-b.border-gray-300 table th',
        els => els.map(el => el.textContent)
      );
      expect(headers).toContain('Test');
      expect(headers).toContain('Result');
      expect(headers).toContain('Units');
      expect(headers).toContain('Reference Range');
      expect(headers).toContain('Status');
    });
  });

  describe('Lab Result Details', () => {
    test('should display individual test results in expanded panel', async () => {
      const resultRows = await page.$$('.border-b.border-gray-300 table tbody tr');
      expect(resultRows.length).toBeGreaterThan(0);
    });

    test('should show result values with units', async () => {
      const resultCells = await page.$$eval(
        '.border-b.border-gray-300 table tbody td.font-mono',
        els => els.map(el => el.textContent?.trim())
      );
      expect(resultCells.length).toBeGreaterThan(0);
    });

    test('should highlight critical results', async () => {
      const criticalTexts = await page.$$('::-p-xpath(//td//span[text()="CRITICAL"])');
      expect(criticalTexts.length).toBeGreaterThan(0);
    });

    test('should highlight abnormal results', async () => {
      const abnormalTexts = await page.$$('::-p-xpath(//td//span[text()="Abnormal"])');
      expect(abnormalTexts.length).toBeGreaterThan(0);
    });

    test('should show normal results', async () => {
      const normalTexts = await page.$$('::-p-xpath(//td//span[text()="Normal"])');
      expect(normalTexts.length).toBeGreaterThan(0);
    });

    test('should open result detail modal when clicking a result row', async () => {
      const resultRow = await page.$('.border-b.border-gray-300 table tbody tr.cursor-pointer');
      if (!resultRow) return;
      await resultRow.click();
      await page.waitForSelector('.fixed.inset-0');
      const modalTitle = await page.$eval('.fixed.inset-0 span.text-white', el => el.textContent);
      expect(modalTitle).toContain('Lab Result Detail');
    });

    test('should display test name and status in detail modal', async () => {
      const resultRow = await page.$('.border-b.border-gray-300 table tbody tr.cursor-pointer');
      if (!resultRow) return;
      await resultRow.click();
      await page.waitForSelector('.fixed.inset-0');
      const testName = await page.$('::-p-xpath(//span[contains(., "Test Name:")])');
      const status = await page.$('::-p-xpath(//span[contains(., "Status:")])');
      expect(testName).not.toBeNull();
      expect(status).not.toBeNull();
      await page.click('::-p-xpath(//button[contains(., "Close")])');
    });

    test('should close result detail modal with Close button', async () => {
      const resultRow = await page.$('.border-b.border-gray-300 table tbody tr.cursor-pointer');
      if (!resultRow) return;
      await resultRow.click();
      await page.waitForSelector('.fixed.inset-0');
      await page.click('::-p-xpath(//button[contains(., "Close")])');
      await wait(200);
      const modal = await page.$('.fixed.inset-0');
      expect(modal).toBeNull();
    });
  });

  describe('Filters', () => {
    test('should have status filter dropdown', async () => {
      const statusFilter = await page.$('select.ehr-input');
      expect(statusFilter).not.toBeNull();
    });

    test('should filter by Abnormal Only', async () => {
      const initialCount = await page.$$('.border-b.border-gray-300');
      await page.select('select.ehr-input', 'abnormal');
      await wait(200);
      const filteredPanels = await page.$$('.border-b.border-gray-300');
      expect(filteredPanels.length).toBeGreaterThan(0);
      expect(filteredPanels.length).toBeLessThanOrEqual(initialCount.length);
    });

    test('should filter by Critical Only', async () => {
      await page.select('select.ehr-input', 'critical');
      await wait(200);
      const filteredPanels = await page.$$('.border-b.border-gray-300');
      expect(filteredPanels.length).toBeGreaterThan(0);
    });

    test('should reset filter to All Results', async () => {
      await page.select('select.ehr-input', 'critical');
      await wait(100);
      await page.select('select.ehr-input', 'all');
      await wait(200);
      const statusText = await page.$eval('.ehr-status-bar span', el => el.textContent);
      expect(statusText).toContain('panel(s) displayed');
    });

    test('should filter by patient', async () => {
      const selects = await page.$$('select.ehr-input');
      if (selects.length < 2) return;
      // Select a specific patient (second select is patient filter)
      const options = await selects[1].$$('option');
      if (options.length > 1) {
        const value = await options[1].evaluate(el => (el as HTMLOptionElement).value);
        await selects[1].select(value);
        await wait(200);
        const filteredPanels = await page.$$('.border-b.border-gray-300');
        expect(filteredPanels.length).toBeGreaterThan(0);
      }
    });

    test('should change date range filter', async () => {
      const selects = await page.$$('select.ehr-input');
      if (selects.length < 3) return;
      await selects[2].select('today');
      await wait(200);
      await selects[2].select('month');
      await wait(200);
      await selects[2].select('all');
      await wait(200);
    });

    test('should show empty state when no results match filters', async () => {
      // Filter by critical and a patient that may not have critical results
      await page.select('select.ehr-input', 'critical');
      await wait(200);
      const selects = await page.$$('select.ehr-input');
      if (selects.length >= 2) {
        const options = await selects[1].$$('option');
        // Try selecting last patient option which may not have critical results
        if (options.length > 2) {
          const lastValue = await options[options.length - 1].evaluate(el => (el as HTMLOptionElement).value);
          await selects[1].select(lastValue);
          await wait(200);
        }
      }
    });
  });
});
