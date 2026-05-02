package com.amaradism.diagnocar.data.history

import android.app.Application

class HistoryRepository(application: Application) {
    private val historyDao = HistoryDatabase.getDatabase(application).HistoryDao()

    suspend fun addHistory(historyEntity: HistoryEntity) {
        historyDao.addHistory(historyEntity)
    }

    fun getHistory(): List<HistoryEntity> = historyDao.getHistory()
}
