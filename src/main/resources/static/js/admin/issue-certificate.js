/* src/main/resources/static/js/admin/issue-certificate.js */
document.addEventListener("DOMContentLoaded", async () => {
  renderSidebar("issue-certificate");
  await loadDropdownData();

  // Set default issue date to today
  document.getElementById("issueDate").value = new Date().toISOString().split("T")[0];

  document.getElementById("issueForm").addEventListener("submit", handleIssueSubmit);
  document.getElementById("templateSelect").addEventListener("change", handleTemplateChange);
  document.getElementById("previewBtn").addEventListener("click", showPreview);
});

let loadedTemplates = [];

async function loadDropdownData() {
  try {
    showLoader();
    const studentsData = await apiRequest("/admin/students?size=100&status=ACTIVE");
    const templatesData = await apiRequest("/admin/templates?activeOnly=true");

    const studentSelect = document.getElementById("studentSelect");
    studentSelect.innerHTML = '<option value="">-- Select Active Student --</option>' +
      studentsData.content.map(s => `<option value="${s.id}">${s.name} (${s.rollNumber}) - ${s.course}</option>`).join("");

    loadedTemplates = templatesData;
    const templateSelect = document.getElementById("templateSelect");
    templateSelect.innerHTML = '<option value="">-- Select Certificate Template --</option>' +
      templatesData.map(t => `<option value="${t.id}">${t.templateName} - ${t.certificateTitle}</option>`).join("");

  } catch (err) {
    showError("Failed to load options for issuance: " + err.message);
  } finally {
    hideLoader();
  }
}

function handleTemplateChange() {
  const templateId = document.getElementById("templateSelect").value;
  const selected = loadedTemplates.find(t => t.id == templateId);
  if (selected) {
    document.getElementById("certificateTitle").value = selected.certificateTitle;
    document.getElementById("description").value = selected.description || "";
  }
}

function showPreview() {
  const studentSelect = document.getElementById("studentSelect");
  const templateSelect = document.getElementById("templateSelect");

  if (!studentSelect.value || !templateSelect.value) {
    showWarning("Please select a student and a template first.");
    return;
  }

  const studentText = studentSelect.options[studentSelect.selectedIndex].text;
  const templateText = templateSelect.options[templateSelect.selectedIndex].text;
  const title = document.getElementById("certificateTitle").value;
  const date = document.getElementById("issueDate").value;

  showInfo(`PREVIEW DETAILS:\n\nStudent: ${studentText}\nTemplate: ${templateText}\nTitle: ${title}\nIssue Date: ${date}`);
}

async function handleIssueSubmit(e) {
  e.preventDefault();

  const payload = {
    studentId: parseInt(document.getElementById("studentSelect").value),
    templateId: parseInt(document.getElementById("templateSelect").value),
    certificateTitle: document.getElementById("certificateTitle").value,
    description: document.getElementById("description").value,
    issueDate: document.getElementById("issueDate").value,
    certificateNumber: document.getElementById("certificateNumber").value || null
  };

  try {
    showLoader();
    const cert = await apiRequest("/admin/certificates", {
      method: "POST",
      body: JSON.stringify(payload)
    });

    showSuccess(`Certificate issued successfully!\n\nCert No: ${cert.certificateNumber}\nVerification Code: ${cert.verificationCode}`, () => {
      window.location.href = "/admin/certificates.html";
    });
  } catch (err) {
    showError("Failed to issue certificate: " + err.message);
  } finally {
    hideLoader();
  }
}
