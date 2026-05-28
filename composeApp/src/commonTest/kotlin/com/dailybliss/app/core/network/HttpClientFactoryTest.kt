package com.dailybliss.app.core.network

import kotlin.test.Test
import kotlin.test.assertNotNull

class HttpClientFactoryTest {

    @Test
    fun `create should return client`() {
        val client = HttpClientFactory.create(enableLogging = true)
        assertNotNull(client)
        client.close()
    }

    @Test
    fun `create should return client without logging`() {
        val client = HttpClientFactory.create(enableLogging = false)
        assertNotNull(client)
        client.close()
    }
}
