package com.practice.expensetracker.ui.common

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.practice.expensetracker.AppContainer
import com.practice.expensetracker.ExpenseApp
import com.practice.expensetracker.ui.debug.DebugViewModel
import com.practice.expensetracker.ui.edit.AddEditViewModel
import com.practice.expensetracker.ui.list.ExpenseListViewModel
import com.practice.expensetracker.ui.settings.SettingsViewModel
import com.practice.expensetracker.ui.stats.StatsViewModel

private fun CreationExtras.container(): AppContainer =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ExpenseApp).container

val AppViewModelFactory = viewModelFactory {
    initializer {
        val c = container()
        ExpenseListViewModel(c.repository, c.settings, c.messages)
    }
    initializer {
        val c = container()
        AddEditViewModel(createSavedStateHandle(), c.repository, c.settings, c.messages)
    }
    initializer {
        val c = container()
        StatsViewModel(c.repository, c.settings)
    }
    initializer {
        val c = container()
        SettingsViewModel(c.settings, c.messages)
    }
    initializer {
        val c = container()
        DebugViewModel(c.repository, c.settings)
    }
}
