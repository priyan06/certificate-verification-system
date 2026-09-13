/* src/main/resources/static/js/components/sidebar.js */
function renderSidebar(activeMenuId) {
  const user = Auth.getCurrentUser();
  if (!user) {
    window.location.href = APP_CONFIG.routes.login;
    return;
  }

  const isAdmin = user.role === "ADMIN";
  const container = document.getElementById("sidebarContainer");
  if (!container) return;

  const adminNav = `
    <a href="/admin/dashboard.html" class="nav-item ${activeMenuId === 'dashboard' ? 'active' : ''}">
      <span class="nav-icon">📊</span> Dashboard
    </a>
    <a href="/admin/students.html" class="nav-item ${activeMenuId === 'students' ? 'active' : ''}">
      <span class="nav-icon">👥</span> Students
    </a>
    <a href="/admin/templates.html" class="nav-item ${activeMenuId === 'templates' ? 'active' : ''}">
      <span class="nav-icon">📋</span> Certificate Templates
    </a>
    <a href="/admin/issue-certificate.html" class="nav-item ${activeMenuId === 'issue-certificate' ? 'active' : ''}">
      <span class="nav-icon">🎓</span> Issue Certificate
    </a>
    <a href="/admin/certificates.html" class="nav-item ${activeMenuId === 'certificates' ? 'active' : ''}">
      <span class="nav-icon">📜</span> Certificates
    </a>
  `;

  const studentNav = `
    <a href="/student/dashboard.html" class="nav-item ${activeMenuId === 'dashboard' ? 'active' : ''}">
      <span class="nav-icon">📊</span> Dashboard
    </a>
    <a href="/student/certificates.html" class="nav-item ${activeMenuId === 'certificates' ? 'active' : ''}">
      <span class="nav-icon">📜</span> My Certificates
    </a>
    <a href="/student/profile.html" class="nav-item ${activeMenuId === 'profile' ? 'active' : ''}">
      <span class="nav-icon">👤</span> My Profile
    </a>
    <a href="/student/change-password.html" class="nav-item ${activeMenuId === 'change-password' ? 'active' : ''}">
      <span class="nav-icon">🔑</span> Change Password
    </a>
  `;

  const sidebarHTML = `
    <aside class="sidebar">
      <div class="sidebar-brand">
        <div class="sidebar-brand-icon">✓</div>
        <div class="sidebar-brand-text">
          CertifyVerify
          <div style="font-size: 0.7rem; font-weight: normal; color: var(--text-secondary);">${isAdmin ? 'Admin Portal' : 'Student Portal'}</div>
        </div>
      </div>
      <nav class="sidebar-nav">
        ${isAdmin ? adminNav : studentNav}
      </nav>
      <div class="sidebar-footer">
        <div class="user-profile-summary">
          <div class="avatar">${(user.name || user.username || 'U').charAt(0).toUpperCase()}</div>
          <div class="user-info-text">
            <div class="user-info-name">${user.name || user.username}</div>
            <div class="user-info-role">${user.role}</div>
          </div>
        </div>
        <button id="logoutBtn" class="btn btn-secondary btn-sm btn-logout">
          🚪 Logout
        </button>
      </div>
    </aside>
  `;

  container.innerHTML = sidebarHTML;

  document.getElementById("logoutBtn").addEventListener("click", () => {
    showConfirm({
      title: "Logout Confirmation",
      message: "Are you sure you want to log out of your session?",
      confirmText: "Logout",
      onConfirm: () => Auth.logout()
    });
  });
}
