package com.example.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val dao: CalculationHistoryDao) {
    val allHistory: Flow<List<CalculationHistory>> = dao.getAllHistory()

    suspend fun insert(history: CalculationHistory) {
        dao.insertHistory(history)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteHistoryById(id)
    }

    suspend fun clearAll() {
        dao.clearAllHistory()
    }
}
