/* src/main/resources/static/js/components/loader.js */
(function() {
  document.addEventListener("DOMContentLoaded", () => {
    if (!document.getElementById("globalLoaderOverlay")) {
      const loaderHTML = `
        <div id="globalLoaderOverlay" class="modal-overlay">
          <div style="background: var(--bg-secondary); border: 1px solid var(--border-color); padding: 24px 36px; border-radius: var(--radius-lg); display: flex; align-items: center; gap: 16px;">
            <div class="loader-spinner"></div>
            <span style="font-weight: 600; color: var(--text-primary);">Processing, please wait...</span>
          </div>
        </div>
      `;
      document.body.insertAdjacentHTML("beforeend", loaderHTML);
    }
  });
})();

function showLoader() {
  const loader = document.getElementById("globalLoaderOverlay");
  if (loader) loader.classList.add("active");
}

function hideLoader() {
  const loader = document.getElementById("globalLoaderOverlay");
  if (loader) loader.classList.remove("active");
}
