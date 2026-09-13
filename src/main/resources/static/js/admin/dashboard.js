/* src/main/resources/static/js/admin/dashboard.js */
let statusChartInstance = null;

document.addEventListener("DOMContentLoaded", async () => {
  renderSidebar("dashboard");
  loadDashboardData();
});

async function loadDashboardData() {
  try {
    showLoader();
    // Retrieve statistics ONCE from the backend to populate cards and donut chart
    const stats = await apiRequest("/admin/dashboard/stats");
    renderDashboardCards(stats);
    renderCertificateStatusChart(stats);

    // Load recent activity feed
    await loadRecentActivity();
  } catch (err) {
    showError("Failed to load dashboard statistics: " + err.message);
  } finally {
    hideLoader();
  }
}

function renderDashboardCards(stats) {
  if (!stats) return;
  const totalCerts = stats.totalCertificates !== undefined ? stats.totalCertificates : (stats.totalCertificatesIssued || 0);

  document.getElementById("statTotalStudents").textContent = stats.totalStudents || 0;
  document.getElementById("statTotalCertificates").textContent = totalCerts;
  document.getElementById("statActiveCertificates").textContent = stats.activeCertificates || 0;
  document.getElementById("statRevokedCertificates").textContent = stats.revokedCertificates || 0;
  document.getElementById("statTotalTemplates").textContent = stats.totalTemplates || 0;
}

function renderCertificateStatusChart(stats) {
  const container = document.getElementById("statusOverviewBody");
  if (!container) return;

  const active = Number(stats?.activeCertificates) || 0;
  const revoked = Number(stats?.revokedCertificates) || 0;
  const total = active + revoked;

  // Empty State: if active and revoked are both 0
  if (total === 0) {
    if (statusChartInstance) {
      statusChartInstance.destroy();
      statusChartInstance = null;
    }
    container.innerHTML = `
      <div class="empty-state" style="padding: var(--spacing-xl) var(--spacing-md);">
        <div class="empty-state-icon">📜</div>
        <div style="font-weight: 600; color: var(--text-primary); margin-bottom: var(--spacing-xs);">
          No certificates issued yet
        </div>
        <p style="font-size: 0.85rem; color: var(--text-secondary); max-width: 240px; margin: 0 auto; line-height: 1.5;">
          Certificate status information will appear once certificates are issued.
        </p>
      </div>
    `;
    return;
  }

  // Calculate percentages
  const activePct = ((active / total) * 100).toFixed(1);
  const revokedPct = ((revoked / total) * 100).toFixed(1);

  // Read theme colors from CSS variables (no hardcoded application colors)
  const computedStyle = getComputedStyle(document.documentElement);
  const successColor = computedStyle.getPropertyValue("--success-color").trim() || "#22C55E";
  const dangerColor = computedStyle.getPropertyValue("--danger-color").trim() || "#EF4444";
  const bgSecondary = computedStyle.getPropertyValue("--bg-secondary").trim() || "#18181B";
  const textPrimary = computedStyle.getPropertyValue("--text-primary").trim() || "#FAFAFA";
  const textSecondary = computedStyle.getPropertyValue("--text-secondary").trim() || "#A1A1AA";

  // Build chart wrapper and textual accessible legend
  container.innerHTML = `
    <div class="chart-wrapper">
      <canvas id="certificateStatusChart" class="chart-canvas" aria-label="Certificate Status Donut Chart" role="img"></canvas>
      <div class="chart-center-overlay" aria-hidden="true">
        <span class="chart-center-number">${total}</span>
        <span class="chart-center-label">Total Issued</span>
      </div>
    </div>

    <!-- Textual accessible legend with counts & percentages -->
    <div class="chart-legend" role="region" aria-label="Certificate Status Legend">
      <div class="legend-row">
        <div class="legend-label-group">
          <span class="legend-dot legend-dot-active" aria-hidden="true"></span>
          <span class="legend-name">Active</span>
        </div>
        <div class="legend-values">
          <span class="legend-count">${active}</span>
          <span class="legend-pct">${activePct}%</span>
        </div>
      </div>

      <div class="legend-row">
        <div class="legend-label-group">
          <span class="legend-dot legend-dot-revoked" aria-hidden="true"></span>
          <span class="legend-name">Revoked</span>
        </div>
        <div class="legend-values">
          <span class="legend-count">${revoked}</span>
          <span class="legend-pct">${revokedPct}%</span>
        </div>
      </div>
    </div>
  `;

  const canvas = document.getElementById("certificateStatusChart");
  if (!canvas) return;

  if (typeof Chart !== "undefined") {
    if (statusChartInstance) {
      statusChartInstance.destroy();
    }

    const ctx = canvas.getContext("2d");
    statusChartInstance = new Chart(ctx, {
      type: "doughnut",
      data: {
        labels: ["Active", "Revoked"],
        datasets: [{
          data: [active, revoked],
          backgroundColor: [successColor, dangerColor],
          borderColor: bgSecondary,
          borderWidth: 2,
          hoverOffset: 4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: "70%",
        animation: {
          duration: 350
        },
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            backgroundColor: bgSecondary,
            titleColor: textPrimary,
            bodyColor: textSecondary,
            borderColor: "rgba(255, 255, 255, 0.1)",
            borderWidth: 1,
            padding: 10,
            cornerRadius: 8,
            boxPadding: 4,
            callbacks: {
              label: function(context) {
                const val = context.raw || 0;
                const pct = ((val / total) * 100).toFixed(1);
                return ` ${context.label}: ${val} (${pct}%)`;
              }
            }
          }
        }
      }
    });
  } else {
    // Offline/Fallback: Clean SVG donut chart
    renderSvgDonutFallback(canvas, active, revoked, total, successColor, dangerColor);
  }
}

