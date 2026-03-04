
package com.longboilauncher.app.core.security

/**
 * Interface for requesting unlock of the Private Space.
 */
interface AuthGate {
    /**
     * Requests an unlock from the user.
     * @return true if authentication was successful, false otherwise.
     */
    suspend fun requestUnlock(): Boolean
}
