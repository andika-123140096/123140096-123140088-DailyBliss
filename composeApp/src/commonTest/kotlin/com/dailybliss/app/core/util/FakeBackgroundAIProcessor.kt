package com.dailybliss.app.core.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fake BackgroundAIProcessor for testing.
 */
class FakeBackgroundAIProcessor : BackgroundAIProcessor {
    private val _isProcessing = MutableStateFlow(false)
    override val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    var processMomentCalled = false
    var updateGlobalSummaryCalled = false
    var lastProcessedMomentId: Long? = null

    fun setProcessing(processing: Boolean) {
        _isProcessing.value = processing
    }

    override fun processMoment(momentId: Long, force: Boolean) {
        processMomentCalled = true
        lastProcessedMomentId = momentId
    }

    override fun updateGlobalSummary() {
        updateGlobalSummaryCalled = true
    }
}
