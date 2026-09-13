/* src/main/resources/static/js/common/storage.js */
const Storage = {
  get(key, defaultValue = null) {
    const val = localStorage.getItem(key);
    return val ? JSON.parse(val) : defaultValue;
  },

  set(key, value) {
    localStorage.setItem(key, JSON.stringify(value));
  },

  remove(key) {
    localStorage.removeItem(key);
  }
};
