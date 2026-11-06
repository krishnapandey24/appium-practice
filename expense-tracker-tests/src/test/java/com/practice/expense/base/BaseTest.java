package com.practice.expense.base;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Map;

/**
 * One Appium session per test class. Before every test the app's data is wiped
 * and the app is relaunched, so each test starts from the empty state.
 */
public abstract class BaseTest {

    public static final String APP_PACKAGE = "com.practice.expensetracker";
    private static final String SERVER_URL = System.getProperty("appium.url", "http://127.0.0.1:4723");

    protected AndroidDriver driver;

    @BeforeClass(alwaysRun = true)
    public void startSession() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options()
                .setPlatformName("Android")
                .setAutomationName("UiAutomator2")
                .setAppPackage(APP_PACKAGE)
                .setAppActivity(".MainActivity")
                .setNoReset(true)
                .setNewCommandTimeout(Duration.ofSeconds(120));
        driver = new AndroidDriver(new URL(SERVER_URL), options);
    }

    @BeforeMethod(alwaysRun = true)
    public void resetApp() {
        driver.executeScript("mobile: clearApp", Map.of("appId", APP_PACKAGE));
        driver.executeScript("mobile: activateApp", Map.of("appId", APP_PACKAGE));
    }

    @AfterClass(alwaysRun = true)
    public void stopSession() {
        if (driver != null) driver.quit();
    }

    /** Opens a screen directly, e.g. deepLink("debug") or deepLink("expense/3"). */
    protected void deepLink(String path) {
        driver.executeScript("mobile: deepLink", Map.of(
                "url", "expensetracker://" + path,
                "package", APP_PACKAGE));
    }
}
