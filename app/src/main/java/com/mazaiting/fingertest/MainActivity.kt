package com.mazaiting.fingertest

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mazaiting.biometric.BiometricPromptManager
import com.mazaiting.biometric.interfaces.OnBiometricIdentifyCallback
import com.mazaiting.common.toast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    
    // 初始化生物识别管理者
    val mManager = BiometricPromptManager.from(this)
    val stringBuilder = StringBuilder()
    stringBuilder.append("SDK 版本：" + Build.VERSION.SDK_INT)
    stringBuilder.append("\n")
    stringBuilder.append("指纹是否可用 : " + mManager.isBiometricPromptEnable())
    stringBuilder.append("\n")
    stringBuilder.append("键盘是否安全 : " + mManager.isKeyguardSecure())
    stringBuilder.append("\n")
    // 文本显示
    textView.text = stringBuilder.toString()
    
    button.setOnClickListener {
      // 判断是否设置可用
      mManager.isBiometricSettingEnable()
      // 设置开启指纹验证
      mManager.setBiometricSettingEnable(true)
      // 判断是否可用
      if (mManager.isBiometricPromptEnable()) {
        // 验证
        mManager.authenticate(object : OnBiometricIdentifyCallback {
          override fun onUsePassword() {
            toast("使用密码")
          }
          
          override fun onSucceed() {
            toast("成功")
          }
          
          override fun onFailed() {
            toast("失败")
          }
          
          override fun onError(code: Int, result: String) {
            toast("$code:$result")
          }
          
          override fun onCancel() {
            toast("取消")
          }
        })
      }
    }
  }
}
