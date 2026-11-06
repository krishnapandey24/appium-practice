package com.practice.expense.tests;

import com.practice.expense.base.BaseTest;
import com.practice.expense.pages.DebugPage;
import com.practice.expense.pages.InsightsPage;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class InsightsTest extends BaseTest {

    @Test
    public void thisMonthTotalsMatchSampleData() {
        deepLink("debug");
        InsightsPage insights = new DebugPage(driver).loadSampleData().back().openInsights();

        assertEquals(insights.total(), "₹12,799.25");
        assertEquals(insights.count(), "12");
        assertEquals(insights.legendAmount("BILLS"), "₹3,339.50");
        assertTrue(insights.pieChartDescription().startsWith("Spending by category: Bills"));
    }
}
