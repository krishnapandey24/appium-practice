package com.practice.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class Category(val label: String) {
    FOOD("Food"),
    TRANSPORT("Transport"),
    BILLS("Bills"),
    SHOPPING("Shopping"),
    ENTERTAINMENT("Entertainment"),
    HEALTH("Health"),
    EDUCATION("Education"),
    OTHER("Other"),
}

enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    CARD("Card"),
    UPI("UPI"),
}

enum class Currency(val code: String, val symbol: String, val displayName: String) {
    INR("INR", "₹", "Indian Rupee"),
    USD("USD", "$", "US Dollar"),
    EUR("EUR", "€", "Euro"),
}

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: Category,
    val date: LocalDate,
    val paymentMethod: PaymentMethod,
    val note: String = "",
    val reimbursable: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
