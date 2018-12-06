package com.mazaiting.biometric

import android.app.Activity
import android.content.DialogInterface
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.support.annotation.Nullable
import android.support.annotation.RequiresApi
import android.util.Base64
import java.security.spec.ECGenParameterSpec
import java.security.*
//import com.mazaiting.log.L

/**
 * 指纹识别
 * Android9.0系统以上可使用
 */
@RequiresApi(Build.VERSION_CODES.P)
class BiometricPromptApi28(activity: Activity) : IBiometricPromptImpl {
  
  companion object {
    /** 钥匙名 */
    private const val KEY_NAME = "BiometricPromptApi28"
  }
  /**
   * 当前Activity
   */
  private val mActivity: Activity = activity
  /**
   * 生物识别对象
   */
  private var mBiometricPrompt: BiometricPrompt? = null
  /**
   * 控制取消对象
   */
  private var mCancellationSignal: CancellationSignal? = null
  
  /**
   * 指纹识别回调
   */
  private var mManagerIdentifyCallback: BiometricPromptManager.OnBiometricIdentifyCallback? = null
  /**
   * 签名对象
   */
  private var mSignature: Signature? = null
  /**
   * 签名信息
   */
  private var mToBeSignedMessage = ""
  
  init {
    mBiometricPrompt = BiometricPrompt
            .Builder(activity)
            .setTitle(activity.resources.getString(R.string.biometric_dialog_title))
            .setDescription(activity.resources.getString(R.string.biometric_dialog_subtitle))
            .setSubtitle("")
            .setNegativeButton(activity.resources.getString(R.string.biometric_dialog_use_password),
                    activity.mainExecutor, DialogInterface.OnClickListener { _, _ ->
              mManagerIdentifyCallback?.onUsePassword()
              mCancellationSignal?.cancel()
            })
            .build()
    
    try {
      // 生成钥匙对
      val keyPair: KeyPair = generateKeyPair(KEY_NAME, true)
      // 设置签名信息
      mToBeSignedMessage = StringBuilder()
              .append(Base64.encodeToString(keyPair.public.encoded, Base64.URL_SAFE))
              .append(":")
              .append(KEY_NAME)
              .append(":")
              .append("12345")
              .toString()
      // 初始化签名
      mSignature = initSignature(KEY_NAME)
    } catch (e: Exception) {
      throw RuntimeException(e)
    }
  }
  
  @RequiresApi(Build.VERSION_CODES.P)
  override fun authenticate(cancel: CancellationSignal, callback: BiometricPromptManager.OnBiometricIdentifyCallback) {
    this.mManagerIdentifyCallback = callback
    this.mCancellationSignal = cancel
    if (null == this.mCancellationSignal) {
      this.mCancellationSignal = CancellationSignal()
    }
    // 初始化是否取消验证对象
    this.mCancellationSignal?.setOnCancelListener {
//      L.d("取消验证")
      // 取消验证
      mManagerIdentifyCallback?.onCancel()
    }
    mBiometricPrompt?.authenticate(BiometricPrompt.CryptoObject(mSignature!!), mCancellationSignal!!, this.mActivity.mainExecutor, BiometricPromptCallbackImpl())
  }
  
  /**
   * 生成钥匙对
   * @param keyName 钥匙名
   * @param invalidatedByBiometricEnrollment 生物识别是否失效
   * @return 钥匙对
   */
  private fun generateKeyPair(keyName: String, invalidatedByBiometricEnrollment: Boolean): KeyPair{
    // 初始化钥匙对生成器
    val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
    // 设置签名及钥匙名称
    val builder: KeyGenParameterSpec.Builder = KeyGenParameterSpec.Builder(keyName, KeyProperties.PURPOSE_SIGN)
            // 设置密钥
            .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            // 设置算法
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA512)
            // 设置用户请求验证
            .setUserAuthenticationRequired(true)
            // 设置生物识别是否失效
            .setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment)
    // 初始化
    keyPairGenerator.initialize(builder.build())
    // 生成钥匙对
    return keyPairGenerator.generateKeyPair()
  }
  
  /**
   * 获取钥匙对
   * @param keyName 钥匙名
   * @return 钥匙对
   */
  @Nullable  @Throws(Exception::class)
  private fun getKeyPair(keyName: String): KeyPair? {
    // 初始化钥匙商店
    val keyStore = KeyStore.getInstance("AndroidKeyStore")
    // 加载
    keyStore.load(null)
    // 判断是否包含此钥匙名称
    if (keyStore.containsAlias(keyName)) {
      // 获取公钥
      val publicKey = keyStore.getCertificate(keyName).publicKey
      // 获取私钥
      val privateKey = keyStore.getKey(keyName, null) as PrivateKey
      // 返回钥匙对
      return KeyPair(publicKey, privateKey)
    }
    return null
  }
  
  /**
   * 初始化签名
   * @param keyName 钥匙名
   * @return 签名对象
   */
  @Throws(Exception::class)
  private fun initSignature(keyName: String): Signature? {
    // 获取钥匙对
    val keyPair = getKeyPair(keyName)
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
   * 生物验证回调
   */
  @RequiresApi(Build.VERSION_CODES.P)
  private inner class BiometricPromptCallbackImpl : BiometricPrompt.AuthenticationCallback() {
    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
      super.onAuthenticationError(errorCode, errString)
      mCancellationSignal?.cancel()
    }
  
    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
      super.onAuthenticationSucceeded(result)
      mManagerIdentifyCallback?.onSucceed()
      mCancellationSignal?.cancel()
    }
  }
}





















