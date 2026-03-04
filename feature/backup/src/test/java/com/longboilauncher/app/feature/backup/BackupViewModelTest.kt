package com.longboilauncher.feature.backup

import android.net.Uri
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class BackupViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var backupManager: BackupManager
    private lateinit var viewModel: BackupViewModel

    private val testUri: Uri = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        backupManager = mockk(relaxed = true)
        viewModel = BackupViewModel(backupManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is not loading`() {
        val state = viewModel.uiState.value
        assertThat(state.isExporting).isFalse()
        assertThat(state.isImporting).isFalse()
    }

    @Test
    fun `export calls manager and completes`() =
        runTest(testDispatcher) {
            coEvery { backupManager.exportToUri(any()) } returns Result.success(Unit)

            viewModel.onEvent(BackupEvent.ExportBackup(testUri))
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { backupManager.exportToUri(testUri) }
            assertThat(viewModel.uiState.value.isExporting).isFalse()
        }

    @Test
    fun `export emits success effect`() =
        runTest(testDispatcher) {
            coEvery { backupManager.exportToUri(any()) } returns Result.success(Unit)

            viewModel.effects.test {
                viewModel.onEvent(BackupEvent.ExportBackup(testUri))
                testDispatcher.scheduler.advanceUntilIdle()

                val effect1 = awaitItem()
                assertThat(effect1).isInstanceOf(BackupEffect.ShowMessage::class.java)
                val messageEffect = effect1 as BackupEffect.ShowMessage
                assertThat(messageEffect.messageRes).isEqualTo(R.string.backup_export_success)
                assertThat(messageEffect.formatArgs).isEmpty()

                val effect2 = awaitItem()
                assertThat(effect2).isEqualTo(BackupEffect.ExportSuccess)
            }
        }

    @Test
    fun `export emits failure effect on error`() =
        runTest(testDispatcher) {
            coEvery { backupManager.exportToUri(any()) } returns
                Result.failure(Exception("Test error"))

            viewModel.effects.test {
                viewModel.onEvent(BackupEvent.ExportBackup(testUri))
                testDispatcher.scheduler.advanceUntilIdle()

                val effect = awaitItem()
                assertThat(effect).isInstanceOf(BackupEffect.ShowMessage::class.java)
                val messageEffect = effect as BackupEffect.ShowMessage
                assertThat(messageEffect.messageRes).isEqualTo(R.string.backup_export_failed)
                assertThat(messageEffect.formatArgs).containsExactly("Test error")
            }
        }

    @Test
    fun `import calls manager and completes`() =
        runTest(testDispatcher) {
            coEvery { backupManager.importFromUri(any()) } returns Result.success(Unit)

            viewModel.onEvent(BackupEvent.ImportBackup(testUri))
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { backupManager.importFromUri(testUri) }
            assertThat(viewModel.uiState.value.isImporting).isFalse()
        }

    @Test
    fun `import emits success effect`() =
        runTest(testDispatcher) {
            coEvery { backupManager.importFromUri(any()) } returns Result.success(Unit)

            viewModel.effects.test {
                viewModel.onEvent(BackupEvent.ImportBackup(testUri))
                testDispatcher.scheduler.advanceUntilIdle()

                val effect1 = awaitItem()
                assertThat(effect1).isInstanceOf(BackupEffect.ShowMessage::class.java)
                val messageEffect = effect1 as BackupEffect.ShowMessage
                assertThat(messageEffect.messageRes).isEqualTo(R.string.backup_import_success)
                assertThat(messageEffect.formatArgs).isEmpty()

                val effect2 = awaitItem()
                assertThat(effect2).isEqualTo(BackupEffect.ImportSuccess)
            }
        }

    @Test
    fun `import emits failure effect on error`() =
        runTest(testDispatcher) {
            coEvery { backupManager.importFromUri(any()) } returns
                Result.failure(Exception("Import error"))

            viewModel.effects.test {
                viewModel.onEvent(BackupEvent.ImportBackup(testUri))
                testDispatcher.scheduler.advanceUntilIdle()

                val effect = awaitItem()
                assertThat(effect).isInstanceOf(BackupEffect.ShowMessage::class.java)
                val messageEffect = effect as BackupEffect.ShowMessage
                assertThat(messageEffect.messageRes).isEqualTo(R.string.backup_import_failed)
                assertThat(messageEffect.formatArgs).containsExactly("Import error")
            }
        }
}
