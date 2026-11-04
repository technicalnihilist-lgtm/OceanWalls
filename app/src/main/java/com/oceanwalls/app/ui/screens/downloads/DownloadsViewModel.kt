package com.oceanwalls.app.ui.screens.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oceanwalls.app.data.DownloadRepository
import com.oceanwalls.app.data.local.DownloadEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DownloadsViewModel(private val repository: DownloadRepository) : ViewModel() {

    val items: StateFlow<List<DownloadEntity>> = repository.downloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(pageId: Long) { viewModelScope.launch { repository.delete(pageId) } }
}
