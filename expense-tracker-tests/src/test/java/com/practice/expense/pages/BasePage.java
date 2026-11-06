package com.practice.expense.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public abstract class BasePage {

    protected final AndroidDriver driver;
    private final WebDriverWait wait;

    protected BasePage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Compose test tags are exposed as resource-ids WITHOUT a package prefix
     * ("save_button", not "com.practice.expensetracker:id/save_button").
     * AppiumBy.id() adds the package prefix and finds nothing, so match the raw resource-id.
     */
    protected static By tag(String testTag) {
        return AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"" + testTag + "\")");
    }

    protected static By text(String text) {
        return AppiumBy.androidUIAutomator("new UiSelector().text(\"" + text + "\")");
    }

    protected WebElement waitFor(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void tap(By locator) {
        waitFor(locator).click();
    }

    /**
     * Replaces the field's whole value. On Compose text fields clear() doesn't empty the
     * field and sendKeys() appends to what is there, so use the driver's replace command.
     */
    protected void type(By locator, String value) {
        WebElement field = waitFor(locator);
        driver.executeScript("mobile: replaceElementValue", Map.of(
                "elementId", ((org.openqa.selenium.remote.RemoteWebElement) field).getId(),
                "text", value));
    }

    protected String textOf(By locator) {
        return waitFor(locator).getText();
    }

    protected boolean isShown(By locator) {
        List<WebElement> found = driver.findElements(locator);
        return !found.isEmpty() && found.get(0).isDisplayed();
    }

    protected boolean isChecked(By locator) {
        return Boolean.parseBoolean(waitFor(locator).getAttribute("checked"));
    }

    /** Scrolls the first scrollable container until the element with this tag is visible. */
    protected WebElement scrollTo(String testTag) {
        return driver.findElement(AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true))"
                        + ".scrollIntoView(new UiSelector().resourceId(\"" + testTag + "\"))"));
    }

    protected void hideKeyboard() {
        if (driver.isKeyboardShown()) driver.hideKeyboard();
    }

    protected void swipeLeft(WebElement element) {
        driver.executeScript("mobile: swipeGesture", Map.of(
                "elementId", ((org.openqa.selenium.remote.RemoteWebElement) element).getId(),
                "direction", "left",
                "percent", 0.8));
    }

    public String screenTitle() {
        return textOf(tag("screen_title"));
    }

    public String snackbarMessage() {
        return textOf(tag("snackbar_message"));
    }
}
