package com.ali.demono.features.game.presentation.ui.helper

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Helper class for creating Material Design dialogs
 * Follows clean architecture principles with proper separation of concerns
 */
object MaterialDialogHelper {

    fun showInputDialog(
        context: Context,
        title: String,
        customView: View,
        positiveButtonText: String,
        negativeButtonText: String,
        onPositiveClick: () -> Unit,
        onNegativeClick: (() -> Unit)? = null
    ): AlertDialog {
        return MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setView(customView)
            .setPositiveButton(positiveButtonText) { _, _ ->
                onPositiveClick()
            }
            .setNegativeButton(negativeButtonText) { _, _ ->
                onNegativeClick?.invoke()
            }
            .setCancelable(false)
            .create()
            .apply {
                show()
            }
    }

    fun showConfirmationDialog(
        context: Context,
        title: String,
        message: String,
        positiveButtonText: String,
        negativeButtonText: String,
        onPositiveClick: () -> Unit,
        onNegativeClick: (() -> Unit)? = null,
        cancelable: Boolean = true
    ): AlertDialog {
        return MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { _, _ ->
                onPositiveClick()
            }
            .setNegativeButton(negativeButtonText) { _, _ ->
                onNegativeClick?.invoke()
            }
            .setCancelable(cancelable)
            .create()
            .apply {
                show()
            }
    }
} 