/* src/main/resources/static/js/student/profile.js */
document.addEventListener("DOMContentLoaded", async () => {
  renderSidebar("profile");
  await loadProfile();
});

async function loadProfile() {
  try {
    showLoader();
    const p = await apiRequest("/student/profile");
    document.getElementById("profileName").textContent = p.name;
    document.getElementById("profileRollNumber").textContent = p.rollNumber;
    document.getElementById("profileRegisterNumber").textContent = p.registerNumber;
    document.getElementById("profileEmail").textContent = p.email;
    document.getElementById("profilePhone").textContent = p.mobileNumber || "-";
    document.getElementById("profileCourse").textContent = p.course;
    document.getElementById("profileDepartment").textContent = p.department || "-";
    document.getElementById("profileBatch").textContent = p.batch || "-";
    document.getElementById("profileDob").textContent = Utils.formatDate(p.dateOfBirth);
    document.getElementById("profileAddress").textContent = p.address || "-";
    document.getElementById("profileStatus").textContent = p.status;
    document.getElementById("profileStatusBadge").className = `badge ${Utils.getStatusBadgeClass(p.status)}`;
  } catch (err) {
    showError("Failed to load student profile: " + err.message);
  } finally {
    hideLoader();
  }
}
