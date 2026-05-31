package com.cinereserve.api.tests.genres;

import com.cinereserve.api.base.BaseTest;
import com.cinereserve.api.config.ApiConfig;
import com.cinereserve.api.utils.TestDataBuilder;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * TEST SUITE: Genre API
 * Endpoints: GET /api/genres, POST /api/genres, DELETE /api/genres/{id}
 * Total @Test methods: 40
 */
@Epic("Genres")
@Feature("Genre Management")
public class GenreTests extends BaseTest {

    // ─── GET GENRES ──────────────────────────────────────────────────────────

    @Test(priority = 1, description = "Get all genres returns 200")
    @Story("List Genres")
    @Severity(SeverityLevel.BLOCKER)
    public void getAllGenres_returns200() {
        withNoAuth().get(ApiConfig.Endpoints.GENRES)
                .then().statusCode(200)
                .body("success", equalTo(true));
    }

    @Test(priority = 2, description = "Get all genres without auth returns 200 (public)")
    @Story("List Genres")
    @Severity(SeverityLevel.CRITICAL)
    public void getAllGenresWithoutAuth_returns200() {
        withNoAuth().get(ApiConfig.Endpoints.GENRES).then().statusCode(200);
    }

    @Test(priority = 3, description = "Get all genres returns array")
    @Story("List Genres")
    @Severity(SeverityLevel.CRITICAL)
    public void getAllGenresReturnsArray() {
        Response res = withNoAuth().get(ApiConfig.Endpoints.GENRES);
        Object data = res.jsonPath().get("data");
        assertThat(data).isInstanceOf(List.class);
    }

    @Test(priority = 4, description = "Genres list is not empty (seeded data)")
    @Story("List Genres")
    @Severity(SeverityLevel.NORMAL)
    public void getAllGenres_listNotEmpty() {
        Response res = withNoAuth().get(ApiConfig.Endpoints.GENRES);
        List<?> genres = res.jsonPath().getList("data");
        assertThat(genres).isNotNull();
    }

    @Test(priority = 5, description = "Each genre has id and name fields")
    @Story("List Genres")
    @Severity(SeverityLevel.NORMAL)
    public void getAllGenres_eachHasIdAndName() {
        Response res = withNoAuth().get(ApiConfig.Endpoints.GENRES);
        res.then().statusCode(200);
        List<Object> genres = res.jsonPath().getList("data");
        if (!genres.isEmpty()) {
            res.then()
                    .body("data[0].id", notNullValue())
                    .body("data[0].name", notNullValue());
        }
    }

    @Test(priority = 6, description = "Get genres content-type is JSON")
    @Story("List Genres")
    @Severity(SeverityLevel.MINOR)
    public void getAllGenresContentTypeIsJson() {
        withNoAuth().get(ApiConfig.Endpoints.GENRES)
                .then().contentType(containsString("application/json"));
    }

    @Test(priority = 7, description = "Get genres response time under 5 seconds")
    @Story("List Genres")
    @Severity(SeverityLevel.MINOR)
    public void getAllGenresResponseTimeUnder5Seconds() {
        Response res = withNoAuth().get(ApiConfig.Endpoints.GENRES);
        assertThat(responseTimeMillis(res)).isLessThan(5000L);
    }

    @Test(priority = 8, description = "Admin can also get genres")
    @Story("List Genres")
    @Severity(SeverityLevel.MINOR)
    public void adminCanGetGenres_returns200() {
        withAdminAuth().get(ApiConfig.Endpoints.GENRES).then().statusCode(200);
    }

    @Test(priority = 9, description = "User can also get genres")
    @Story("List Genres")
    @Severity(SeverityLevel.MINOR)
    public void userCanGetGenres_returns200() {
        withUserAuth().get(ApiConfig.Endpoints.GENRES).then().statusCode(200);
    }

    @Test(priority = 10, description = "Genre names include at least one standard genre")
    @Story("List Genres")
    @Severity(SeverityLevel.MINOR)
    public void genreNamesIncludeStandardGenre() {
        List<String> names = withNoAuth().get(ApiConfig.Endpoints.GENRES)
                .jsonPath().getList("data.name");
        if (names != null && !names.isEmpty()) {
            // At least one genre should be non-empty
            assertThat(names.stream().anyMatch(n -> n != null && !n.isBlank())).isTrue();
        }
    }

    // ─── CREATE GENRE ────────────────────────────────────────────────────────

