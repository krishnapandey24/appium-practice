package com.practice.expense.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;

import java.util.Comparator;
import java.util.List;

public class ExpenseListPage extends BasePage {

    public ExpenseListPage(AndroidDriver driver) {
        super(driver);
    }

    public ExpenseListPage waitUntilLoaded() {
        waitFor(tag("expense_count_text"));
        return this;
    }

    public AddExpensePage tapAdd() {
        tap(tag("add_expense_fab"));
        return new AddExpensePage(driver);
    }

    public boolean isEmptyStateShown() {
        return isShown(tag("empty_state"));
    }

    public String countText() {
        return textOf(tag("expense_count_text"));
    }

    public String spentThisMonth() {
        return textOf(tag("spent_amount"));
    }

    public void search(String query) {
        type(tag("search_field"), query);
        hideKeyboard();
    }

    public void sortBy(String option) {
        tap(tag("sort_button"));
        tap(tag("sort_option_" + option));
    }

    public void tapCategoryChip(String category) {
        tap(tag("filter_chip_" + category));
    }

    public void tapPeriodChip(String period) {
        tap(tag("period_chip_" + period));
    }

    /** Titles of the visible rows, top to bottom (lazy list: only what is on screen). */
    public List<String> visibleTitles() {
        waitUntilLoaded();
        List<WebElement> titles = driver.findElements(AppiumBy.androidUIAutomator(
                "new UiSelector().resourceIdMatches(\"expense_item_title_.*\")"));
        return titles.stream()
                .sorted(Comparator.comparingInt(e -> e.getLocation().getY()))
                .map(WebElement::getText)
                .toList();
    }

    public WebElement rowTitled(String title) {
        return waitFor(AppiumBy.androidUIAutomator(
                "new UiSelector().resourceIdMatches(\"expense_item_title_.*\").text(\"" + title + "\")"));
    }

    /** Returns the database id behind a row, read from its resource-id. */
    public String idOf(String title) {
        String resourceId = rowTitled(title).getAttribute("resource-id");
        return resourceId.substring(resourceId.lastIndexOf('_') + 1);
    }

    public String amountOf(String title) {
        return textOf(tag("expense_item_amount_" + idOf(title)));
    }

    public AddExpensePage open(String title) {
        rowTitled(title).click();
        return new AddExpensePage(driver);
    }

    public void swipeToDelete(String title) {
        swipeLeft(waitFor(tag("expense_item_" + idOf(title))));
    }

    public void confirmDelete() {
        tap(tag("delete_dialog_confirm"));
    }

    public void undoDelete() {
        tap(tag("snackbar_action"));
    }

    public InsightsPage openInsights() {
        tap(tag("nav_stats"));
        return new InsightsPage(driver);
    }

    public SettingsPage openSettings() {
        tap(tag("nav_settings"));
        return new SettingsPage(driver);
    }
}
