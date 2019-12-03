package com.mazaiting.biometric.api

import android.content.DialogInterface
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.mazaiting.biometric.R
import com.mazaiting.biometric.interfaces.IBiometricPromptImpl
import com.mazaiting.biometric.interfaces.OnBiometricIdentifyCallback
import com.mazaiting.biometric.util.CryptoObjectHelper
import com.mazaiting.common.debug

/**
 * 指纹识别
 * Android9.0系统以上可使用
 * @param activity 当前活动
 */
@RequiresApi(Build.VERSION_CODES.P)
class ApiP(private val activity: AppCompatActivity) : IBiometricPromptImpl {
  
  /**
   * 生物识别对象
   */
  private var biometricPrompt: BiometricPrompt? = null
  /**
   * 控制取消对象
   */
  private lateinit var cancellationSignal: CancellationSignal
  /**
   * 指纹识别回调
   */
  private var managerIdentifyCallback: OnBiometricIdentifyCallback? = null
  
  init {
    // 初始化生物识别对话框
    biometricPrompt = BiometricPrompt
        .Builder(activity)
        .setTitle(activity.resources.getString(R.string.biometric_dialog_title))
        .setDescription(activity.resources.getString(R.string.biometric_dialog_subtitle))
        .setSubtitle("")
        .setNegativeButton(activity.resources.getString(R.string.biometric_dialog_use_password),
            activity.mainExecutor, DialogInterface.OnClickListener { _, _ ->
          // 使用密钥
          managerIdentifyCallback?.onUsePassword()
          // 取消
          cancellationSignal.cancel()
        })
        .build()
  }
  
  @RequiresApi(Build.VERSION_CODES.P)
  override fun authenticate(cancel: CancellationSignal, callback: OnBiometricIdentifyCallback?) {
    managerIdentifyCallback = callback
    cancellationSignal = cancel
    // 初始化是否取消验证对象
    this.cancellationSignal.setOnCancelListener {
      //      L.d("取消验证")
      // 取消验证
      managerIdentifyCallback?.onCancel()
    }
    try {
      // Crypto 对象
      val crypto = CryptoObjectHelper().buildCryptoObjectP()
      // 验证
      crypto?.let {
        biometricPrompt?.authenticate(it, cancellationSignal, this.activity.mainExecutor, BiometricPromptCallbackImpl())
      }
      
    } catch (e: Exception) {
      e.message?.let { managerIdentifyCallback?.onError(0, it) }
    }
  }
  
  /**
   * 生物验证回调
   */
  @RequiresApi(Build.VERSION_CODES.P)
  private inner class BiometricPromptCallbackImpl : BiometricPrompt.AuthenticationCallback() {
    
    override fun onAuthenticationFailed() {
      super.onAuthenticationFailed()
      debug("ApiP: failed")
      // 失败
      managerIdentifyCallback?.onFailed()
    }
    
    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
      super.onAuthenticationError(errorCode, errString)
      debug("errorCode: $errorCode, msg: $errString")
      // 取消
      managerIdentifyCallback?.onError(errorCode, errString.toString())
      // 取消
      cancellationSignal.cancel()
    }
    
    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
      super.onAuthenticationSucceeded(result)
      debug("result: $result")
      // 成功
      managerIdentifyCallback?.onSucceed()
      // 取消
      cancellationSignal.cancel()
    }
  }
}





















