    var recommendationSections = window.serverRecommendationSections || [];
    var trendingMovies = window.serverTrendingMovies || [];
    var heroMovies = [];


    if (recommendationSections && recommendationSections.length > 0) {
        heroMovies = recommendationSections[0].movies;
    }
    else if (trendingMovies && trendingMovies.length > 0) {
        heroMovies = trendingMovies.map(m => ({
            id: m.ImdbID || m.id,
            title: m.title,
            posterPath: m.poster,
            aiReason: m.plot || "Trending worldwide right now."
        }));
    }

    let currentIndex = 0;
    let autoSlideInterval;

    const posterEl = document.getElementById('hero-poster');
    const titleEl = document.getElementById('hero-title');
    const descEl = document.getElementById('hero-desc');
    const btnEl = document.getElementById('hero-btn');

    function showHeroMovie(index) {
        if (heroMovies.length === 0) return;

        if (index >= heroMovies.length) currentIndex = 0;
        else if (index < 0) currentIndex = heroMovies.length - 1;
        else currentIndex = index;

        const movie = heroMovies[currentIndex];

        posterEl.style.opacity = 0;

        setTimeout(() => {
            posterEl.src = movie.posterPath;
            titleEl.textContent = movie.title;
            descEl.textContent = movie.aiReason ? '"' + movie.aiReason + '"' : "Recommended for you.";
            btnEl.href = '/movie/' + movie.id;
            posterEl.style.opacity = 1;
        }, 300);
    }

    function nextSlide() {
        showHeroMovie(currentIndex + 1);
        resetTimer();
    }

    function prevSlide() {
        showHeroMovie(currentIndex - 1);
        resetTimer();
    }

    function resetTimer() {
        clearInterval(autoSlideInterval);
        autoSlideInterval = setInterval(() => {
            showHeroMovie(currentIndex + 1);
        }, 7500);
    }


    if (heroMovies.length > 0) {
        showHeroMovie(0);
        resetTimer();
    }