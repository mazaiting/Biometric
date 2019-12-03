# Biometric
Biometric verify

# 依赖
```
  implementation "com.mazaiting:biometric:1.0.1"
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
```

