(function () {
  "use strict";

  const api = window.MealOpsApi;
  const store = window.MealOpsStore;
  const ui = window.MealOpsUI;

  const state = {
    admin: store.currentAdmin(),
    activePage: "overviewPage",
    categories: [],
    dishes: [],
    setmeals: [],
    orders: [],
    logs: []
  };

  function debug(value) {
    ui.showDebug(ui.$("#adminDebug"), value);
  }

  function renderAdminStatus() {
    const token = api.getToken("admin");
    ui.$("#adminStatus").textContent = token ? `${state.admin && state.admin.name || "管理员"} 已登录` : "未登录";
  }

  function rowsFrom(response) {
    return ui.normalizePage(api.dataOf(response)).records;
  }

  function renderTable(selector, headers, rows, emptyText) {
    const table = ui.$(selector);
    if (!rows.length) {
      table.innerHTML = `<tbody><tr><td>${ui.empty(emptyText || "暂无数据")}</td></tr></tbody>`;
      return;
    }
    table.innerHTML = `
      <thead><tr>${headers.map((item) => `<th>${ui.escapeHtml(item.label)}</th>`).join("")}</tr></thead>
      <tbody>
        ${rows.map((row) => `<tr>${headers.map((header) => `<td>${header.render ? header.render(row) : ui.escapeHtml(row[header.key])}</td>`).join("")}</tr>`).join("")}
      </tbody>
    `;
  }

  function metric(label, value, hint) {
    return `<div class="metric-card"><span>${ui.escapeHtml(label)}</span><strong>${ui.escapeHtml(value)}</strong><p class="muted">${ui.escapeHtml(hint || "")}</p></div>`;
  }

  function setPage(pageId) {
    state.activePage = pageId;
    ui.$all(".admin-page").forEach((item) => item.classList.toggle("active", item.id === pageId));
    ui.$all(".admin-nav .nav-item").forEach((item) => item.classList.toggle("active", item.dataset.target === pageId));
    refreshCurrent();
  }

  async function loadOverview() {
    const metrics = ui.$("#metricsGrid");
    metrics.innerHTML = [
      metric("分类", state.categories.length || "-", "菜品/套餐分类"),
      metric("菜品", state.dishes.length || "-", "当前分页或列表"),
      metric("套餐", state.setmeals.length || "-", "当前分页或列表"),
      metric("订单", state.orders.length || "-", "管理端订单")
    ].join("");
    try {
      const hot = await api.hotDishes({ limit: 5 });
      const rows = api.dataOf(hot) || [];
      ui.$("#overviewContent").innerHTML = rows.length ? `
        <div class="table-wrap">
          <table>
            <thead><tr><th>热销菜品</th><th>销量</th></tr></thead>
            <tbody>${rows.map((item) => `<tr><td>${ui.escapeHtml(item.name)}</td><td>${ui.escapeHtml(item.number || item.count || 0)}</td></tr>`).join("")}</tbody>
          </table>
        </div>
      ` : ui.empty("暂无热销统计", "完成订单后会产生统计。");
      debug(hot);
    } catch (error) {
      ui.$("#overviewContent").innerHTML = ui.errorState(ui.errorMessage(error));
      debug(error.detail || error.message);
    }
  }

  async function loadCategories() {
    try {
      const type = ui.$("#categoryTypeFilter").value;
      const response = type ? await api.categories(type, "admin") : await api.categoryPage({ page: 1, pageSize: 50 });
      state.categories = Array.isArray(api.dataOf(response)) ? api.dataOf(response) : rowsFrom(response);
      renderTable("#categoryTable", [
        { label: "ID", key: "id" },
        { label: "名称", key: "name" },
        { label: "类型", render: (row) => row.type === 2 ? "套餐分类" : "菜品分类" },
        { label: "排序", key: "sort" },
        { label: "状态", render: (row) => `<span class="status-pill ${row.status === 0 ? "danger" : "ok"}">${row.status === 0 ? "禁用" : "启用"}</span>` },
        { label: "操作", render: (row) => `<div class="cell-actions"><button data-edit-category="${ui.escapeHtml(row.id)}" type="button">编辑</button><button class="danger" data-delete-category="${ui.escapeHtml(row.id)}" type="button">删除</button></div>` }
      ], state.categories, "暂无分类");
      debug(response);
    } catch (error) {
      ui.$("#categoryTable").innerHTML = `<tbody><tr><td>${ui.errorState(ui.errorMessage(error))}</td></tr></tbody>`;
      debug(error.detail || error.message);
    }
  }

  async function loadDishes() {
    try {
      const response = await api.dishPage({ page: 1, pageSize: 50, name: ui.$("#dishKeyword").value.trim() });
      state.dishes = rowsFrom(response);
      renderTable("#dishTable", [
        { label: "ID", key: "id" },
        { label: "菜品", render: (row) => `<strong>${ui.escapeHtml(row.name)}</strong><p class="muted">${ui.escapeHtml(row.description || "")}</p>` },
        { label: "分类", render: (row) => ui.escapeHtml(row.categoryName || row.categoryId || "-") },
        { label: "价格", render: (row) => ui.money(row.price) },
        { label: "库存", key: "stock" },
        { label: "状态", render: (row) => `<span class="status-pill ${row.status === 0 ? "danger" : "ok"}">${row.status === 0 ? "停售" : "起售"}</span>` },
        { label: "操作", render: (row) => `<div class="cell-actions"><button data-edit-dish="${ui.escapeHtml(row.id)}" type="button">编辑</button><button class="secondary" data-toggle-dish="${ui.escapeHtml(row.id)}" data-status="${row.status === 0 ? 1 : 0}" type="button">${row.status === 0 ? "起售" : "停售"}</button></div>` }
      ], state.dishes, "暂无菜品");
      debug(response);
    } catch (error) {
      ui.$("#dishTable").innerHTML = `<tbody><tr><td>${ui.errorState(ui.errorMessage(error))}</td></tr></tbody>`;
      debug(error.detail || error.message);
    }
  }

  async function loadSetmeals() {
    try {
      const response = await api.setmealPage({ page: 1, pageSize: 50, name: ui.$("#setmealKeyword").value.trim() });
      state.setmeals = rowsFrom(response);
      renderTable("#setmealTable", [
        { label: "ID", key: "id" },
        { label: "套餐", render: (row) => `<strong>${ui.escapeHtml(row.name)}</strong><p class="muted">${ui.escapeHtml(row.description || "")}</p>` },
        { label: "分类", render: (row) => ui.escapeHtml(row.categoryName || row.categoryId || "-") },
        { label: "价格", render: (row) => ui.money(row.price) },
        { label: "状态", render: (row) => `<span class="status-pill ${row.status === 0 ? "danger" : "ok"}">${row.status === 0 ? "停售" : "起售"}</span>` },
        { label: "操作", render: (row) => `<div class="cell-actions"><button data-edit-setmeal="${ui.escapeHtml(row.id)}" type="button">编辑</button><button class="danger" data-delete-setmeal="${ui.escapeHtml(row.id)}" type="button">删除</button></div>` }
      ], state.setmeals, "暂无套餐");
      debug(response);
    } catch (error) {
      ui.$("#setmealTable").innerHTML = `<tbody><tr><td>${ui.errorState(ui.errorMessage(error))}</td></tr></tbody>`;
      debug(error.detail || error.message);
    }
  }

  async function loadOrders() {
    try {
      const params = { page: 1, pageSize: 50 };
      if (ui.$("#orderStatusFilter").value) params.status = ui.$("#orderStatusFilter").value;
      const response = await api.orders(params);
      state.orders = rowsFrom(response);
      renderTable("#orderTable", [
        { label: "订单号", render: (row) => `<strong>${ui.escapeHtml(row.number)}</strong><p class="muted">#${ui.escapeHtml(row.id)}</p>` },
        { label: "客户", render: (row) => `${ui.escapeHtml(row.consignee || row.userName || "-")}<p class="muted">${ui.escapeHtml(row.phone || "")}</p>` },
        { label: "金额", render: (row) => ui.money(row.amount) },
        { label: "订单状态", render: (row) => `<span class="status-pill">${ui.orderStatus(row.status)}</span>` },
        { label: "支付", render: (row) => `<span class="status-pill ${row.payStatus === 1 ? "ok" : "warn"}">${ui.payStatus(row.payStatus)}</span>` },
        { label: "下单时间", render: (row) => ui.dateTime(row.orderTime) },
        { label: "操作", render: (row) => `<div class="cell-actions"><button data-order-action="3" data-order-id="${ui.escapeHtml(row.id)}" type="button">确认</button><button data-order-action="4" data-order-id="${ui.escapeHtml(row.id)}" type="button">派送</button><button data-order-action="5" data-order-id="${ui.escapeHtml(row.id)}" type="button">完成</button></div>` }
      ], state.orders, "暂无订单");
      state.orders.forEach(store.paymentForOrder);
      debug(response);
    } catch (error) {
      ui.$("#orderTable").innerHTML = `<tbody><tr><td>${ui.errorState(ui.errorMessage(error))}</td></tr></tbody>`;
      debug(error.detail || error.message);
    }
  }

  async function loadTables() {
    const planned = await api.tryTableSessions({ page: 1, pageSize: 50 });
    const rows = planned.ok ? ui.normalizePage(planned.data).records : store.tableSessions();
    renderTable("#tableSessionTable", [
      { label: "会话", key: "id" },
      { label: "桌台", render: (row) => ui.escapeHtml(row.tableName || row.tableNo || row.tableId || "-") },
      { label: "人数", render: (row) => ui.escapeHtml(row.partySize || row.guests || "-") },
      { label: "开台用户", render: (row) => ui.escapeHtml(row.creatorUserId || row.phone || "-") },
      { label: "状态", render: (row) => `<span class="status-pill ${row.status === "ACTIVE" ? "ok" : "warn"}">${ui.escapeHtml(row.status || "点单中")}</span>` },
      { label: "开台时间", render: (row) => ui.dateTime(row.openedAt || row.createdAt) }
    ], rows, "暂无桌台会话");
    debug(planned.ok ? planned.data : planned.error && (planned.error.detail || planned.error.message));
  }

  async function loadPayments() {
    const planned = await api.tryPayments({ page: 1, pageSize: 50 });
    const rows = planned.ok ? ui.normalizePage(planned.data).records : store.payments();
    renderTable("#paymentTable", [
      { label: "支付单", render: (row) => ui.escapeHtml(row.paymentNo || row.id) },
      { label: "订单", render: (row) => ui.escapeHtml(row.orderId || row.orderNumber || "-") },
      { label: "金额", render: (row) => ui.money(row.amount) },
      { label: "方式", render: (row) => Number(row.payMethod || row.method) === 2 ? "支付宝" : "微信支付" },
      { label: "状态", render: (row) => `<span class="status-pill ${Number(row.status) === 1 || row.status === "已支付" ? "ok" : "warn"}">${Number(row.status) === 1 ? "已支付" : ui.escapeHtml(row.status || "待支付")}</span>` },
      { label: "支付时间", render: (row) => ui.dateTime(row.paidAt) },
      { label: "创建时间", render: (row) => ui.dateTime(row.createTime || row.createdAt) }
    ], rows, "暂无支付记录");
    debug(planned.ok ? planned.data : planned.error && (planned.error.detail || planned.error.message));
  }

  async function loadLogs() {
    try {
      const response = await api.logs({ page: 1, pageSize: 50 });
      state.logs = rowsFrom(response);
      renderTable("#logTable", [
        { label: "时间", render: (row) => ui.dateTime(row.operationTime) },
        { label: "用户", key: "operationUser" },
        { label: "类型", key: "operationType" },
        { label: "方法", key: "operationMethod" },
        { label: "结果", render: (row) => `<span class="status-pill ${String(row.operationResult || "").includes("SUCCESS") ? "ok" : ""}">${ui.escapeHtml(row.operationResult)}</span>` },
        { label: "耗时", render: (row) => `${ui.escapeHtml(row.duration || 0)} ms` }
      ], state.logs, "暂无日志");
      debug(response);
    } catch (error) {
      ui.$("#logTable").innerHTML = `<tbody><tr><td>${ui.errorState(ui.errorMessage(error))}</td></tr></tbody>`;
      debug(error.detail || error.message);
    }
  }

  async function refreshCurrent() {
    const map = {
      overviewPage: loadOverview,
      categoryPage: loadCategories,
      dishPage: loadDishes,
      setmealPage: loadSetmeals,
      tablePage: loadTables,
      orderPage: loadOrders,
      paymentPage: loadPayments,
      logPage: loadLogs
    };
    const fn = map[state.activePage] || loadOverview;
    await fn();
  }

  function formField(name, label, value, type) {
    return `<label>${ui.escapeHtml(label)}<input name="${ui.escapeHtml(name)}" type="${type || "text"}" value="${ui.escapeHtml(value || "")}"></label>`;
  }

  function openCategoryForm(row) {
    const item = row || { type: 1, sort: 99, status: 1 };
    ui.$("#adminDrawerTitle").textContent = item.id ? "编辑分类" : "新增分类";
    ui.$("#adminEditForm").innerHTML = `
      ${item.id ? formField("id", "ID", item.id) : ""}
      <label>类型<select name="type"><option value="1" ${item.type === 1 ? "selected" : ""}>菜品分类</option><option value="2" ${item.type === 2 ? "selected" : ""}>套餐分类</option></select></label>
      ${formField("name", "名称", item.name || "")}
      ${formField("sort", "排序", item.sort || 99, "number")}
      <button data-save-form="category" type="submit">保存分类</button>
    `;
    ui.openDrawer("#adminDrawer", "#adminDrawerBackdrop");
  }

  function openDishForm(row) {
    const item = row || { price: 0, stock: 100, status: 1, categoryId: 1001 };
    ui.$("#adminDrawerTitle").textContent = item.id ? "编辑菜品" : "新增菜品";
    ui.$("#adminEditForm").innerHTML = `
      ${item.id ? formField("id", "ID", item.id) : ""}
      ${formField("name", "菜品名称", item.name || "")}
      ${formField("categoryId", "分类ID", item.categoryId || 1001)}
      ${formField("price", "价格", item.price || 0, "number")}
      ${formField("stock", "库存", item.stock || 100, "number")}
      <label>状态<select name="status"><option value="1" ${item.status !== 0 ? "selected" : ""}>起售</option><option value="0" ${item.status === 0 ? "selected" : ""}>停售</option></select></label>
      <label>描述<textarea name="description">${ui.escapeHtml(item.description || "")}</textarea></label>
      <label>口味 JSON<textarea name="flavors">[{"name":"辣度","value":"[\\\"不辣\\\",\\\"微辣\\\",\\\"中辣\\\"]"}]</textarea></label>
      <button data-save-form="dish" type="submit">保存菜品</button>
    `;
    ui.openDrawer("#adminDrawer", "#adminDrawerBackdrop");
  }

  function openSetmealForm(row) {
    const item = row || { price: 0, status: 1, categoryId: 2001 };
    ui.$("#adminDrawerTitle").textContent = item.id ? "编辑套餐" : "新增套餐";
    ui.$("#adminEditForm").innerHTML = `
      ${item.id ? formField("id", "ID", item.id) : ""}
      ${formField("name", "套餐名称", item.name || "")}
      ${formField("categoryId", "分类ID", item.categoryId || 2001)}
      ${formField("price", "价格", item.price || 0, "number")}
      <label>状态<select name="status"><option value="1" ${item.status !== 0 ? "selected" : ""}>起售</option><option value="0" ${item.status === 0 ? "selected" : ""}>停售</option></select></label>
      <label>描述<textarea name="description">${ui.escapeHtml(item.description || "")}</textarea></label>
      <label>套餐菜品 JSON<textarea name="setmealDishes">[{"dishId":3001,"name":"宫保鸡丁","price":28,"copies":1}]</textarea></label>
      <button data-save-form="setmeal" type="submit">保存套餐</button>
    `;
    ui.openDrawer("#adminDrawer", "#adminDrawerBackdrop");
  }

  function formPayload(form) {
    const data = new FormData(form);
    const payload = {};
    data.forEach((value, key) => {
      payload[key] = value;
    });
    ["id", "type", "sort", "categoryId", "status", "stock"].forEach((key) => {
      if (payload[key] !== undefined && payload[key] !== "") payload[key] = Number(payload[key]);
    });
    if (payload.price !== undefined) payload.price = Number(payload.price);
    if (payload.flavors) {
      try { payload.flavors = JSON.parse(payload.flavors); } catch (error) { payload.flavors = []; }
    }
    if (payload.setmealDishes) {
      try { payload.setmealDishes = JSON.parse(payload.setmealDishes); } catch (error) { payload.setmealDishes = []; }
    }
    return payload;
  }

  async function saveForm(kind, form) {
    const payload = formPayload(form);
    try {
      const response = kind === "category" ? await api.saveCategory(payload) : kind === "dish" ? await api.saveDish(payload) : await api.saveSetmeal(payload);
      ui.toast("保存成功", "ok");
      ui.closeDrawer("#adminDrawer", "#adminDrawerBackdrop");
      debug(response);
      await refreshCurrent();
    } catch (error) {
      ui.toast(ui.errorMessage(error), "error");
      debug(error.detail || error.message);
    }
  }

  function bindEvents() {
    ui.$all(".admin-nav .nav-item").forEach((button) => button.addEventListener("click", () => setPage(button.dataset.target)));
    ui.$("#adminLoginBtn").addEventListener("click", async () => {
      const button = ui.$("#adminLoginBtn");
      ui.setBusy(button, true, "登录中...");
      try {
        const data = await api.adminLogin(ui.$("#adminUsername").value.trim(), ui.$("#adminPassword").value);
        state.admin = store.saveAdmin(data);
        renderAdminStatus();
        ui.toast("管理员登录成功", "ok");
        debug(data);
        await refreshCurrent();
      } catch (error) {
        ui.toast(ui.errorMessage(error), "error");
        debug(error.detail || error.message);
      } finally {
        ui.setBusy(button, false);
      }
    });
    ui.$("#adminLogoutBtn").addEventListener("click", () => {
      api.logout("admin");
      store.remove("admin");
      state.admin = null;
      renderAdminStatus();
      ui.toast("已退出登录");
    });
    ui.$("#adminHealthBtn").addEventListener("click", async () => {
      try {
        const response = await api.health();
        ui.toast("服务健康", "ok");
        debug(response);
      } catch (error) {
        ui.toast(ui.errorMessage(error), "error");
      }
    });
    ui.$("#adminRefreshBtn").addEventListener("click", refreshCurrent);
    ui.$("#loadOverviewBtn").addEventListener("click", loadOverview);
    ui.$all("[data-refresh]").forEach((button) => button.addEventListener("click", refreshCurrent));
    ui.$("#categoryTypeFilter").addEventListener("change", loadCategories);
    ui.$("#dishKeyword").addEventListener("input", () => window.clearTimeout(state.dishTimer) || (state.dishTimer = window.setTimeout(loadDishes, 220)));
    ui.$("#setmealKeyword").addEventListener("input", () => window.clearTimeout(state.setmealTimer) || (state.setmealTimer = window.setTimeout(loadSetmeals, 220)));
    ui.$("#orderStatusFilter").addEventListener("change", loadOrders);
    ui.$("#newCategoryBtn").addEventListener("click", () => openCategoryForm());
    ui.$("#newDishBtn").addEventListener("click", () => openDishForm());
    ui.$("#newSetmealBtn").addEventListener("click", () => openSetmealForm());

    document.body.addEventListener("click", async (event) => {
      const close = event.target.closest("[data-close-drawer]");
      if (close) ui.closeDrawer(close.dataset.closeDrawer, "#adminDrawerBackdrop");
      const categoryId = event.target.closest("[data-edit-category]") && event.target.closest("[data-edit-category]").dataset.editCategory;
      const dishId = event.target.closest("[data-edit-dish]") && event.target.closest("[data-edit-dish]").dataset.editDish;
      const setmealId = event.target.closest("[data-edit-setmeal]") && event.target.closest("[data-edit-setmeal]").dataset.editSetmeal;
      if (categoryId) openCategoryForm(state.categories.find((item) => String(item.id) === String(categoryId)));
      if (dishId) openDishForm(state.dishes.find((item) => String(item.id) === String(dishId)));
      if (setmealId) openSetmealForm(state.setmeals.find((item) => String(item.id) === String(setmealId)));

      const deleteCategory = event.target.closest("[data-delete-category]");
      if (deleteCategory && window.confirm("确认删除该分类？仍有关联商品时后端会拒绝。")) {
        try {
          const response = await api.deleteCategory(deleteCategory.dataset.deleteCategory);
          ui.toast("分类已删除", "ok");
          debug(response);
          await loadCategories();
        } catch (error) {
          ui.toast(ui.errorMessage(error), "error");
          debug(error.detail || error.message);
        }
      }

      const toggleDish = event.target.closest("[data-toggle-dish]");
      if (toggleDish) {
        try {
          const response = await api.dishStatus(toggleDish.dataset.toggleDish, toggleDish.dataset.status);
          ui.toast("菜品状态已更新", "ok");
          debug(response);
          await loadDishes();
        } catch (error) {
          ui.toast(ui.errorMessage(error), "error");
          debug(error.detail || error.message);
        }
      }

      const deleteSetmeal = event.target.closest("[data-delete-setmeal]");
      if (deleteSetmeal && window.confirm("确认删除该套餐？起售套餐后端会拒绝删除。")) {
        try {
          const response = await api.deleteSetmeal(deleteSetmeal.dataset.deleteSetmeal);
          ui.toast("套餐已删除", "ok");
          debug(response);
          await loadSetmeals();
        } catch (error) {
          ui.toast(ui.errorMessage(error), "error");
          debug(error.detail || error.message);
        }
      }

      const orderButton = event.target.closest("[data-order-action]");
      if (orderButton) {
        try {
          const response = await api.orderStatus(orderButton.dataset.orderId, Number(orderButton.dataset.orderAction));
          ui.toast("订单状态已更新", "ok");
          debug(response);
          await loadOrders();
        } catch (error) {
          ui.toast(ui.errorMessage(error), "error");
          debug(error.detail || error.message);
        }
      }
    });

    ui.$("#adminDrawerBackdrop").addEventListener("click", () => ui.closeDrawer("#adminDrawer", "#adminDrawerBackdrop"));
    ui.$("#adminEditForm").addEventListener("submit", (event) => {
      event.preventDefault();
      const button = event.submitter;
      if (button && button.dataset.saveForm) saveForm(button.dataset.saveForm, event.currentTarget);
    });
  }

  document.addEventListener("DOMContentLoaded", () => {
    bindEvents();
    renderAdminStatus();
    Promise.allSettled([loadCategories(), loadDishes(), loadSetmeals(), loadOrders()]).then(loadOverview);
  });
})();
