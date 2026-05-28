package com.dailybliss.app.core.util

class FakeLocationTracker : LocationTracker {
    var mockLocation: Location? = Location(-6.0, 106.0)

    override suspend fun getCurrentLocation(): Location? = mockLocation
}
