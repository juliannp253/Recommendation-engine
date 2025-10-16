document.addEventListener('DOMContentLoaded', function() {
        const MAX_SELECTIONS = 5;
        const form = document.getElementById('genre-form');
        const checkboxes = form.querySelectorAll('input[type="checkbox"][name="genres"]');
        const errorMsg = document.getElementById('error-msg');

        function validateSelections() {
            // Count how many checkboxes are marked
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

        // Add listener to every checked box
        checkboxes.forEach(checkbox => {
            checkbox.addEventListener('change', function() {
                const checkedCount = Array.from(checkboxes).filter(cb => cb.checked).length;

                if (checkedCount > MAX_SELECTIONS) {

                    this.checked = false;

                    // Warining message
                    errorMsg.textContent = `Limit reached: You can only select ${MAX_SELECTIONS} genres.`;
                    errorMsg.style.display = 'block';

                    // Hide message if there is no errors
                    setTimeout(() => {
                        errorMsg.style.display = 'none';
                    }, 1500);

                } else {
                    // Valid selection
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