package com.longboilauncher.app.core.common

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.common.truth.Truth.assertThat
import com.longboilauncher.app.core.settings.PreferencesRepository
import com.longboilauncher.app.core.common.LauncherRoleManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowRoleManager

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class LauncherRoleManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val context = mockk<Context>(relaxed = true)
    private val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)
    private val roleManager = mockk<RoleManager>(relaxed = true)
    private lateinit var launcherRoleManager: LauncherRoleManager

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { context.getSystemService(Context.ROLE_SERVICE) } returns roleManager
        every { context.packageName } returns "com.longboilauncher.app"

        launcherRoleManager = LauncherRoleManager(context, preferencesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `isDefaultLauncher is true when role is held`() = runTest {
        every { roleManager.isRoleHeld(RoleManager.ROLE_HOME) } returns true

        launcherRoleManager.checkDefaultLauncher()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(launcherRoleManager.isDefaultLauncher.value).isTrue()
        assertThat(launcherRoleManager.shouldRequestRole.value).isFalse()
    }

    @Test
    fun `isDefaultLauncher is false when role is not held`() = runTest {
        every { roleManager.isRoleHeld(RoleManager.ROLE_HOME) } returns false

        launcherRoleManager.checkDefaultLauncher()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(launcherRoleManager.isDefaultLauncher.value).isFalse()
        assertThat(launcherRoleManager.shouldRequestRole.value).isTrue()
    }

    @Test
    fun `requestDefaultLauncher returns correct intent on API 29+`() {
        val intent = Intent("com.android.role.ID_LIKE_IT")
        every { roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME) } returns intent

        val result = launcherRoleManager.requestDefaultLauncher()
        assertThat(result).isEqualTo(intent)
    }

    @Test
    fun `dismissRoleRequest updates state`() {
        launcherRoleManager.dismissRoleRequest()
        assertThat(launcherRoleManager.shouldRequestRole.value).isFalse()
    }
}
