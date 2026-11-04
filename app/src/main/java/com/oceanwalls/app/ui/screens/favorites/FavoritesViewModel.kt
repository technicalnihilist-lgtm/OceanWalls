package com.oceanwalls.app.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oceanwalls.app.data.FavoritesRepository
import com.oceanwalls.app.data.model.Wallpaper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class FavoritesViewModel(favorites: FavoritesRepository) : ViewModel() {
    val items: StateFlow<List<Wallpaper>> = favorites.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
