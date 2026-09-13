/* src/main/resources/static/js/student/change-password.js */
document.addEventListener("DOMContentLoaded", () => {
  renderSidebar("change-password");
  document.getElementById("changePasswordForm").addEventListener("submit", handlePasswordChange);
});

async function handlePasswordChange(e) {
  e.preventDefault();

  const oldPassword = document.getElementById("oldPassword").value;
  const newPassword = document.getElementById("newPassword").value;
  const confirmPassword = document.getElementById("confirmPassword").value;

  if (newPassword !== confirmPassword) {
    showError("New password and confirm password do not match.");
    return;
  }

  try {
    showLoader();
    await apiRequest("/auth/change-password", {
      method: "POST",
      body: JSON.stringify({ oldPassword, newPassword })
    });
    showSuccess("Your password has been changed successfully.", () => {
      document.getElementById("changePasswordForm").reset();
    });
  } catch (err) {
    showError("Failed to change password: " + err.message);
  } finally {
    hideLoader();
  }
}
