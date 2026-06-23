(function () {
  "use strict";

  const PREFIX = "mealops-demo:";

  function read(key, fallback) {
    try {
      const text = localStorage.getItem(PREFIX + key);
      return text ? JSON.parse(text) : fallback;
    } catch (error) {
      return fallback;
    }
  }

  function write(key, value) {
    localStorage.setItem(PREFIX + key, JSON.stringify(value));
    return value;
  }

  function remove(key) {
    localStorage.removeItem(PREFIX + key);
  }

  function createSession(input) {
    const session = {
      id: input.id,
      tableId: input.tableId,
      tableNo: input.tableNo || input.tableName || input.tableId,
      tableName: input.tableName || input.tableNo || input.tableId,
      guests: Number(input.guests || input.partySize || 1),
      partySize: Number(input.partySize || input.guests || 1),
      phone: input.phone || "",
      mode: input.mode || "create",
      status: input.status || "点单中",
      createdAt: input.openedAt || input.createdAt || new Date().toISOString(),
      source: input.source || "api"
    };
    write("session", session);
    const sessions = read("admin-table-sessions", []);
    const next = [session].concat(sessions.filter((item) => item.id !== session.id)).slice(0, 20);
    write("admin-table-sessions", next);
    return session;
  }

  function currentSession() {
    return read("session", null);
  }

  function clearSession() {
    remove("session");
  }

  function saveUser(user) {
    return write("user", user);
  }

  function currentUser() {
    return read("user", null);
  }

  function saveAdmin(admin) {
    return write("admin", admin);
  }

  function currentAdmin() {
    return read("admin", null);
  }

  function paymentForOrder(order) {
    if (!order) return null;
    const id = order.id || order.orderId || order.orderNumber || order.number;
    const existing = read("payments", []);
    const found = existing.find((item) => String(item.orderId) === String(id));
    if (found) return found;
    const record = {
      id: "PAY-" + Date.now().toString(36),
      orderId: id,
      orderNumber: order.orderNumber || order.number || "-",
      amount: Number(order.orderAmount || order.amount || 0),
      method: order.payMethod || 1,
      status: order.payStatus === 1 ? "已支付" : "待支付",
      createdAt: new Date().toISOString(),
      source: "模拟"
    };
    write("payments", [record].concat(existing).slice(0, 30));
    return record;
  }

  function payments() {
    return read("payments", []);
  }

  function markPayment(orderId, status) {
    const list = payments().map((item) => String(item.orderId) === String(orderId) ? Object.assign({}, item, { status }) : item);
    return write("payments", list);
  }

  function tableSessions() {
    return read("admin-table-sessions", []);
  }

  window.MealOpsStore = {
    read,
    write,
    remove,
    saveUser,
    currentUser,
    saveAdmin,
    currentAdmin,
    createSession,
    currentSession,
    clearSession,
    paymentForOrder,
    payments,
    markPayment,
    tableSessions
  };
})();
