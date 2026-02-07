package com.longboilauncher.app.core.testing

import com.longboilauncher.app.core.model.AppEntry
import com.longboilauncher.app.core.model.FavoriteEntry
import com.longboilauncher.app.core.model.ProfileType

/**
 * Test data factories for common model objects.
 */
object TestAppEntries {
    fun createAppEntry(
        packageName: String = "com.test.app",
        className: String = "MainActivity",
        label: String = "Test App",
        userIdentifier: Int = 0,
        profile: ProfileType = ProfileType.PERSONAL,
    ): AppEntry =
        AppEntry(
            packageName = packageName,
            className = className,
            label = label,
            userIdentifier = userIdentifier,
            profile = profile,
        )

    fun createFavoriteEntry(
        id: String = "fav_1",
        appEntry: AppEntry = createAppEntry(),
        position: Int = 0,
    ): FavoriteEntry =
        FavoriteEntry(
            id = id,
            appEntry = appEntry,
            position = position,
        )
}
