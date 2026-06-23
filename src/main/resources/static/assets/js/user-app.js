(function () {
  "use strict";

  const api = window.MealOpsApi;
  const store = window.MealOpsStore;
  const ui = window.MealOpsUI;

  const state = {
    user: store.currentUser(),
    session: store.currentSession(),
    kind: "dish",
    categoryId: "",
    categories: [],
    products: [],
    cart: [],
    selectedProduct: null,
    selectedFlavor: "",
    selectedQuantity: 1,
    lastOrder: null
  };

  function debug(value) {
    ui.showDebug(ui.$("#debugOutput"), value);
  }

  function renderStatus() {
    const token = api.getToken("user");
    const userText = state.user ? `${state.user.name || state.user.phone} 已登录` : "未登录，示例手机号 13900000000";
    ui.$("#userStatus").textContent = token ? userText : "未登录，示例手机号 13900000000";
    ui.$("#loginPanel").style.display = token ? "none" : "grid";
    ui.$("#sessionTable").textContent = state.session ? state.session.tableName || state.session.tableNo : "未选择";
    ui.$("#sessionGuests").textContent = state.session ? (state.session.partySize || state.session.guests) + " 人" : "-";
    ui.$("#sessionState").textContent = state.session ? `${state.session.status} · 接口` : "待创建";
  }

  function activeType() {
    return state.kind === "dish" ? 1 : 2;
  }

  function renderCategories() {
    const rail = ui.$("#categoryRail");
    if (!state.categories.length) {
      rail.innerHTML = `<button class="chip active" type="button">全部</button>`;
      return;
    }
    rail.innerHTML = [
      `<button class="chip ${state.categoryId ? "" : "active"}" data-category="" type="button">全部</button>`,
      ...state.categories.map((item) => (
        `<button class="chip ${String(state.categoryId) === String(item.id) ? "active" : ""}" data-category="${ui.escapeHtml(item.id)}" type="button">${ui.escapeHtml(item.name)}</button>`
      ))
    ].join("");
  }

  function productMatches(product) {
    const keyword = ui.$("#searchInput").value.trim().toLowerCase();
    if (!keyword) return true;
    return [product.name, product.description, product.categoryName]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(keyword));
  }

  function renderProducts() {
    const list = ui.$("#productList");
    const rows = state.products.filter(productMatches);
    if (!rows.length) {
      list.innerHTML = ui.empty("暂无商品", "可切换分类或检查接口状态。");
      return;
    }
    list.innerHTML = rows.map((item) => {
      const disabled = item.status === 0;
      return `
        <article class="product-card">
          <div class="product-art" aria-hidden="true">${ui.initials(item.name)}</div>
          <div class="product-body">
            <div class="detail-row">
              <h3 class="product-title">${ui.escapeHtml(item.name)}</h3>
              <span class="status-pill ${disabled ? "danger" : "ok"}">${disabled ? "停售" : "起售"}</span>
            </div>
            <p class="product-desc">${ui.escapeHtml(item.description || "堂食点单商品")}</p>
            <div class="price-row">
              <span class="price">${ui.money(item.price)}</span>
              <button data-product="${ui.escapeHtml(item.id)}" ${disabled ? "disabled" : ""} type="button">选规格</button>
            </div>
          </div>
        </article>
      `;
    }).join("");
  }

  function renderCart() {
    const total = state.cart.reduce((sum, item) => sum + Number(item.amount || 0) * Number(item.number || 1), 0);
    const count = state.cart.reduce((sum, item) => sum + Number(item.number || 0), 0);
    ui.$("#cartTotal").textContent = ui.money(total);
    ui.$("#cartSummary").textContent = count ? `${count} 件商品 · ${state.session ? state.session.tableName || state.session.tableNo : "未选桌"}` : "购物车为空";
    ui.$("#cartDrawerHint").textContent = state.session ? `${state.session.tableName || state.session.tableNo} 同桌已点` : "当前桌台购物车";
    const target = ui.$("#cartList");
    if (!state.cart.length) {
      target.innerHTML = ui.empty("同桌还没有点菜", "从商品列表选择规格后加购。");
      return;
    }
    target.innerHTML = state.cart.map((item) => `
      <div class="line-item">
        <div>
          <strong>${ui.escapeHtml(item.name)}</strong>
          <p class="muted">${ui.escapeHtml(item.dishFlavor || "默认规格")} · ${ui.money(item.amount)}</p>
        </div>
        <div class="qty-control" aria-label="数量">
          <button data-cart-dec="${ui.escapeHtml(item.id)}" data-number="${ui.escapeHtml(item.number || 1)}" type="button">-</button>
          <span>${ui.escapeHtml(item.number || 1)}</span>
          <button data-cart-inc="${ui.escapeHtml(item.id)}" data-number="${ui.escapeHtml(item.number || 1)}" type="button">+</button>
        </div>
      </div>
    `).join("");
  }

  async function loadCategories() {
    try {
      const response = await api.categories(activeType(), "user");
      state.categories = api.dataOf(response) || [];
      if (state.categoryId && !state.categories.some((item) => String(item.id) === String(state.categoryId))) {
        state.categoryId = "";
      }
      renderCategories();
    } catch (error) {
      state.categories = [];
      renderCategories();
      ui.toast(ui.errorMessage(error), "error");
      debug(error.detail || error.message);
    }
  }

  async function loadProducts() {
    const params = { status: 1 };
    if (state.categoryId) params.categoryId = state.categoryId;
    try {
      const response = state.kind === "dish" ? await api.dishList(params, "user") : await api.setmealList(params, "user");
      state.products = api.dataOf(response) || [];
      renderProducts();
      debug(response);
    } catch (error) {
      state.products = [];
      ui.$("#productList").innerHTML = ui.errorState(ui.errorMessage(error));
      debug(error.detail || error.message);
    }
  }

  async function loadCart() {
    if (!state.session || !state.session.id) {
      state.cart = [];
      renderCart();
      return;
    }
    try {
      const response = await api.diningCart(state.session.id);
      state.cart = api.dataOf(response) || [];
      renderCart();
    } catch (error) {
      state.cart = [];
      renderCart();
      if (api.getToken("user")) ui.toast(ui.errorMessage(error), "error");
    }
  }

  async function refreshAll() {
    renderStatus();
    await loadCategories();
    await loadProducts();
    await loadCart();
  }

  function firstFlavor(product) {
    const flavors = Array.isArray(product.flavors) ? product.flavors : [];
    if (!flavors.length) return "";
    try {
      const values = JSON.parse(flavors[0].value || "[]");
      return values[0] || "";
    } catch (error) {
      return "";
    }
  }

  async function openProduct(productId) {
    const product = state.products.find((item) => String(item.id) === String(productId));
    if (!product) return;
    state.selectedProduct = product;
    state.selectedQuantity = 1;
    state.selectedFlavor = firstFlavor(product);
    if (state.kind === "dish") {
      try {
        const detail = await api.dishDetail(product.id, "user");
        state.selectedProduct = Object.assign({}, product, api.dataOf(detail) || {});
      } catch (error) {
        debug(error.detail || error.message);
      }
    }
    renderProductDrawer();
    ui.openDrawer("#productDrawer", "#drawerBackdrop");
  }

  function renderProductDrawer() {
    const product = state.selectedProduct;
    if (!product) return;
    const flavors = Array.isArray(product.flavors) ? product.flavors : [];
    ui.$("#productDrawerTitle").textContent = product.name;
    ui.$("#productDrawerBody").innerHTML = `
      <div class="product-card" style="grid-template-columns:82px minmax(0,1fr);">
        <div class="product-art" aria-hidden="true">${ui.initials(product.name)}</div>
        <div class="product-body">
          <p class="product-desc">${ui.escapeHtml(product.description || "堂食点单商品")}</p>
          <span class="price">${ui.money(product.price)}</span>
        </div>
      </div>
      <div class="form-grid">
        ${flavors.map((flavor) => {
          let values = [];
          try { values = JSON.parse(flavor.value || "[]"); } catch (error) { values = []; }
          return `<label>${ui.escapeHtml(flavor.name)}
            <select id="flavorSelect">${values.map((value) => `<option value="${ui.escapeHtml(value)}" ${value === state.selectedFlavor ? "selected" : ""}>${ui.escapeHtml(value)}</option>`).join("")}</select>
          </label>`;
        }).join("")}
        <label>数量
          <input id="quantityInput" type="number" min="1" max="99" value="${state.selectedQuantity}">
        </label>
        <button id="addSelectedBtn" type="button">加入同桌已点</button>
      </div>
    `;
  }

  async function addSelected() {
    const product = state.selectedProduct;
    if (!product) return;
    if (!state.session || !state.session.id) {
      ui.toast("请先创建或加入桌台会话", "error");
      return;
    }
    const quantity = Math.max(1, Number(ui.$("#quantityInput").value || 1));
    const flavorSelect = ui.$("#flavorSelect");
    const payload = state.kind === "dish"
      ? { dishId: product.id, dishFlavor: flavorSelect ? flavorSelect.value : state.selectedFlavor, number: quantity }
      : { setmealId: product.id, number: quantity };
    try {
      await api.addDiningCartItem(state.session.id, payload);
      ui.toast("已加入同桌已点", "ok");
      ui.closeDrawer("#productDrawer", "#drawerBackdrop");
      await loadCart();
    } catch (error) {
      ui.toast(ui.errorMessage(error), "error");
      debug(error.detail || error.message);
    }
  }

  async function createSession(mode) {
    const phone = ui.$("#phoneInput").value.trim() || state.user && state.user.phone || "";
    const tableSelect = ui.$("#tableSelect");
    const selected = tableSelect.options[tableSelect.selectedIndex];
    try {
      let response;
      if (mode === "join") {
        const sessionId = ui.$("#joinSessionInput").value.trim() || selected.dataset.session;
        if (!sessionId) {
          ui.toast("请输入要加入的桌台会话号", "error");
          return;
        }
        response = await api.joinDiningSession(sessionId);
      } else {
        response = await api.createDiningSession({
          tableId: Number(tableSelect.value),
          partySize: Number(ui.$("#guestInput").value || 1)
        });
      }
      const session = api.dataOf(response) || {};
      state.session = store.createSession(Object.assign({}, session, {
        tableNo: selected.textContent.trim(),
        phone,
        mode,
        source: "api"
      }));
      renderStatus();
      await loadCart();
      ui.toast(mode === "join" ? "已加入桌台会话" : "桌台会话已创建", "ok");
      debug(response);
    } catch (error) {
      ui.toast(ui.errorMessage(error), "error");
      debug(error.detail || error.message);
    }
  }

  async function ensureAddress() {
    try {
      const response = await api.defaultAddress();
      const data = api.dataOf(response);
      if (data && data.id) return data.id;
    } catch (error) {
      debug(error.detail || error.message);
    }
    const response = await api.saveAddress({
      userId: state.user && state.user.id,
      consignee: ui.$("#consigneeInput").value.trim() || "演示顾客",
      sex: "未知",
      phone: ui.$("#addressPhoneInput").value.trim() || ui.$("#phoneInput").value.trim(),
      provinceName: "",
      cityName: "",
      districtName: "",
      detail: (state.session ? state.session.tableNo + " " : "") + ui.$("#addressDetailInput").value.trim(),
      label: "堂食",
      isDefault: 1
    });
    const created = api.dataOf(response);
    if (created && created.id) return created.id;
    const listResponse = await api.addresses();
    const addresses = api.dataOf(listResponse) || [];
    const preferred = addresses.find((item) => item.isDefault === 1) || addresses[addresses.length - 1];
    return preferred && preferred.id ? preferred.id : 7001;
  }

  async function submitOrder(markPaid) {
    if (!state.session || !state.session.id) {
      ui.toast("请先创建或加入桌台会话", "error");
      return;
    }
    if (!state.cart.length) {
      ui.toast("购物车为空，无法提交订单", "error");
      return;
    }
    const button = markPaid ? ui.$("#simulatePaidBtn") : ui.$("#confirmSubmitBtn");
    ui.setBusy(button, true);
    try {
      const response = await api.submitDiningOrder(state.session.id);
      const order = api.dataOf(response) || {};
      state.lastOrder = order;
      const paymentResponse = await api.prepay(order.orderId || order.id);
      const payment = api.dataOf(paymentResponse);
      if (markPaid && payment && payment.paymentId) {
        await api.confirmPayment(payment.paymentId);
      }
      ui.toast(markPaid ? "订单已提交，支付状态已模拟为已支付" : "订单已提交，待支付", "ok");
      ui.closeDrawer("#orderDrawer", "#drawerBackdrop");
      await loadCart();
      debug({ order: response, payment: paymentResponse });
    } catch (error) {
      ui.toast(ui.errorMessage(error), "error");
      debug(error.detail || error.message);
    } finally {
      ui.setBusy(button, false);
    }
  }

  function bindEvents() {
    ui.$("#loginUserBtn").addEventListener("click", async () => {
      const button = ui.$("#loginUserBtn");
      ui.setBusy(button, true, "登录中...");
      try {
        const data = await api.userLogin(ui.$("#phoneInput").value.trim(), ui.$("#nameInput").value.trim());
        state.user = store.saveUser(data);
        ui.$("#consigneeInput").value = data.name || "演示顾客";
        ui.$("#addressPhoneInput").value = data.phone || ui.$("#phoneInput").value;
        ui.toast("登录成功", "ok");
        debug(data);
        await refreshAll();
      } catch (error) {
        ui.toast(ui.errorMessage(error), "error");
        debug(error.detail || error.message);
      } finally {
        ui.setBusy(button, false);
      }
    });

    ui.$("#logoutUserBtn").addEventListener("click", () => {
      api.logout("user");
      state.user = null;
      store.remove("user");
      renderStatus();
      ui.toast("已退出登录");
    });

    ui.$("#refreshUserBtn").addEventListener("click", refreshAll);
    ui.$("#createSessionBtn").addEventListener("click", () => createSession("create"));
    ui.$("#joinSessionBtn").addEventListener("click", () => createSession("join"));
    ui.$("#clearSessionBtn").addEventListener("click", () => {
      store.clearSession();
      state.session = null;
      renderStatus();
    });
    ui.$("#sameTableBtn").addEventListener("click", () => {
      renderCart();
      ui.openDrawer("#cartDrawer", "#drawerBackdrop");
    });

    ui.$("#kindTabs").addEventListener("click", async (event) => {
      const button = event.target.closest("[data-kind]");
      if (!button) return;
      state.kind = button.dataset.kind;
      state.categoryId = "";
      ui.$all("#kindTabs .chip").forEach((item) => item.classList.toggle("active", item === button));
      await loadCategories();
      await loadProducts();
    });

    ui.$("#categoryRail").addEventListener("click", async (event) => {
      const button = event.target.closest("[data-category]");
      if (!button) return;
      state.categoryId = button.dataset.category;
      renderCategories();
      await loadProducts();
    });

    ui.$("#searchInput").addEventListener("input", renderProducts);
    ui.$("#productList").addEventListener("click", (event) => {
      const button = event.target.closest("[data-product]");
      if (button) openProduct(button.dataset.product);
    });

    document.body.addEventListener("click", (event) => {
      const closeButton = event.target.closest("[data-close-drawer]");
      if (closeButton) ui.closeDrawer(closeButton.dataset.closeDrawer, "#drawerBackdrop");
    });
    ui.$("#drawerBackdrop").addEventListener("click", () => {
      ui.closeDrawer("#productDrawer", "#drawerBackdrop");
      ui.closeDrawer("#cartDrawer", "#drawerBackdrop");
      ui.closeDrawer("#orderDrawer", "#drawerBackdrop");
    });
    ui.$("#productDrawer").addEventListener("click", (event) => {
      if (event.target.closest("#addSelectedBtn")) addSelected();
    });

    ui.$("#openCartBtn").addEventListener("click", () => {
      renderCart();
      ui.openDrawer("#cartDrawer", "#drawerBackdrop");
    });
    ui.$("#cartDrawer").addEventListener("click", async (event) => {
      if (!state.session || !state.session.id) return;
      const inc = event.target.closest("[data-cart-inc]");
      const dec = event.target.closest("[data-cart-dec]");
      const button = inc || dec;
      if (!button) return;
      const itemId = button.dataset.cartInc || button.dataset.cartDec;
      const current = Number(button.dataset.number || 1);
      try {
        if (inc) {
          await api.updateDiningCartItem(state.session.id, itemId, current + 1);
        } else if (current <= 1) {
          await api.deleteDiningCartItem(state.session.id, itemId);
        } else {
          await api.updateDiningCartItem(state.session.id, itemId, current - 1);
        }
        await loadCart();
      } catch (error) {
        ui.toast(ui.errorMessage(error), "error");
        debug(error.detail || error.message);
      }
    });
    ui.$("#submitOrderOpenBtn").addEventListener("click", () => ui.openDrawer("#orderDrawer", "#drawerBackdrop"));
    ui.$("#submitOrderBtn").addEventListener("click", () => ui.openDrawer("#orderDrawer", "#drawerBackdrop"));
    ui.$("#cleanCartBtn").addEventListener("click", async () => {
      try {
        if (!state.session || !state.session.id) {
          ui.toast("请先创建或加入桌台会话", "error");
          return;
        }
        await api.clearDiningCart(state.session.id);
        await loadCart();
        ui.toast("购物车已清空", "ok");
      } catch (error) {
        ui.toast(ui.errorMessage(error), "error");
      }
    });
    ui.$("#confirmSubmitBtn").addEventListener("click", () => submitOrder(false));
    ui.$("#simulatePaidBtn").addEventListener("click", () => submitOrder(true));
  }

  document.addEventListener("DOMContentLoaded", () => {
    bindEvents();
    renderStatus();
    refreshAll();
  });
})();
