package com.practice.expensetracker.data

import java.time.LocalDate
import kotlin.random.Random

object SampleData {

    /**
     * A fixed dataset so tests can assert exact numbers.
     * Dates are relative to the current month so filters like "This month" always have data.
     * The expected values for this dataset are listed in README.md.
     */
    fun fixed(today: LocalDate = LocalDate.now()): List<Expense> {
        val month = today.withDayOfMonth(1)
        val lastDay = today.withDayOfMonth(today.lengthOfMonth())
        val prev = month.minusMonths(1)
        val prev2 = month.minusMonths(2)
        return listOf(
            Expense(title = "Groceries - BigBasket", amount = 1250.00, category = Category.FOOD, date = month.withDayOfMonth(2), paymentMethod = PaymentMethod.UPI),
            Expense(title = "Metro card recharge", amount = 500.00, category = Category.TRANSPORT, date = month.withDayOfMonth(3), paymentMethod = PaymentMethod.CARD),
            Expense(title = "Electricity bill", amount = 2340.50, category = Category.BILLS, date = month.withDayOfMonth(5), paymentMethod = PaymentMethod.CARD, note = "October usage"),
            Expense(title = "Lunch with team", amount = 860.00, category = Category.FOOD, date = month.withDayOfMonth(6), paymentMethod = PaymentMethod.CASH, reimbursable = true),
            Expense(title = "Movie tickets", amount = 640.00, category = Category.ENTERTAINMENT, date = month.withDayOfMonth(8), paymentMethod = PaymentMethod.UPI),
            Expense(title = "Uber to airport", amount = 1120.00, category = Category.TRANSPORT, date = month.withDayOfMonth(10), paymentMethod = PaymentMethod.UPI, reimbursable = true),
            Expense(title = "Pharmacy", amount = 315.75, category = Category.HEALTH, date = month.withDayOfMonth(12), paymentMethod = PaymentMethod.CASH),
            Expense(title = "Coffee", amount = 180.00, category = Category.FOOD, date = month.withDayOfMonth(14), paymentMethod = PaymentMethod.CARD),
            Expense(title = "New headphones", amount = 2999.00, category = Category.SHOPPING, date = month.withDayOfMonth(15), paymentMethod = PaymentMethod.CARD),
            Expense(title = "Internet bill", amount = 999.00, category = Category.BILLS, date = month.withDayOfMonth(18), paymentMethod = PaymentMethod.CARD),
            Expense(title = "Gym membership", amount = 1500.00, category = Category.HEALTH, date = month.withDayOfMonth(20), paymentMethod = PaymentMethod.UPI),
            Expense(title = "Dinner", amount = 95.00, category = Category.FOOD, date = lastDay, paymentMethod = PaymentMethod.CASH),
            Expense(title = "Rent", amount = 15000.00, category = Category.BILLS, date = prev.withDayOfMonth(1), paymentMethod = PaymentMethod.CARD),
            Expense(title = "Books", amount = 650.00, category = Category.EDUCATION, date = prev.withDayOfMonth(9), paymentMethod = PaymentMethod.UPI),
            Expense(title = "Petrol", amount = 2200.00, category = Category.TRANSPORT, date = prev2.withDayOfMonth(11), paymentMethod = PaymentMethod.CARD),
            Expense(title = "Birthday gift", amount = 1800.00, category = Category.SHOPPING, date = prev2.withDayOfMonth(22), paymentMethod = PaymentMethod.UPI),
        )
    }

    private val titles = mapOf(
        Category.FOOD to listOf("Groceries", "Lunch", "Dinner", "Coffee", "Snacks", "Pizza night", "Breakfast"),
        Category.TRANSPORT to listOf("Uber", "Metro", "Petrol", "Auto rickshaw", "Parking", "Bus pass"),
        Category.BILLS to listOf("Electricity bill", "Water bill", "Phone recharge", "Internet bill", "Gas cylinder"),
        Category.SHOPPING to listOf("T-shirt", "Shoes", "Amazon order", "Kitchen items", "Backpack"),
        Category.ENTERTAINMENT to listOf("Movie", "Concert", "Netflix", "Bowling", "Game purchase"),
        Category.HEALTH to listOf("Pharmacy", "Doctor visit", "Gym", "Vitamins", "Lab test"),
        Category.EDUCATION to listOf("Books", "Online course", "Stationery", "Workshop"),
        Category.OTHER to listOf("Gift", "Donation", "Haircut", "Laundry"),
    )

    fun random(random: Random = Random.Default, today: LocalDate = LocalDate.now()): Expense {
        val category = Category.entries.random(random)
        val amount = (random.nextInt(50, 5000) + if (random.nextBoolean()) 0.5 else 0.0)
        return Expense(
            title = titles.getValue(category).random(random),
            amount = amount,
            category = category,
            date = today.minusDays(random.nextLong(0, 90)),
            paymentMethod = PaymentMethod.entries.random(random),
            note = if (random.nextInt(4) == 0) "Auto-generated" else "",
            reimbursable = random.nextInt(6) == 0,
        )
    }
}
