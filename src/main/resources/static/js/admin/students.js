/* src/main/resources/static/js/admin/students.js */
const DEFAULT_STUDENT_STATUS = "ACTIVE";
let currentPage = 0;

document.addEventListener("DOMContentLoaded", () => {
  renderSidebar("students");

  // Status filter defaults to Active on every navigation
  const statusFilter = document.getElementById("statusFilter");
  if (statusFilter) {
    statusFilter.value = DEFAULT_STUDENT_STATUS;
  }

  loadStudents(0);

  document.getElementById("searchInput").addEventListener("input", debounce(() => loadStudents(0), 400));
  statusFilter.addEventListener("change", () => loadStudents(0));
});

function debounce(func, wait) {
  let timeout;
  return function(...args) {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(this, args), wait);
  };
}

async function loadStudents(page = 0) {
  currentPage = page;
  const query = document.getElementById("searchInput").value.trim();
  const status = document.getElementById("statusFilter").value;

  try {
    showLoader();
    let url = `/admin/students?page=${page}&size=10`;
    if (query) url += `&query=${encodeURIComponent(query)}`;
    if (status) url += `&status=${encodeURIComponent(status)}`;

    const pageData = await apiRequest(url);
    renderStudentsTable(pageData.content);
    renderPagination("paginationContainer", pageData, (newPage) => loadStudents(newPage));
  } catch (err) {
    showError("Failed to fetch students: " + err.message);
  } finally {
    hideLoader();
  }
}

function renderStudentsTable(students) {
  const tbody = document.getElementById("studentsTableBody");
  if (!students || students.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6"><div class="empty-state">No students found</div></td></tr>`;
    return;
  }

  tbody.innerHTML = students.map(s => `
    <tr>
      <td><strong>${escapeHtml(s.registerNumber || "-")}</strong></td>
      <td>${escapeHtml(s.name)}</td>
      <td>${escapeHtml(s.course || "-")}</td>
      <td>${escapeHtml(s.department || "-")}</td>
      <td>${escapeHtml(s.email)}</td>
      <td>
        <div class="table-actions">
          <a href="/admin/student-form.html?id=${s.id}" class="btn btn-secondary btn-sm">Edit</a>
          <button onclick="toggleStatus(${s.id}, '${escapeJs(s.name)}', '${s.status}')" class="btn btn-secondary btn-sm">
            ${s.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
          </button>
          <button onclick="resetPassword(${s.id}, '${escapeJs(s.name)}')" class="btn btn-ghost btn-sm" title="Reset Password">🔑</button>
        </div>
      </td>
    </tr>
  `).join("");
}

async function toggleStatus(id, name, currentStatus) {
  const newStatus = currentStatus === 'ACTIVE' ? 'Deactivate' : 'Activate';
  showConfirm({
    title: `${newStatus} Account`,
    message: `Are you sure you want to ${newStatus.toLowerCase()} student ${name}?`,
    confirmText: newStatus,
    onConfirm: async () => {
      try {
        showLoader();
        await apiRequest(`/admin/students/${id}/status`, { method: "PUT" });
        showSuccess(`Student ${name} status updated successfully.`);
        loadStudents(currentPage);
      } catch (err) {
        showError("Failed to update status: " + err.message);
      } finally {
        hideLoader();
      }
    }
  });
}

async function resetPassword(id, name) {
  showConfirm({
    title: "Reset Password",
    message: `Are you sure you want to reset the password for ${name}? A new temporary password will be automatically generated.`,
    confirmText: "Reset Password",
    onConfirm: async () => {
      try {
        showLoader();
        const res = await apiRequest(`/admin/students/${id}/reset-password`, {
          method: "PUT"
        });
        const tempPass = (res && res.temporaryPassword) ? res.temporaryPassword : res;
        showModal({
          title: "Password Reset Successfully",
          message: `Temporary password:\n\n${tempPass}\n\nThe student will be required to change this password after signing in.`,
          type: "success"
        });
      } catch (err) {
        showError("Failed to reset password: " + err.message);
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

function escapeJs(str) {
  if (!str) return "";
  return String(str).replace(/'/g, "\\'");
}
