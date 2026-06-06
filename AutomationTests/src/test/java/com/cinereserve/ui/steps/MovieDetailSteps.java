package com.cinereserve.ui.steps;

import com.cinereserve.ui.context.DriverContext;
import com.cinereserve.ui.pages.MovieDetailPage;
import io.cucumber.java.en.*;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for Movie Detail page interactions.
 */
public class MovieDetailSteps {

    private static final Logger log = LogManager.getLogger(MovieDetailSteps.class);

    private final DriverContext ctx;
    private MovieDetailPage movieDetailPage;
    private int currentMovieId;

    public MovieDetailSteps(DriverContext ctx) {
        this.ctx = ctx;
    }

    @Given("a movie exists in the system with id {int}")
    public void setTargetMovieId(int movieId) {
        this.currentMovieId = movieId;
        Allure.step("Using movie id: " + movieId);
        log.info("Target movie id set to {}", movieId);
    }

    @When("the user navigates to the movie detail page for movie {int}")
    public void navigateToMovieDetail(int movieId) {
        movieDetailPage = new MovieDetailPage(ctx.driver());
        movieDetailPage.open(movieId);
        Allure.step("Navigated to /movies/" + movieId);
        log.info("Navigated to movie detail page for id={}", movieId);
    }

    @Then("the movie detail page should be visible")
    public void verifyMovieDetailPageVisible() {
        String url = ctx.driver().getCurrentUrl();
        Allure.step("Current URL: " + url);
        assertThat(url).contains("/movies/");
    }

    @Then("the page should display a showtimes or booking section")
    public void verifyShowtimeSectionPresent() {
        boolean hasShowtimes = movieDetailPage.hasShowtimes();
        boolean hasBookButton = movieDetailPage.isBookButtonVisible();
        Allure.step("Showtimes visible: " + hasShowtimes + ", Book button: " + hasBookButton);
        assertThat(hasShowtimes || hasBookButton)
                .as("Page should have showtimes list or a book button").isTrue();
    }

    @Then("there should be a booking call-to-action on the page")
    public void verifyBookingCallToAction() {
        boolean hasBookButton = movieDetailPage.isBookButtonVisible();
        Allure.step("Book button visible: " + hasBookButton);
        assertThat(hasBookButton)
                .as("Page should have a book/select booking CTA").isTrue();
    }
}
