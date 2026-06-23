import { createRequire } from "node:module";
import fs from "node:fs/promises";
import path from "node:path";

const require = createRequire(import.meta.url);
const { chromium } = require("C:/Users/闫奕衡/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright");
const baseUrl = "http://127.0.0.1:8080";
const screenshotDir = "F:/MealOps/docs/screenshots";
const chromePath = "C:/Program Files/Google/Chrome/Application/chrome.exe";
const runId = Date.now().toString().slice(-6);

async function waitOutput(page) {
  await page.waitForFunction(() => {
    const text = document.querySelector("#output")?.textContent || "";
    return text && !text.startsWith("请求中") && text !== "等待请求...";
  }, { timeout: 15000 });
  await maskSensitive(page);
}

async function maskSensitive(page) {
  await page.evaluate(() => {
    const output = document.querySelector("#output");
    if (output) {
      output.textContent = output.textContent
        .replace(/eyJ[A-Za-z0-9._-]+/g, "[MASKED_TOKEN]")
        .replace(/Bearer\s+[A-Za-z0-9._-]+/g, "Bearer [MASKED_TOKEN]");
    }
    document.querySelectorAll("input[type=password]").forEach((input) => {
      input.value = "******";
    });
  });
}

async function shot(page, name) {
  await maskSensitive(page);
  await page.screenshot({ path: path.join(screenshotDir, name), fullPage: false });
}

async function clickAndShot(page, selector, name) {
  await page.click(selector);
  await waitOutput(page);
  await shot(page, name);
}

async function rawRequest(page, method, requestPath, body, name) {
  await page.click('[data-tab="rawPanel"]');
  await page.selectOption("#rawMethod", method);
  await page.fill("#rawPath", requestPath);
  await page.fill("#rawBody", body === undefined ? "{}" : JSON.stringify(body, null, 2));
  await clickAndShot(page, "#rawSendBtn", name);
}

async function main() {
  await fs.mkdir(screenshotDir, { recursive: true });
  const browser = await chromium.launch({
    headless: true,
    executablePath: chromePath
  });
  const context = await browser.newContext({
    viewport: { width: 1365, height: 900 },
    deviceScaleFactor: 1
  });

  const admin = await context.newPage();
  await admin.goto(`${baseUrl}/static/admin.html`, { waitUntil: "networkidle" });
  await clickAndShot(admin, "#loginBtn", "08-admin-login.png");
  await clickAndShot(admin, "#listCategoryBtn", "09-category-list.png");
  await admin.fill("#categoryName", `截图分类${runId}`);
  await clickAndShot(admin, "#saveCategoryBtn", "10-category-save.png");
  await admin.click('[data-tab="dishPanel"]');
  await clickAndShot(admin, "#listDishBtn", "11-dish-page.png");
  await admin.fill("#dishName", `截图菜品${runId}`);
  await clickAndShot(admin, "#saveDishBtn", "12-dish-save.png");
  await admin.click('[data-tab="setmealPanel"]');
  await clickAndShot(admin, "#listSetmealBtn", "13-setmeal-page.png");
  await admin.fill("#setmealName", `截图套餐${runId}`);
  await clickAndShot(admin, "#saveSetmealBtn", "14-setmeal-save.png");
  await rawRequest(admin, "GET", "/order/page?page=1&pageSize=10", undefined, "15-admin-order-page.png");
  await admin.click('button[data-call="GET /logs/page?page=1&pageSize=10"]');
  await waitOutput(admin);
  await shot(admin, "16-operation-log.png");

  const user = await context.newPage();
  await user.goto(`${baseUrl}/static/user.html`, { waitUntil: "networkidle" });
  await clickAndShot(user, "#loginBtn", "17-user-login.png");
  await user.click('button[data-call="GET /dish/list?categoryId=1001"]');
  await waitOutput(user);
  await shot(user, "18-user-dish-list.png");
  await user.click('button[data-call="GET /setmeal/list?categoryId=2001"]');
  await waitOutput(user);
  await shot(user, "19-user-setmeal-list.png");
  await user.click('[data-tab="addressPanel"]');
  await clickAndShot(user, "#addressListBtn", "20-address-book.png");
  await user.click('[data-tab="cartPanel"]');
  await clickAndShot(user, "#addDishBtn", "21-cart-add.png");
  await clickAndShot(user, "#cartListBtn", "22-cart-list.png");
  await user.click('[data-tab="orderPanel"]');
  await clickAndShot(user, "#submitOrderBtn", "23-order-submit.png");
  await clickAndShot(user, "#orderListBtn", "24-user-order-page.png");

  const noToken = await context.newPage();
  await noToken.goto(`${baseUrl}/static/admin.html`, { waitUntil: "networkidle" });
  await noToken.evaluate(() => localStorage.clear());
  await noToken.reload({ waitUntil: "networkidle" });
  await rawRequest(noToken, "GET", "/dish/page?page=1&pageSize=10", undefined, "27-auth-required.png");

  await rawRequest(admin, "DELETE", "/category?id=1001", undefined, "28-category-delete-guard.png");

  await user.click('[data-tab="cartPanel"]');
  await clickAndShot(user, "#cartCleanBtn", "29-empty-cart-order-clean.png");
  await user.click('[data-tab="orderPanel"]');
  await clickAndShot(user, "#submitOrderBtn", "29-empty-cart-order.png");
  await fs.rm(path.join(screenshotDir, "29-empty-cart-order-clean.png"), { force: true });

  const fail = await context.newPage();
  await fail.goto(`${baseUrl}/static/admin.html`, { waitUntil: "networkidle" });
  await fail.evaluate(() => localStorage.clear());
  await fail.reload({ waitUntil: "networkidle" });
  await fail.fill("#password", "wrong-password");
  await clickAndShot(fail, "#loginBtn", "25-login-fail.png");
  for (let i = 0; i < 5; i++) {
    await fail.click("#loginBtn").catch(() => {});
    await waitOutput(fail).catch(() => {});
  }
  await shot(fail, "26-login-lock.png");

  await browser.close();
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
