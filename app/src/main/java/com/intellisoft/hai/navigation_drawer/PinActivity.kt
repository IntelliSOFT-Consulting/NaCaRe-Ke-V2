package com.intellisoft.hai.navigation_drawer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.intellisoft.hai.R
import com.intellisoft.hai.helper_class.FormatterClass
import com.intellisoft.hai.helper_class.PinLockStatus
import com.intellisoft.hai.pinlockview.IndicatorDots
import com.intellisoft.hai.pinlockview.PinLockListener
import com.intellisoft.hai.pinlockview.PinLockView

class PinActivity : AppCompatActivity() {

  private val TAG = "PinLockView"
  private val formatterClass = FormatterClass()
  private lateinit var mPinLockView: PinLockView
  private lateinit var mIndicatorDots: IndicatorDots
  private lateinit var profile_name: TextView
  private lateinit var tv_forgot: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_pin)
    mPinLockView = findViewById(R.id.pin_lock_view)
    mIndicatorDots = findViewById(R.id.indicator_dots)
    profile_name = findViewById(R.id.profile_name)
    tv_forgot = findViewById(R.id.tv_forgot)

    mPinLockView.attachIndicatorDots(mIndicatorDots)
    mPinLockView.setPinLockListener(mPinLockListener)
    mPinLockView.pinLength = 4
    mPinLockView.textColor = ContextCompat.getColor(this@PinActivity, R.color.white)
    mIndicatorDots.indicatorType = IndicatorDots.IndicatorType.FILL_WITH_ANIMATION
    tv_forgot.setOnClickListener { resetAndProceed(true) }
  }

  private fun resetAndProceed(b: Boolean) {
    if (b) {
      formatterClass.deleteSharedPref(PinLockStatus.INITIAL.name, this@PinActivity)
      formatterClass.deleteSharedPref(PinLockStatus.CONFIRMED.name, this@PinActivity)
      formatterClass.deleteSharedPref(PinLockStatus.LOCK.name, this@PinActivity)
    }
    val intent = Intent(this@PinActivity, MainActivity::class.java)
    startActivity(intent)
    this@PinActivity.finish()
  }

  private val mPinLockListener: PinLockListener =
      object : PinLockListener {
        override fun onComplete(pin: String) {
          Log.d(TAG, "Pin complete: $pin")
          checkPin(pin)
        }

        override fun onEmpty() {
          Log.d(TAG, "Pin empty")
        }

        override fun onPinChange(pinLength: Int, intermediatePin: String) {
          Log.d(TAG, "Pin changed, new length $pinLength with intermediate pin $intermediatePin")
        }
      }

  private fun checkPin(pin: String) {
    val storedPin = formatterClass.getSharedPref(PinLockStatus.LOCK.name, this@PinActivity)
    if (pin == storedPin) {
      resetAndProceed(false)
    } else {
      mPinLockView.resetPinLockView()
      Toast.makeText(this@PinActivity, "Invalid PIN, please try again", Toast.LENGTH_SHORT).show()
    }
  }
}
