package com.mazaiting.biometric

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import com.mazaiting.biometric.constant.Constant
import com.mazaiting.biometric.api.ApiM
import com.mazaiting.biometric.api.ApiP
import com.mazaiting.biometric.interfaces.IBiometricPromptImpl
import com.mazaiting.biometric.interfaces.OnBiometricIdentifyCallback
import com.mazaiting.common.logDebug
import com.mazaiting.sp.SpUtil
import org.jetbrains.annotations.NotNull

/**
 * 生物识别
 * @param activity 当前Activity
 */
class BiometricPromptManager(private val activity: AppCompatActivity) {
  
  companion object {
    /**
     * 静态方法获取当前对象
     */
    fun from(activity: AppCompatActivity): BiometricPromptManager = BiometricPromptManager(activity)
  }
  
  /**
   * 生物识别对象
   */
  private lateinit var impl: IBiometricPromptImpl
  
  init {
    // 判断 API
    if (isAboveApi28()) {
      impl = ApiP(activity)
    } else if (isAboveApi23()) {
      impl = ApiM(activity)
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
  fun authenticate(@NonNull callback: OnBiometricIdentifyCallback?) {
    authenticate(CancellationSignal(), callback)
  }
  
  /**
   * 验证指纹
   * @param cancel 取消控制对象
   * @param callback 生物验证回调
   */
  fun authenticate(@NotNull cancel: CancellationSignal,
                   @NonNull callback: OnBiometricIdentifyCallback?) {
    impl.authenticate(cancel, callback)
  }
  
  /**
   * 判断是否键盘安全
   * @return true: 键盘安全; false: 键盘不安全
   */
  fun isKeyguardSecure(): Boolean =
      (activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isKeyguardSecure
  
  /**
   * 判断生物识别是否可用
   * @return true: 可用; false: 不可用
   */
  fun isBiometricPromptEnable(): Boolean =
      with(FingerprintManagerCompat.from(activity)) {
        return isAboveApi23()
            && this.isHardwareDetected
            && this.hasEnrolledFingerprints()
            && isKeyguardSecure()
      }
  
  /**
   * 获取是否开启指纹验证
   * @return true: 开启; false: 未开启
   */
  fun isBiometricSettingEnable(): Boolean = SpUtil.instance.getBoolean(activity, Constant.KEY_BIOMETRIC_SWITCH_ENABLE)
  
  /**
   * 设置是否使用生物功能
   * @param enable true: 可用; false: 不可用
   */
  fun setBiometricSettingEnable(enable: Boolean) {
    SpUtil.instance.put(activity, Constant.KEY_BIOMETRIC_SWITCH_ENABLE, enable)
  }
  
}