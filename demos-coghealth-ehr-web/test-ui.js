const puppeteer = require('puppeteer');

(async () => {
  const browser = await puppeteer.launch({ headless: true });
  const page = await browser.newPage();
  await page.setViewport({ width: 1400, height: 900 });
  
  console.log('Testing Dashboard...');
  await page.goto('http://localhost:5173/', { waitUntil: 'networkidle0' });
  
  // Check for modern elements
  const modernElements = await page.evaluate(() => {
    const issues = [];
    
    // Check for rounded elements
    const roundedFull = document.querySelectorAll('[class*="rounded-full"]');
    if (roundedFull.length > 0) {
      issues.push(`Found ${roundedFull.length} rounded-full elements`);
    }
    
    // Check for colored badges (non-gray)
    const coloredBadges = document.querySelectorAll('[class*="bg-green-"], [class*="bg-blue-"], [class*="bg-amber-"], [class*="bg-red-"], [class*="bg-purple-"], [class*="bg-orange-"]');
    coloredBadges.forEach(el => {
      if (!el.className.includes('text-')) {
        issues.push(`Colored element: ${el.className.substring(0, 100)}`);
      }
    });
    
    // Check panel structure
    const panels = document.querySelectorAll('.ehr-panel');
    panels.forEach((panel, i) => {
      const style = window.getComputedStyle(panel);
      issues.push(`Panel ${i}: flex=${style.flex}, height=${style.height}`);
    });
    
    return issues;
  });
  
  console.log('Modern elements found:');
  modernElements.forEach(i => console.log('  -', i));
  
  // Take screenshot
  await page.screenshot({ path: 'dashboard-screenshot.png', fullPage: true });
  console.log('Screenshot saved to dashboard-screenshot.png');
  
  // Test collapsing a panel
  console.log('\nTesting panel collapse...');
  const inboxHeader = await page.$('.ehr-panel .ehr-header');
  if (inboxHeader) {
    const beforeHeight = await page.evaluate(() => {
      const panels = document.querySelectorAll('.ehr-panel');
      return Array.from(panels).map(p => p.getBoundingClientRect().height);
    });
    console.log('Before collapse heights:', beforeHeight);
    
    await inboxHeader.click();
    await page.waitForTimeout(500);
    
    const afterHeight = await page.evaluate(() => {
      const panels = document.querySelectorAll('.ehr-panel');
      return Array.from(panels).map(p => p.getBoundingClientRect().height);
    });
    console.log('After collapse heights:', afterHeight);
    
    await page.screenshot({ path: 'dashboard-collapsed.png', fullPage: true });
  }
  
  await browser.close();
  console.log('\nDone!');
})();
