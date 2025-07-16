package com.ali.demono.features.game.presentation.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ali.demono.core.extensions.enableEdgeToEdgeDisplay
import com.ali.demono.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        enableEdgeToEdgeDisplay(binding.root)
        navigateToGame()
    }

    private fun setupBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


    private fun navigateToGame() {
        lifecycleScope.launch {
            delay(1000)
            startActivity(Intent(this@MainActivity, GameActivity::class.java))
            finish()
        }
    }
}