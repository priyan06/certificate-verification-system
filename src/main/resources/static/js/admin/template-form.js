/* src/main/resources/static/js/admin/template-form.js */
let templateId = null;

document.addEventListener("DOMContentLoaded", async () => {
  renderSidebar("templates");
  templateId = Utils.getUrlParam("id");

  // Setup file input listeners for preview and base64 conversion
  setupImageUploader("logoFileInput", "logoPath", "logoPreview", "logoPreviewWrapper");
  setupImageUploader("sig1FileInput", "signatoryOneImage", "sig1Preview", "sig1PreviewWrapper", "sig1Placeholder");
  setupImageUploader("sig2FileInput", "signatoryTwoImage", "sig2Preview", "sig2PreviewWrapper", "sig2Placeholder");

  if (templateId) {
    document.getElementById("formTitle").textContent = "Edit Certificate Template";
    await loadTemplateDetails(templateId);
  }

  document.getElementById("templateForm").addEventListener("submit", handleSubmit);
  document.getElementById("previewCertBtn").addEventListener("click", openCertPreview);
});

function setupImageUploader(fileInputId, hiddenInputId, imgPreviewId, wrapperId, placeholderId) {
  const fileInput = document.getElementById(fileInputId);
  const hiddenInput = document.getElementById(hiddenInputId);
  const imgPreview = document.getElementById(imgPreviewId);
  const placeholder = placeholderId ? document.getElementById(placeholderId) : null;

  fileInput.addEventListener("change", (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (!file.type.startsWith("image/")) {
      showError("Please select a valid image file.");
      fileInput.value = "";
      return;
    }

    const reader = new FileReader();
    reader.onload = (event) => {
      const base64 = event.target.result;
      hiddenInput.value = base64;
      imgPreview.src = base64;
      imgPreview.style.display = "block";
      if (placeholder) placeholder.style.display = "none";
    };
    reader.readAsDataURL(file);
  });
}

async function loadTemplateDetails(id) {
  try {
    showLoader();
    const t = await apiRequest(`/admin/templates/${id}`);
    document.getElementById("templateName").value = t.templateName || "";
    document.getElementById("certificateTitle").value = t.certificateTitle || "";
    document.getElementById("description").value = t.description || "";
    
    // Signatory 1
    document.getElementById("signatoryOneName").value = t.signatoryOneName || "";
    document.getElementById("signatoryOneDesignation").value = t.signatoryOneDesignation || "";
    if (t.signatoryOneImage) {
      document.getElementById("signatoryOneImage").value = t.signatoryOneImage;
      const sig1Img = document.getElementById("sig1Preview");
      sig1Img.src = t.signatoryOneImage;
      sig1Img.style.display = "block";
      const p1 = document.getElementById("sig1Placeholder");
      if (p1) p1.style.display = "none";
    }

    // Signatory 2
    document.getElementById("signatoryTwoName").value = t.signatoryTwoName || "";
    document.getElementById("signatoryTwoDesignation").value = t.signatoryTwoDesignation || "";
    if (t.signatoryTwoImage) {
      document.getElementById("signatoryTwoImage").value = t.signatoryTwoImage;
      const sig2Img = document.getElementById("sig2Preview");
      sig2Img.src = t.signatoryTwoImage;
      sig2Img.style.display = "block";
      const p2 = document.getElementById("sig2Placeholder");
      if (p2) p2.style.display = "none";
    }

    // Logo
    if (t.logoPath) {
      document.getElementById("logoPath").value = t.logoPath;
      document.getElementById("logoPreview").src = t.logoPath;
    }

    document.getElementById("active").checked = t.active !== false;
  } catch (err) {
    showError("Failed to load template: " + err.message);
  } finally {
    hideLoader();
  }
}

async function handleSubmit(e) {
  e.preventDefault();

  const payload = {
    templateName: document.getElementById("templateName").value.trim(),
    certificateTitle: document.getElementById("certificateTitle").value.trim(),
    description: document.getElementById("description").value.trim(),
    logoPath: document.getElementById("logoPath").value,
    signatoryOneName: document.getElementById("signatoryOneName").value.trim(),
    signatoryOneDesignation: document.getElementById("signatoryOneDesignation").value.trim(),
    signatoryOneImage: document.getElementById("signatoryOneImage").value || null,
    signatoryTwoName: document.getElementById("signatoryTwoName").value.trim() || null,
    signatoryTwoDesignation: document.getElementById("signatoryTwoDesignation").value.trim() || null,
    signatoryTwoImage: document.getElementById("signatoryTwoImage").value || null,
    active: document.getElementById("active").checked
  };

  try {
    showLoader();
    if (templateId) {
      await apiRequest(`/admin/templates/${templateId}`, {
        method: "PUT",
        body: JSON.stringify(payload)
      });
      showSuccess("Certificate Template updated successfully.", () => {
        window.location.href = "/admin/templates.html";
      });
    } else {
      await apiRequest("/admin/templates", {
        method: "POST",
        body: JSON.stringify(payload)
      });
      showSuccess("Certificate Template created successfully.", () => {
        window.location.href = "/admin/templates.html";
      });
    }
  } catch (err) {
    showError("Failed to save template: " + err.message);
  } finally {
    hideLoader();
  }
}

function openCertPreview() {
  const title = document.getElementById("certificateTitle").value.trim() || "CERTIFICATE OF ACHIEVEMENT";
  const desc = document.getElementById("description").value.trim() || "This certificate is awarded for successfully completing the program requirements.";
  const logo = document.getElementById("logoPath").value || "/images/default-logo.png";
  
  const sig1Name = document.getElementById("signatoryOneName").value.trim() || "Signatory 1";
  const sig1Role = document.getElementById("signatoryOneDesignation").value.trim() || "Role / Designation";
  const sig1Img = document.getElementById("signatoryOneImage").value;

  const sig2Name = document.getElementById("signatoryTwoName").value.trim() || "Signatory 2";
  const sig2Role = document.getElementById("signatoryTwoDesignation").value.trim() || "Role / Designation";
  const sig2Img = document.getElementById("signatoryTwoImage").value;

  document.getElementById("pvTitle").textContent = title.toUpperCase();
  document.getElementById("pvDescription").textContent = desc;
  document.getElementById("pvLogo").src = logo;

  document.getElementById("pvSig1Name").textContent = sig1Name;
  document.getElementById("pvSig1Role").textContent = sig1Role;
  const pv1 = document.getElementById("pvSig1Img");
  if (sig1Img) {
    pv1.src = sig1Img;
    pv1.style.display = "block";
  } else {
    pv1.style.display = "none";
  }

  document.getElementById("pvSig2Name").textContent = sig2Name;
  document.getElementById("pvSig2Role").textContent = sig2Role;
  const pv2 = document.getElementById("pvSig2Img");
  if (sig2Img) {
    pv2.src = sig2Img;
    pv2.style.display = "block";
  } else {
    pv2.style.display = "none";
  }

  const modal = document.getElementById("certPreviewModal");
  modal.classList.add("active");
}

function closeCertPreview() {
  const modal = document.getElementById("certPreviewModal");
  modal.classList.remove("active");
}

// Close popup when clicking backdrop outside modal-card
document.addEventListener("click", (e) => {
  const modal = document.getElementById("certPreviewModal");
  if (modal && e.target === modal) {
    closeCertPreview();
  }
});

// Close popup when pressing Escape key
document.addEventListener("keydown", (e) => {
  if (e.key === "Escape") {
    closeCertPreview();
  }
});
