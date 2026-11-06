package com.practice.expense.pages;

import io.appium.java_client.android.AndroidDriver;

public class SettingsPage extends BasePage {

    public SettingsPage(AndroidDriver driver) {
        super(driver);
        waitFor(tag("settings_currency"));
    }

    public void chooseCurrency(String code) {
        tap(tag("settings_currency"));
        tap(tag("currency_option_" + code));
    }

    public String currencyValue() {
        return textOf(tag("settings_currency_value"));
    }

    public void setBudget(String value) {
        type(tag("settings_budget_input"), value);
        tap(tag("settings_budget_save"));
    }

    public boolean isConfirmDeleteOn() {
        return isChecked(tag("settings_confirm_delete_switch"));
    }

    public void toggleConfirmDelete() {
        tap(tag("settings_confirm_delete_switch"));
    }

    public ExpenseListPage openExpenses() {
        tap(tag("nav_expenses"));
        return new ExpenseListPage(driver).waitUntilLoaded();
    }
}
