document.addEventListener('DOMContentLoaded', function() {
    const MIN_RATINGS_REQUIRED = 5;
    const form = document.getElementById('rating-form');
    const allMovieCards = document.querySelectorAll('.movie-card');

    const ratedMoviesListContainer = document.getElementById('rated-movies-list');
    const movieFoundContainer = document.getElementById('manual-search-results');
    const errorMsg = document.getElementById('error-msg');

    // ----------------------------------------------------
    // MAIN FUNCTION, initiate Card
    // ----------------------------------------------------
    function initializeCard(card) {
        if (card.hasAttribute('data-initialized')) {
            return;
        }
        card.setAttribute('data-initialized', 'true');

        const movieId = card.getAttribute('data-movie-id');
        const ratingControls = card.querySelector('.rating-controls');

        ratingControls.innerHTML = '<span class="rating-label">Rating:</span>';
        const hiddenInput = card.querySelector(`input[type="hidden"][id$="${movieId}"]`);
        const posterWrapper = card.querySelector('.poster-wrapper');

        if (!ratingControls || !hiddenInput || !posterWrapper) {
            console.error("Missing elements for card initialization:", card);
            return;
        }

        // Create Stars container manually
        const starsContainer = document.createElement('div');
        starsContainer.classList.add('stars');

        for (let i = 5; i >= 1; i--) {
            const star = document.createElement('span');
            star.textContent = '☆';
            star.setAttribute('data-rating', i);
            star.addEventListener('click', function() {
                handleStarClick(card, starsContainer, hiddenInput, i);
                checkRatingCount();
            });
            starsContainer.appendChild(star);  // Add Star to container
        }
        ratingControls.appendChild(starsContainer); // Add Stars Container into page, 'Rating: [    ]'


        // Manage Click intoPoster
        posterWrapper.addEventListener('click', function() {
            const isSelected = card.classList.toggle('selected');

            if (isSelected) {
                ratingControls.style.display = 'flex'; // If card isSelected, show ratingControls: 'Rating: [  ]'
            } else {
                ratingControls.style.display = 'none';
                handleStarClick(card, starsContainer, hiddenInput, 0);
            }
            checkRatingCount();
        });
    }

    // ----------------------------------------------------
    // CLICK ON STARS
    // ----------------------------------------------------
    function handleStarClick(card, starsContainer, hiddenInput, newRating) {
        hiddenInput.value = newRating;

        const stars = starsContainer.querySelectorAll('span'); // Get container of 5 Stars
        stars.forEach(star => { // Check for every Star
            const starValue = parseInt(star.getAttribute('data-rating')); // Get Star's number
            if (starValue <= newRating) {
                star.classList.add('rated'); // Paint missing Stars for New Rating
            } else {
                star.classList.remove('rated'); // Unpaint
            }
        });

        if (newRating > 0 && !card.classList.contains('selected')) {
             card.classList.add('selected');
             const ratingControls = card.querySelector('.rating-controls');
             if (ratingControls) ratingControls.style.display = 'flex';
        } // Ensures card's selection

        if (newRating === 0) {
            card.classList.remove('selected');
        } // Remove Card's selection if Rating=0
    }

    // ----------------------------------------------------
    // MANUAL PERSISTANCE LOGIC (CLONE)
    // ----------------------------------------------------
    function saveManualRating(cardToMove) {

        const ratingInput = cardToMove.querySelector('input[type="hidden"]');

        if (!cardToMove.classList.contains('selected') || ratingInput.value === '0') {
            alert("Please rate the movie before moving it!");
            return;
        }

        // Clone visual Card and
        const ratedCard = cardToMove.cloneNode(true);
        ratedCard.classList.add('is-saved');
        ratedCard.removeAttribute('data-initialized');

        // Move clone visual card to the container of final list
        ratedMoviesListContainer.appendChild(ratedCard);

        // Prepare space
        movieFoundContainer.innerHTML = '';

        // Restart card to get event listeners
        initializeCard(ratedCard);

        // Check for ratings
        checkRatingCount();
    }

    // ----------------------------------------------------
    // CheckRating and Validation Logic
    // ----------------------------------------------------
    function checkRatingCount() {
        // Search all rating inputs in the document (carrusel + manual)
        const allHiddenInputs = document.querySelectorAll('.movie-card input[type="hidden"]');

        const ratedCount = Array.from(allHiddenInputs) // Count rated movies
                               .filter(input => parseInt(input.value) > 0) // Discard Movies without a Rating
                               .length;

        if (ratedCount < MIN_RATINGS_REQUIRED) {
            errorMsg.textContent = `Please rate at least ${MIN_RATINGS_REQUIRED} movies.`;
            errorMsg.style.display = 'block';
            return false;
        } else {
            errorMsg.style.display = 'none';
            return true;
        }
    }

    // ----------------------------------------------------
    // FINAL RECOLECTION
    // ----------------------------------------------------
    function collectInputsForSubmission() {
        const formSubmissionContainer = document.getElementById('input-container-for-submission');

                // Prepare space
                formSubmissionContainer.innerHTML = '';

                const allInputs = document.querySelectorAll('.movie-card input[type="hidden"]');

                allInputs.forEach(input => {
                    if (parseInt(input.value) > 0) {
                        // SEND A COPY OF ALL RATED
                        const clonedInput = input.cloneNode(true);
                        formSubmissionContainer.appendChild(clonedInput);
                    }
                });
    }


    // ----------------------------------------------------
    // INIZIATILZATION AND FINAL SUBMISSION
    // ----------------------------------------------------

    // Initiate carrusels
    allMovieCards.forEach(initializeCard);

    // Final submission
    form.addEventListener('submit', function(event) {
        if (!checkRatingCount()) {
            event.preventDefault();
        } else {
            collectInputsForSubmission();
        }
    });

    const searchButton = document.getElementById('search-manual-btn');
    const searchInput = document.getElementById('manualTitle');
    const searchResultsContainer = document.getElementById('manual-search-results');

    if (searchButton && searchInput && searchResultsContainer) {
        searchButton.addEventListener('click', function() {
            const title = searchInput.value.trim();
            if (title.length === 0) { alert('Please enter a movie title.'); return; }

            searchResultsContainer.innerHTML = '<h4>Searching...</h4>';
            const url = `/rating-form/api/search?title=${encodeURIComponent(title)}`;

            fetch(url, { method: 'GET', headers: { 'Content-Type': 'application/json' } }) // ASYNC
            .then(response => {
                if (!response.ok) { throw new Error('Movie not found.'); }
                return response.json();
            })
            .then(movie => {
                const movieCardHtml = createManualMovieCardHtml(movie);
                searchResultsContainer.innerHTML = movieCardHtml; // Replece Searching... with Movie Card HTML

                setTimeout(() => {
                    const newCard = searchResultsContainer.querySelector('.movie-card');
                    if (newCard) {
                        initializeCard(newCard);


                        const moveButton = document.createElement('button');
                        moveButton.textContent = "Add to My Ratings";
                        moveButton.classList.add('search-manual-btn', 'add-rating-btn');

                        const buttonContainer = document.createElement('div');
                        buttonContainer.classList.add('add-btn-container');
                        buttonContainer.appendChild(moveButton);
                        searchResultsContainer.insertAdjacentElement('beforeend', buttonContainer);

                        moveButton.addEventListener('click', () => {
                            saveManualRating(newCard);
                            buttonContainer.remove();
                        });
                    }
                }, 50);
            })
            .catch(error => {
                searchResultsContainer.innerHTML = `<h4 class="error-msg">${error.message} Please try a different title.</h4>`;
                console.error('Fetch error:', error);
            });
        });
    }

    // HTML Function
    function createManualMovieCardHtml(movie) {
        const inputName = `rating_${movie.id}`;
        const movieYear = movie.year || ' ';
        return `
            <h4>Movie Found:</h4>
            <div class="movie-card" data-movie-id="${movie.id}">
                <div class="poster-wrapper">
                    <img src="${movie.posterUrl}" alt="${movie.title}" class="movie-poster">
                    <div class="selection-check">✓</div>
                </div>
                <p class="movie-title">${movie.title}</p>
                <p class="movie-year">${movieYear}</p>
                <div class="rating-controls" style="display: none;">
                    <span class="rating-label">Rating:</span>
                </div>
                <input type="hidden" name="${inputName}" id="${inputName}" value="0">
            </div>
        `;
    }

    checkRatingCount();
});