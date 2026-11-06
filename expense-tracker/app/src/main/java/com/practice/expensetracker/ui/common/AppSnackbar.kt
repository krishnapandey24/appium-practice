package com.practice.expensetracker.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/** One app-wide snackbar state, shown by whichever screen's Scaffold is on top. */
val LocalSnackbarHostState = staticCompositionLocalOf { SnackbarHostState() }

@Composable
fun AppSnackbarHost(hostState: SnackbarHostState = LocalSnackbarHostState.current) {
    SnackbarHost(hostState) { data ->
        Snackbar(
            modifier = Modifier.padding(12.dp).testTag(TestTags.SNACKBAR),
            action = data.visuals.actionLabel?.let { label ->
                {
                    TextButton(
                        onClick = { data.performAction() },
                        modifier = Modifier.testTag(TestTags.SNACKBAR_ACTION),
                    ) { Text(label) }
                }
            },
        ) {
            Text(data.visuals.message, modifier = Modifier.testTag(TestTags.SNACKBAR_MESSAGE))
        }
    }
}
