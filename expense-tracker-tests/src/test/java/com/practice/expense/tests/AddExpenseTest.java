package com.practice.expense.tests;

import com.practice.expense.base.BaseTest;
import com.practice.expense.pages.AddExpensePage;
import com.practice.expense.pages.ExpenseListPage;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AddExpenseTest extends BaseTest {

    @Test
    public void addedExpenseAppearsInList() {
        ExpenseListPage list = new ExpenseListPage(driver).waitUntilLoaded()
                .tapAdd()
                .title("Team lunch")
                .amount("450")
                .category("FOOD")
                .payment("CASH")
                .saveAndReturn();

        assertEquals(list.snackbarMessage(), "Expense added");
        assertEquals(list.countText(), "Showing 1 expenses");
        assertEquals(list.amountOf("Team lunch"), "₹450.00");
    }

    @Test
    public void emptyFormShowsAllErrors() {
        AddExpensePage form = new ExpenseListPage(driver).waitUntilLoaded().tapAdd();
        form.save();

        assertEquals(form.errorText("title"), "Title is required");
        assertEquals(form.errorText("amount"), "Amount is required");
        assertEquals(form.errorText("category"), "Choose a category");
        assertEquals(form.screenTitle(), "Add expense", "Should stay on the form");
    }

    @Test
    public void titleLongerThan40CharactersIsRejected() {
        AddExpensePage form = new ExpenseListPage(driver).waitUntilLoaded().tapAdd()
                .title("A".repeat(41))
                .amount("100")
                .category("OTHER");
        form.save();

        assertEquals(form.errorText("title"), "Title must be 40 characters or less");
    }

    @Test
    public void titleOfExactly40CharactersIsAccepted() {
        String title = "B".repeat(40);
        ExpenseListPage list = new ExpenseListPage(driver).waitUntilLoaded().tapAdd()
                .title(title)
                .amount("100")
                .category("OTHER")
                .saveAndReturn();

        assertTrue(list.visibleTitles().contains(title));
    }
}
