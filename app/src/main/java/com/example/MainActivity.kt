package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.CwcViewModel
import com.example.CwcViewModelFactory
import com.example.data.AppDatabase
import com.example.data.CwcRepository
import com.example.ui.MainAppContainer
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize unified local Android Room Database & Clean Repository
    val database = AppDatabase.getDatabase(applicationContext)
    val dao = database.appDao()
    val repository = CwcRepository(dao)

    // Instantiate View Model with proper application reference
    val viewModelClassFactory = CwcViewModelFactory(application, repository)
    val viewModel: CwcViewModel by viewModels { viewModelClassFactory }

    setContent {
      MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          MainAppContainer(viewModel = viewModel)
        }
      }
    }
  }
}
