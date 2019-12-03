package com.mazaiting.biometric.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.mazaiting.biometric.R
import com.mazaiting.biometric.constant.BiometricState
import com.mazaiting.biometric.interfaces.OnBiometricPromptDialogActionCallback

/**
 * 指纹验证对话框
 */
class BiometricPromptDialog : DialogFragment() {
  
  companion object {
    
    /**
     * 生物失败对话框
     */
    val instance by lazy(LazyThreadSafetyMode.NONE) { BiometricPromptDialog() }
  }
  
  /**
   * 提示信息
   */
  private var tvState: TextView? = null
  /**
   * 取消按钮
   */
  private var tvCancel: TextView? = null
  /**
   * 对话框按钮回调
   */
  private var mDialogActionCallback: OnBiometricPromptDialogActionCallback? = null
  
  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    setupWindow(dialog?.window)
  }
  
  @Nullable
  override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View {
    // 加载布局
    val view = inflater.inflate(R.layout.layout_biometric_prompt_dialog, container)
    // 获取根视图
    val rootView = view.findViewById<View>(R.id.root_view)
    // 不可点击
    rootView.isClickable = false
    // 获取状态显示
    tvState = view.findViewById(R.id.state_tv)
    // 获取使用密码按钮
    val tvUsePassword = view.findViewById<TextView>(R.id.use_password_btn)
    // 获取取消按钮
    tvCancel = view.findViewById(R.id.cancel_btn)
    // 设置使用密码监听
    tvUsePassword.setOnClickListener {
      // 使用密码
      mDialogActionCallback?.onUsePassword()
      // 取消
      dismiss()
    }
    tvCancel?.setOnClickListener {
      // 取消
      mDialogActionCallback?.onCancel()
      // 取消
      dismiss()
    }
    return view
  }
  
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = super.onCreateDialog(savedInstanceState)
    // 设置请求无标题
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    // 设置背景
    dialog.window?.setBackgroundDrawableResource(R.color.bg_biometric_prompt_dialog)
    return dialog
  }
  
  override fun onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    // 关闭回调
    mDialogActionCallback?.onDialogDismiss()
  }
  
  /**
   * 设置窗口
   */
  private fun setupWindow(window: Window?) {
    window?.let {
      val lp = it.attributes
      lp.gravity = Gravity.CENTER
      lp.dimAmount = 0f
      lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
      it.attributes = lp
      it.setBackgroundDrawableResource(R.color.bg_biometric_prompt_dialog)
      it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
  }
  
  /**
   * 设置状态
   * @param state 状态
   */
  fun setState(state: BiometricState) {
    tvState?.let { it ->
      when (state) {
        BiometricState.STATE_NORMAL -> {
          setState(R.color.text_quaternary, R.string.biometric_dialog_state_normal, View.VISIBLE)
        }
        BiometricState.STATE_FAILED -> {
          setState(R.color.text_red, R.string.biometric_dialog_state_failed, View.VISIBLE)
        }
        BiometricState.STATE_ERROR -> {
          setState(R.color.text_red, R.string.biometric_dialog_state_error, View.GONE)
        }
        BiometricState.STATE_SUCCEED -> {
          setState(R.color.text_green, R.string.biometric_dialog_state_succeeded, View.VISIBLE)
          // 延时关闭
          it.postDelayed({ dismiss() }, 500)
        }
      }
    }
  }
  
  /**
   * 设置状态
   * @param colorId 颜色 ID
   * @param msgId 消息 ID
   * @param visibility 是否可见
   */
  private fun setState(colorId: Int, msgId: Int, visibility: Int) {
    context?.let { itCtx ->
      tvState?.let { itTv ->
        tvCancel?.let { itCancel ->
          itTv.setTextColor(ContextCompat.getColor(itCtx, colorId))
          itTv.text = itCtx.getString(msgId)
          itCancel.visibility = visibility
        }
      }
    }
  }
  
  /**
   * 设置生物识别对话框
   */
  fun setOnBiometricPromptDialogActionCallback(callback: OnBiometricPromptDialogActionCallback) {
    mDialogActionCallback = callback
  }
  
}