package com.mazaiting.biometric.interfaces

import android.os.CancellationSignal
import androidx.annotation.NonNull
import org.jetbrains.annotations.NotNull

/**
 * 验证接口
 * ApiP和ApiM的实例都要实现它
 */
interface IBiometricPromptImpl {
  /**
   * 验证
   * @param cancel 取消控制对象
   * @param callback 生物验证回调
   */
  fun authenticate(@NotNull cancel: CancellationSignal, @NonNull callback: OnBiometricIdentifyCallback?)
}