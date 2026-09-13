/* src/main/resources/static/js/admin/certificates.js */
let currentPage = 0;

document.addEventListener("DOMContentLoaded", async () => {
  renderSidebar("certificates");
  await loadTemplatesFilter();
  loadCertificates(0);

  document.getElementById("searchInput").addEventListener("input", debounce(() => loadCertificates(0), 400));
  document.getElementById("statusFilter").addEventListener("change", () => loadCertificates(0));
  document.getElementById("templateFilter").addEventListener("change", () => loadCertificates(0));
});

function debounce(func, wait) {
  let timeout;
  return function(...args) {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(this, args), wait);
  };
}

async function loadTemplatesFilter() {
  try {
    const templates = await apiRequest("/admin/templates");
    const select = document.getElementById("templateFilter");
    select.innerHTML = '<option value="">All Templates</option>' +
      templates.map(t => `<option value="${t.id}">${t.templateName}</option>`).join("");
  } catch (err) {
    console.error("Failed to load template filter:", err);
  }
}

async function loadCertificates(page = 0) {
  currentPage = page;
  const query = document.getElementById("searchInput").value;
  const status = document.getElementById("statusFilter").value;
  const templateId = document.getElementById("templateFilter").value;

  try {
    showLoader();
    let url = `/admin/certificates?page=${page}&size=10`;
    if (query) url += `&query=${encodeURIComponent(query)}`;
    if (status) url += `&status=${encodeURIComponent(status)}`;
    if (templateId) url += `&templateId=${encodeURIComponent(templateId)}`;

    const pageData = await apiRequest(url);
    renderCertificatesTable(pageData.content);
    renderPagination("paginationContainer", pageData, (newPage) => loadCertificates(newPage));
  } catch (err) {
    showError("Failed to fetch certificates: " + err.message);
  } finally {
    hideLoader();
  }
}

function renderCertificatesTable(certificates) {
  const tbody = document.getElementById("certificatesTableBody");
  if (!certificates || certificates.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7"><div class="empty-state">No certificates found</div></td></tr>`;
    return;
  }

  tbody.innerHTML = certificates.map(c => `
    <tr>
      <td><strong>${escapeHtml(c.certificateNumber)}</strong></td>
      <td>${escapeHtml(c.studentName)} (${escapeHtml(c.studentRollNumber)})</td>
      <td>${escapeHtml(c.certificateTitle)}</td>
      <td>${Utils.formatDate(c.issueDate)}</td>
      <td><span class="badge ${Utils.getStatusBadgeClass(c.status)}">${escapeHtml(c.status)}</span></td>
      <td>
        <a href="${c.verificationUrl}" target="_blank" style="font-size: 0.8rem;" class="btn btn-ghost btn-sm">🔍 Verify</a>
      </td>
      <td>
        <div class="table-actions">
          <a href="/api/certificates/${c.id}/download" target="_blank" class="btn btn-primary btn-sm">📥 Download PDF</a>
          ${c.status === 'ACTIVE' ? `
            <button onclick="revokeCert(${c.id}, '${escapeHtml(c.certificateNumber)}')" class="btn btn-danger btn-sm">Revoke</button>
          ` : `
            <button onclick="activateCert(${c.id}, '${escapeHtml(c.certificateNumber)}')" class="btn btn-success btn-sm">Active</button>
          `}
        </div>
      </td>
    </tr>
  `).join("");
}

function activateCert(id, certNum) {
  showConfirm({
    title: "Mark Certificate as Active?",
    message: "This certificate is currently revoked.\nRestoring it will make the certificate valid again during public verification.",
    confirmText: "Mark Active",
    cancelText: "Cancel",
    onConfirm: async () => {
      try {
        showLoader();
        await apiRequest(`/admin/certificates/${id}/activate`, {
          method: "PUT"
        });
        showModal({
          title: "Certificate Activated",
          message: "The certificate is active again and can now be successfully verified.",
          type: "success",
          confirmText: "OK",
          onConfirm: () => loadCertificates(currentPage)
        });
        loadCertificates(currentPage);
      } catch (err) {
        showError("Failed to activate certificate: " + err.message);
      } finally {
        hideLoader();
      }
    }
  });
}

function revokeCert(id, certNum) {
  showConfirm({
    title: "Revoke Certificate",
    message: `Are you sure you want to revoke certificate ${certNum}? Public verification will display "Certificate Revoked".`,
    confirmText: "Revoke",
    cancelText: "Cancel",
    onConfirm: async () => {
      try {
        showLoader();
        await apiRequest(`/admin/certificates/${id}/revoke`, {
          method: "PUT",
          body: JSON.stringify({ reason: "Administrative revocation" })
        });
        showModal({
          title: "Certificate Revoked",
          message: `Certificate ${certNum} has been revoked.`,
          type: "warning",
          confirmText: "OK",
          onConfirm: () => loadCertificates(currentPage)
        });
        loadCertificates(currentPage);
      } catch (err) {
        showError("Failed to revoke certificate: " + err.message);
      } finally {
        hideLoader();
      }
    }
  });
}

function escapeHtml(str) {
  if (!str) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}
