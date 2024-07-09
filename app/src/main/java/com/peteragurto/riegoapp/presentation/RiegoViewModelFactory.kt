package com.peteragurto.riegoapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.peteragurto.riegoapp.data.DataStoreRepository

class RiegoViewModelFactory(
    private val dataStoreRepository: DataStoreRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RiegoViewModel::class.java)) {
            return RiegoViewModel(dataStoreRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}