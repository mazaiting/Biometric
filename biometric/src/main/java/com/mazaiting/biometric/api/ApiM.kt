package com.mazaiting.biometric.api

import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.mazaiting.biometric.constant.BiometricState
import com.mazaiting.biometric.dialog.BiometricPromptDialog
import com.mazaiting.biometric.util.CryptoObjectHelper
import com.mazaiting.biometric.interfaces.IBiometricPromptImpl
import com.mazaiting.biometric.interfaces.OnBiometricIdentifyCallback
import com.mazaiting.biometric.interfaces.OnBiometricPromptDialogActionCallback
import com.mazaiting.common.debug

/**
 * 指纹识别
 * Android6.0系统以上可使用
 * @param activity 活动
 */
@RequiresApi(Build.VERSION_CODES.M)
class ApiM(private val activity: AppCompatActivity) : IBiometricPromptImpl {
  /**
   * 生物识别对话框
   */
  private var dialog: BiometricPromptDialog? = null
  /**
   * 指纹管理者
   */
  private var manager: FingerprintManager? = null
  /**
   * 控制取消对象
   */
  private var cancelSignal: CancellationSignal? = null
  /**
   * 指纹识别回调
   */
  private var onBiometricIdentifyCallback: OnBiometricIdentifyCallback? = null
  
  /**
   * 指纹验证回调
   */
  private val callback: FingerprintManager.AuthenticationCallback = FingerprintManagerCallbackImpl()
  
  init {
    // 获取指纹管理者
    this.manager = getFingerprintManager(activity)
  }
  
  override fun authenticate(cancel: CancellationSignal, callback: OnBiometricIdentifyCallback?) {
    // 指纹识别回调
    onBiometricIdentifyCallback = callback
    // 自定义对话框
    dialog = BiometricPromptDialog.instance
    // 设置对话框按钮回调
    dialog?.setOnBiometricPromptDialogActionCallback(object : OnBiometricPromptDialogActionCallback {
      override fun onDialogDismiss() {
        // 对话框消失，使用密码，点击取消，识别成功之后
        cancelSignal?.let {
          if (!it.isCanceled) {
            it.cancel()
          }
        }
      }
      
      override fun onUsePassword() {
        onBiometricIdentifyCallback?.onUsePassword()
      }
      
      override fun onCancel() {
        onBiometricIdentifyCallback?.onCancel()
      }
      
    })
    // 显示
    dialog?.show(this.activity.supportFragmentManager, "BiometricPromptApi23")
    // 取消信号量
    cancelSignal = cancel
    // 设置取消监听
    cancelSignal?.setOnCancelListener { dialog?.dismiss() }
    try {
      // 加密工具
      val crypto = CryptoObjectHelper().buildCryptoObjectM()
      // 验证
      manager?.authenticate(crypto, cancelSignal, 0, this.callback, null)
    } catch (e: Exception) {
      e.message?.let { onBiometricIdentifyCallback?.onError(0, it) }
    }
    
  }
  
  /**
   * 获取指纹管理者
   * @param context 上下文
   */
  private fun getFingerprintManager(context: Context): FingerprintManager? {
    if (null == manager) {
      manager = context.getSystemService(FingerprintManager::class.java)
    }
    return manager
  }
  
  /**
   * 指纹验证回调
   * 内部类
   */
  private inner class FingerprintManagerCallbackImpl : FingerprintManager.AuthenticationCallback() {
    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
      super.onAuthenticationError(errMsgId, errString)
      debug("onAuthenticationError() called with: errMsgId = [$errMsgId], errString = [$errString]")
      dialog?.setState(BiometricState.STATE_ERROR)
      onBiometricIdentifyCallback?.onError(errMsgId, errString.toString())
    }
    
    override fun onAuthenticationFailed() {
      super.onAuthenticationFailed()
      debug("onAuthenticationFailed() called")
      dialog?.setState(BiometricState.STATE_FAILED)
      onBiometricIdentifyCallback?.onFailed()
    }
    
    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
      super.onAuthenticationHelp(helpMsgId, helpString)
      debug("onAuthenticationHelp() called with: helpMsgId = [$helpMsgId], helpString = [$helpString]")
      dialog?.setState(BiometricState.STATE_FAILED)
      onBiometricIdentifyCallback?.onFailed()
    }
    
    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {
      super.onAuthenticationSucceeded(result)
      debug("onAuthenticationSucceeded: $result")
      dialog?.setState(BiometricState.STATE_SUCCEED)
      onBiometricIdentifyCallback?.onSucceed()
    }
  }
}