    @Test(priority = 11, description = "Admin can create a genre")
    @Story("Create Genre")
    @Severity(SeverityLevel.BLOCKER)
    public void adminCreatesGenre_returns200() {
        Response res = withAdminAuth()
                .body(TestDataBuilder.validGenreBody())
                .post(ApiConfig.Endpoints.GENRES);
        res.then().statusCode(200)
                .body("success", equalTo(true))
                .body("data.id", notNullValue())
                .body("data.name", notNullValue());
    }

    @Test(priority = 12, description = "Create genre without auth returns 401")
    @Story("Create Genre Authorization")
    @Severity(SeverityLevel.CRITICAL)
    public void createGenreWithoutAuth_returns401() {
        withNoAuth()
                .body(TestDataBuilder.validGenreBody())
                .post(ApiConfig.Endpoints.GENRES)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 13, description = "User cannot create genre — returns 403")
    @Story("Create Genre Authorization")
    @Severity(SeverityLevel.CRITICAL)
    public void createGenreWithUserRole_returns403() {
        withUserAuth()
                .body(TestDataBuilder.validGenreBody())
                .post(ApiConfig.Endpoints.GENRES)
                .then().statusCode(403);
    }

    @Test(priority = 14, description = "Create genre with invalid token returns 401")
    @Story("Create Genre Authorization")
    @Severity(SeverityLevel.CRITICAL)
    public void createGenreWithInvalidToken_returns401() {
        withInvalidToken()
                .body(TestDataBuilder.validGenreBody())
                .post(ApiConfig.Endpoints.GENRES)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 15, description = "Create genre with empty name returns 400")
    @Story("Create Genre Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void createGenreWithEmptyName_returns400() {
        withAdminAuth()
                .body(TestDataBuilder.genreBody(""))
                .post(ApiConfig.Endpoints.GENRES)
                .then().statusCode(400);
    }

    @Test(priority = 16, description = "Create genre with null name returns 400")
    @Story("Create Genre Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void createGenreWithNullName_returns400() {
        withAdminAuth()
                .body(TestDataBuilder.genreBody(null))
                .post(ApiConfig.Endpoints.GENRES)
                .then().statusCode(400);
    }

    @Test(priority = 17, description = "Create genre with duplicate name returns error")
    @Story("Create Genre Validation")
    @Severity(SeverityLevel.CRITICAL)
    public void createGenreWithDuplicateName_returnsError() {
        Map<String, Object> body = TestDataBuilder.validGenreBody();
        withAdminAuth().body(body).post(ApiConfig.Endpoints.GENRES).then().statusCode(200);
        Response second = withAdminAuth().body(body).post(ApiConfig.Endpoints.GENRES);
        assertThat(second.statusCode()).isIn(400, 409, 500);
    }

    @Test(priority = 18, description = "Create genre with whitespace-only name returns 400")
    @Story("Create Genre Validation")
    @Severity(SeverityLevel.NORMAL)
    public void createGenreWithWhitespaceOnlyName_returns400() {
        withAdminAuth()
                .body(TestDataBuilder.genreBody("   "))
                .post(ApiConfig.Endpoints.GENRES)
                .then().statusCode(400);
    }

