/**
 * Page Object Model (POM) classes for the CineReserve frontend.
 *
 * <p>Each class in this package models a single page or reusable component
 * of the application UI. Pages encapsulate element locators and user
 * interactions so that test/step-definition code never touches
 * {@code WebDriver} directly.</p>
 *
 * <h2>Naming convention</h2>
 * <ul>
 *   <li>{@code HomePage.java}       — {@code /}</li>
 *   <li>{@code LoginPage.java}      — {@code /login}</li>
 *   <li>{@code MoviesPage.java}     — {@code /movies}</li>
 *   <li>{@code MovieDetailPage.java}— {@code /movies/{id}}</li>
 *   <li>{@code CheckoutPage.java}   — reservation checkout flow</li>
 * </ul>
 *
 * <h2>Template</h2>
 * <pre>
 * import com.cinereserve.ui.context.DriverContext;
 * import org.openqa.selenium.By;
 * import org.openqa.selenium.WebElement;
 * import org.openqa.selenium.support.PageFactory;
 * import org.openqa.selenium.support.FindBy;
 *
 * public class LoginPage {
 *
 *     private final DriverContext ctx;
 *
 *     &#64;FindBy(id = "email")    private WebElement emailInput;
 *     &#64;FindBy(id = "password") private WebElement passwordInput;
 *     &#64;FindBy(css = "[type='submit']") private WebElement submitButton;
 *
 *     public LoginPage(DriverContext ctx) {
 *         this.ctx = ctx;
 *         PageFactory.initElements(ctx.driver(), this);
 *     }
 *
 *     public void login(String email, String password) {
 *         emailInput.clear();
 *         emailInput.sendKeys(email);
 *         passwordInput.clear();
 *         passwordInput.sendKeys(password);
 *         submitButton.click();
 *     }
 * }
 * </pre>
 */
package com.cinereserve.ui.pages;

