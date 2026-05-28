package com.dailybliss.app.data.remote.dto

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JsonParsingTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parse IpResponse`() {
        val raw = """{"latitude": 1.0, "longitude": 2.0, "city": "Test"}"""
        val obj = json.decodeFromString<IpResponse>(raw)
        assertEquals(1.0, obj.latitude)
        assertEquals("Test", obj.city)
    }

    @Test
    fun `parse WeatherResponse`() {
        val raw = """{"current": {"temperature_2m": 25.0, "wind_speed_10m": 10.0}}"""
        val obj = json.decodeFromString<WeatherResponse>(raw)
        assertEquals(25.0, obj.current.temperature)
    }

    @Test
    fun `parse NewsResponse`() {
        val raw = """{"data": [{"title": "T", "contentSnippet": "S", "link": "L", "image": {"small": "s", "large": "l"}}]}"""
        val obj = json.decodeFromString<NewsResponse>(raw)
        assertEquals(1, obj.data.size)
        assertEquals("T", obj.data[0].title)
        assertEquals("l", obj.data[0].image?.large)
    }

    @Test
    fun `parse FrankfurterResponse`() {
        val raw = """{"amount": 1.0, "base": "USD", "date": "2024", "rates": {"IDR": 15000.0}}"""
        val obj = json.decodeFromString<FrankfurterResponse>(raw)
        assertEquals(15000.0, obj.rates["IDR"])
    }
}
