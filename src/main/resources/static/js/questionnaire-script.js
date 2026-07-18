document.addEventListener('DOMContentLoaded', function() {
        const MAX_SELECTIONS = 5;
        const form = document.getElementById('genre-form');
        const checkboxes = form.querySelectorAll('input[type="checkbox"][name="genres"]');
        const errorMsg = document.getElementById('error-msg');

        function validateSelections() {
            const checkedCount = Array.from(checkboxes).filter(cb => cb.checked).length;

            if (checkedCount === 0) {
                errorMsg.textContent = "Please select at least one genre.";
                errorMsg.style.display = 'block';
                return false;
            } else if (checkedCount > MAX_SELECTIONS) {
                errorMsg.textContent = `You can select a maximum of ${MAX_SELECTIONS} genres.`;
                errorMsg.style.display = 'block';
                return false;
            } else {
                errorMsg.style.display = 'none';
                return true;
            }
        }

        checkboxes.forEach(checkbox => {
            checkbox.addEventListener('change', function() {
                const checkedCount = Array.from(checkboxes).filter(cb => cb.checked).length;

                if (checkedCount > MAX_SELECTIONS) {

                    this.checked = false;

                    errorMsg.textContent = `Limit reached: You can only select ${MAX_SELECTIONS} genres.`;
                    errorMsg.style.display = 'block';

                    setTimeout(() => {
                        errorMsg.style.display = 'none';
                    }, 1500);

                } else {
                    errorMsg.style.display = 'none';
                }

            });
        });

        form.addEventListener('submit', function(event) {
            if (!validateSelections()) {
                event.preventDefault();
            }
        });
});