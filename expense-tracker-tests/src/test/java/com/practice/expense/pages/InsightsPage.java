package com.practice.expense.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class InsightsPage extends BasePage {

    public InsightsPage(AndroidDriver driver) {
        super(driver);
        waitFor(tag("stats_total"));
    }

    public String total() {
        return textOf(tag("stats_total"));
    }

    public String count() {
        return textOf(tag("stats_count"));
    }

    public String legendAmount(String category) {
        return textOf(tag("legend_amount_" + category));
    }

    public List<Integer> legendPercents() {
        List<WebElement> percents = driver.findElements(AppiumBy.androidUIAutomator(
                "new UiSelector().resourceIdMatches(\"legend_percent_.*\")"));
        return percents.stream()
                .map(e -> Integer.parseInt(e.getText().replace("%", "").trim()))
                .toList();
    }

    public String pieChartDescription() {
        return waitFor(tag("pie_chart")).getAttribute("content-desc");
    }
}
