/* src/main/resources/static/js/components/pagination.js */
function renderPagination(containerId, pageData, onPageChange) {
  const container = document.getElementById(containerId);
  if (!container || !pageData) return;

  const { number: currentPage, totalPages, totalElements } = pageData;
  if (totalPages <= 1) {
    container.innerHTML = `<div class="pagination"><span>Showing ${totalElements} records</span></div>`;
    return;
  }

  const prevDisabled = currentPage === 0 ? "disabled" : "";
  const nextDisabled = currentPage >= totalPages - 1 ? "disabled" : "";

  container.innerHTML = `
    <div class="pagination">
      <span>Page ${currentPage + 1} of ${totalPages} (${totalElements} total items)</span>
      <div class="pagination-controls">
        <button class="btn btn-secondary btn-sm" id="paginationPrevBtn" ${prevDisabled}>← Previous</button>
        <button class="btn btn-secondary btn-sm" id="paginationNextBtn" ${nextDisabled}>Next →</button>
      </div>
    </div>
  `;

  const prevBtn = document.getElementById("paginationPrevBtn");
  const nextBtn = document.getElementById("paginationNextBtn");

  if (prevBtn && !prevDisabled) {
    prevBtn.addEventListener("click", () => onPageChange(currentPage - 1));
  }
  if (nextBtn && !nextDisabled) {
    nextBtn.addEventListener("click", () => onPageChange(currentPage + 1));
  }
}
