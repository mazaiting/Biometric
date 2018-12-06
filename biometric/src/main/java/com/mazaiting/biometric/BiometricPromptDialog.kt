package com.mazaiting.biometric

import android.app.Activity
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.TextView


class BiometricPromptDialog : DialogFragment() {
  private var mStateTv: TextView? = null
  private var mUsePasswordBtn: TextView? = null
  private var mCancelBtn: TextView? = null
  private var mActivity: Activity? = null
  private var mDialogActionCallback: OnBiometricPromptDialogActionCallback? = null
  
  companion object {
    
    const val STATE_NORMAL = 1
    const val STATE_FAILED = 2
    const val STATE_ERROR = 3
    const val STATE_SUCCEED = 4
    
    fun newInstance(): BiometricPromptDialog {
      return BiometricPromptDialog()
    }
  }
  
  fun setOnBiometricPromptDialogActionCallback(callback: OnBiometricPromptDialogActionCallback) {
    mDialogActionCallback = callback
  }
  
  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    setupWindow(dialog.window)
  }
  
  @Nullable override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View {
    val view = inflater.inflate(R.layout.layout_biometric_prompt_dialog, container)
    
    val rootView = view.findViewById<View>(R.id.root_view)
    rootView.isClickable = false
    
    mStateTv = view.findViewById(R.id.state_tv)
    mUsePasswordBtn = view.findViewById(R.id.use_password_btn)
    mCancelBtn = view.findViewById(R.id.cancel_btn)
    
//    mUsePasswordBtn!!.visibility = View.GONE
    mUsePasswordBtn!!.setOnClickListener {
      if (mDialogActionCallback != null) {
        mDialogActionCallback!!.onUsePassword()
      }
  
      dismiss()
    }
    mCancelBtn!!.setOnClickListener {
      if (mDialogActionCallback != null) {
        mDialogActionCallback!!.onCancel()
      }
      dismiss()
    }
    return view
  }
  
  override fun onAttach(context: Context) {
    super.onAttach(context)
    mActivity = context as Activity
  }
  
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = super.onCreateDialog(savedInstanceState)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    if (dialog.window != null) {
      dialog.window?.setBackgroundDrawableResource(R.color.bg_biometric_prompt_dialog)
    }
    return dialog
  }
  
  override fun onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    
    if (mDialogActionCallback != null) {
      mDialogActionCallback!!.onDialogDismiss()
    }
  }
  
  private fun setupWindow(window: Window?) {
    if (window != null) {
      val lp = window.attributes
      lp.gravity = Gravity.CENTER
      lp.dimAmount = 0f
      lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
      window.attributes = lp
      window.setBackgroundDrawableResource(R.color.bg_biometric_prompt_dialog)
      window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
  }
  
  fun setState(state: Int) {
    when (state) {
      STATE_NORMAL -> {
        mStateTv!!.setTextColor(ContextCompat.getColor(mActivity!!, R.color.text_quaternary))
        mStateTv!!.text = mActivity!!.getString(R.string.biometric_dialog_state_normal)
        mCancelBtn!!.visibility = View.VISIBLE
      }
      STATE_FAILED -> {
        mStateTv!!.setTextColor(ContextCompat.getColor(mActivity!!, R.color.text_red))
        mStateTv!!.text = mActivity!!.getString(R.string.biometric_dialog_state_failed)
        mCancelBtn!!.visibility = View.VISIBLE
      }
      STATE_ERROR -> {
        mStateTv!!.setTextColor(ContextCompat.getColor(mActivity!!, R.color.text_red))
        mStateTv!!.text = mActivity!!.getString(R.string.biometric_dialog_state_error)
        mCancelBtn!!.visibility = View.GONE
      }
      STATE_SUCCEED -> {
        mStateTv!!.setTextColor(ContextCompat.getColor(mActivity!!, R.color.text_green))
        mStateTv!!.text = mActivity!!.getString(R.string.biometric_dialog_state_succeeded)
        mCancelBtn!!.visibility = View.VISIBLE
        
        mStateTv!!.postDelayed({ dismiss() }, 500)
      }
    }
  }
  
  
  interface OnBiometricPromptDialogActionCallback {
    fun onDialogDismiss()
    fun onUsePassword()
    fun onCancel()
  }
}