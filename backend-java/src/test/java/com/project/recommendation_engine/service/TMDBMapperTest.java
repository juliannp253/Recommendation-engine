package com.project.recommendation_engine.service;

import com.project.recommendation_engine.model.Movie;
import com.project.recommendation_engine.model.TMDBResponse;
import com.project.recommendation_engine.model.tmdb.TmdbApiModels.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TMDBMapperTest {

    private final TMDBMapper mapper = new TMDBMapper();

    @Test
    void toResponse_mapsAllFieldsCorrectly_withWatchProviders() {
        TmdbMovie movie = new TmdbMovie();
        movie.setId(550L);
        movie.setTitle("Fight Club");
        movie.setPosterPath("/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg");
        movie.setOverview("A ticking-time-bomb insomniac...");
        movie.setReleaseDate("1999-10-15");
        movie.setVoteAverage(8.433);

        Genre genreDrama = new Genre();
        genreDrama.setId(18);
        genreDrama.setName("Drama");
        Genre genreThriller = new Genre();
        genreThriller.setId(53);
        genreThriller.setName("Thriller");
        movie.setGenres(List.of(genreDrama, genreThriller));

        Credits credits = new Credits();

        Crew director = new Crew();
        director.setName("David Fincher");
        director.setJob("Director");
        Crew writer = new Crew();
        writer.setName("Chuck Palahniuk");
        writer.setJob("Novel");
        credits.setCrew(List.of(director, writer));

        Cast actor1 = new Cast();
        actor1.setName("Edward Norton");
        actor1.setOrder(0);
        Cast actor2 = new Cast();
        actor2.setName("Brad Pitt");
        actor2.setOrder(1);
        Cast actorExtras = new Cast();
        actorExtras.setName("Extra Actor");
        actorExtras.setOrder(10); // order >= 5 should be ignored
        credits.setCast(List.of(actor2, actor1, actorExtras));

        movie.setCredits(credits);

        ProviderItem streamProvider = createProviderItem("Max", "/max_logo.jpg");
        ProviderItem rentProvider = createProviderItem("Apple TV", "/apple_logo.jpg");
        ProviderItem buyProvider = createProviderItem("Amazon Video", "/amazon_logo.jpg");

        RegionInfo regionInfo = createRegionInfo(
                "https://www.themoviedb.org/movie/550/watch?locale=US",
                List.of(streamProvider),
                List.of(rentProvider),
                List.of(buyProvider)
        );

        TMDBResponse response = mapper.toResponse(movie, regionInfo);

        assertNotNull(response);
        assertEquals("Fight Club", response.getTitle());
        assertEquals("550", response.getTmdbID());
        assertEquals("https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg", response.getPoster());
        assertEquals("A ticking-time-bomb insomniac...", response.getPlot());
        assertEquals("1999", response.getYear());
        assertEquals(String.format("%.1f", 8.433), response.getTmdbRating());
        assertEquals("True", response.getResponse());
        assertEquals("Drama, Thriller", response.getGenre());
        assertEquals("David Fincher", response.getDirector());
        assertEquals("Edward Norton, Brad Pitt", response.getActors());

        assertEquals("https://www.themoviedb.org/movie/550/watch?locale=US", response.getWatchLink());
        assertEquals(1, response.getFlatrateProviders().size());
        assertEquals("Max", response.getFlatrateProviders().get(0).getName());
        assertEquals("https://image.tmdb.org/t/p/original/max_logo.jpg", response.getFlatrateProviders().get(0).getLogoUrl());

        assertEquals(1, response.getRentProviders().size());
        assertEquals("Apple TV", response.getRentProviders().get(0).getName());

        assertEquals(1, response.getBuyProviders().size());
        assertEquals("Amazon Video", response.getBuyProviders().get(0).getName());
    }

    @Test
    void toResponse_handlesNullMovie_returnsNull() {
        assertNull(mapper.toResponse(null, new RegionInfo()));
    }

    @Test
    void toResponse_handlesMissingFields_usesFallbacks() {
        TmdbMovie movie = new TmdbMovie();
        movie.setId(123L);
        movie.setTitle("Minimal Movie");

        TMDBResponse response = mapper.toResponse(movie, null);

        assertNotNull(response);
        assertEquals("Minimal Movie", response.getTitle());
        assertEquals("123", response.getTmdbID());
        assertNull(response.getPoster());
        assertNull(response.getPlot());
        assertNull(response.getYear());
        assertEquals("N/A", response.getTmdbRating());
        assertEquals("N/A", response.getGenre());
        assertEquals("N/A", response.getDirector());
        assertEquals("N/A", response.getActors());
        assertNull(response.getWatchLink());
        assertTrue(response.getFlatrateProviders().isEmpty());
    }

    @Test
    void toMovie_mapsCorrectly() {
        TmdbMovie movie = new TmdbMovie();
        movie.setId(99L);
        movie.setTitle("Inception");
        movie.setPosterPath("/inception.jpg");

        Movie result = mapper.toMovie(movie);

        assertNotNull(result);
        assertEquals("99", result.getId());
        assertEquals("Inception", result.getTitle());
        assertEquals("https://image.tmdb.org/t/p/w500/inception.jpg", result.getPosterUrl());
    }

    @Test
    void toMovie_handlesNull_returnsNull() {
        assertNull(mapper.toMovie(null));
    }

    private ProviderItem createProviderItem(String name, String logoPath) {
        ProviderItem item = new ProviderItem();
        setField(item, "providerName", name);
        setField(item, "logoPath", logoPath);
        return item;
    }

    private RegionInfo createRegionInfo(String link, List<ProviderItem> flatrate, List<ProviderItem> rent, List<ProviderItem> buy) {
        RegionInfo regionInfo = new RegionInfo();
        setField(regionInfo, "link", link);
        setField(regionInfo, "flatrate", flatrate);
        setField(regionInfo, "rent", rent);
        setField(regionInfo, "buy", buy);
        return regionInfo;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName + " on " + target.getClass().getSimpleName(), e);
        }
    }
}
