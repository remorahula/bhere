package io.getstream.kmp.auth

interface SecureStore {
  suspend fun putString(key: String, value: String)
  suspend fun getString(key: String): String?
  suspend fun delete(key: String)
}

expect fun createSecureStore(): SecureStore
