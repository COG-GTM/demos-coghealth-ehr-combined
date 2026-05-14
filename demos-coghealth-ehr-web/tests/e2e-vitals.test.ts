import puppeteer, { Browser, Page } from 'puppeteer';

const BASE_URL = 'http://localhost:5173';
const wait = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

describe('Vitals Page E2E Tests', () => {
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
    await page.goto(`${BASE_URL}/vitals`);
    await page.waitForSelector('.ehr-status-bar');
  });

  describe('Page Layout', () => {
    test('should display the Vital Signs Flowsheet header', async () => {
      const headerText = await page.$eval('.ehr-panel > .ehr-header', el => el.textContent);
      expect(headerText).toContain('Vital Signs Flowsheet');
    });

    test('should display patient info in header', async () => {
      const headerText = await page.$eval('.ehr-panel > .ehr-header', el => el.textContent);
      expect(headerText).toContain('Smith, John');
      expect(headerText).toContain('MRN001234');
    });

    test('should display Record Vitals, Print, and Refresh buttons', async () => {
      const recordBtn = await page.$('::-p-xpath(//button[contains(., "Record Vitals")])');
      const printBtn = await page.$('::-p-xpath(//button[contains(., "Print")])');
      const refreshBtn = await page.$('::-p-xpath(//button[contains(., "Refresh")])');
      expect(recordBtn).not.toBeNull();
      expect(printBtn).not.toBeNull();
      expect(refreshBtn).not.toBeNull();
    });

    test('should display status bar with reading count', async () => {
      const statusText = await page.$eval('.ehr-status-bar span', el => el.textContent);
      expect(statusText).toContain('readings displayed');
    });

    test('should display legend (Normal, Abnormal, Critical)', async () => {
      const normalLegend = await page.$('::-p-xpath(//span[contains(., "Normal")])');
      const abnormalLegend = await page.$('::-p-xpath(//span[contains(., "Abnormal")])');
      const criticalLegend = await page.$('::-p-xpath(//span[contains(., "Critical")])');
      expect(normalLegend).not.toBeNull();
      expect(abnormalLegend).not.toBeNull();
      expect(criticalLegend).not.toBeNull();
    });
  });

  describe('Vitals Flowsheet Table', () => {
    test('should display vital sign names as rows', async () => {
      const vitalNames = await page.$$eval(
        'table tbody td.font-semibold',
        els => els.map(el => el.textContent?.trim())
      );
      const expectedVitals = ['BP Systolic', 'BP Diastolic', 'Heart Rate', 'Temperature', 'Resp Rate', 'SpO2', 'Weight', 'Pain Level'];
      for (const name of expectedVitals) {
        expect(vitalNames.some(v => v?.includes(name))).toBe(true);
      }
    });

    test('should display reference ranges for each vital', async () => {
      const ranges = await page.$$eval(
        'table tbody td .text-\\[9px\\].text-gray-500',
        els => els.map(el => el.textContent?.trim())
      );
      expect(ranges.length).toBeGreaterThan(0);
    });

    test('should display multiple reading columns', async () => {
      const timeHeaders = await page.$$('table thead th');
      // At least: Vital Sign col + Trend col + reading columns
      expect(timeHeaders.length).toBeGreaterThan(3);
    });

    test('should display Recorded By row', async () => {
      const recordedByRow = await page.$('::-p-xpath(//td[contains(., "Recorded By")])');
      expect(recordedByRow).not.toBeNull();
    });

    test('should display Location row', async () => {
      const locationRow = await page.$('::-p-xpath(//td[contains(., "Location")])');
      expect(locationRow).not.toBeNull();
    });

    test('should display sparkline trend charts', async () => {
      const sparklines = await page.$$('table tbody svg');
      expect(sparklines.length).toBeGreaterThan(0);
    });

    test('should show critical values with alert icon', async () => {
      const criticalAlerts = await page.$$('table tbody td span.font-mono svg');
      expect(criticalAlerts.length).toBeGreaterThan(0);
    });
  });

  describe('Vital Reading Detail Modal', () => {
    test('should open detail modal when clicking a vital cell', async () => {
      const vitalCell = await page.$('table tbody td.cursor-pointer');
      if (!vitalCell) return;
      await vitalCell.click();
      await page.waitForSelector('.fixed.inset-0');
      const modalTitle = await page.$eval('.fixed.inset-0 span.text-white', el => el.textContent);
      expect(modalTitle).toContain('Vital Signs Detail');
    });

    test('should display reading information in detail modal', async () => {
      const vitalCell = await page.$('table tbody td.cursor-pointer');
      if (!vitalCell) return;
      await vitalCell.click();
      await page.waitForSelector('.fixed.inset-0');
      const dateTimeLabel = await page.$('::-p-xpath(//span[contains(., "Date/Time:")])');
      const recordedByLabel = await page.$('::-p-xpath(//span[contains(., "Recorded By:")])');
      const locationLabel = await page.$('::-p-xpath(//span[contains(., "Location:")])');
      expect(dateTimeLabel).not.toBeNull();
      expect(recordedByLabel).not.toBeNull();
      expect(locationLabel).not.toBeNull();
      await page.click('::-p-xpath(//button[contains(., "Close")])');
    });

    test('should display all vital signs in detail modal', async () => {
      const vitalCell = await page.$('table tbody td.cursor-pointer');
      if (!vitalCell) return;
      await vitalCell.click();
      await page.waitForSelector('.fixed.inset-0');

      const vitalsFieldset = await page.$('::-p-xpath(//legend[contains(., "Vital Signs")])');
      expect(vitalsFieldset).not.toBeNull();

      await page.click('::-p-xpath(//button[contains(., "Close")])');
    });

    test('should close detail modal with Close button', async () => {
      const vitalCell = await page.$('table tbody td.cursor-pointer');
      if (!vitalCell) return;
      await vitalCell.click();
      await page.waitForSelector('.fixed.inset-0');
      await page.click('::-p-xpath(//button[contains(., "Close")])');
      await wait(200);
      const modal = await page.$('.fixed.inset-0');
      expect(modal).toBeNull();
    });
  });

  describe('Record Vitals Dialog', () => {
    test('should open Record Vitals dialog', async () => {
      await page.click('::-p-xpath(//button[contains(., "Record Vitals")])');
      await page.waitForSelector('.fixed.inset-0');
      const modalTitle = await page.$eval('.fixed.inset-0 span.text-white', el => el.textContent);
      expect(modalTitle).toContain('Record Vital Signs');
    });

    test('should display patient info in Record Vitals dialog', async () => {
      await page.click('::-p-xpath(//button[contains(., "Record Vitals")])');
      await page.waitForSelector('.fixed.inset-0');
      const patientInfo = await page.$('::-p-xpath(//fieldset[contains(., "Patient")]//span[contains(., "Smith, John")])');
      expect(patientInfo).not.toBeNull();
      await page.click('::-p-xpath(//button[contains(., "Cancel")])');
    });

    test('should show input fields for all vital signs', async () => {
      await page.click('::-p-xpath(//button[contains(., "Record Vitals")])');
      await page.waitForSelector('.fixed.inset-0');
      const inputs = await page.$$('.fixed.inset-0 input[type="number"]');
      expect(inputs.length).toBeGreaterThanOrEqual(8);
      await page.click('::-p-xpath(//button[contains(., "Cancel")])');
    });

    test('should show Notes textarea', async () => {
      await page.click('::-p-xpath(//button[contains(., "Record Vitals")])');
      await page.waitForSelector('.fixed.inset-0');
      const textarea = await page.$('.fixed.inset-0 textarea');
      expect(textarea).not.toBeNull();
      await page.click('::-p-xpath(//button[contains(., "Cancel")])');
    });

    test('should have Save and Cancel buttons', async () => {
      await page.click('::-p-xpath(//button[contains(., "Record Vitals")])');
      await page.waitForSelector('.fixed.inset-0');
      const saveBtn = await page.$('::-p-xpath(//button[contains(., "Save")])');
      const cancelBtn = await page.$('::-p-xpath(//button[contains(., "Cancel")])');
      expect(saveBtn).not.toBeNull();
      expect(cancelBtn).not.toBeNull();
      await page.click('::-p-xpath(//button[contains(., "Cancel")])');
    });

    test('should close Record Vitals dialog with Cancel', async () => {
      await page.click('::-p-xpath(//button[contains(., "Record Vitals")])');
      await page.waitForSelector('.fixed.inset-0');
      await page.click('::-p-xpath(//button[contains(., "Cancel")])');
      await wait(200);
      const modal = await page.$('.fixed.inset-0');
      expect(modal).toBeNull();
    });

    test('should close Record Vitals dialog with Save', async () => {
      await page.click('::-p-xpath(//button[contains(., "Record Vitals")])');
      await page.waitForSelector('.fixed.inset-0');
      await page.click('::-p-xpath(//button[contains(., "Save")])');
      await wait(200);
      const modal = await page.$('.fixed.inset-0');
      expect(modal).toBeNull();
    });
  });

  describe('Time Range Filter', () => {
    test('should have time range dropdown', async () => {
      const timeRange = await page.$('select.ehr-input');
      expect(timeRange).not.toBeNull();
    });

    test('should change time range to Last 24 Hours', async () => {
      await page.select('select.ehr-input', '24h');
      await wait(200);
    });

    test('should change time range to Last 7 Days', async () => {
      await page.select('select.ehr-input', '7d');
      await wait(200);
    });

    test('should change time range to Last 30 Days', async () => {
      await page.select('select.ehr-input', '30d');
      await wait(200);
    });
  });
});
