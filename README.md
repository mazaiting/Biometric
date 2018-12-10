# Biometric
Biometric verify

# 依赖
```
  // 必须
  implementation "com.mazaiting:sp:1.0.0"
  implementation "com.mazaiting:biometric:1.0.0"
```

# 使用方法
```
// 定义生物识别管理者
private var mManager: BiometricPromptManager? = null
// 初始化生物识别管理者
mManager = BiometricPromptManager.from(this)
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
```

