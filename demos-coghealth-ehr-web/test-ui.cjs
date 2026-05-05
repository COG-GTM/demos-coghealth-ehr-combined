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
    
    const rounded = document.querySelectorAll('[class*="rounded"]');
    rounded.forEach(el => {
      if (el.className.includes('rounded-full') || el.className.includes('rounded-sm') || el.className.includes('rounded-t')) return;
      const text = el.textContent?.substring(0, 30) || '';
      issues.push(`Rounded: ${el.tagName} "${text}" - ${el.className.substring(0, 80)}`);
    });
    
    // Check panel structure
    const panels = document.querySelectorAll('.ehr-panel');
    panels.forEach((panel, i) => {
      const style = window.getComputedStyle(panel);
      const header = panel.querySelector('.ehr-header');
      issues.push(`Panel ${i}: flex=${style.flex}, height=${panel.offsetHeight}px, header="${header?.textContent?.substring(0,20)}"`);
    });
    
    return issues;
  });
  
  console.log('Issues found:');
  modernElements.forEach(i => console.log('  -', i));
  
  // Test collapsing a panel
  console.log('\nTesting panel collapse...');
  const inboxHeader = await page.$('.ehr-panel .ehr-header');
  if (inboxHeader) {
    const beforeHeight = await page.evaluate(() => {
      const panels = document.querySelectorAll('.ehr-panel');
      return Array.from(panels).map(p => ({ h: p.offsetHeight, flex: window.getComputedStyle(p).flex }));
    });
    console.log('Before collapse:', beforeHeight);
    
    await inboxHeader.click();
    await new Promise(r => setTimeout(r, 500));
    
    const afterHeight = await page.evaluate(() => {
      const panels = document.querySelectorAll('.ehr-panel');
      return Array.from(panels).map(p => ({ h: p.offsetHeight, flex: window.getComputedStyle(p).flex }));
    });
    console.log('After collapse:', afterHeight);
  }
  
  await browser.close();
  console.log('\nDone!');
})();
