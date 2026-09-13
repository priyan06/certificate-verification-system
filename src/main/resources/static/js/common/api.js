/* src/main/resources/static/js/common/api.js */
async function apiRequest(endpoint, options = {}) {
  const url = APP_CONFIG.apiBaseUrl + endpoint;
  
  const defaultHeaders = {
    "Content-Type": "application/json"
  };

  const config = {
    credentials: "same-origin",
    headers: { ...defaultHeaders, ...options.headers },
    ...options
  };

  try {
    const response = await fetch(url, config);

    if (response.status === 401) {
      // Session expired or unauthorized
      if (!window.location.pathname.endsWith("/login.html") && !window.location.pathname.endsWith("/verify.html")) {
        showError("Session expired or unauthorized. Please log in again.", () => {
          window.location.href = APP_CONFIG.routes.login;
        });
      }
      throw new Error("Unauthorized access");
    }

    if (!response.ok) {
      let errorMessage = "An error occurred while processing your request.";
      try {
        const errorData = await response.json();
        if (errorData && errorData.message) {
          errorMessage = errorData.message;
        }
      } catch (e) {
        errorMessage = `HTTP Error ${response.status}: ${response.statusText}`;
      }
      throw new Error(errorMessage);
    }

    if (response.status === 204) {
      return null;
    }

    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
      return await response.json();
    }
    
    return await response.text();

  } catch (error) {
    console.error("API Request Error:", error);
    throw error;
  }
}
