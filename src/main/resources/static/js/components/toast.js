/* src/main/resources/static/js/components/toast.js */
(function() {
  document.addEventListener("DOMContentLoaded", () => {
    if (!document.getElementById("toastContainer")) {
      const div = document.createElement("div");
      div.id = "toastContainer";
      div.className = "toast-container";
      document.body.appendChild(div);
    }
  });
})();

function showToast(message, type = "info", duration = 3000) {
  const container = document.getElementById("toastContainer");
  if (!container) return;

  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `<span>${message}</span>`;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = "0";
    toast.style.transition = "opacity 0.3s ease";
    setTimeout(() => toast.remove(), 300);
  }, duration);
}
