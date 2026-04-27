package com.longboilauncher.app.core.icons

import coil.request.Options
import com.google.common.truth.Truth.assertThat
import com.longboilauncher.app.core.model.AppEntry
import io.mockk.mockk
import org.junit.Test

class AppEntryKeyerTest {
    private val keyer = AppEntryKeyer()
    private val options: Options = mockk(relaxed = true)

    private fun appEntry(
        packageName: String = "com.test.app",
        className: String = "MainActivity",
        userSerialNumber: Long = 0L,
        lastUpdateTime: Long = 0L,
    ): AppEntry =
        AppEntry(
            packageName = packageName,
            className = className,
            label = "Test",
            userSerialNumber = userSerialNumber,
            lastUpdateTime = lastUpdateTime,
        )

    @Test
    fun `key embeds package, class, user, and lastUpdateTime`() {
        val key = keyer.key(appEntry(lastUpdateTime = 12345L), options)

        assertThat(key).isEqualTo("app:com.test.app/MainActivity:0:12345")
    }

    @Test
    fun `same app on different users produces distinct keys`() {
        val personal = keyer.key(appEntry(userSerialNumber = 0L), options)
        val work = keyer.key(appEntry(userSerialNumber = 10L), options)

        assertThat(personal).isNotEqualTo(work)
    }

    @Test
    fun `different lastUpdateTime busts the cache key`() {
        val before = keyer.key(appEntry(lastUpdateTime = 100L), options)
        val after = keyer.key(appEntry(lastUpdateTime = 200L), options)

        assertThat(before).isNotEqualTo(after)
    }

    @Test
    fun `different components on the same package produce distinct keys`() {
        val main = keyer.key(appEntry(className = "MainActivity"), options)
        val settings = keyer.key(appEntry(className = "SettingsActivity"), options)

        assertThat(main).isNotEqualTo(settings)
    }

    @Test
    fun `key is stable for the same input`() {
        val entry = appEntry(lastUpdateTime = 999L)

        assertThat(keyer.key(entry, options)).isEqualTo(keyer.key(entry, options))
    }
}
