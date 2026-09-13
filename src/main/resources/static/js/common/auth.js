/* src/main/resources/static/js/common/auth.js */
const Auth = {
  getCurrentUser() {
    const userJson = localStorage.getItem("user");
    return userJson ? JSON.parse(userJson) : null;
  },

  setCurrentUser(user) {
    localStorage.setItem("user", JSON.stringify(user));
  },

  clearCurrentUser() {
    localStorage.removeItem("user");
  },

  isLoggedIn() {
    return this.getCurrentUser() !== null;
  },

  isAdmin() {
    const user = this.getCurrentUser();
    return user && user.role === "ADMIN";
  },

  isStudent() {
    const user = this.getCurrentUser();
    return user && user.role === "STUDENT";
  },

  async logout() {
    this.clearCurrentUser();
    window.location.href = APP_CONFIG.routes.login;
  }
};
