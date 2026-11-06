package com.practice.expense.tests;

import com.practice.expense.base.BaseTest;
import com.practice.expense.pages.DebugPage;
import com.practice.expense.pages.ExpenseListPage;
import com.practice.expense.pages.InsightsPage;
import com.practice.expense.pages.SettingsPage;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class NavigationTest extends BaseTest {

    @Test
    public void freshInstallShowsEmptyState() {
        ExpenseListPage list = new ExpenseListPage(driver).waitUntilLoaded();

        assertEquals(list.screenTitle(), "Expenses");
        assertTrue(list.isEmptyStateShown(), "Empty state should be visible with no data");
        assertEquals(list.countText(), "Showing 0 expenses");
        assertEquals(list.spentThisMonth(), "₹0.00");
    }

    @Test
    public void bottomNavOpensEachScreen() {
        ExpenseListPage list = new ExpenseListPage(driver).waitUntilLoaded();

        InsightsPage insights = list.openInsights();
        assertEquals(insights.screenTitle(), "Insights");

        SettingsPage settings = new ExpenseListPage(driver).openSettings();
        assertEquals(settings.screenTitle(), "Settings");

        ExpenseListPage back = settings.openExpenses();
        assertEquals(back.screenTitle(), "Expenses");
    }

    @Test
    public void deepLinkOpensDebugTools() {
        deepLink("debug");
        DebugPage debug = new DebugPage(driver);

        assertEquals(debug.screenTitle(), "Debug tools");
        assertEquals(debug.expenseCount(), 0);
    }
}
