/* src/main/resources/static/js/student/dashboard.js */
document.addEventListener("DOMContentLoaded", async () => {
  renderSidebar("dashboard");
  await loadStudentDashboard();
});

async function loadStudentDashboard() {
  try {
    showLoader();
    const profile = await apiRequest("/student/profile");
    const certificates = await apiRequest("/student/certificates");

    document.getElementById("studentWelcomeName").textContent = profile.name;
    document.getElementById("studentRollNumber").textContent = profile.rollNumber;
    document.getElementById("studentCourse").textContent = profile.course;
    document.getElementById("totalCertificatesCount").textContent = certificates.length;

    renderRecentCertificates(certificates);
  } catch (err) {
    showError("Failed to load dashboard: " + err.message);
  } finally {
    hideLoader();
  }
}

function renderRecentCertificates(certificates) {
  const container = document.getElementById("certificatesContainer");
  if (!certificates || certificates.length === 0) {
    container.innerHTML = `<div class="empty-state"><div class="empty-state-icon">📜</div><p>No certificates have been issued yet.</p></div>`;
    return;
  }

  container.innerHTML = certificates.map(c => `
    <div class="card" style="margin-bottom: var(--spacing-md);">
      <div class="card-header">
        <div>
          <h4 class="card-title">${c.certificateTitle}</h4>
          <p class="text-secondary" style="font-size: 0.85rem;">Certificate No: ${c.certificateNumber} | Issued: ${Utils.formatDate(c.issueDate)}</p>
        </div>
        <span class="badge ${Utils.getStatusBadgeClass(c.status)}">${c.status}</span>
      </div>
      <p style="font-size: 0.9rem; margin-bottom: var(--spacing-md);">${c.description || ''}</p>
      <div style="display: flex; gap: var(--spacing-md); align-items: center;">
        <a href="/api/certificates/${c.id}/download" target="_blank" class="btn btn-primary btn-sm">📥 Download Certificate PDF</a>
        <a href="${c.verificationUrl}" target="_blank" class="btn btn-secondary btn-sm">🔍 View Public Verification</a>
      </div>
    </div>
  `).join("");
}
