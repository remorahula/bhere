package io.getstream.kmp.auth

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.getstream.kmp.platform.AndroidPlatform

actual fun createSecureStore(): SecureStore {
  val ctx = AndroidPlatform.appContext

  val masterKey = MasterKey.Builder(ctx)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

  val prefs = EncryptedSharedPreferences.create(
    ctx,
    "secure_store",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
  )

  return object : SecureStore {
    override suspend fun putString(key: String, value: String) { prefs.edit().putString(key, value).apply() }
    override suspend fun getString(key: String): String? = prefs.getString(key, null)
    override suspend fun delete(key: String) { prefs.edit().remove(key).apply() }
  }
}
