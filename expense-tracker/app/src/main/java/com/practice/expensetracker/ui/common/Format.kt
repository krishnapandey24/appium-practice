package com.practice.expensetracker.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.practice.expensetracker.data.Category
import com.practice.expensetracker.data.Currency
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)

fun formatMoney(amount: Double, currency: Currency): String =
    currency.symbol + String.format(Locale.US, "%,.2f", amount)

fun formatDate(date: LocalDate): String = date.format(dateFormatter)

val Category.icon: ImageVector
    get() = when (this) {
        Category.FOOD -> Icons.Filled.Restaurant
        Category.TRANSPORT -> Icons.Filled.DirectionsCar
        Category.BILLS -> Icons.AutoMirrored.Filled.ReceiptLong
        Category.SHOPPING -> Icons.Filled.ShoppingBag
        Category.ENTERTAINMENT -> Icons.Filled.Movie
        Category.HEALTH -> Icons.Filled.LocalHospital
        Category.EDUCATION -> Icons.Filled.School
        Category.OTHER -> Icons.Filled.MoreHoriz
    }

val Category.color: Color
    get() = when (this) {
        Category.FOOD -> Color(0xFFE07A3F)
        Category.TRANSPORT -> Color(0xFF3F7FE0)
        Category.BILLS -> Color(0xFF8E5BD6)
        Category.SHOPPING -> Color(0xFFD64F8E)
        Category.ENTERTAINMENT -> Color(0xFFE0B23F)
        Category.HEALTH -> Color(0xFF3FB58A)
        Category.EDUCATION -> Color(0xFF4FA9C9)
        Category.OTHER -> Color(0xFF8A8F98)
    }
