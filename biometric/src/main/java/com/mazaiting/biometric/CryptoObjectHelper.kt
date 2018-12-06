package com.mazaiting.biometric

import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.support.annotation.RequiresApi
import java.security.Key
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

/**
 * 加密工具类
 */
@RequiresApi(Build.VERSION_CODES.M)
class CryptoObjectHelper @Throws(Exception::class) constructor() {
  /**
   * 密钥
   */
  private val _keystore: KeyStore
  
  init {
    // 获取密钥
    _keystore = KeyStore.getInstance(KEYSTORE_NAME)
    _keystore.load(null)
  }
  
  companion object {
    // This can be key name you want. Should be unique for the app.
    internal const val KEY_NAME = "com.nestia.android.uikit.biometric.CryptoObjectHelper"
    // We always use this keystore on Android.
    internal const val KEYSTORE_NAME = "AndroidKeyStore"
    // Should be no need to change these values.
    internal const val KEY_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    internal const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
    internal const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
    internal const val TRANSFORMATION = "$KEY_ALGORITHM/$BLOCK_MODE/$ENCRYPTION_PADDING"
  }
  
  @Throws(Exception::class)
  fun buildCryptoObject(): FingerprintManager.CryptoObject = FingerprintManager.CryptoObject(createCipher(true))
  
  @Throws(Exception::class)
  internal fun createCipher(retry: Boolean): Cipher {
    val key = getKey()
    val cipher = Cipher.getInstance(TRANSFORMATION)
    try {
      cipher.init(Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE, key)
    } catch (e: KeyPermanentlyInvalidatedException) {
      _keystore.deleteEntry(KEY_NAME)
      if (retry) {
        createCipher(false)
      } else {
        throw Exception("Could not create the cipher for fingerprint authentication.", e)
      }
    }
    
    return cipher
  }
  
  @Throws(Exception::class)
  internal fun getKey(): Key {
    if (!_keystore.isKeyEntry(KEY_NAME)) {
      createKey()
    }
    return _keystore.getKey(KEY_NAME, null)
  }
  
  @Throws(Exception::class)
  internal fun createKey() {
    val keyGen = KeyGenerator.getInstance(KEY_ALGORITHM, KEYSTORE_NAME)
    val keyGenSpec = KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(BLOCK_MODE)
            .setEncryptionPaddings(ENCRYPTION_PADDING)
            .setUserAuthenticationRequired(true)
            .build()
    keyGen.init(keyGenSpec)
    keyGen.generateKey()
  }

  
}