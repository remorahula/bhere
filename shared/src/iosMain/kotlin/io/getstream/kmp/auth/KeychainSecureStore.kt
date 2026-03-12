@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.getstream.kmp.auth

import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*
import platform.posix.memcpy

actual fun createSecureStore(): SecureStore = KeychainSecureStore()

private class KeychainSecureStore : SecureStore {
  private val service = "com.placeholder.app.service" // TODO replace

  override suspend fun putString(key: String, value: String) {
    delete(key)
    val data = value.encodeToByteArray().toNSData()

    val query = mapOf(
      kSecClass to kSecClassGenericPassword,
      kSecAttrService to service,
      kSecAttrAccount to key,
      kSecValueData to data,
      kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
    )

    val status = SecItemAdd(query as CFDictionaryRef?, null)
    if (status != errSecSuccess) error("Keychain put failed: $status")
  }

  override suspend fun getString(key: String): String? {
    val query = mapOf(
      kSecClass to kSecClassGenericPassword,
      kSecAttrService to service,
      kSecAttrAccount to key,
      kSecReturnData to kCFBooleanTrue,
      kSecMatchLimit to kSecMatchLimitOne
    )

    memScoped {
      val out = alloc<CFTypeRefVar>()
      val status = SecItemCopyMatching(query as CFDictionaryRef?, out.ptr)

      return when (status) {
        errSecItemNotFound -> null
        errSecSuccess -> (out.value as NSData).toByteArray().decodeToString()
        else -> error("Keychain get failed: $status")
      }
    }
  }

  override suspend fun delete(key: String) {
    val query = mapOf(
      kSecClass to kSecClassGenericPassword,
      kSecAttrService to service,
      kSecAttrAccount to key
    )

    val status = SecItemDelete(query as CFDictionaryRef?)
    if (status != errSecSuccess && status != errSecItemNotFound) {
      error("Keychain delete failed: $status")
    }
  }
}

@OptIn(BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData {
  if (isEmpty()) return NSData()
  return usePinned { pinned ->
    NSData.create(
      bytes = pinned.addressOf(0),
      length = size.toULong()
    )
  }
}

private fun NSData.toByteArray(): ByteArray {
  val len = length.toInt()
  if (len == 0) return ByteArray(0)

  val out = ByteArray(len)
  out.usePinned { pinned ->
    memcpy(pinned.addressOf(0), bytes, length)
  }
  return out
}