package com.practice.expensetracker

import android.app.Application
import com.practice.expensetracker.data.AppDatabase
import com.practice.expensetracker.data.ExpenseRepository
import com.practice.expensetracker.data.SettingsStore
import kotlinx.coroutines.channels.Channel

class AppContainer(app: Application) {
    val settings = SettingsStore(app)
    val database = AppDatabase.create(app)
    val repository = ExpenseRepository(database.expenseDao(), settings)

    /** One-off messages (e.g. "Expense added") shown in the app-wide snackbar. */
    val messages = Channel<String>(Channel.BUFFERED)
}

class ExpenseApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
