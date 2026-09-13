/* src/main/resources/static/js/student/certificates.js */
document.addEventListener("DOMContentLoaded", async () => {
  renderSidebar("certificates");
  await loadMyCertificates();
});

async function loadMyCertificates() {
  try {
    showLoader();
    const certificates = await apiRequest("/student/certificates");
    renderCertificatesList(certificates);
  } catch (err) {
    showError("Failed to fetch certificates: " + err.message);
  } finally {
    hideLoader();
  }
}

function renderCertificatesList(certificates) {
  const container = document.getElementById("certificatesGrid");
  if (!certificates || certificates.length === 0) {
    container.innerHTML = `<div class="empty-state"><div class="empty-state-icon">📜</div><p>No certificates found for your account.</p></div>`;
    return;
  }

  container.innerHTML = certificates.map(c => `
    <div class="card" style="margin-bottom: var(--spacing-lg);">
      <div class="card-header">
        <div>
          <h3 class="card-title" style="font-size: 1.2rem; color: var(--accent-primary);">${c.certificateTitle}</h3>
          <p class="text-secondary" style="font-size: 0.85rem;">Certificate Number: ${c.certificateNumber}</p>
        </div>
        <span class="badge ${Utils.getStatusBadgeClass(c.status)}">${c.status}</span>
      </div>
      <p style="margin-bottom: var(--spacing-md); line-height: 1.6;">${c.description || ''}</p>
      
      <div style="background: var(--bg-tertiary); padding: var(--spacing-md); border-radius: var(--radius-md); margin-bottom: var(--spacing-md); font-size: 0.85rem;">
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 8px;">
          <div><strong>Signatory 1:</strong> ${c.signatoryOneName || c.signatoryName || ''}</div>
          ${c.signatoryTwoName ? `<div><strong>Signatory 2:</strong> ${c.signatoryTwoName}</div>` : ''}
          <div><strong>Issue Date:</strong> ${Utils.formatDate(c.issueDate)}</div>
          <div><strong>Verification Code:</strong> ${c.verificationCode}</div>
        </div>
      </div>

      <div style="display: flex; gap: var(--spacing-md); flex-wrap: wrap; align-items: center;">
        <a href="/api/certificates/${c.id}/download" target="_blank" class="btn btn-primary">
          📥 Download Official PDF
        </a>
        <a href="${c.verificationUrl}" target="_blank" class="btn btn-secondary">
          🔍 Verify Authenticity URL
        </a>
      </div>
    </div>
  `).join("");
}
