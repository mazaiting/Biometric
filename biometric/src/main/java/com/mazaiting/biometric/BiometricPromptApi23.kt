package com.mazaiting.biometric

import android.app.Activity
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.support.annotation.RequiresApi

/**
 * 指纹识别
 * Android6.0系统以上可使用
 */
@RequiresApi(Build.VERSION_CODES.M)
class BiometricPromptApi23(activity: Activity) : IBiometricPromptImpl {
  /**
   * 当前Activity
   */
  private val mActivity: Activity = activity
  /**
   * 生物识别对话框
   */
  private var mDialog: BiometricPromptDialog? = null
  /**
   * 指纹管理者
   */
  private var mFingerprintManager: FingerprintManager? = null
  
  /**
   * 控制取消对象
   */
  private var mCancellationSignal: CancellationSignal? = null
  
  /**
   * 指纹识别回调
   */
  private var mManagerIdentifyCallback: BiometricPromptManager.OnBiometricIdentifyCallback? = null
  
  /**
   * 指纹验证回调
   */
  private val mFmAuthCallback: FingerprintManager.AuthenticationCallback = FingerprintManagerCallbackImpl()
  
  init {
    this.mFingerprintManager = getFingerprintManager(activity)
  }
  
  override fun authenticate(cancel: CancellationSignal, callback: BiometricPromptManager.OnBiometricIdentifyCallback) {
    // 指纹识别回调
    this.mManagerIdentifyCallback = callback
    
    // 自定义对话框
    mDialog = BiometricPromptDialog.newInstance()
    mDialog?.setOnBiometricPromptDialogActionCallback(object : BiometricPromptDialog.OnBiometricPromptDialogActionCallback {
      override fun onDialogDismiss() {
        // 对话框消失，使用密码，点击取消，识别成功之后
        if (!mCancellationSignal?.isCanceled!!) {
          mCancellationSignal?.cancel()
        }
      }
      
      override fun onUsePassword() {
        // 使用密码
        mManagerIdentifyCallback?.onUsePassword()
      }
      
      override fun onCancel() {
        mManagerIdentifyCallback?.onCancel()
      }
      
    })
    mDialog?.show(this.mActivity.fragmentManager, "BiometricPromptApi23")
    this.mCancellationSignal = cancel
    if (null != this.mCancellationSignal) {
      this.mCancellationSignal = CancellationSignal()
    }
    mCancellationSignal?.setOnCancelListener {
      mDialog?.dismiss()
    }
    try {
      val cryptoObjectHelper = CryptoObjectHelper()
      mFingerprintManager?.authenticate(cryptoObjectHelper.buildCryptoObject(), mCancellationSignal, 0, mFmAuthCallback, null)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
  
  /**
   * 判断是否有指纹功能
   * @return true: 有指纹功能; false: 没有指纹功能
   */
  fun isHardwareDetected(): Boolean {
    return mFingerprintManager?.isHardwareDetected ?: false
  }
  
  /**
   * 判断是否有指纹录入
   * @return true: 有指纹录入; false: 没有指纹录入
   */
  fun hasEnrolledFingerprints(): Boolean {
    return mFingerprintManager?.hasEnrolledFingerprints() ?: false
  }
  
  /**
   * 获取指纹管理者
   */
  private fun getFingerprintManager(context: Context): FingerprintManager? {
    if (null == mFingerprintManager) {
      mFingerprintManager = context.getSystemService(FingerprintManager::class.java)
    }
    return mFingerprintManager
  }
  
  /**
   * 指纹验证回调
   * 内部类
   */
  inner class FingerprintManagerCallbackImpl : FingerprintManager.AuthenticationCallback() {
    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
      super.onAuthenticationError(errMsgId, errString)
//      Log.d("TAG","onAuthenticationError() called with: errMsgId = [$errMsgId], errString = [$errString]")
      mDialog?.setState(BiometricPromptDialog.STATE_ERROR)
      mManagerIdentifyCallback?.onError(errMsgId, errString.toString())
    }
    
    override fun onAuthenticationFailed() {
      super.onAuthenticationFailed()
//      Log.d("TAG","onAuthenticationFailed() called")
      mDialog?.setState(BiometricPromptDialog.STATE_FAILED)
      mManagerIdentifyCallback?.onFailed()
    }
    
    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
      super.onAuthenticationHelp(helpMsgId, helpString)
//      Log.d("TAG","onAuthenticationHelp() called with: helpMsgId = [$helpMsgId], helpString = [$helpString]")
      mDialog?.setState(BiometricPromptDialog.STATE_FAILED)
      mManagerIdentifyCallback?.onFailed()
    }
    
    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {
      super.onAuthenticationSucceeded(result)
//      Log.d("TAG","onAuthenticationSucceeded: $result")
      mDialog?.setState(BiometricPromptDialog.STATE_SUCCEED)
      mManagerIdentifyCallback?.onSucceed()
    }
  }
}