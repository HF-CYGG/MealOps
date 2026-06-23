import { spawn } from "node:child_process";
import fs from "node:fs/promises";
import path from "node:path";

const baseUrl = "http://127.0.0.1:8080";
const screenshotDir = "F:/MealOps/docs/screenshots";
const chromePath = "C:/Program Files/Google/Chrome/Application/chrome.exe";
const userDataDir = "F:/MealOps/.tmp-chrome-cdp";
const debugPort = 9223;
const runId = Date.now().toString().slice(-6);

class CdpPage {
  constructor(wsUrl) {
    this.ws = new WebSocket(wsUrl);
    this.nextId = 1;
    this.pending = new Map();
    this.ws.addEventListener("message", (event) => {
      const message = JSON.parse(event.data);
      if (message.id && this.pending.has(message.id)) {
        const { resolve, reject } = this.pending.get(message.id);
        this.pending.delete(message.id);
        if (message.error) reject(new Error(message.error.message));
        else resolve(message.result || {});
      }
    });
  }

  async open() {
    await new Promise((resolve, reject) => {
      this.ws.addEventListener("open", resolve, { once: true });
      this.ws.addEventListener("error", reject, { once: true });
    });
    await this.send("Page.enable");
    await this.send("Runtime.enable");
    await this.send("Emulation.setDeviceMetricsOverride", {
      width: 1365,
      height: 900,
      deviceScaleFactor: 1,
      mobile: false
    });
  }

  send(method, params = {}) {
    const id = this.nextId++;
    this.ws.send(JSON.stringify({ id, method, params }));
    return new Promise((resolve, reject) => this.pending.set(id, { resolve, reject }));
  }

  async eval(expression) {
    const result = await this.send("Runtime.evaluate", {
      expression,
      awaitPromise: true,
      returnByValue: true
    });
    if (result.exceptionDetails) throw new Error(result.exceptionDetails.text || "Runtime.evaluate failed");
    return result.result?.value;
  }

  async goto(url) {
    await this.send("Page.navigate", { url });
    await this.waitFor("document.readyState === 'complete'", 15000);
  }

  async waitFor(expression, timeoutMs = 15000) {
    const started = Date.now();
    while (Date.now() - started < timeoutMs) {
      if (await this.eval(expression).catch(() => false)) return;
      await sleep(250);
    }
    throw new Error(`Timed out waiting for ${expression}`);
  }

  async waitOutput() {
    await this.waitFor(`(() => {
      const text = document.querySelector("#output")?.textContent || "";
      return text && !text.startsWith("请求中") && text !== "等待请求...";
    })()`, 20000);
    await this.mask();
  }

  async mask() {
    await this.eval(`(() => {
      const output = document.querySelector("#output");
      if (output) {
        output.textContent = output.textContent
          .replace(/eyJ[A-Za-z0-9._-]+/g, "[MASKED_TOKEN]")
          .replace(/Bearer\\s+[A-Za-z0-9._-]+/g, "Bearer [MASKED_TOKEN]");
      }
      document.querySelectorAll("input[type=password]").forEach((input) => input.value = "******");
    })()`);
  }

  async click(selector) {
    await this.eval(`document.querySelector(${JSON.stringify(selector)}).click()`);
  }

  async fill(selector, value) {
    await this.eval(`(() => {
      const element = document.querySelector(${JSON.stringify(selector)});
      element.value = ${JSON.stringify(value)};
      element.dispatchEvent(new Event("input", { bubbles: true }));
      element.dispatchEvent(new Event("change", { bubbles: true }));
    })()`);
  }

  async select(selector, value) {
    await this.fill(selector, value);
  }

  async shot(name) {
    await this.mask();
    const result = await this.send("Page.captureScreenshot", { format: "png", fromSurface: true });
    await fs.writeFile(path.join(screenshotDir, name), Buffer.from(result.data, "base64"));
  }

