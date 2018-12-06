package com.mazaiting.biometric

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.support.annotation.NonNull
import com.mazaiting.sp.SpUtil

/**
 * 生物识别
 * @param activity 当前Activity
 */
class BiometricPromptManager(activity: Activity) {
  /**
   * 当前Activity
   */
  private val mActivity: Activity = activity
  /**
   * 生物识别对象
   */
  private lateinit var mImpl: IBiometricPromptImpl
  
  companion object {
    /**
     * 静态方法获取当前对象
     */
    fun from(activity: Activity): BiometricPromptManager = BiometricPromptManager(activity)
  }
  
  init {
    // 设置当前界面
    if (isAboveApi28()) {
      mImpl = BiometricPromptApi28(activity)
    } else if (isAboveApi23()){
      mImpl = BiometricPromptApi23(activity)
    }
  }
  
  /**
   * 判断系统版本是否高于Android9.0
   */
  private fun isAboveApi28(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
  
  /**
   * 判断系统版本是否高于Android6.0
   */
  private fun isAboveApi23(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
  
  /**
   * 验证指纹
   * @param callback 生物验证回调
   */
  fun authenticate(@NonNull callback: OnBiometricIdentifyCallback) {
    mImpl.authenticate(CancellationSignal(), callback)
  }
  
  /**
   * 验证指纹
   * @param cancel 取消控制对象
   * @param callback 生物验证回调
   */
  fun authenticate(@NonNull cancel: CancellationSignal,
                   @NonNull callback: OnBiometricIdentifyCallback) {
    mImpl.authenticate(cancel, callback)
  }
  
  /**
   * 判断是否已录入指纹
   * @return true: 已录入; false: 没有录入指纹
   */
  fun hasEnrolledFingerprints(): Boolean {
    return when {
      isAboveApi28() -> {
        // 获取指纹识别管理者
        val manager: FingerprintManager = this.mActivity.getSystemService(FingerprintManager::class.java)
        // 判断是否已录入指纹
        manager.hasEnrolledFingerprints()
      }
      isAboveApi23() -> {
        // 获取指纹识别管理者
        val manager: FingerprintManager = this.mActivity.getSystemService(FingerprintManager::class.java)
        // 判断是否已录入指纹
        manager.hasEnrolledFingerprints()
      }
      else -> false
    }
  }
  
  /**
   * 判断是否有指纹功能
   * @return true: 有指纹功能;false: 没有指纹功能.
   */
  fun isHardwareDetected(): Boolean {
    return when{
      isAboveApi28() -> {
        // 获取指纹管理者
        val fm: FingerprintManager = this.mActivity.getSystemService(FingerprintManager::class.java)
        // 判断是否有指纹功能
        fm.isHardwareDetected
      }
      isAboveApi23() -> {
        // 获取指纹管理者
        val fm: FingerprintManager = this.mActivity.getSystemService(FingerprintManager::class.java)
        // 判断是否有指纹功能
        fm.isHardwareDetected
      }
      else -> false
    }
  }
  
  /**
   * 判断是否键盘安全
   * @return true: 键盘安全; false: 键盘不安全
   */
  fun isKeyguardSecure(): Boolean{
    // 获取键盘管理对象
    val keyguardManager: KeyguardManager = this.mActivity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    // 判断键盘是否安全
    return keyguardManager.isKeyguardSecure
  }
  
  /**
   * 判断生物识别是否可用
   * @return true: 可用; false: 不可用
   */
  fun isBiometricPromptEnable(): Boolean = isAboveApi23() && isHardwareDetected() && hasEnrolledFingerprints() && isKeyguardSecure()
  
  /**
   * 获取是否开启指纹验证
   * @return true: 开启; false: 未开启
   */
  fun isBiometricSettingEnable(): Boolean = SpUtil.instance.getBoolean(this.mActivity, Constant.KEY_BIOMETRIC_SWITCH_ENABLE)
  
  /**
   * 设置是否使用生物功能
   * @param enable true: 可用; false: 不可用
   */
  fun setBiometricSettingEnable(enable: Boolean) {
    SpUtil.instance.put(this.mActivity, Constant.KEY_BIOMETRIC_SWITCH_ENABLE, enable)
  }
  
  /**
   * 生物验证回调
   */
  interface OnBiometricIdentifyCallback {
    /**
     * 使用密码登陆
     */
    fun onUsePassword()
  
    /**
     * 验证成功
     */
    fun onSucceed()
  
    /**
     * 失败
     */
    fun onFailed()
  
    /**
     * 失败
     * @param code 错误码
     * @param result 错误信息
     */
    fun onError(code: Int, result: String)
  
    /**
     * 取消
     */
    fun onCancel()
  }
}