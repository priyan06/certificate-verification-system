/* src/main/resources/static/js/admin/templates.js */
document.addEventListener("DOMContentLoaded", () => {
  renderSidebar("templates");
  loadTemplates();
});

async function loadTemplates() {
  try {
    showLoader();
    const templates = await apiRequest("/admin/templates");
    renderTemplatesTable(templates);
  } catch (err) {
    showError("Failed to fetch certificate templates: " + err.message);
  } finally {
    hideLoader();
  }
}

function renderTemplatesTable(templates) {
  const tbody = document.getElementById("templatesTableBody");
  if (!templates || templates.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5"><div class="empty-state">No templates found</div></td></tr>`;
    return;
  }

  tbody.innerHTML = templates.map(t => {
    let sigText = `${t.signatoryOneName || ''} <span class="text-secondary" style="font-size:0.8rem;">(${t.signatoryOneDesignation || ''})</span>`;
    if (t.signatoryTwoName) {
      sigText += `<br><span style="font-size:0.85rem; margin-top:2px; display:inline-block;">${t.signatoryTwoName} <span class="text-secondary" style="font-size:0.75rem;">(${t.signatoryTwoDesignation || ''})</span></span>`;
    }

    return `
    <tr>
      <td><strong>${t.templateName}</strong></td>
      <td><span class="badge badge-neutral" style="font-weight: 600;">${t.certificateTitle}</span></td>
      <td>${sigText}</td>
      <td><span class="badge ${t.active ? 'badge-success' : 'badge-neutral'}">${t.active ? 'ACTIVE' : 'INACTIVE'}</span></td>
      <td>
        <div class="table-actions">
          <a href="/admin/template-form.html?id=${t.id}" class="btn btn-secondary btn-sm">Edit</a>
          <button onclick="toggleTemplateStatus(${t.id}, '${t.templateName}', ${t.active})" class="btn btn-secondary btn-sm">
            ${t.active ? 'Deactivate' : 'Activate'}
          </button>
        </div>
      </td>
    </tr>
  `;
  }).join("");
}

async function toggleTemplateStatus(id, name, isActive) {
  const action = isActive ? 'deactivate' : 'activate';
  showConfirm({
    title: `${isActive ? 'Deactivate' : 'Activate'} Template`,
    message: `Are you sure you want to ${action} template "${name}"?`,
    confirmText: isActive ? 'Deactivate' : 'Activate',
    onConfirm: async () => {
      try {
        showLoader();
        await apiRequest(`/admin/templates/${id}/status`, { method: "PUT" });
        showSuccess(`Template status updated.`);
        loadTemplates();
      } catch (err) {
        showError("Failed to update status: " + err.message);
      } finally {
        hideLoader();
      }
    }
  });
}
