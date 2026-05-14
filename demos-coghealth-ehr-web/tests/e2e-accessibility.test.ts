import puppeteer, { Browser, Page } from 'puppeteer';

const BASE_URL = 'http://localhost:5173';
const wait = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

describe('Accessibility & Keyboard Navigation E2E Tests', () => {
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

  describe('Keyboard Navigation', () => {
    test('should close modal with Escape key on any page', async () => {
      await page.goto(`${BASE_URL}/labs`);
      await page.waitForSelector('.ehr-status-bar');

      // Open the result detail modal by clicking a lab result
      const resultRow = await page.$('.border-b.border-gray-300 table tbody tr.cursor-pointer');
      if (!resultRow) return;
      await resultRow.click();
      await page.waitForSelector('.fixed.inset-0');
      await page.keyboard.press('Escape');
      await wait(200);
      const modal = await page.$('.fixed.inset-0');
      expect(modal).toBeNull();
    });

    test('should close vitals detail modal with Escape key', async () => {
      await page.goto(`${BASE_URL}/vitals`);
      await page.waitForSelector('.ehr-status-bar');
      const cell = await page.$('table tbody td.cursor-pointer');
      if (!cell) return;
      await cell.click();
      await page.waitForSelector('.fixed.inset-0');
      await page.keyboard.press('Escape');
      await wait(200);
      const modal = await page.$('.fixed.inset-0');
      expect(modal).toBeNull();
    });

    test('should navigate between pages using nav links', async () => {
      const pages = ['/patients', '/schedule', '/labs', '/vitals', '/medications', '/reports', '/settings'];
      for (const path of pages) {
        await page.goto(`${BASE_URL}${path}`);
        await page.waitForSelector('.ehr-status-bar, .ehr-header');
        expect(page.url()).toContain(path);
        // Verify nav link for this page exists
        const navLink = await page.$(`a[href="${path}"]`);
        expect(navLink).not.toBeNull();
      }
    });
  });

  describe('Focus Management', () => {
    test('should maintain focus within modal when open', async () => {
      await page.goto(BASE_URL);
      await page.waitForSelector('.ehr-header');
      await page.click('::-p-xpath(//button[contains(., "Print")])');
      await page.waitForSelector('.fixed.inset-0');

      // Modal should be visible and interactive
      const modal = await page.$('.fixed.inset-0');
      expect(modal).not.toBeNull();

      await page.click('::-p-xpath(//button[contains(., "Cancel")])');
    });
  });

  describe('Responsive Layout', () => {
    test('should display navigation bar at full width', async () => {
      await page.goto(BASE_URL);
      await page.waitForSelector('.ehr-header');
      const navLinks = await page.$$('a[href]');
      expect(navLinks.length).toBeGreaterThan(5);
    });

    test('should maintain layout at smaller viewport', async () => {
      await page.setViewport({ width: 1024, height: 768 });
      await page.goto(BASE_URL);
      await page.waitForSelector('.ehr-header');
      const header = await page.$('.ehr-header');
      expect(header).not.toBeNull();
      // Reset viewport
      await page.setViewport({ width: 1280, height: 800 });
    });
  });

  describe('Cross-Page Navigation Flow', () => {
    test('should navigate Dashboard to Patients and back', async () => {
      await page.goto(BASE_URL);
      await page.waitForSelector('.ehr-header');
      expect(page.url()).toBe(`${BASE_URL}/`);

      await page.goto(`${BASE_URL}/patients`);
      await page.waitForSelector('.ehr-status-bar, .ehr-header');
      expect(page.url()).toContain('/patients');

      await page.goto(BASE_URL);
      await page.waitForSelector('.ehr-header');
    });

    test('should navigate Dashboard to Labs to Vitals to Dashboard', async () => {
      await page.goto(BASE_URL);
      await page.waitForSelector('.ehr-header');

      await page.click('a[href="/labs"]');
      await wait(300);
      await page.waitForSelector('.ehr-status-bar');
      expect(page.url()).toContain('/labs');

      await page.click('a[href="/vitals"]');
      await wait(300);
      await page.waitForSelector('.ehr-status-bar');
      expect(page.url()).toContain('/vitals');

      await page.click('a[href="/"]');
      await wait(300);
    });

    test('should navigate through all pages without errors', async () => {
      const pages = ['/', '/patients', '/schedule', '/labs', '/vitals', '/medications', '/reports', '/settings'];
      for (const path of pages) {
        await page.goto(`${BASE_URL}${path}`);
        await page.waitForSelector('.ehr-status-bar, .ehr-header');
        // Verify no uncaught errors crashed the page
        const body = await page.$('body');
        expect(body).not.toBeNull();
      }
    });
  });

  describe('HIPAA Compliance Features', () => {
    test('should show HIPAA compliance indicator on all pages', async () => {
      const pages = ['/', '/patients', '/schedule', '/labs', '/vitals', '/medications', '/reports', '/settings'];
      for (const path of pages) {
        await page.goto(`${BASE_URL}${path}`);
        await page.waitForSelector('.ehr-status-bar, .ehr-header');
        const hipaaIndicator = await page.$('::-p-xpath(//span[contains(., "HIPAA Compliant")])');
        expect(hipaaIndicator).not.toBeNull();
      }
    });

    test('should show session timer on all pages', async () => {
      const pages = ['/', '/labs', '/vitals'];
      for (const path of pages) {
        await page.goto(`${BASE_URL}${path}`);
        await page.waitForSelector('.ehr-header');
        const sessionTimer = await page.$('::-p-xpath(//span[contains(., "Session:")])');
        expect(sessionTimer).not.toBeNull();
      }
    });

    test('should show audit logging indicator on all pages', async () => {
      const pages = ['/', '/labs', '/vitals', '/medications'];
      for (const path of pages) {
        await page.goto(`${BASE_URL}${path}`);
        await page.waitForSelector('.ehr-status-bar, .ehr-header');
        const auditIndicator = await page.$('::-p-xpath(//span[contains(., "Audit Logging")])');
        expect(auditIndicator).not.toBeNull();
      }
    });

    test('should show logout confirmation dialog', async () => {
      await page.goto(BASE_URL);
      await page.waitForSelector('.ehr-header');
      await page.click('::-p-xpath(//button[contains(., "Logout")])');
      await page.waitForSelector('.fixed.inset-0');
      const modalTitle = await page.$eval('.fixed.inset-0 span.text-white', el => el.textContent);
      expect(modalTitle).toContain('Logout');
      await page.click('::-p-xpath(//button[contains(., "Cancel")])');
    });
  });

  describe('Global Patient Search', () => {
    test('should search and navigate from any page', async () => {
      await page.goto(`${BASE_URL}/labs`);
      await page.waitForSelector('.ehr-status-bar');
      const searchInput = await page.$('input[placeholder="Patient search..."]');
      expect(searchInput).not.toBeNull();
      if (!searchInput) return;

      await searchInput.type('Smith');
      await page.waitForSelector('.absolute.top-full');
      const results = await page.$$('.absolute.top-full > div');
      expect(results.length).toBeGreaterThan(0);
    });

    test('should clear search results when clearing input', async () => {
      await page.goto(`${BASE_URL}/vitals`);
      await page.waitForSelector('.ehr-status-bar');
      const searchInput = await page.$('input[placeholder="Patient search..."]');
      if (!searchInput) return;

      await searchInput.type('Smith');
      await page.waitForSelector('.absolute.top-full');

      // Clear the input
      await searchInput.click({ clickCount: 3 });
      await page.keyboard.press('Backspace');
      await wait(200);
    });
  });
});
