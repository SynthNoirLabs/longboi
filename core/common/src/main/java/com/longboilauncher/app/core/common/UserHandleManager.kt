package com.longboilauncher.app.core.common

import android.content.Context
import android.os.UserHandle
import android.os.UserManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserHandleManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager

        /**
         * Gets a stable serial number for a [UserHandle].
         * This is the recommended way to persist user identification.
         */
        fun getSerialNumberForUser(user: UserHandle): Long = userManager.getSerialNumberForUser(user)

        /**
         * Resolves a serial number back to a [UserHandle].
         * Returns null if the user no longer exists.
         */
        fun getUserForSerialNumber(serialNumber: Long): UserHandle? = userManager.getUserForSerialNumber(serialNumber)

        /**
         * Returns the [UserHandle] for the current process.
         */
        fun myUserHandle(): UserHandle = android.os.Process.myUserHandle()

        /**
         * Returns the serial number for the current process user.
         */
        fun myUserSerialNumber(): Long = getSerialNumberForUser(myUserHandle())
    }
