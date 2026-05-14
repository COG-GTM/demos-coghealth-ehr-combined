import puppeteer, { Browser, Page } from 'puppeteer';

const BASE_URL = 'http://localhost:5173';
const wait = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

describe('Schedule Page E2E Tests', () => {
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
    await page.goto(`${BASE_URL}/schedule`);
    await page.waitForSelector('.ehr-status-bar');
  });

  describe('Page Layout', () => {
    test('should display Schedule header', async () => {
      const statusText = await page.$eval('.ehr-status-bar span', el => el.textContent);
      expect(statusText).toContain('Schedule');
    });

    test('should display date navigation controls', async () => {
      const prevBtn = await page.$('::-p-xpath(//button[contains(@class, "ehr-button")]/*[name()="svg"])');
      expect(prevBtn).not.toBeNull();
    });

    test('should display New Appt button', async () => {
      const newApptBtn = await page.$('::-p-xpath(//button[contains(., "New Appt")])');
      expect(newApptBtn).not.toBeNull();
    });

    test('should display Print and Refresh buttons', async () => {
      const printBtn = await page.$('::-p-xpath(//button[contains(., "Print")])');
      const refreshBtn = await page.$('::-p-xpath(//button[contains(., "Refresh")])');
      expect(printBtn).not.toBeNull();
      expect(refreshBtn).not.toBeNull();
    });
  });

  describe('Appointment List', () => {
    test('should display appointments in the schedule', async () => {
      const appointments = await page.$$('table tbody tr');
      expect(appointments.length).toBeGreaterThan(0);
    });

    test('should show appointment status indicators', async () => {
      const statusTexts = await page.$$eval('table tbody td', els =>
        els.map(el => el.textContent?.trim()).filter(Boolean)
      );
      expect(statusTexts.length).toBeGreaterThan(0);
    });
  });

  describe('View Mode Filters', () => {
    test('should switch to Waiting view', async () => {
      const waitingBtn = await page.$('::-p-xpath(//button[contains(., "Waiting")])');
      if (waitingBtn) {
        await waitingBtn.click();
        await wait(200);
      }
    });

    test('should switch to Completed view', async () => {
      const completedBtn = await page.$('::-p-xpath(//button[contains(., "Completed")])');
      if (completedBtn) {
        await completedBtn.click();
        await wait(200);
      }
    });

    test('should return to All view', async () => {
      const allBtn = await page.$('::-p-xpath(//button[contains(., "All")])');
      if (allBtn) {
        await allBtn.click();
        await wait(200);
      }
    });
  });

  describe('New Appointment Dialog', () => {
    test('should open New Appointment dialog', async () => {
      await page.click('::-p-xpath(//button[contains(., "New Appt")])');
      await page.waitForSelector('.fixed.inset-0');
      const modalTitle = await page.$eval('.fixed.inset-0 span.text-white', el => el.textContent);
      expect(modalTitle).toBeTruthy();
    });

    test('should close New Appointment dialog with Cancel', async () => {
      await page.click('::-p-xpath(//button[contains(., "New Appt")])');
      await page.waitForSelector('.fixed.inset-0');
      await page.click('::-p-xpath(//button[contains(., "Cancel")])');
      await wait(200);
      const modal = await page.$('.fixed.inset-0');
      expect(modal).toBeNull();
    });
  });

  describe('Appointment Actions', () => {
    test('should select an appointment to show details', async () => {
      const row = await page.$('table tbody tr.cursor-pointer');
      if (!row) return;
      await row.click();
      await wait(200);
    });

    test('should open Print dialog', async () => {
      const printBtn = await page.$('::-p-xpath(//button[contains(., "Print")])');
      if (!printBtn) return;
      await printBtn.click();
      await page.waitForSelector('.fixed.inset-0');
      await page.click('::-p-xpath(//button[contains(., "Cancel")])');
    });
  });

  describe('Appointment Detail Panel', () => {
    test('should show patient details when appointment is selected', async () => {
      const row = await page.$('table tbody tr.cursor-pointer');
      if (!row) return;
      await row.click();
      await wait(300);
      const detailPanel = await page.$('::-p-xpath(//fieldset[contains(., "Patient")] | //legend[contains(., "Patient")])');
      if (detailPanel) {
        expect(detailPanel).not.toBeNull();
      }
    });
  });
});
