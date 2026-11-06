package com.practice.expense.pages;

import io.appium.java_client.android.AndroidDriver;

public class AddExpensePage extends BasePage {

    public AddExpensePage(AndroidDriver driver) {
        super(driver);
        waitFor(tag("input_title"));
    }

    public AddExpensePage title(String value) {
        type(tag("input_title"), value);
        return this;
    }

    public AddExpensePage amount(String value) {
        type(tag("input_amount"), value);
        hideKeyboard();
        return this;
    }

    public AddExpensePage category(String category) {
        tap(tag("input_category"));
        tap(tag("category_option_" + category));
        return this;
    }

    public AddExpensePage payment(String method) {
        scrollTo("payment_option_" + method).click();
        return this;
    }

    public AddExpensePage reimbursable() {
        scrollTo("checkbox_reimbursable").click();
        return this;
    }

    public String categoryValue() {
        return textOf(tag("input_category_value"));
    }

    public String dateValue() {
        return textOf(tag("input_date_value"));
    }

    /** Taps save. Returns to the list on success, stays here when validation fails. */
    public void save() {
        hideKeyboard();
        scrollTo("save_button").click();
    }

    public ExpenseListPage saveAndReturn() {
        save();
        return new ExpenseListPage(driver).waitUntilLoaded();
    }

    public boolean hasError(String field) {
        return isShown(tag("error_" + field));
    }

    public String errorText(String field) {
        return textOf(tag("error_" + field));
    }

    public void back() {
        hideKeyboard();
        tap(tag("back_button"));
    }

    public boolean isDiscardDialogShown() {
        return isShown(tag("discard_dialog"));
    }
}
