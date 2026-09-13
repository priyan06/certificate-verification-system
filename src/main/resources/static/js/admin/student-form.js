/* src/main/resources/static/js/admin/student-form.js */
let studentId = null;

document.addEventListener("DOMContentLoaded", async () => {
  renderSidebar("students");
  studentId = Utils.getUrlParam("id");

  setupPasswordToggle("initialPassword", "passwordToggleBtn");

  const nameInput = document.getElementById("name");
  const dobInput = document.getElementById("dateOfBirth");
  const passwordInput = document.getElementById("initialPassword");

  function updatePasswordPreview() {
    if (studentId) return; // Never update or display password on edit
    const preview = computeInitialPasswordPreview(nameInput.value, dobInput.value);
    passwordInput.value = preview;
  }

  nameInput.addEventListener("input", updatePasswordPreview);
  dobInput.addEventListener("change", updatePasswordPreview);

  if (studentId) {
    document.getElementById("formTitle").textContent = "Edit Student Profile";
    document.getElementById("formSubtitle").textContent = "Update student credentials and program registration details";
    document.getElementById("passwordRow").style.display = "none";
    document.getElementById("rollNumberRow").style.display = "grid";
    await loadStudentDetails(studentId);
  }

  document.getElementById("studentForm").addEventListener("submit", handleSubmit);
});

async function loadStudentDetails(id) {
  try {
    showLoader();
    const student = await apiRequest(`/admin/students/${id}`);
    document.getElementById("rollNumber").value = student.rollNumber || "";
    document.getElementById("registerNumber").value = student.registerNumber || "";
    document.getElementById("name").value = student.name || "";
    document.getElementById("email").value = student.email || "";
    document.getElementById("mobileNumber").value = student.mobileNumber || "";
    document.getElementById("course").value = student.course || "";
    document.getElementById("department").value = student.department || "";
    document.getElementById("batch").value = student.batch || "";
    document.getElementById("dateOfBirth").value = student.dateOfBirth || "";
    document.getElementById("address").value = student.address || "";
    document.getElementById("status").value = student.status || "ACTIVE";
  } catch (err) {
    showError("Failed to load student details: " + err.message);
  } finally {
    hideLoader();
  }
}

async function handleSubmit(e) {
  e.preventDefault();

  const registerNumber = document.getElementById("registerNumber").value.trim();
  const name = document.getElementById("name").value.trim();
  const email = document.getElementById("email").value.trim();
  const mobileNumber = document.getElementById("mobileNumber").value.trim();
  const course = document.getElementById("course").value.trim();
  const department = document.getElementById("department").value.trim();
  const batch = document.getElementById("batch").value.trim();
  const dateOfBirth = document.getElementById("dateOfBirth").value;
  const address = document.getElementById("address").value.trim();

  if (!dateOfBirth) {
    showError("Date of birth is required.");
    return;
  }

  try {
    showLoader();
    if (studentId) {
      const payload = {
        registerNumber,
        name,
        email,
        mobileNumber,
        course,
        department,
        batch,
        dateOfBirth,
        address,
        status: document.getElementById("status").value
      };
      await apiRequest(`/admin/students/${studentId}`, {
        method: "PUT",
        body: JSON.stringify(payload)
      });
      showSuccess("Student profile updated successfully.", () => {
        window.location.href = "/admin/students.html";
      });
    } else {
      const payload = {
        registerNumber,
        name,
        email,
        mobileNumber,
        course,
        department,
        batch,
        dateOfBirth,
        address
      };
      const createdStudent = await apiRequest("/admin/students", {
        method: "POST",
        body: JSON.stringify(payload)
      });
      const rollNum = createdStudent?.rollNumber || "Auto-generated";
      showSuccess(`Student account created successfully.\n\nAssigned Roll Number: ${rollNum}`, () => {
        window.location.href = "/admin/students.html";
      });
    }
  } catch (err) {
    showError(err.message || "Failed to save student profile.");
  } finally {
    hideLoader();
  }
}
