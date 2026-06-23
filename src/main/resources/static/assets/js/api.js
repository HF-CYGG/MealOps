(function () {
  "use strict";

  const TOKEN_KEYS = {
    user: "mealops-user-token",
    admin: "mealops-admin-token"
  };

  class ApiError extends Error {
    constructor(message, detail) {
      super(message);
      this.name = "ApiError";
      this.detail = detail;
    }
  }

  function baseUrl() {
    return window.location.origin.replace(/\/$/, "");
  }

  function tokenKey(role) {
    return TOKEN_KEYS[role] || TOKEN_KEYS.user;
  }

  function getToken(role) {
    return localStorage.getItem(tokenKey(role)) || "";
  }

  function setToken(role, token) {
    if (token) {
      localStorage.setItem(tokenKey(role), token);
      return;
    }
    localStorage.removeItem(tokenKey(role));
  }

  function dataOf(response) {
    if (response && Object.prototype.hasOwnProperty.call(response, "code")) {
      return response.data;
    }
    return response;
  }

  function toQuery(params) {
    const query = new URLSearchParams();
    Object.entries(params || {}).forEach(([key, value]) => {
      if (value !== undefined && value !== null && String(value) !== "") {
        query.set(key, value);
      }
    });
    const text = query.toString();
    return text ? "?" + text : "";
  }

  async function parseBody(response) {
    const text = await response.text();
    if (!text) return null;
    try {
      return JSON.parse(text);
    } catch (error) {
      return text;
    }
  }

  async function request(method, path, body, options) {
    const role = options && options.role ? options.role : "user";
    const finalPath = path + (options && options.query ? toQuery(options.query) : "");
    const headers = { Accept: "application/json" };
    if (body !== undefined) headers["Content-Type"] = "application/json";
    const token = getToken(role);
    if (token) {
      headers.Authorization = "Bearer " + token;
      headers.token = token;
    }

    const response = await fetch(baseUrl() + finalPath, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body)
    });
    const parsed = await parseBody(response);
    if (!response.ok) {
      throw new ApiError(response.status + " " + response.statusText, parsed);
    }
    if (parsed && parsed.code === 0) {
      throw new ApiError(parsed.msg || "业务请求失败", parsed);
    }
    return parsed;
  }

  function get(path, params, options) {
    return request("GET", path + toQuery(params), undefined, options);
  }

  function post(path, body, options) {
    return request("POST", path, body || {}, options);
  }

  function put(path, body, options) {
    return request("PUT", path, body || {}, options);
  }

  function del(path, params, options) {
    return request("DELETE", path + toQuery(params), undefined, options);
  }

  async function userLogin(phone, name) {
    const response = await post("/user/login", { phone, name }, { role: "user" });
    const data = dataOf(response) || {};
    setToken("user", data.token);
    return data;
  }

  async function adminLogin(username, password) {
    const response = await post("/employee/login", { username, password }, { role: "admin" });
    const data = dataOf(response) || {};
    setToken("admin", data.token);
    return data;
  }

  async function tryPlanned(method, path, body, options) {
    try {
      const response = await request(method, path, body, options);
      return { ok: true, data: dataOf(response), source: "api" };
    } catch (error) {
      return { ok: false, error, data: null, source: "local" };
    }
  }

  window.MealOpsApi = {
    ApiError,
    dataOf,
    getToken,
    setToken,
    request,
    get,
    post,
    put,
    del,
    userLogin,
    adminLogin,
    logout(role) {
      setToken(role, "");
      return role === "admin" ? post("/employee/logout", {}, { role }).catch(() => null) : post("/user/logout", {}, { role }).catch(() => null);
    },
    health() {
      return get("/health");
    },
    categories(type, role) {
      return get("/category/list", type ? { type } : {}, { role });
    },
    categoryPage(params) {
      return get("/category/page", params, { role: "admin" });
    },
    saveCategory(payload) {
      return payload.id ? put("/category", payload, { role: "admin" }) : post("/category", payload, { role: "admin" });
    },
    deleteCategory(id) {
      return del("/category", { id }, { role: "admin" });
    },
    dishList(params, role) {
      return get("/dish/list", params, { role });
    },
    dishPage(params) {
      return get("/dish/page", params, { role: "admin" });
    },
    dishDetail(id, role) {
      return get("/dish/" + encodeURIComponent(id), {}, { role });
    },
    saveDish(payload) {
      return payload.id ? put("/dish", payload, { role: "admin" }) : post("/dish", payload, { role: "admin" });
    },
    dishStatus(id, status) {
      return post("/dish/status/" + status, undefined, { role: "admin", query: { ids: id } });
    },
    deleteDish(ids) {
      return del("/dish", { ids }, { role: "admin" });
    },
    setmealList(params, role) {
      return get("/setmeal/list", params, { role });
    },
    setmealPage(params) {
      return get("/setmeal/page", params, { role: "admin" });
    },
    setmealDetail(id, role) {
      return get("/setmeal/" + encodeURIComponent(id), {}, { role });
    },
    saveSetmeal(payload) {
      return payload.id ? put("/setmeal", payload, { role: "admin" }) : post("/setmeal", payload, { role: "admin" });
    },
    deleteSetmeal(ids) {
      return del("/setmeal", { ids }, { role: "admin" });
    },
    cartList() {
      return get("/shoppingCart/list", {}, { role: "user" });
    },
    addCart(payload) {
      return post("/shoppingCart/add", payload, { role: "user" });
    },
    subCart(payload) {
      return post("/shoppingCart/sub", payload, { role: "user" });
    },
    cleanCart() {
      return del("/shoppingCart/clean", {}, { role: "user" });
    },
    addresses() {
      return get("/addressBook/list", {}, { role: "user" });
    },
    defaultAddress() {
      return get("/addressBook/default", {}, { role: "user" });
    },
    saveAddress(payload) {
      return payload.id ? put("/addressBook", payload, { role: "user" }) : post("/addressBook", payload, { role: "user" });
    },
    submitOrder(payload) {
      return post("/order/submit", payload, { role: "user" });
    },
    userOrders(params) {
      return get("/order/userPage", params, { role: "user" });
    },
    orders(params) {
      return get("/order/page", params, { role: "admin" });
    },
    orderDetail(id, role) {
      return get("/order/" + encodeURIComponent(id), {}, { role });
    },
    orderStatus(id, status) {
      return post("/order/status", { id, status }, { role: "admin" });
    },
    confirmOrder(id) {
      return put("/order/confirm", { id }, { role: "admin" });
    },
    rejectOrder(id, rejectionReason) {
      return put("/order/rejection", { id, rejectionReason }, { role: "admin" });
    },
    deliverOrder(id) {
      return put("/order/delivery/" + encodeURIComponent(id), {}, { role: "admin" });
    },
    completeOrder(id) {
      return put("/order/complete/" + encodeURIComponent(id), {}, { role: "admin" });
    },
    logs(params) {
      return get("/logs/page", params, { role: "admin" });
    },
    hotDishes(params) {
      return get("/stats/hot-dishes", params, { role: "admin" });
    },
    diningTables(params) {
      return get("/diningTables/page", params, { role: "admin" });
    },
    createDiningSession(payload) {
      return post("/diningSessions", payload, { role: "user" });
    },
    diningSession(id, role) {
      return get("/diningSessions/" + encodeURIComponent(id), {}, { role: role || "user" });
    },
    diningSessions(params) {
      return get("/diningSessions/page", params, { role: "admin" });
    },
    joinDiningSession(id) {
      return post("/diningSessions/" + encodeURIComponent(id) + "/join", {}, { role: "user" });
    },
    diningCart(id) {
      return get("/diningSessions/" + encodeURIComponent(id) + "/cart", {}, { role: "user" });
    },
    addDiningCartItem(id, payload) {
      return post("/diningSessions/" + encodeURIComponent(id) + "/cart/items", payload, { role: "user" });
    },
    updateDiningCartItem(sessionId, itemId, number) {
      return request("PATCH", "/diningSessions/" + encodeURIComponent(sessionId) + "/cart/items/" + encodeURIComponent(itemId), { number }, { role: "user" });
    },
    deleteDiningCartItem(sessionId, itemId) {
      return del("/diningSessions/" + encodeURIComponent(sessionId) + "/cart/items/" + encodeURIComponent(itemId), {}, { role: "user" });
    },
    clearDiningCart(id) {
      return del("/diningSessions/" + encodeURIComponent(id) + "/cart", {}, { role: "user" });
    },
    submitDiningOrder(id) {
      return post("/diningSessions/" + encodeURIComponent(id) + "/orders", {}, { role: "user" });
    },
    prepay(orderId) {
      return post("/orders/" + encodeURIComponent(orderId) + "/payments/prepay", {}, { role: "user" });
    },
    paymentByOrder(orderId, role) {
      return get("/orders/" + encodeURIComponent(orderId) + "/payment", {}, { role: role || "user" });
    },
    confirmPayment(paymentId) {
      return post("/payments/" + encodeURIComponent(paymentId) + "/confirm", {}, { role: "user" });
    },
    payments(params) {
      return get("/payments/page", params, { role: "admin" });
    },
    catalogItems(params, role) {
      return get("/catalog/items", params, { role: role || "user" });
    },
    tryTableSession(payload) {
      return tryPlanned("POST", "/diningSessions", payload, { role: "user" });
    },
    tryTableSessions(params) {
      return tryPlanned("GET", "/diningSessions/page" + toQuery(params), undefined, { role: "admin" });
    },
    tryPayments(params) {
      return tryPlanned("GET", "/payments/page" + toQuery(params), undefined, { role: "admin" });
    }
  };
})();
