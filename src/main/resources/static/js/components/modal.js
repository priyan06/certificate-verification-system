/* src/main/resources/static/js/components/modal.js */
(function() {
  // Create modal DOM structure on load
  document.addEventListener("DOMContentLoaded", () => {
    if (!document.getElementById("commonModalOverlay")) {
      const modalHTML = `
        <div id="commonModalOverlay" class="modal-overlay">
          <div class="modal-card">
            <div class="modal-header">
              <div id="modalIcon" class="modal-icon info">ℹ</div>
              <div id="modalTitle" class="modal-title">Title</div>
            </div>
            <div id="modalBody" class="modal-body">Message body</div>
            <div id="modalFooter" class="modal-footer">
              <button id="modalCancelBtn" class="btn btn-secondary">Cancel</button>
              <button id="modalConfirmBtn" class="btn btn-primary">OK</button>
            </div>
          </div>
        </div>
      `;
      document.body.insertAdjacentHTML("beforeend", modalHTML);
    }
  });
})();

function showModal({ title, message, type = "info", confirmText = "OK", cancelText = null, onConfirm = null, onCancel = null }) {
  const overlay = document.getElementById("commonModalOverlay");
  const iconEl = document.getElementById("modalIcon");
  const titleEl = document.getElementById("modalTitle");
  const bodyEl = document.getElementById("modalBody");
  const confirmBtn = document.getElementById("modalConfirmBtn");
  const cancelBtn = document.getElementById("modalCancelBtn");

  titleEl.textContent = title;
  bodyEl.textContent = message;

  // Icon & Type
  iconEl.className = `modal-icon ${type}`;
  switch(type) {
    case "success": iconEl.textContent = "✓"; break;
    case "error": iconEl.textContent = "✕"; break;
    case "warning": iconEl.textContent = "⚠"; break;
    case "info": default: iconEl.textContent = "ℹ"; break;
  }

  // Buttons
  confirmBtn.textContent = confirmText;
  if (cancelText) {
    cancelBtn.style.display = "inline-flex";
    cancelBtn.textContent = cancelText;
  } else {
    cancelBtn.style.display = "none";
  }

  // Event handlers
  const close = () => {
    overlay.classList.remove("active");
  };

  confirmBtn.onclick = () => {
    close();
    if (onConfirm) onConfirm();
  };

  cancelBtn.onclick = () => {
    close();
    if (onCancel) onCancel();
  };

  overlay.classList.add("active");
}

function showSuccess(message, onConfirm = null) {
  showModal({ title: "Success", message, type: "success", confirmText: "OK", onConfirm });
}

function showError(message, onConfirm = null) {
  showModal({ title: "Error", message, type: "error", confirmText: "OK", onConfirm });
}

function showWarning(message, onConfirm = null) {
  showModal({ title: "Warning", message, type: "warning", confirmText: "OK", onConfirm });
}

function showInfo(message, onConfirm = null) {
  showModal({ title: "Notice", message, type: "info", confirmText: "OK", onConfirm });
}

function showConfirm({ title = "Confirm Action", message, confirmText = "Confirm", cancelText = "Cancel", onConfirm, onCancel }) {
  showModal({ title, message, type: "warning", confirmText, cancelText, onConfirm, onCancel });
}
