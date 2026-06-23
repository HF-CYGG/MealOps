(function () {
  "use strict";

  function $(selector, root) {
    return (root || document).querySelector(selector);
  }

  function $all(selector, root) {
    return Array.from((root || document).querySelectorAll(selector));
  }

  function escapeHtml(value) {
    return String(value === undefined || value === null ? "" : value)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }

  function money(value) {
    const number = Number(value || 0);
    return "¥" + number.toFixed(2);
  }

  function dateTime(value) {
    if (!value) return "-";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return String(value).replace("T", " ");
    return date.toLocaleString("zh-CN", { hour12: false });
  }

  function statusText(value, map) {
    return map[value] || map[String(value)] || value || "-";
  }

  function normalizePage(data) {
    const value = window.MealOpsApi.dataOf(data) || data || {};
    if (Array.isArray(value)) {
      return { records: value, total: value.length };
    }
    return {
      records: value.records || value.list || value.rows || [],
      total: Number(value.total || value.records && value.records.length || 0),
      raw: value
    };
  }

  function showDebug(target, value) {
    if (!target) return;
    target.textContent = typeof value === "string" ? value : JSON.stringify(value, null, 2);
  }

  function ensureToastStack() {
    let stack = $(".toast-stack");
    if (!stack) {
      stack = document.createElement("div");
      stack.className = "toast-stack";
      stack.setAttribute("aria-live", "polite");
      document.body.appendChild(stack);
    }
    return stack;
  }

  function toast(message, type) {
    const item = document.createElement("div");
    item.className = "toast " + (type || "");
    item.textContent = message;
    ensureToastStack().appendChild(item);
    setTimeout(() => item.remove(), 4200);
  }

  function errorMessage(error) {
    if (!error) return "请求失败，请稍后重试";
    if (error.detail && error.detail.msg) return error.detail.msg;
    if (typeof error.detail === "string") return error.detail;
    return error.message || "请求失败，请稍后重试";
  }

  function setBusy(button, busy, text) {
    if (!button) return;
    if (busy) {
      button.dataset.originalText = button.textContent;
      button.textContent = text || "处理中...";
      button.disabled = true;
      return;
    }
    button.textContent = button.dataset.originalText || button.textContent;
    button.disabled = false;
  }

  function empty(label, action) {
    return `<div class="empty-state"><strong>${escapeHtml(label)}</strong>${action ? `<span>${escapeHtml(action)}</span>` : ""}</div>`;
  }

  function errorState(label) {
    return `<div class="error-state"><strong>${escapeHtml(label)}</strong><span>请检查登录状态或后端接口。</span></div>`;
  }

  function initials(name) {
    const text = String(name || "菜").trim();
    return escapeHtml(text.slice(0, 2));
  }

  function orderStatus(value) {
    return statusText(value, {
      1: "待付款",
      2: "待确认",
      3: "已确认",
      4: "派送中",
      5: "已完成",
      6: "已取消"
    });
  }

  function payStatus(value) {
    return statusText(value, {
      0: "未支付",
      1: "已支付"
    });
  }

  function openDrawer(drawerSelector, backdropSelector) {
    const drawer = $(drawerSelector);
    const backdrop = $(backdropSelector || ".drawer-backdrop");
    if (backdrop) backdrop.classList.add("open");
    if (drawer) drawer.classList.add("open");
  }

  function closeDrawer(drawerSelector, backdropSelector) {
    const drawer = $(drawerSelector);
    const backdrop = $(backdropSelector || ".drawer-backdrop");
    if (drawer) drawer.classList.remove("open");
    if (backdrop) backdrop.classList.remove("open");
  }

  function bindTabs(rootSelector, buttonSelector, panelSelector, activeClass) {
    const root = $(rootSelector) || document;
    const buttons = $all(buttonSelector, root);
    const panels = $all(panelSelector, root);
    buttons.forEach((button) => {
      button.addEventListener("click", () => {
        buttons.forEach((item) => item.classList.remove(activeClass || "active"));
        panels.forEach((item) => item.classList.remove(activeClass || "active"));
        button.classList.add(activeClass || "active");
        const panel = $("#" + button.dataset.target, root);
        if (panel) panel.classList.add(activeClass || "active");
      });
    });
  }

  window.MealOpsUI = {
    $,
    $all,
    escapeHtml,
    money,
    dateTime,
    normalizePage,
    showDebug,
    toast,
    errorMessage,
    setBusy,
    empty,
    errorState,
    initials,
    orderStatus,
    payStatus,
    openDrawer,
    closeDrawer,
    bindTabs
  };
})();