function renderSvgDonutFallback(canvas, active, revoked, total, successColor, dangerColor) {
  const activeRatio = active / total;
  const radius = 70;
  const strokeWidth = 26;
  const circumference = 2 * Math.PI * radius;
  const activeDash = activeRatio * circumference;

  canvas.style.display = "none";
  const parent = canvas.parentElement;

  const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
  svg.setAttribute("viewBox", "0 0 200 200");
  svg.setAttribute("width", "100%");
  svg.setAttribute("height", "100%");
  svg.innerHTML = `
    <circle cx="100" cy="100" r="${radius}" fill="none" stroke="${dangerColor}" stroke-width="${strokeWidth}" />
    <circle cx="100" cy="100" r="${radius}" fill="none" stroke="${successColor}" stroke-width="${strokeWidth}"
      stroke-dasharray="${activeDash} ${circumference}"
      stroke-dashoffset="0"
      transform="rotate(-90 100 100)" />
  `;
  parent.insertBefore(svg, parent.firstChild);
}

async function loadRecentActivity() {
  const activityList = document.getElementById("recentActivityList");
  if (!activityList) return;

  try {
    // Attempt to load audit activity logs first
    let logs = [];
    try {
      logs = await apiRequest("/admin/recent-activity");
    } catch (e) {
      // Ignore and fallback
    }

    if (Array.isArray(logs) && logs.length > 0) {
      activityList.innerHTML = logs.map(item => {
        let iconClass = "issued";
        let iconSymbol = "🎓";
        let badgeClass = "badge-success";
        let statusText = "ACTIVE";
        let actionText = escapeHtml(item.details || item.action);

        if (item.action === "CERTIFICATE_REACTIVATED") {
          iconClass = "reactivated";
          iconSymbol = "🔄";
          badgeClass = "badge-success";
          statusText = "ACTIVE";
          actionText = `Certificate <strong>${escapeHtml(item.certificateNumber)}</strong> reactivated`;
        } else if (item.action === "CERTIFICATE_REVOKED") {
          iconClass = "revoked";
          iconSymbol = "⚠️";
          badgeClass = "badge-danger";
          statusText = "REVOKED";
          actionText = `Certificate <strong>${escapeHtml(item.certificateNumber)}</strong> revoked`;
        } else if (item.action === "CERTIFICATE_ISSUED") {
          iconClass = "issued";
          iconSymbol = "🎓";
          badgeClass = "badge-success";
          statusText = "ACTIVE";
          actionText = `Certificate <strong>${escapeHtml(item.certificateNumber)}</strong> issued`;
        }

        const dateText = item.timestamp ? Utils.formatDateTime(item.timestamp) : "Recent";
        const actorText = item.performedBy ? `by ${escapeHtml(item.performedBy)}` : "";

        return `
          <div class="activity-item">
            <div class="activity-left">
              <div class="activity-icon ${iconClass}">${iconSymbol}</div>
              <div class="activity-details">
                <div class="activity-title">${actionText}</div>
                <div class="activity-meta">
                  <span>${escapeHtml(item.certificateNumber || "")}</span>
                  ${actorText ? `<span>•</span><span>${actorText}</span>` : ""}
                  <span>•</span>
                  <span>${dateText}</span>
                </div>
              </div>
            </div>
            <span class="badge ${badgeClass}">${statusText}</span>
          </div>
        `;
      }).join("");
      return;
    }

    // Fallback to recent certificates
    const data = await apiRequest("/admin/certificates?page=0&size=5");
    const certs = data?.content || [];

    if (certs.length === 0) {
      activityList.innerHTML = `
        <div class="empty-state" style="padding: var(--spacing-lg);">
          <div class="empty-state-icon">📋</div>
          <div style="font-weight: 500; color: var(--text-primary);">No recent activity</div>
          <p style="font-size: 0.8rem; color: var(--text-secondary); margin-top: 4px;">Recent certificate issuances and actions will show here.</p>
        </div>
      `;
      return;
    }

    activityList.innerHTML = certs.map(cert => {
      const isRevoked = cert.status === "REVOKED";
      const iconClass = isRevoked ? "revoked" : "issued";
      const iconSymbol = isRevoked ? "⚠️" : "🎓";
      const actionText = isRevoked
        ? `Certificate revoked for <strong>${escapeHtml(cert.studentName)}</strong>`
        : `Certificate issued to <strong>${escapeHtml(cert.studentName)}</strong>`;
      const dateText = cert.createdAt ? Utils.formatDateTime(cert.createdAt) : (cert.issueDate || "Recent");
      const badgeClass = isRevoked ? "badge-danger" : "badge-success";

      return `
        <div class="activity-item">
          <div class="activity-left">
            <div class="activity-icon ${iconClass}">${iconSymbol}</div>
            <div class="activity-details">
              <div class="activity-title">${actionText}</div>
              <div class="activity-meta">
                <span>${escapeHtml(cert.certificateNumber)}</span>
                <span>•</span>
                <span>${escapeHtml(cert.certificateTitle || "Certificate")}</span>
                <span>•</span>
                <span>${dateText}</span>
              </div>
            </div>
          </div>
          <span class="badge ${badgeClass}">${escapeHtml(cert.status)}</span>
        </div>
      `;
    }).join("");

  } catch (err) {
    console.warn("Could not load recent activity:", err);
    activityList.innerHTML = `
      <div class="empty-state" style="padding: var(--spacing-md);">
        <p style="font-size: 0.85rem; color: var(--text-secondary);">Recent activity will appear once events occur.</p>
      </div>
    `;
  }
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
