/* src/main/resources/static/js/components/password-toggle.js */
/**
 * Attaches visibility toggle behavior to a password field and button.
 */
function setupPasswordToggle(inputOrId, buttonOrId) {
  const input = typeof inputOrId === "string" ? document.getElementById(inputOrId) : inputOrId;
  const button = typeof buttonOrId === "string" ? document.getElementById(buttonOrId) : buttonOrId;
  if (!input || !button) return;

  button.addEventListener("click", (e) => {
    e.preventDefault();
    const isPassword = input.type === "password";
    input.type = isPassword ? "text" : "password";
    button.setAttribute("aria-label", isPassword ? "Hide password" : "Show password");
    button.title = isPassword ? "Hide password" : "Show password";
    button.textContent = isPassword ? "👁️" : "🔒";
  });
}

/**
 * Computes the initial password preview according to the system's generation rules:
 * First 4 alphabetic letters of the student name + DDMMYYYY format.
 * Matches backend com.example.certificateverification.util.InitialPasswordGenerator.
 */
function computeInitialPasswordPreview(name, dobStr) {
  if (!name || !dobStr) return "";
  const lettersOnly = name.replace(/[^a-zA-Z]/g, "");
  let prefix = lettersOnly.substring(0, Math.min(4, lettersOnly.length));
  if (!prefix) {
    prefix = "STU";
  }

  const parts = dobStr.split("-");
  if (parts.length !== 3) return "";
  const [year, month, day] = parts;
  return `${prefix}${day}${month}${year}`;
}
