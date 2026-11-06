package com.practice.expense.pages;

import io.appium.java_client.android.AndroidDriver;

public class DebugPage extends BasePage {

    public DebugPage(AndroidDriver driver) {
        super(driver);
        waitFor(tag("debug_expense_count"));
    }

    public int expenseCount() {
        return Integer.parseInt(textOf(tag("debug_expense_count")));
    }

    public DebugPage loadSampleData() {
        tap(tag("debug_load_sample"));
        waitFor(text("Loaded sample dataset (16 expenses)"));
        return this;
    }

    public DebugPage generate(int count) {
        type(tag("debug_count_input"), String.valueOf(count));
        hideKeyboard();
        tap(tag("debug_generate_button"));
        waitFor(text("Generated " + count + " expenses"));
        return this;
    }

    public ExpenseListPage back() {
        tap(tag("back_button"));
        return new ExpenseListPage(driver).waitUntilLoaded();
    }
}
