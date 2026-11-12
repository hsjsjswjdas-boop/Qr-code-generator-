package com.example.data.model

import java.util.UUID

data class SavedProfile(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val vpa: String,
    val payeeName: String
)

data class QrHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val vpa: String,
    val payeeName: String,
    val amount: String,
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)
