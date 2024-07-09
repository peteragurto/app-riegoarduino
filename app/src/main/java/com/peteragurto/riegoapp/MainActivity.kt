package com.peteragurto.riegoapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.peteragurto.riegoapp.data.DataStoreRepository
import com.peteragurto.riegoapp.presentation.RiegoScreen
import com.peteragurto.riegoapp.presentation.RiegoViewModel
import com.peteragurto.riegoapp.presentation.RiegoViewModelFactory
import com.peteragurto.riegoapp.ui.theme.RiegoAppTheme

private val Context.dataStore by preferencesDataStore(name = "riego")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataStoreRepository = DataStoreRepository(dataStore)
        val factory = RiegoViewModelFactory(dataStoreRepository)
        val viewModel = ViewModelProvider(this, factory).get(RiegoViewModel::class.java)

        enableEdgeToEdge()
        setContent {
            RiegoAppTheme {
                RiegoScreen(viewModel = viewModel)
            }
        }
    }
}

