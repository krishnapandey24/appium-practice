package com.practice.expense.tests;

import com.practice.expense.base.BaseTest;
import com.practice.expense.pages.DebugPage;
import com.practice.expense.pages.ExpenseListPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ExpenseListTest extends BaseTest {

    private ExpenseListPage list;

    @BeforeMethod
    public void loadSampleData() {
        deepLink("debug");
        list = new DebugPage(driver).loadSampleData().back();
    }

    @Test
    public void sampleDataShowsAllSixteenExpenses() {
        assertEquals(list.countText(), "Showing 16 expenses");
    }

    @Test
    public void sortOldestFirstStartsWithPetrol() {
        list.sortBy("DATE_ASC");
        assertEquals(list.visibleTitles().subList(0, 2), List.of("Petrol", "Birthday gift"));
    }

    @Test
    public void searchByExactTitleFindsOneExpense() {
        list.search("Pharmacy");
        assertEquals(list.visibleTitles(), List.of("Pharmacy"));
    }

    @Test
    public void editingTitleUpdatesTheList() {
        list.open("Coffee").title("Cold coffee").saveAndReturn();

        List<String> titles = list.visibleTitles();
        assertTrue(titles.contains("Cold coffee"), "Renamed row should be visible: " + titles);
        assertFalse(titles.contains("Coffee"), "Old title should be gone: " + titles);
    }

    @Test
    public void swipeAndConfirmDeletesExpense() {
        list.swipeToDelete("Gym membership");
        list.confirmDelete();

        assertEquals(list.snackbarMessage(), "Expense deleted");
        assertEquals(list.countText(), "Showing 15 expenses");
    }
}
