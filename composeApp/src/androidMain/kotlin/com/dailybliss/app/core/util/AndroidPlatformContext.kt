package com.dailybliss.app.core.util

import android.content.Context

/**
 * Android implementation of PlatformContext
 *
 * Membungkus android.content.Context agar
 * PlatformContext dapat digunakan di shared code.
 */
class AndroidPlatformContext(val androidContext: Context) : PlatformContext
