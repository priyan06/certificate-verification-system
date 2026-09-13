/* src/main/resources/static/js/common/utils.js */
const Utils = {
  formatDate(dateStr) {
    if (!dateStr) return "-";
    const date = new Date(dateStr);
    return date.toLocaleDateString("en-US", { year: "numeric", month: "short", day: "numeric" });
  },

  formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return "-";
    const date = new Date(dateTimeStr);
    return date.toLocaleDateString("en-US", { year: "numeric", month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" });
  },

  getUrlParam(paramName) {
    const params = new URLSearchParams(window.location.search);
    return params.get(paramName);
  },

  getStatusBadgeClass(status) {
    if (!status) return "badge-neutral";
    switch (status.toUpperCase()) {
      case "ACTIVE":
      case "VALID":
        return "badge-success";
      case "REVOKED":
      case "INACTIVE":
        return "badge-danger";
      case "EXPIRED":
      case "WARNING":
        return "badge-warning";
      default:
        return "badge-neutral";
    }
  }
};
