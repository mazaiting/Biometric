package com.mazaiting.biometric.util

import android.hardware.biometrics.BiometricPrompt
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import java.security.*
import java.security.spec.ECGenParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

/**
 * 加密工具类
 */
@RequiresApi(Build.VERSION_CODES.M)
class CryptoObjectHelper @Throws(Exception::class) constructor() {
  
  companion object {
    /**
     * KEY 名称
     */
    private const val KEY_NAME = "com.mazaiting.biometric.util.CryptoObjectHelper"
    /**
     * 签名
     */
    private const val KEYSTORE_NAME = "AndroidKeyStore"
    /**
     * AES 加密
     */
    private const val KEY_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    /**
     * 填充模式
     */
    private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
    /**
     * 加密填充
     */
    private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
    /**
     * 获取 Cipher 传入的字符串
     */
    private const val TRANSFORMATION = "$KEY_ALGORITHM/$BLOCK_MODE/$ENCRYPTION_PADDING"
  }
  
  /**
   * 密钥
   */
  private val keyStore: KeyStore
  
  init {
    // 获取密钥
    keyStore = KeyStore.getInstance(KEYSTORE_NAME)
    keyStore.load(null)
  }
  
  /**
   * 创建 Crypto
   * Android 6.0
   */
  @RequiresApi(Build.VERSION_CODES.M)
  @Throws(Exception::class)
  fun buildCryptoObjectM(): FingerprintManager.CryptoObject = FingerprintManager.CryptoObject(createCipher(true))
  
  /**
   * 创建 Crypto
   * Android 9.0
   */
  @Throws(Exception::class)
  @RequiresApi(Build.VERSION_CODES.P)
  fun buildCryptoObjectP(): BiometricPrompt.CryptoObject? = initSignature()?.let { BiometricPrompt.CryptoObject(it) }
  
  /**
   * 创建 cipher
   * @param retry 是否重试
   */
  @Throws(Exception::class)
  private fun createCipher(retry: Boolean): Cipher {
    // 获取 key
    val key = getKey()
    // 获取 cipher 实例
    val cipher = Cipher.getInstance(TRANSFORMATION)
    try {
      // 初始化
      cipher.init(Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE, key)
    } catch (e: KeyPermanentlyInvalidatedException) {
      // 删除
      keyStore.deleteEntry(KEY_NAME)
      // 重试
      if (retry) {
        createCipher(false)
      } else {
        throw Exception("Could not create the cipher for fingerprint authentication.", e)
      }
    }
    
    return cipher
  }
  
  /**
   * 获取 KEY
   */
  @Throws(Exception::class)
  private fun getKey(): Key {
    // 判断是否包含该可以
    if (!keyStore.isKeyEntry(KEY_NAME)) {
      // 创建
      createKey()
    }
    // 获取
    return keyStore.getKey(KEY_NAME, null)
  }
  
  /**
   * 创建 KEY
   */
  @Throws(Exception::class)
  private fun createKey() {
    // 获取 KEY 生成器实例
    val keyGen = KeyGenerator.getInstance(KEY_ALGORITHM, KEYSTORE_NAME)
    // 构建参数
    val keyGenSpec = KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
        .setBlockModes(BLOCK_MODE)
        .setEncryptionPaddings(ENCRYPTION_PADDING)
        .setUserAuthenticationRequired(true)
        .build()
    // 初始化
    keyGen.init(keyGenSpec)
    // 生成
    keyGen.generateKey()
  }
  
  /**
   * 初始化签名
   * @return 签名对象
   */
  @RequiresApi(Build.VERSION_CODES.P)
  @Throws(Exception::class)
  private fun initSignature(): Signature? {
    // 获取钥匙对
    val keyPair = getKeyPair()
    // 判断钥匙对是否为空
    if (keyPair != null) {
      // 获取签名
      val signature = Signature.getInstance("SHA256withECDSA")
      // 初始化
      signature.initSign(keyPair.private)
      return signature
    }
    return null
  }
  
  
  /**
   * 获取钥匙对
   * @return 钥匙对
   */
  @Nullable
  @RequiresApi(Build.VERSION_CODES.P)
  @Throws(Exception::class)
  private fun getKeyPair(): KeyPair? {
    // 判断是否包含此钥匙名称
    if (!keyStore.containsAlias(KEY_NAME)) {
      generateKeyPair()
    }
    // 获取公钥
    val publicKey = keyStore.getCertificate(KEY_NAME).publicKey
    // 获取私钥
    val privateKey = keyStore.getKey(KEY_NAME, null) as PrivateKey
    // 返回钥匙对
    return KeyPair(publicKey, privateKey)
  }
  
  /**
   * 生成钥匙对
   * @return 钥匙对
   */
  @RequiresApi(Build.VERSION_CODES.P)
  private fun generateKeyPair(): KeyPair {
    // 初始化钥匙对生成器
    val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, KEYSTORE_NAME)
    // 设置签名及钥匙名称
    val builder: KeyGenParameterSpec.Builder = KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_SIGN)
        // 设置密钥
        .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
        // 设置算法
        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA512)
        // 设置用户请求验证
        .setUserAuthenticationRequired(true)
        // 设置生物识别是否失效
        .setInvalidatedByBiometricEnrollment(true)
    // 初始化
    keyPairGenerator.initialize(builder.build())
    // 生成钥匙对
    return keyPairGenerator.generateKeyPair()
  }
}