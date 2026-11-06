package com.practice.expensetracker.ui.common

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId

/**
 * Exposes Compose test tags to UiAutomator / Appium as resource-ids.
 *
 * Apply it on the root of the activity AND on the content of every dialog, dropdown
 * menu and popup: those open in a separate window, so they do not inherit the flag
 * from the activity root.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.appiumRoot(): Modifier = this.semantics { testTagsAsResourceId = true }
