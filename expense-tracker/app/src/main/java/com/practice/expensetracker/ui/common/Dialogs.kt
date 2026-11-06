package com.practice.expensetracker.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun DeleteExpenseDialog(
    expenseTitle: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onConfirm,
        modifier = Modifier.appiumRoot().testTag(TestTags.DELETE_DIALOG),
        title = { Text("Delete expense?") },
        text = { Text("\"$expenseTitle\" will be removed.") },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.testTag(TestTags.DELETE_DIALOG_CONFIRM)) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel, modifier = Modifier.testTag(TestTags.DELETE_DIALOG_CANCEL)) {
                Text("Cancel")
            }
        },
    )
}
