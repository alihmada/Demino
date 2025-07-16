package com.ali.demono.core.extensions

import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun ComponentActivity.enableEdgeToEdgeDisplay(rootView: View) {
    enableEdgeToEdge()
    ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        insets
    }
}