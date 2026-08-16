package com.gogame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gogame.ui.GameScreen
import com.gogame.ui.GameViewModel
import com.gogame.ui.GoGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoGameTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val viewModel: GameViewModel = viewModel()
                    GameScreen(viewModel = viewModel)
                }
            }
        }
    }
}
