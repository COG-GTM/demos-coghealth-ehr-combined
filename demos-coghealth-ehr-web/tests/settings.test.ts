import puppeteer, { Browser, Page } from 'puppeteer';

const BASE_URL = 'http://localhost:5173';
const SETTINGS_URL = `${BASE_URL}/settings`;
const wait = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

describe('Settings (Users) Page E2E Tests', () => {
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
    // Clear persisted settings so each test starts from defaults
    await page.goto(SETTINGS_URL);
    await page.evaluate(() => localStorage.removeItem('coghealth_settings'));
    await page.goto(SETTINGS_URL);
    await page.waitForSelector('.ehr-header');
  });

  // ---------------------------------------------------------------------------
  // Page Structure
  // ---------------------------------------------------------------------------
  describe('Page Structure', () => {
    test('should display the Settings page header', async () => {
      const headerText = await page.$eval('.ehr-header', el => el.textContent);
      expect(headerText).toContain('System Settings');
    });

    test('should display Save Changes button in header', async () => {
      const saveBtn = await page.$('::-p-xpath(//button[contains(., "Save Changes")])');
      expect(saveBtn).not.toBeNull();
    });

    test('should display all five navigation tabs', async () => {
      const tabLabels = await page.$$eval(
        'button.w-full.flex.items-center',
        els => els.map(el => el.textContent?.trim())
      );
      expect(tabLabels.some(t => t?.includes('Profile'))).toBe(true);
      expect(tabLabels.some(t => t?.includes('Notifications'))).toBe(true);
      expect(tabLabels.some(t => t?.includes('Security'))).toBe(true);
      expect(tabLabels.some(t => t?.includes('Appearance'))).toBe(true);
      expect(tabLabels.some(t => t?.includes('Practice'))).toBe(true);
    });

    test('should display status bar with Settings label', async () => {
      const statusText = await page.$eval('.ehr-status-bar', el => el.textContent);
      expect(statusText).toContain('Settings');
    });

    test('status bar should show current user name', async () => {
      const statusText = await page.$eval('.ehr-status-bar', el => el.textContent);
      expect(statusText).toContain('Dr. Sarah Anderson');
    });

    test('status bar should reflect active tab name', async () => {
      const statusText = await page.$eval('.ehr-status-bar span:first-child', el => el.textContent);
      expect(statusText).toContain('Profile');
    });

    test('status bar should update when tab changes', async () => {
      await page.click('::-p-xpath(//button[contains(., "Notifications")])');
      await wait(100);
      const statusText = await page.$eval('.ehr-status-bar span:first-child', el => el.textContent);
      expect(statusText).toContain('Notifications');
    });

    test('Profile tab should be active by default', async () => {
      const profileFieldset = await page.$('::-p-xpath(//fieldset[.//legend[contains(., "User Profile")]])');
      expect(profileFieldset).not.toBeNull();
    });
  });

  // ---------------------------------------------------------------------------
  // Tab Navigation
  // ---------------------------------------------------------------------------
  describe('Tab Navigation', () => {
    test('clicking Notifications tab should show notification channels', async () => {
      await page.click('::-p-xpath(//button[contains(., "Notifications")])');
      await wait(100);
      const channelsHeader = await page.$('::-p-xpath(//*[contains(., "Notification Channels")])');
      expect(channelsHeader).not.toBeNull();
    });

    test('clicking Security tab should show security settings', async () => {
      await page.click('::-p-xpath(//button[contains(., "Security")])');
      await wait(100);
      const securityHeader = await page.$('::-p-xpath(//*[contains(., "Security Settings")])');
      expect(securityHeader).not.toBeNull();
    });

    test('clicking Appearance tab should show theme options', async () => {
      await page.click('::-p-xpath(//button[contains(., "Appearance")])');
      await wait(100);
      const themeFieldset = await page.$('::-p-xpath(//fieldset[.//legend[contains(., "Theme")]])');
      expect(themeFieldset).not.toBeNull();
    });

    test('clicking Practice tab should show practice information', async () => {
      await page.click('::-p-xpath(//button[contains(., "Practice")])');
      await wait(100);
      const practiceFieldset = await page.$('::-p-xpath(//fieldset[.//legend[contains(., "Practice Information")]])');
      expect(practiceFieldset).not.toBeNull();
    });

    test('clicking Profile tab should return to profile view', async () => {
      await page.click('::-p-xpath(//button[contains(., "Security")])');
      await wait(100);
      await page.click('::-p-xpath(//button[contains(., "Profile")])');
      await wait(100);
      const profileFieldset = await page.$('::-p-xpath(//fieldset[.//legend[contains(., "User Profile")]])');
      expect(profileFieldset).not.toBeNull();
    });

    test('active tab should have distinct styling', async () => {
      const activeTabClass = await page.$eval(
        '::-p-xpath(//button[contains(., "Profile") and contains(@class, "font-semibold")])',
        el => el.className
      );
      expect(activeTabClass).toContain('font-semibold');
    });
  });

  // ---------------------------------------------------------------------------
  // Profile Tab
  // ---------------------------------------------------------------------------
  describe('Profile Tab', () => {
    test('should display user avatar with correct initials', async () => {
      const avatarText = await page.$eval(
        '.w-14.h-14 span',
        el => el.textContent?.trim()
      );
      expect(avatarText).toBe('SA');
    });

    test('should display Change Photo button', async () => {
      const changePhotoBtn = await page.$('::-p-xpath(//button[contains(., "Change Photo")])');
      expect(changePhotoBtn).not.toBeNull();
    });

    test('Change Photo button should open an info dialog', async () => {
      await page.click('::-p-xpath(//button[contains(., "Change Photo")])');
      await page.waitForSelector('.fixed.inset-0');
      const dialogText = await page.$eval('.fixed.inset-0', el => el.textContent);
      expect(dialogText).toContain('Change Photo');
      await page.click('::-p-xpath(//button[contains(., "OK")])');
    });

    test('should display First Name field with default value "Sarah"', async () => {
      const value = await page.$eval('input[value="Sarah"]', el => (el as HTMLInputElement).value);
      expect(value).toBe('Sarah');
    });

    test('should display Last Name field with default value "Anderson"', async () => {
      const value = await page.$eval('input[value="Anderson"]', el => (el as HTMLInputElement).value);
      expect(value).toBe('Anderson');
    });

    test('should display Email field with default value', async () => {
      const value = await page.$eval(
        'input[type="email"]',
        el => (el as HTMLInputElement).value
      );
      expect(value).toBe('sarah.anderson@coghealth.com');
    });

    test('should display Phone field with default value', async () => {
      const value = await page.$eval(
        'input[type="tel"]',
        el => (el as HTMLInputElement).value
      );
      expect(value).toBe('(555) 123-4567');
    });

    test('should display NPI Number field with default value', async () => {
      const value = await page.$eval(
        'input.ehr-input.w-full.font-mono',
        el => (el as HTMLInputElement).value
      );
      expect(value).toBe('1234567890');
    });

    test('should display Specialty select with Internal Medicine selected', async () => {
      const value = await page.$eval(
        'select.ehr-input.w-full',
        el => (el as HTMLSelectElement).value
      );
      expect(value).toBe('Internal Medicine');
    });

    test('should allow editing the First Name field', async () => {
      const input = await page.$('input[value="Sarah"]');
      await input?.click({ clickCount: 3 });
      await input?.type('Jane');
      const value = await input?.evaluate(el => (el as HTMLInputElement).value);
      expect(value).toBe('Jane');
    });

    test('should allow editing the Last Name field', async () => {
      const input = await page.$('input[value="Anderson"]');
      await input?.click({ clickCount: 3 });
      await input?.type('Smith');
      const value = await input?.evaluate(el => (el as HTMLInputElement).value);
      expect(value).toBe('Smith');
    });

    test('should allow editing the Email field', async () => {
      const input = await page.$('input[type="email"]');
      await input?.click({ clickCount: 3 });
      await input?.type('new@coghealth.com');
      const value = await input?.evaluate(el => (el as HTMLInputElement).value);
      expect(value).toBe('new@coghealth.com');
    });

    test('should allow editing the NPI field', async () => {
      const input = await page.$('input.ehr-input.w-full.font-mono');
      await input?.click({ clickCount: 3 });
      await input?.type('9876543210');
      const value = await input?.evaluate(el => (el as HTMLInputElement).value);
      expect(value).toBe('9876543210');
    });

    test('should allow changing the Specialty', async () => {
      await page.select('select.ehr-input.w-full', 'Cardiology');
      const value = await page.$eval(
        'select.ehr-input.w-full',
        el => (el as HTMLSelectElement).value
      );
      expect(value).toBe('Cardiology');
    });

    test('Specialty select should have expected options', async () => {
      const options = await page.$$eval(
        'select.ehr-input.w-full option',
        els => els.map(el => el.textContent?.trim())
      );
      expect(options).toContain('Internal Medicine');
      expect(options).toContain('Family Medicine');
      expect(options).toContain('Cardiology');
      expect(options).toContain('Neurology');
    });
  });

  // ---------------------------------------------------------------------------
  // Notifications Tab
  // ---------------------------------------------------------------------------
  describe('Notifications Tab', () => {
    beforeEach(async () => {
      await page.click('::-p-xpath(//button[contains(., "Notifications")])');
      await wait(150);
    });

    test('should display Notification Channels section', async () => {
      const section = await page.$('::-p-xpath(//*[contains(., "Notification Channels")])');
      expect(section).not.toBeNull();
    });

    test('should display Alert Types section', async () => {
      const section = await page.$('::-p-xpath(//*[contains(., "Alert Types")])');
      expect(section).not.toBeNull();
    });

    test('Email Notifications checkbox should be checked by default', async () => {
      const checkboxes = await page.$$('input[type="checkbox"]');
      const emailChecked = await checkboxes[0]?.evaluate(el => (el as HTMLInputElement).checked);
      expect(emailChecked).toBe(true);
    });

    test('SMS Notifications checkbox should be unchecked by default', async () => {
      const checkboxes = await page.$$('input[type="checkbox"]');
      const smsChecked = await checkboxes[1]?.evaluate(el => (el as HTMLInputElement).checked);
      expect(smsChecked).toBe(false);
    });

    test('should display Email Notifications label', async () => {
      const label = await page.$('::-p-xpath(//*[contains(., "Email Notifications")])');
      expect(label).not.toBeNull();
    });

    test('should display SMS Notifications label', async () => {
      const label = await page.$('::-p-xpath(//*[contains(., "SMS Notifications")])');
      expect(label).not.toBeNull();
    });

    test('should allow toggling Email Notifications checkbox', async () => {
      const checkboxes = await page.$$('input[type="checkbox"]');
      const before = await checkboxes[0]?.evaluate(el => (el as HTMLInputElement).checked);
      await checkboxes[0]?.click();
      await wait(100);
      const after = await checkboxes[0]?.evaluate(el => (el as HTMLInputElement).checked);
      expect(after).toBe(!before);
    });

    test('should allow enabling SMS Notifications', async () => {
      const checkboxes = await page.$$('input[type="checkbox"]');
      await checkboxes[1]?.click();
      await wait(100);
      const checked = await checkboxes[1]?.evaluate(el => (el as HTMLInputElement).checked);
      expect(checked).toBe(true);
    });

    test('Lab Results alert type should be visible', async () => {
      const label = await page.$('::-p-xpath(//*[contains(., "Lab Results")])');
      expect(label).not.toBeNull();
    });

    test('Appointments alert type should be visible', async () => {
      const label = await page.$('::-p-xpath(//*[contains(., "Appointments")])');
      expect(label).not.toBeNull();
    });

    test('Messages alert type should be visible', async () => {
      const label = await page.$('::-p-xpath(//*[contains(., "Messages")])');
      expect(label).not.toBeNull();
    });

    test('System Updates alert type should be visible', async () => {
      const label = await page.$('::-p-xpath(//*[contains(., "System Updates")])');
      expect(label).not.toBeNull();
    });

    test('Notification Channels section should be collapsible', async () => {
      const collapseBtn = await page.$('::-p-xpath(//*[contains(., "Notification Channels")]//*[text()="-"])');
      expect(collapseBtn).not.toBeNull();
      await page.click('::-p-xpath(//*[contains(@class,"ehr-header")][contains(., "Notification Channels")])');
      await wait(100);
      const expandBtn = await page.$('::-p-xpath(//*[contains(., "Notification Channels")]//*[text()="+"])');
      expect(expandBtn).not.toBeNull();
    });

    test('Alert Types section should be collapsible', async () => {
      const collapseBtn = await page.$('::-p-xpath(//*[contains(., "Alert Types")]//*[text()="-"])');
      expect(collapseBtn).not.toBeNull();
      await page.click('::-p-xpath(//*[contains(@class,"ehr-header")][contains(., "Alert Types")])');
      await wait(100);
      const expandBtn = await page.$('::-p-xpath(//*[contains(., "Alert Types")]//*[text()="+"])');
      expect(expandBtn).not.toBeNull();
    });

    test('Notification Channels section should re-expand after collapsing', async () => {
      await page.click('::-p-xpath(//*[contains(@class,"ehr-header")][contains(., "Notification Channels")])');
      await wait(100);
      await page.click('::-p-xpath(//*[contains(@class,"ehr-header")][contains(., "Notification Channels")])');
      await wait(100);
      const emailLabel = await page.$('::-p-xpath(//*[contains(., "Email Notifications")])');
      expect(emailLabel).not.toBeNull();
    });
  });

  // ---------------------------------------------------------------------------
  // Security Tab
  // ---------------------------------------------------------------------------
  describe('Security Tab', () => {
    beforeEach(async () => {
      await page.click('::-p-xpath(//button[contains(., "Security")])');
      await wait(150);
    });

    test('should display Security Settings panel', async () => {
      const panel = await page.$('::-p-xpath(//*[contains(., "Security Settings")])');
      expect(panel).not.toBeNull();
    });

    test('should display Password row with Change button', async () => {
      const changeBtn = await page.$('::-p-xpath(//button[contains(., "Change")])');
      expect(changeBtn).not.toBeNull();
    });

    test('Change Password button should open an info dialog', async () => {
      await page.click('::-p-xpath(//button[contains(., "Change")])');
      await page.waitForSelector('.fixed.inset-0');
      const dialogText = await page.$eval('.fixed.inset-0', el => el.textContent);
      expect(dialogText).toContain('Password');
      await page.click('::-p-xpath(//button[contains(., "OK")])');
    });

    test('should display Two-Factor Authentication row', async () => {
      const mfaRow = await page.$('::-p-xpath(//*[contains(., "Two-Factor Authentication")])');
      expect(mfaRow).not.toBeNull();
    });

    test('Two-Factor Authentication should show Enabled badge', async () => {
      const badge = await page.$('::-p-xpath(//*[contains(., "Enabled")])');
      expect(badge).not.toBeNull();
    });

    test('should display Active Sessions row with View button', async () => {
      const viewBtn = await page.$('::-p-xpath(//button[contains(., "View")])');
      expect(viewBtn).not.toBeNull();
    });

    test('View Active Sessions button should open an info dialog', async () => {
      await page.click('::-p-xpath(//button[contains(., "View")])');
      await page.waitForSelector('.fixed.inset-0');
      const dialogText = await page.$eval('.fixed.inset-0', el => el.textContent);
      expect(dialogText).toContain('Sessions');
      await page.click('::-p-xpath(//button[contains(., "OK")])');
    });

    test('should display Recent Activity fieldset', async () => {
      const fieldset = await page.$('::-p-xpath(//fieldset[.//legend[contains(., "Recent Activity")]])');
      expect(fieldset).not.toBeNull();
    });

    test('Recent Activity table should have at least one row', async () => {
      const rows = await page.$$('::-p-xpath(//fieldset[.//legend[contains(., "Recent Activity")]]//tbody/tr)');
      expect(rows.length).toBeGreaterThan(0);
    });

    test('Recent Activity should show login event', async () => {
      const activityText = await page.$eval(
        '::-p-xpath(//fieldset[.//legend[contains(., "Recent Activity")]])',
        el => el.textContent
      );
      expect(activityText?.toLowerCase()).toContain('login');
    });

    test('Recent Activity should show location information', async () => {
      const activityText = await page.$eval(
        '::-p-xpath(//fieldset[.//legend[contains(., "Recent Activity")]])',
        el => el.textContent
      );
      expect(activityText).toContain('San Francisco');
    });

    test('Security Settings panel should be collapsible', async () => {
      const collapseBtn = await page.$('::-p-xpath(//*[contains(., "Security Settings")]//*[text()="-"])');
      expect(collapseBtn).not.toBeNull();
      await page.click('::-p-xpath(//*[contains(@class,"ehr-header")][contains(., "Security Settings")])');
      await wait(100);
      const expandBtn = await page.$('::-p-xpath(//*[contains(., "Security Settings")]//*[text()="+"])');
      expect(expandBtn).not.toBeNull();
    });
  });

  // ---------------------------------------------------------------------------
  // Appearance Tab
  // ---------------------------------------------------------------------------
  describe('Appearance Tab', () => {
    beforeEach(async () => {
      await page.click('::-p-xpath(//button[contains(., "Appearance")])');
      await wait(150);
    });

    test('should display Theme fieldset', async () => {
      const fieldset = await page.$('::-p-xpath(//fieldset[.//legend[contains(., "Theme")]])');
      expect(fieldset).not.toBeNull();
    });

    test('should display Light theme option', async () => {
      const btn = await page.$('::-p-xpath(//button[contains(., "Light")])');
      expect(btn).not.toBeNull();
    });

    test('should display Dark theme option', async () => {
      const btn = await page.$('::-p-xpath(//button[contains(., "Dark")])');
      expect(btn).not.toBeNull();
    });

    test('should display System theme option', async () => {
      const btn = await page.$('::-p-xpath(//button[contains(., "System")])');
      expect(btn).not.toBeNull();
    });

    test('Light theme should be selected by default', async () => {
      const lightBtn = await page.$('::-p-xpath(//button[contains(., "Light") and contains(@class, "bg-white")])');
      expect(lightBtn).not.toBeNull();
    });

    test('clicking Dark theme should select it', async () => {
      await page.click('::-p-xpath(//button[contains(., "Dark")])');
      await wait(100);
      const darkBtn = await page.$('::-p-xpath(//button[contains(., "Dark") and contains(@class, "bg-white")])');
      expect(darkBtn).not.toBeNull();
    });

    test('clicking System theme should select it', async () => {
      await page.click('::-p-xpath(//button[contains(., "System")])');
      await wait(100);
      const systemBtn = await page.$('::-p-xpath(//button[contains(., "System") and contains(@class, "bg-white")])');
      expect(systemBtn).not.toBeNull();
    });

    test('selecting a new theme deselects the previous', async () => {
      await page.click('::-p-xpath(//button[contains(., "Dark")])');
      await wait(100);
      const lightActive = await page.$('::-p-xpath(//button[contains(., "Light") and contains(@class, "bg-white")])');
      expect(lightActive).toBeNull();
    });

    test('should display Display Options fieldset', async () => {
      const fieldset = await page.$('::-p-xpath(//fieldset[.//legend[contains(., "Display Options")]])');
      expect(fieldset).not.toBeNull();
    });

    test('should display Font Size select with default Medium', async () => {
      const value = await page.$eval(
        '::-p-xpath(//fieldset[.//legend[contains(., "Display Options")]]//select)',
        el => (el as HTMLSelectElement).value
      );
      expect(value).toBe('medium');
    });

    test('Font Size select should include Small, Medium, and Large options', async () => {
      const options = await page.$$eval(
        '::-p-xpath(//fieldset[.//legend[contains(., "Display Options")]]//select/option)',
        els => els.map(el => (el as HTMLOptionElement).value)
      );
      expect(options).toContain('small');
      expect(options).toContain('medium');
      expect(options).toContain('large');
    });

    test('should allow changing Font Size to Large', async () => {
      const select = await page.$('::-p-xpath(//fieldset[.//legend[contains(., "Display Options")]]//select)');
      await page.select('::-p-xpath(//fieldset[.//legend[contains(., "Display Options")]]//select)', 'large');
      const value = await select?.evaluate(el => (el as HTMLSelectElement).value);
      expect(value).toBe('large');
    });

    test('Compact Mode checkbox should be unchecked by default', async () => {
      const checkbox = await page.$('::-p-xpath(//fieldset[.//legend[contains(., "Display Options")]]//input[@type="checkbox"])');
      const checked = await checkbox?.evaluate(el => (el as HTMLInputElement).checked);
      expect(checked).toBe(false);
    });

    test('should allow enabling Compact Mode', async () => {
      const checkbox = await page.$('::-p-xpath(//fieldset[.//legend[contains(., "Display Options")]]//input[@type="checkbox"])');
      await checkbox?.click();
      await wait(100);
      const checked = await checkbox?.evaluate(el => (el as HTMLInputElement).checked);
      expect(checked).toBe(true);
    });

    test('should display Compact Mode label text', async () => {
      const label = await page.$('::-p-xpath(//*[contains(., "Compact Mode")])');
      expect(label).not.toBeNull();
    });
  });

  // ---------------------------------------------------------------------------
  // Practice Tab
  // ---------------------------------------------------------------------------
  describe('Practice Tab', () => {
    beforeEach(async () => {
      await page.click('::-p-xpath(//button[contains(., "Practice")])');
      await wait(150);
    });

    test('should display Practice Information fieldset', async () => {
      const fieldset = await page.$('::-p-xpath(//fieldset[.//legend[contains(., "Practice Information")]])');
      expect(fieldset).not.toBeNull();
    });

    test('should display Practice Name input with default value', async () => {
      const value = await page.$eval(
        'input[value="Anderson Family Medicine"]',
        el => (el as HTMLInputElement).value
      );
      expect(value).toBe('Anderson Family Medicine');
    });

    test('should display Tax ID input with default value', async () => {
      const value = await page.$eval(
        'input[value="12-3456789"]',
        el => (el as HTMLInputElement).value
      );
      expect(value).toBe('12-3456789');
    });

    test('should display Address input with default value', async () => {
      const value = await page.$eval(
        'input[value="123 Medical Center Drive, Suite 100"]',
        el => (el as HTMLInputElement).value
      );
      expect(value).toBe('123 Medical Center Drive, Suite 100');
    });

    test('should display City input with default value "Springfield"', async () => {
      const value = await page.$eval(
        'input[value="Springfield"]',
        el => (el as HTMLInputElement).value
      );
      expect(value).toBe('Springfield');
    });

    test('should display State select with Illinois as default', async () => {
      const value = await page.$eval(
        '::-p-xpath(//fieldset[.//legend[contains(., "Practice Information")]]//select)',
        el => (el as HTMLSelectElement).value
      );
      expect(value).toBe('Illinois');
    });

    test('State select should include multiple state options', async () => {
      const options = await page.$$eval(
        '::-p-xpath(//fieldset[.//legend[contains(., "Practice Information")]]//select/option)',
        els => els.map(el => el.textContent?.trim())
      );
      expect(options).toContain('Illinois');
      expect(options).toContain('California');
      expect(options).toContain('New York');
    });

    test('should display Business Hours panel', async () => {
      const panel = await page.$('::-p-xpath(//*[contains(., "Business Hours")])');
      expect(panel).not.toBeNull();
    });

    test('Business Hours should be expanded by default', async () => {
      const mondayRow = await page.$('::-p-xpath(//*[contains(., "Monday")])');
      expect(mondayRow).not.toBeNull();
    });

    test('Business Hours should list all weekdays', async () => {
      const pageText = await page.$eval('body', el => el.textContent);
      ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'].forEach(day => {
        expect(pageText).toContain(day);
      });
    });

    test('Business Hours should have time range selects for each day', async () => {
      const selects = await page.$$('::-p-xpath(//*[contains(., "Monday") or contains(., "Tuesday") or contains(., "Wednesday")]//ancestor::div[1]//following-sibling::select)');
      // Each day has two selects (open/close)
      expect(selects.length).toBeGreaterThan(0);
    });

    test('Business Hours panel should be collapsible', async () => {
      await page.click('::-p-xpath(//*[contains(@class,"ehr-header")][contains(., "Business Hours")])');
      await wait(100);
      const mondayRow = await page.$('::-p-xpath(//*[text()="Monday"])');
      expect(mondayRow).toBeNull();
    });

    test('Business Hours panel should re-expand after collapsing', async () => {
      await page.click('::-p-xpath(//*[contains(@class,"ehr-header")][contains(., "Business Hours")])');
      await wait(100);
      await page.click('::-p-xpath(//*[contains(@class,"ehr-header")][contains(., "Business Hours")])');
      await wait(100);
      const mondayRow = await page.$('::-p-xpath(//*[text()="Monday"])');
      expect(mondayRow).not.toBeNull();
    });

    test('should display Timezone fieldset', async () => {
      const fieldset = await page.$('::-p-xpath(//fieldset[.//legend[contains(., "Timezone")]])');
      expect(fieldset).not.toBeNull();
    });

    test('should display correct timezone text', async () => {
      const timezoneText = await page.$eval(
        '::-p-xpath(//fieldset[.//legend[contains(., "Timezone")]])',
        el => el.textContent
      );
      expect(timezoneText).toContain('America/Chicago');
      expect(timezoneText).toContain('Central Time');
    });
  });

  // ---------------------------------------------------------------------------
  // Save / Persistence
  // ---------------------------------------------------------------------------
  describe('Save and Persistence', () => {
    test('clicking Save Changes should show Saved confirmation', async () => {
      await page.click('::-p-xpath(//button[contains(., "Save Changes")])');
      await page.waitForSelector('::-p-xpath(//button[contains(., "Saved")])');
      const savedBtn = await page.$('::-p-xpath(//button[contains(., "Saved")])');
      expect(savedBtn).not.toBeNull();
    });

    test('Saved button should revert to Save Changes after timeout', async () => {
      await page.click('::-p-xpath(//button[contains(., "Save Changes")])');
      await page.waitForSelector('::-p-xpath(//button[contains(., "Saved")])');
      // The component reverts after 2 seconds
      await wait(2500);
      const saveBtn = await page.$('::-p-xpath(//button[contains(., "Save Changes")])');
      expect(saveBtn).not.toBeNull();
    });

    test('should persist profile changes to localStorage after saving', async () => {
      const input = await page.$('input[value="Sarah"]');
      await input?.click({ clickCount: 3 });
      await input?.type('Jane');
      await page.click('::-p-xpath(//button[contains(., "Save Changes")])');
      await page.waitForSelector('::-p-xpath(//button[contains(., "Saved")])');

      const stored = await page.evaluate(() => {
        const raw = localStorage.getItem('coghealth_settings');
        return raw ? JSON.parse(raw) : null;
      });
      expect(stored).not.toBeNull();
      expect(stored.profile.firstName).toBe('Jane');
    });

    test('should persist notification changes to localStorage', async () => {
      await page.click('::-p-xpath(//button[contains(., "Notifications")])');
      await wait(150);
      const checkboxes = await page.$$('input[type="checkbox"]');
      await checkboxes[1]?.click(); // enable SMS
      await page.click('::-p-xpath(//button[contains(., "Save Changes")])');
      await page.waitForSelector('::-p-xpath(//button[contains(., "Saved")])');

      const stored = await page.evaluate(() => {
        const raw = localStorage.getItem('coghealth_settings');
        return raw ? JSON.parse(raw) : null;
      });
      expect(stored.notifications.smsAlerts).toBe(true);
    });

    test('should restore saved profile values on page reload', async () => {
      const input = await page.$('input[value="Sarah"]');
      await input?.click({ clickCount: 3 });
      await input?.type('Jane');
      await page.click('::-p-xpath(//button[contains(., "Save Changes")])');
      await page.waitForSelector('::-p-xpath(//button[contains(., "Saved")])');

      await page.goto(SETTINGS_URL);
      await page.waitForSelector('.ehr-header');
      const restoredValue = await page.$eval(
        'input[value="Jane"]',
        el => (el as HTMLInputElement).value
      );
      expect(restoredValue).toBe('Jane');
    });
  });
});
