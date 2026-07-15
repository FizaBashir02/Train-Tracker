package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.service.ApiClient
import com.example.ui.AppViewModel
import com.example.ui.MainAppContainer

class MainActivity : ComponentActivity() {
  private val viewModel: AppViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    ApiClient.initialize(this)
    setContent {
      MainAppContainer(viewModel = viewModel)
    }
  }
}
