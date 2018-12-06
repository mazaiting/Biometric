package com.mazaiting.biometric

import android.os.CancellationSignal
import android.support.annotation.NonNull

/**
 * 验证接口
 * Api28和Api23的实例都要继承它
 */
interface IBiometricPromptImpl {
  /**
   * 验证
   * @param cancel 取消控制对象
   * @param callback 生物验证回调
   */
  fun authenticate(@NonNull cancel: CancellationSignal,
                   @NonNull callback: BiometricPromptManager.OnBiometricIdentifyCallback)
}