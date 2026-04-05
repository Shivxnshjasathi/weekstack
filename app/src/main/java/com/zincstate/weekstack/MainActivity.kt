package com.zincstate.weekstack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zincstate.weekstack.presentation.home.HomeScreen
import com.zincstate.weekstack.ui.theme.WeekstackTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeekstackTheme {
                HomeScreen()
            }
        }
    }
}