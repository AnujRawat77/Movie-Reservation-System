/**
 * Cucumber step-definition classes for UI BDD tests.
 *
 * <p>Each class in this package implements the Gherkin steps defined in
 * {@code src/test/resources/features/ui/*.feature} files.</p>
 *
 * <h2>Naming convention</h2>
 * <ul>
 *   <li>{@code LoginSteps.java}      — steps for login / authentication scenarios</li>
 *   <li>{@code MoviesSteps.java}     — steps for browsing movies</li>
 *   <li>{@code ReservationSteps.java}— steps for booking / reservation flow</li>
 * </ul>
 *
 * <h2>Template</h2>
 * <pre>
 * import com.cinereserve.ui.context.DriverContext;
 * import com.cinereserve.ui.pages.LoginPage;
 * import io.cucumber.java.en.Given;
 * import io.cucumber.java.en.Then;
 * import io.cucumber.java.en.When;
 *
 * public class LoginSteps {
 *
 *     private final DriverContext ctx;
 *     private final LoginPage loginPage;
 *
 *     // PicoContainer injects DriverContext
 *     public LoginSteps(DriverContext ctx) {
 *         this.ctx  = ctx;
 *         this.loginPage = new LoginPage(ctx);
 *     }
 *
 *     &#64;Given("the user is on the login page")
 *     public void userOnLoginPage() {
 *         ctx.navigateTo("/login");
 *     }
 *
 *     &#64;When("the user logs in with {string} and {string}")
 *     public void userLogsIn(String email, String password) {
 *         loginPage.login(email, password);
 *     }
 *
 *     &#64;Then("the user should be redirected to the home page")
 *     public void verifyRedirectedHome() {
 *         assertThat(ctx.driver().getCurrentUrl()).endsWith("/");
 *     } * }
 * </pre>
 */
package com.cinereserve.ui.steps;