  close() {
    this.ws.close();
  }
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function waitForChrome() {
  for (let i = 0; i < 60; i++) {
    try {
      const response = await fetch(`http://127.0.0.1:${debugPort}/json/version`);
      if (response.ok) return;
    } catch {
      await sleep(250);
    }
  }
  throw new Error("Chrome DevTools endpoint did not start");
}

async function newPage() {
  const response = await fetch(`http://127.0.0.1:${debugPort}/json/new`, { method: "PUT" });
  const target = await response.json();
  const page = new CdpPage(target.webSocketDebuggerUrl);
  await page.open();
  return page;
}

async function clickAndShot(page, selector, name) {
  await page.click(selector);
  await page.waitOutput();
  await page.shot(name);
}

async function rawRequest(page, method, requestPath, body, name) {
  await page.click('[data-tab="rawPanel"]');
  await page.select("#rawMethod", method);
  await page.fill("#rawPath", requestPath);
  await page.fill("#rawBody", body === undefined ? "{}" : JSON.stringify(body, null, 2));
  await clickAndShot(page, "#rawSendBtn", name);
}

async function main() {
  await fs.mkdir(screenshotDir, { recursive: true });
  await fs.rm(userDataDir, { recursive: true, force: true });
  const chrome = spawn(chromePath, [
    "--headless=new",
    "--disable-gpu",
    "--hide-scrollbars",
    `--remote-debugging-port=${debugPort}`,
    `--user-data-dir=${userDataDir}`,
    "--window-size=1365,900",
    "about:blank"
  ], { stdio: "ignore" });
  await waitForChrome();

  try {
    const admin = await newPage();
    await admin.goto(`${baseUrl}/static/admin.html`);
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
    await admin.waitOutput();
    await admin.shot("16-operation-log.png");

    const user = await newPage();
    await user.goto(`${baseUrl}/static/user.html`);
    await clickAndShot(user, "#loginBtn", "17-user-login.png");
    await user.click('button[data-call="GET /dish/list?categoryId=1001"]');
    await user.waitOutput();
    await user.shot("18-user-dish-list.png");
    await user.click('button[data-call="GET /setmeal/list?categoryId=2001"]');
    await user.waitOutput();
    await user.shot("19-user-setmeal-list.png");
    await user.click('[data-tab="addressPanel"]');
    await clickAndShot(user, "#addressListBtn", "20-address-book.png");
    await user.click('[data-tab="cartPanel"]');
    await clickAndShot(user, "#addDishBtn", "21-cart-add.png");
    await clickAndShot(user, "#cartListBtn", "22-cart-list.png");
    await user.click('[data-tab="orderPanel"]');
    await clickAndShot(user, "#submitOrderBtn", "23-order-submit.png");
    await clickAndShot(user, "#orderListBtn", "24-user-order-page.png");

    const noToken = await newPage();
    await noToken.goto(`${baseUrl}/static/admin.html`);
    await noToken.eval("localStorage.clear()");
    await noToken.goto(`${baseUrl}/static/admin.html`);
    await rawRequest(noToken, "GET", "/dish/page?page=1&pageSize=10", undefined, "27-auth-required.png");

    await rawRequest(admin, "DELETE", "/category?id=1001", undefined, "28-category-delete-guard.png");

    await user.click('[data-tab="cartPanel"]');
    await clickAndShot(user, "#cartCleanBtn", "29-empty-cart-order-clean.png");
    await user.click('[data-tab="orderPanel"]');
    await clickAndShot(user, "#submitOrderBtn", "29-empty-cart-order.png");
    await fs.rm(path.join(screenshotDir, "29-empty-cart-order-clean.png"), { force: true });

    const fail = await newPage();
    await fail.goto(`${baseUrl}/static/admin.html`);
    await fail.eval("localStorage.clear()");
    await fail.goto(`${baseUrl}/static/admin.html`);
    await fail.fill("#password", "wrong-password");
    await clickAndShot(fail, "#loginBtn", "25-login-fail.png");
    for (let i = 0; i < 5; i++) {
      await fail.click("#loginBtn").catch(() => {});
      await fail.waitOutput().catch(() => {});
    }
    await fail.shot("26-login-lock.png");

    admin.close();
    user.close();
    noToken.close();
    fail.close();
  } finally {
    chrome.kill();
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
