package com.mazaiting.fingertest

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.mazaiting.biometric.BiometricPromptManager


class MainActivity : AppCompatActivity() {
  
  private var mTextView: TextView? = null
  private var mButton: Button? = null
  private var mManager: BiometricPromptManager? = null
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
  
    mTextView = findViewById(R.id.text_view)
    mButton = findViewById(R.id.button)
  
    // 初始化生物识别管理者
    mManager = BiometricPromptManager.from(this)
    val stringBuilder = StringBuilder()
//    stringBuilder.append("SDK version is " + Build.VERSION.SDK_INT)
//    stringBuilder.append("\n")
//    stringBuilder.append("isHardwareDetected : " + mManager!!.isHardwareDetected())
//    stringBuilder.append("\n")
//    stringBuilder.append("hasEnrolledFingerprints : " + mManager!!.hasEnrolledFingerprints())
//    stringBuilder.append("\n")
//    stringBuilder.append("isKeyguardSecure : " + mManager!!.isKeyguardSecure())
//    stringBuilder.append("\n")
    stringBuilder.append("SDK 版本：" + Build.VERSION.SDK_INT)
    stringBuilder.append("\n")
    stringBuilder.append("硬件是否支持 : " + mManager!!.isHardwareDetected())
    stringBuilder.append("\n")
    stringBuilder.append("是否已录入指纹 : " + mManager!!.hasEnrolledFingerprints())
    stringBuilder.append("\n")
    stringBuilder.append("键盘是否安全 : " + mManager!!.isKeyguardSecure())
    stringBuilder.append("\n")
  
    mTextView!!.text = stringBuilder.toString()
  
    mButton?.setOnClickListener{
      // 判断是否设置可用
      mManager?.isBiometricSettingEnable()
      // 设置开启指纹验证
      mManager?.setBiometricSettingEnable(true)
      // 判断是否可用
      if (mManager!!.isBiometricPromptEnable()) {
        // 验证
        mManager?.authenticate(object : BiometricPromptManager.OnBiometricIdentifyCallback {
          override fun onUsePassword() {
            Toast.makeText(this@MainActivity, "onUsePassword", Toast.LENGTH_SHORT).show()
          }
  
          override fun onSucceed() {
            Toast.makeText(this@MainActivity, "onSucceeded", Toast.LENGTH_SHORT).show()
          }
  
          override fun onFailed() {
            Toast.makeText(this@MainActivity, "onFailed", Toast.LENGTH_SHORT).show()
          }
      
          override fun onError(code: Int, result: String) {
            Toast.makeText(this@MainActivity, "onError", Toast.LENGTH_SHORT).show()
          }
      
          override fun onCancel() {
            Toast.makeText(this@MainActivity, "onCancel", Toast.LENGTH_SHORT).show()
          }
        })
      }
    }
  }
}
