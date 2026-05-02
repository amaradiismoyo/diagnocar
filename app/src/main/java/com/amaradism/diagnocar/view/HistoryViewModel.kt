package com.amaradism.diagnocar.view

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amaradism.diagnocar.data.history.HistoryEntity
import com.amaradism.diagnocar.data.history.HistoryRepository
import kotlinx.coroutines.launch

class HistoryViewModel(
    application: Application = Application()
) : ViewModel() {

    private val _currentImageUri = MutableLiveData<Uri?>()
    val currentImageUri: LiveData<Uri?> get() = _currentImageUri

    private val historyRepository = HistoryRepository(application)
    var historyList: MutableLiveData<List<HistoryEntity>> = MutableLiveData()

    init {
        historyList.value = getHistory()
    }

    private fun getHistory(): List<HistoryEntity> = historyRepository.getHistory()

    fun setCurrentImageUri(uri: Uri?) {
        _currentImageUri.value = uri
    }

    fun tambahRiwayat(historyEntity: HistoryEntity) {
        viewModelScope.launch {
            historyRepository.addHistory(historyEntity)
        }
    }
}