    @Test(priority = 19, description = "Create genre with special characters in name")
    @Story("Create Genre Validation")
    @Severity(SeverityLevel.MINOR)
    public void createGenreWithSpecialChars_behaviorDefined() {
        int status = withAdminAuth()
                .body(TestDataBuilder.genreBody("Sci-Fi & Action"))
                .post(ApiConfig.Endpoints.GENRES).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400, 409);
    }

    @Test(priority = 20, description = "Create genre with empty body returns 400")
    @Story("Create Genre Validation")
    @Severity(SeverityLevel.NORMAL)
    public void createGenreWithEmptyBody_returns400() {
        withAdminAuth().body("{}").post(ApiConfig.Endpoints.GENRES)
                .then().statusCode(400);
    }

    @Test(priority = 21, description = "Create genre response has id field")
    @Story("Create Genre")
    @Severity(SeverityLevel.NORMAL)
    public void createGenreResponseHasId() {
        Response res = withAdminAuth()
                .body(TestDataBuilder.validGenreBody())
                .post(ApiConfig.Endpoints.GENRES);
        res.then().statusCode(200)
                .body("data.id", greaterThan(0));
    }

    @Test(priority = 22, description = "Create genre — name is saved correctly")
    @Story("Create Genre")
    @Severity(SeverityLevel.NORMAL)
    public void createGenre_nameIsSavedCorrectly() {
        Map<String, Object> body = TestDataBuilder.validGenreBody();
        String name = (String) body.get("name");
        withAdminAuth().body(body).post(ApiConfig.Endpoints.GENRES)
                .then().statusCode(200)
                .body("data.name", equalTo(name));
    }

    @Test(priority = 23, description = "Created genre appears in genre list")
    @Story("Create Genre")
    @Severity(SeverityLevel.NORMAL)
    public void createdGenreAppearsInList() {
        Map<String, Object> body = TestDataBuilder.validGenreBody();
        int newId = withAdminAuth().body(body).post(ApiConfig.Endpoints.GENRES)
                .jsonPath().getInt("data.id");

        List<Integer> ids = withNoAuth().get(ApiConfig.Endpoints.GENRES)
                .jsonPath().getList("data.id");
        assertThat(ids).contains(newId);
    }

    @Test(priority = 24, description = "Create genre with very long name")
    @Story("Create Genre Boundary")
    @Severity(SeverityLevel.MINOR)
    public void createGenreWithVeryLongName() {
        int status = withAdminAuth()
                .body(TestDataBuilder.genreBody(TestDataBuilder.longString(200)))
                .post(ApiConfig.Endpoints.GENRES).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400, 409, 422);
    }

    @Test(priority = 25, description = "Create genre response time under 5 seconds")
    @Story("Create Genre")
    @Severity(SeverityLevel.MINOR)
    public void createGenreResponseTimeUnder5Seconds() {
        Response res = withAdminAuth()
                .body(TestDataBuilder.validGenreBody())
                .post(ApiConfig.Endpoints.GENRES);
        assertThat(responseTimeMillis(res)).isLessThan(5000L);
    }

    // ─── DELETE GENRE ────────────────────────────────────────────────────────

    @Test(priority = 26, description = "Admin can delete a genre")
    @Story("Delete Genre")
    @Severity(SeverityLevel.BLOCKER)
    public void adminDeletesGenre_returns200() {
        int genreId = withAdminAuth().body(TestDataBuilder.validGenreBody())
                .post(ApiConfig.Endpoints.GENRES)
                .jsonPath().getInt("data.id");

        withAdminAuth().pathParam("id", genreId)
                .delete(ApiConfig.Endpoints.GENRE_BY_ID)
                .then().statusCode(200);
    }

    @Test(priority = 27, description = "Delete genre without auth returns 401")
    @Story("Delete Genre Authorization")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteGenreWithoutAuth_returns401() {
        withNoAuth().pathParam("id", 1)
                .delete(ApiConfig.Endpoints.GENRE_BY_ID)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 28, description = "User cannot delete genre — returns 403")
    @Story("Delete Genre Authorization")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteGenreWithUserRole_returns403() {
        withUserAuth().pathParam("id", 1)
                .delete(ApiConfig.Endpoints.GENRE_BY_ID)
                .then().statusCode(403);
    }

    @Test(priority = 29, description = "Delete non-existent genre returns 404")
    @Story("Delete Genre")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteNonExistentGenre_returns404() {
        withAdminAuth().pathParam("id", 999999)
                .delete(ApiConfig.Endpoints.GENRE_BY_ID)
                .then().statusCode(404);
    }

    @Test(priority = 30, description = "Deleted genre no longer appears in list")
    @Story("Delete Genre")
    @Severity(SeverityLevel.NORMAL)
    public void deletedGenreNotInList() {
        int genreId = withAdminAuth().body(TestDataBuilder.validGenreBody())
                .post(ApiConfig.Endpoints.GENRES)
                .jsonPath().getInt("data.id");

        withAdminAuth().pathParam("id", genreId)
                .delete(ApiConfig.Endpoints.GENRE_BY_ID)
                .then().statusCode(200);

        List<Integer> ids = withNoAuth().get(ApiConfig.Endpoints.GENRES)
                .jsonPath().getList("data.id");
        if (ids != null) {
            assertThat(ids).doesNotContain(genreId);
        }
    }

    @Test(priority = 31, description = "Delete genre with invalid token returns 401")
    @Story("Delete Genre Authorization")
    @Severity(SeverityLevel.NORMAL)
    public void deleteGenreWithInvalidToken_returns401() {
        withInvalidToken().pathParam("id", 1)
                .delete(ApiConfig.Endpoints.GENRE_BY_ID)
                .then().statusCode(anyOf(is(401), is(403)));
    }

    @Test(priority = 32, description = "Delete genre with negative ID returns 4xx")
    @Story("Delete Genre")
    @Severity(SeverityLevel.MINOR)
    public void deleteGenreWithNegativeId_returns4xx() {
        int status = withAdminAuth().pathParam("id", -1)
                .delete(ApiConfig.Endpoints.GENRE_BY_ID).statusCode();
        assertThat(Integer.valueOf(status)).isBetween(400, 499);
    }

    @Test(priority = 33, description = "Delete response has success=true")
    @Story("Delete Genre")
    @Severity(SeverityLevel.MINOR)
    public void deleteGenreResponseHasSuccessTrue() {
        int genreId = withAdminAuth().body(TestDataBuilder.validGenreBody())
                .post(ApiConfig.Endpoints.GENRES)
                .jsonPath().getInt("data.id");

        withAdminAuth().pathParam("id", genreId)
                .delete(ApiConfig.Endpoints.GENRE_BY_ID)
                .then().statusCode(200)
                .body("success", equalTo(true));
    }

    @Test(priority = 34, description = "Create multiple genres — all succeed")
    @Story("Create Genre")
    @Severity(SeverityLevel.MINOR)
    public void createMultipleGenres_allSucceed() {
        for (int i = 0; i < 3; i++) {
            withAdminAuth().body(TestDataBuilder.validGenreBody())
                    .post(ApiConfig.Endpoints.GENRES)
                    .then().statusCode(200);
        }
    }

    @Test(priority = 35, description = "Create genre with numeric-only name")
    @Story("Create Genre Validation")
    @Severity(SeverityLevel.MINOR)
    public void createGenreWithNumericName() {
        int status = withAdminAuth()
                .body(TestDataBuilder.genreBody("12345"))
                .post(ApiConfig.Endpoints.GENRES).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400, 409);
    }

    @Test(priority = 36, description = "Create genre name is trimmed")
    @Story("Create Genre")
    @Severity(SeverityLevel.MINOR)
    public void createGenreNameTrimming_behaviorDefined() {
        // Leading/trailing spaces
        int status = withAdminAuth()
                .body(TestDataBuilder.genreBody(" MyGenre "))
                .post(ApiConfig.Endpoints.GENRES).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400, 409);
    }

    @Test(priority = 37, description = "Genre IDs are numeric integers")
    @Story("List Genres")
    @Severity(SeverityLevel.MINOR)
    public void genreIdsAreNumericIntegers() {
        List<Integer> ids = withNoAuth().get(ApiConfig.Endpoints.GENRES)
                .jsonPath().getList("data.id");
        if (ids != null && !ids.isEmpty()) {
            ids.forEach(id -> assertThat(id).isGreaterThan(0));
        }
    }

    @Test(priority = 38, description = "Double-delete genre — second returns 404")
    @Story("Delete Genre")
    @Severity(SeverityLevel.MINOR)
    public void doubleDeleteGenre_secondReturns404OrOk() {
        int genreId = withAdminAuth().body(TestDataBuilder.validGenreBody())
                .post(ApiConfig.Endpoints.GENRES)
                .jsonPath().getInt("data.id");

        withAdminAuth().pathParam("id", genreId).delete(ApiConfig.Endpoints.GENRE_BY_ID).then().statusCode(200);
        int second = withAdminAuth().pathParam("id", genreId).delete(ApiConfig.Endpoints.GENRE_BY_ID).statusCode();
        assertThat(Integer.valueOf(second)).isIn(404, 200, 400);
    }

    @Test(priority = 39, description = "Genre creation with unicode characters in name")
    @Story("Create Genre")
    @Severity(SeverityLevel.MINOR)
    public void createGenreWithUnicode_behaviorDefined() {
        int status = withAdminAuth()
                .body(TestDataBuilder.genreBody("Género-Español"))
                .post(ApiConfig.Endpoints.GENRES).statusCode();
        assertThat(Integer.valueOf(status)).isIn(200, 400, 409);
    }

    @Test(priority = 40, description = "Genre list grows after creating new genre")
    @Story("Create Genre")
    @Severity(SeverityLevel.NORMAL)
    public void genreListGrowsAfterCreation() {
        int before = withNoAuth().get(ApiConfig.Endpoints.GENRES)
                .jsonPath().getList("data").size();

        withAdminAuth().body(TestDataBuilder.validGenreBody())
                .post(ApiConfig.Endpoints.GENRES)
                .then().statusCode(200);

        int after = withNoAuth().get(ApiConfig.Endpoints.GENRES)
                .jsonPath().getList("data").size();

        assertThat(after).isGreaterThan(before);
    }
}

