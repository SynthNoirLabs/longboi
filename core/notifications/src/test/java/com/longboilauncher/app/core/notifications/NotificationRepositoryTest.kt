package com.longboilauncher.app.core.notifications

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationRepositoryTest {
    private lateinit var repository: NotificationRepository

    @Before
    fun setup() {
        repository = NotificationRepository()
    }

    private fun entry(
        key: String = "key",
        packageName: String = "com.test.app",
        title: String = "Title",
        text: String = "Text",
        timestamp: Long = 0L,
        progress: ProgressInfo? = null,
        category: NotificationCategory = NotificationCategory.DEFAULT,
    ): NotificationEntry =
        NotificationEntry(
            key = key,
            packageName = packageName,
            title = title,
            text = text,
            icon = null,
            timestamp = timestamp,
            progress = progress,
            category = category,
        )

    @Test
    fun `notifications flow is initially empty`() =
        runTest {
            assertThat(repository.notifications.value).isEmpty()
        }

    @Test
    fun `updateNotifications replaces full list`() =
        runTest {
            val first = listOf(entry(key = "a"))
            val second = listOf(entry(key = "b"), entry(key = "c"))

            repository.updateNotifications(first)
            assertThat(repository.notifications.value).hasSize(1)

            repository.updateNotifications(second)
            assertThat(repository.notifications.value.map { it.key })
                .containsExactly("b", "c")
                .inOrder()
        }

    @Test
    fun `progressNotifications filters out entries without progress`() =
        runTest {
            repository.updateNotifications(
                listOf(
                    entry(key = "plain"),
                    entry(key = "downloading", progress = ProgressInfo(current = 50, max = 100)),
                ),
            )

            val emitted = repository.progressNotifications.first()
            assertThat(emitted.map { it.key }).containsExactly("downloading")
        }

    @Test
    fun `progressNotifications is sorted by timestamp descending`() =
        runTest {
            repository.updateNotifications(
                listOf(
                    entry(key = "old", timestamp = 100L, progress = ProgressInfo(1, 10)),
                    entry(key = "new", timestamp = 300L, progress = ProgressInfo(1, 10)),
                    entry(key = "mid", timestamp = 200L, progress = ProgressInfo(1, 10)),
                ),
            )

            val emitted = repository.progressNotifications.first()
            assertThat(emitted.map { it.key })
                .containsExactly("new", "mid", "old")
                .inOrder()
        }

    @Test
    fun `progressByCategory groups progress entries by category`() =
        runTest {
            repository.updateNotifications(
                listOf(
                    entry(
                        key = "dl",
                        progress = ProgressInfo(1, 10),
                        category = NotificationCategory.PROGRESS,
                    ),
                    entry(
                        key = "song",
                        progress = ProgressInfo(1, 10),
                        category = NotificationCategory.MEDIA,
                    ),
                    entry(
                        key = "plain",
                        category = NotificationCategory.DEFAULT,
                    ),
                ),
            )

            val grouped = repository.progressByCategory.first()
            assertThat(grouped.keys)
                .containsExactly(NotificationCategory.PROGRESS, NotificationCategory.MEDIA)
            assertThat(grouped[NotificationCategory.PROGRESS]?.map { it.key })
                .containsExactly("dl")
        }

    @Test
    fun `getNotificationsForPackage returns only matching package`() {
        repository.updateNotifications(
            listOf(
                entry(key = "a", packageName = "com.foo"),
                entry(key = "b", packageName = "com.bar"),
                entry(key = "c", packageName = "com.foo"),
            ),
        )

        val foo = repository.getNotificationsForPackage("com.foo")
        assertThat(foo.map { it.key }).containsExactly("a", "c")
    }

    @Test
    fun `getProgressNotificationsForPackage filters by package and progress`() {
        repository.updateNotifications(
            listOf(
                entry(key = "a", packageName = "com.foo"),
                entry(key = "b", packageName = "com.foo", progress = ProgressInfo(1, 10)),
                entry(key = "c", packageName = "com.bar", progress = ProgressInfo(1, 10)),
            ),
        )

        val foo = repository.getProgressNotificationsForPackage("com.foo")
        assertThat(foo.map { it.key }).containsExactly("b")
    }

    @Test
    fun `hasActiveProgress is true for in-flight progress`() {
        repository.updateNotifications(
            listOf(entry(packageName = "com.foo", progress = ProgressInfo(50, 100))),
        )

        assertThat(repository.hasActiveProgress("com.foo")).isTrue()
    }

    @Test
    fun `hasActiveProgress is false when progress is complete`() {
        repository.updateNotifications(
            listOf(entry(packageName = "com.foo", progress = ProgressInfo(100, 100))),
        )

        assertThat(repository.hasActiveProgress("com.foo")).isFalse()
    }

    @Test
    fun `hasActiveProgress is true for indeterminate progress`() {
        repository.updateNotifications(
            listOf(
                entry(
                    packageName = "com.foo",
                    progress = ProgressInfo(0, 0, isIndeterminate = true),
                ),
            ),
        )

        assertThat(repository.hasActiveProgress("com.foo")).isTrue()
    }

    @Test
    fun `hasActiveProgress is false when package has no progress notification`() {
        repository.updateNotifications(listOf(entry(packageName = "com.foo")))

        assertThat(repository.hasActiveProgress("com.foo")).isFalse()
    }

    @Test
    fun `clearNotificationsForPackage removes only matching entries`() {
        repository.updateNotifications(
            listOf(
                entry(key = "a", packageName = "com.foo"),
                entry(key = "b", packageName = "com.bar"),
                entry(key = "c", packageName = "com.foo"),
            ),
        )

        repository.clearNotificationsForPackage("com.foo")

        assertThat(repository.notifications.value.map { it.key }).containsExactly("b")
    }

    @Test
    fun `notifications flow emits new value when updated`() =
        runTest {
            repository.notifications.test {
                assertThat(awaitItem()).isEmpty()

                repository.updateNotifications(listOf(entry(key = "x")))
                assertThat(awaitItem().map { it.key }).containsExactly("x")

                cancelAndIgnoreRemainingEvents()
            }
        }
}
