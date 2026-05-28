package com.dailybliss.app.core.network

class FakeApiConfig(
    override val geminiApiKey: String = "fake_key",
    override val geminiModelName: String = "fake_model"
) : ApiConfig
