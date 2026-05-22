import puppeteer, { Browser, Page } from 'puppeteer';

const BASE_URL = 'http://localhost:5173';
const wait = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

describe('Dashboard Component Rendering Tests', () => {
  let browser: Browser;
  let page: Page;

  beforeAll(async () => {
    browser = await puppeteer.launch({ headless: true });
    page = await browser.newPage();
    await page.setViewport({ width: 1280, height: 800 });
  });

  afterAll(async () => {
    await browser.close();
  });

  beforeEach(async () => {
    await page.goto(BASE_URL);
    // Wait for dashboard to load
    await page.waitForSelector('.ehr-toolbar', { timeout: 10000 });
  });

  describe('Critical Alerts Banner', () => {
    test('should render critical alerts banner when alerts exist', async () => {
      // Wait for potential alerts to load
      await wait(500);
      
      const criticalAlertBanner = await page.$('.ehr-alert-critical');
      expect(criticalAlertBanner).not.toBeNull();
    });

    test('should display alert count in banner', async () => {
      await wait(500);
      
      const alertCountText = await page.$eval('.ehr-alert-critical .font-semibold', el => el.textContent);
      expect(alertCountText).toMatch(/CRITICAL ALERTS \(\d+\)/);
    });

    test('should display shield alert icon', async () => {
      await wait(500);
      
      const shieldIcon = await page.$('.ehr-alert-critical svg');
      expect(shieldIcon).not.toBeNull();
    });

    test('should display patient alert details', async () => {
      await wait(500);
      
      const alertDetails = await page.$$('.ehr-alert-critical .text-\\[11px\\]');
      expect(alertDetails.length).toBeGreaterThan(0);
    });

    test('should have Review All button', async () => {
      await wait(500);
      
      const reviewButton = await page.$('.ehr-alert-critical button');
      expect(reviewButton).not.toBeNull();
      const buttonText = await reviewButton?.evaluate(el => el.textContent);
      expect(buttonText).toContain('Review All');
    });
  });

  describe('Inbox Panel', () => {
    test('should render inbox panel header', async () => {
      const inboxHeaders = await page.$$('.ehr-header');
      let inboxHeader = null;
      
      for (const header of inboxHeaders) {
        const text = await header.evaluate(el => el.textContent);
        if (text?.includes('Inbox')) {
          inboxHeader = header;
          break;
        }
      }
      
      expect(inboxHeader).not.toBeNull();
    });

    test('should display unread count badge', async () => {
      const inboxHeaders = await page.$$('.ehr-header');
      let inboxHeader = null;
      
      for (const header of inboxHeaders) {
        const text = await header.evaluate(el => el.textContent);
        if (text?.includes('Inbox')) {
          inboxHeader = header;
          break;
        }
      }
      
      // Look for badge by checking if header contains "unread" text
      const headerText = await inboxHeader?.evaluate(el => el.textContent);
      expect(headerText).toMatch(/unread/i);
    });

    test('should render inbox tab buttons', async () => {
      const tabs = await page.$$('.ehr-tab');
      expect(tabs.length).toBeGreaterThan(0);
    });

    test('should have All, Results, Messages, Rx Refills, Orders, and Co-sign tabs', async () => {
      const tabLabels = await page.$$eval('.ehr-tab', els => els.map(el => el.textContent));
      expect(tabLabels).toContain('All');
      expect(tabLabels.some(t => t?.includes('Results'))).toBe(true);
      expect(tabLabels.some(t => t?.includes('Messages'))).toBe(true);
      expect(tabLabels.some(t => t?.includes('Rx Refills'))).toBe(true);
      expect(tabLabels.some(t => t?.includes('Orders'))).toBe(true);
      expect(tabLabels.some(t => t?.includes('Co-sign'))).toBe(true);
    });

    test('should render inbox filter dropdowns', async () => {
      // Look for select elements within the inbox subheader
      const inboxSubheader = await page.$('.ehr-subheader');
      const selects = await inboxSubheader?.$$('select');
      
      expect(selects && selects.length > 0).toBe(true);
    });

    test('should render inbox table with headers', async () => {
      const tableHeaders = await page.$$('table thead th');
      expect(tableHeaders.length).toBeGreaterThan(0);
    });

    test('should render inbox items in table body', async () => {
      const tableRows = await page.$$('table tbody tr');
      expect(tableRows.length).toBeGreaterThan(0);
    });

    test('should render Mark All Read button', async () => {
      const markAllReadBtn = await page.$('::-p-xpath(//button[contains(., "Mark All Read")])');
      expect(markAllReadBtn).not.toBeNull();
    });

    test('should render refresh button in inbox', async () => {
      const refreshBtn = await page.$('.ehr-subheader button svg');
      expect(refreshBtn).not.toBeNull();
    });
  });

  describe('Worklist Panel', () => {
    test('should render worklist panel header', async () => {
      const worklistHeaders = await page.$$('.ehr-header');
      const worklistHeader = worklistHeaders.find(async (h) => {
        const text = await h.evaluate(el => el.textContent);
        return text?.includes('Patient Worklist');
      });
      
      expect(worklistHeader).not.toBeNull();
    });

    test('should display patient count in worklist header', async () => {
      const worklistHeaders = await page.$$('.ehr-header');
      for (const header of worklistHeaders) {
        const text = await header.evaluate(el => el.textContent);
        if (text?.includes('Patient Worklist')) {
          expect(text).toMatch(/\d+ patients/);
          break;
        }
      }
    });

    test('should render worklist filter tabs', async () => {
      const filterTabs = await page.$$eval('.ehr-tab', els => 
        els.map(el => el.textContent).filter(t => 
          t === 'All' || t === 'Inpatient' || t === 'Clinic' || t === 'Critical'
        )
      );
      
      expect(filterTabs.length).toBeGreaterThan(0);
    });

    test('should have All, Inpatient, Clinic, and Critical filters', async () => {
      const filterLabels = await page.$$eval('.ehr-tab', els => els.map(el => el.textContent));
      expect(filterLabels).toContain('All');
      expect(filterLabels).toContain('Inpatient');
      expect(filterLabels).toContain('Clinic');
      expect(filterLabels).toContain('Critical');
    });

    test('should render worklist sort dropdown', async () => {
      const sortSelect = await page.$('select:has(option[value="status"])');
      expect(sortSelect).not.toBeNull();
    });

    test('should render worklist table with headers', async () => {
      const worklistTables = await page.$$('table');
      expect(worklistTables.length).toBeGreaterThan(0);
      
      const headers = await worklistTables[0].$$eval('thead th', els => 
        els.map(el => el.textContent)
      );
      
      expect(headers.length).toBeGreaterThan(0);
    });

    test('should render patient rows in worklist', async () => {
      const tableRows = await page.$$('table tbody tr');
      expect(tableRows.length).toBeGreaterThan(0);
    });

    test('should render Print List button in worklist', async () => {
      const printListBtn = await page.$('::-p-xpath(//button[contains(., "Print List")])');
      expect(printListBtn).not.toBeNull();
    });

    test('should render sort toggle button', async () => {
      const sortToggleBtn = await page.$('::-p-xpath(//button[contains(text(), "↑") or contains(text(), "↓")])');
      expect(sortToggleBtn).not.toBeNull();
    });
  });

  describe('Toolbar Actions', () => {
    test('should render toolbar container', async () => {
      const toolbar = await page.$('.ehr-toolbar');
      expect(toolbar).not.toBeNull();
    });

    test('should render Refresh button', async () => {
      const refreshBtn = await page.$('::-p-xpath(//button[contains(., "Refresh")])');
      expect(refreshBtn).not.toBeNull();
    });

    test('should render e-Prescribe button', async () => {
      const prescribeBtn = await page.$('::-p-xpath(//button[contains(., "e-Prescribe")])');
      expect(prescribeBtn).not.toBeNull();
    });

    test('should render Order Labs button', async () => {
      const orderLabsBtn = await page.$('::-p-xpath(//button[contains(., "Order Labs")])');
      expect(orderLabsBtn).not.toBeNull();
    });

    test('should render Order Imaging button', async () => {
      const orderImagingBtn = await page.$('::-p-xpath(//button[contains(., "Order Imaging")])');
      expect(orderImagingBtn).not.toBeNull();
    });

    test('should render New Note button', async () => {
      const newNoteBtn = await page.$('::-p-xpath(//button[contains(., "New Note")])');
      expect(newNoteBtn).not.toBeNull();
    });

    test('should render Referral button', async () => {
      const referralBtn = await page.$('::-p-xpath(//button[contains(., "Referral")])');
      expect(referralBtn).not.toBeNull();
    });

    test('should render Print button', async () => {
      const printBtn = await page.$('::-p-xpath(//button[contains(., "Print")])');
      expect(printBtn).not.toBeNull();
    });

    test('should render notification bell with badge', async () => {
      const notificationBell = await page.$('.ehr-toolbar button:has(svg)');
      expect(notificationBell).not.toBeNull();
      
      const badge = await page.$('.ehr-toolbar button .bg-gray-600');
      expect(badge).not.toBeNull();
    });

    test('should have icons on toolbar buttons', async () => {
      const toolbarButtons = await page.$$('.ehr-toolbar-button');
      expect(toolbarButtons.length).toBeGreaterThan(0);
      
      // Check that at least some buttons have SVG icons
      const buttonsWithIcons = await page.$$('.ehr-toolbar-button svg');
      expect(buttonsWithIcons.length).toBeGreaterThan(0);
    });
  });

  describe('Dashboard Layout', () => {
    test('should have proper flex layout structure', async () => {
      const mainContent = await page.$('.flex.flex-col');
      expect(mainContent).not.toBeNull();
    });

    test('should have left and right column layout', async () => {
      const leftColumn = await page.$('.flex-1.flex.flex-col');
      expect(leftColumn).not.toBeNull();
    });

    test('should have sidebar with fixed width', async () => {
      const sidebar = await page.$('.w-64');
      expect(sidebar).not.toBeNull();
    });
  });
});