package adambl4.experiment.challenge.view

import adambl4.experiment.challenge.GameService
import adambl4.experiment.challenge.R
import adambl4.experiment.challenge.TheGame
import adambl4.experiment.challenge.TheGame.GameConfig
import adambl4.experiment.challenge.utils.extensions.INTENT_ACCESSIBILITY
import adambl4.experiment.challenge.utils.isAccessibilityEnabled
import adambl4.experiment.challenge.utils.isDeviceSupported
import adambl4.experiment.challenge.utils.td
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import org.jetbrains.anko.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Anko().setContentView(this)
        GameConfig.activityContext = this;

        val isTTSchecked = applicationContext.defaultSharedPreferences.getBoolean("is_tts_checked", false)

        if(!isTTSchecked) {
            val ttsCheck = Intent()
            ttsCheck.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
            startActivityForResult(ttsCheck, 145)
        } else {
            checkDevice()
        }
    }

    fun checkDevice(){
        if(!isDeviceSupported()){
            td { "Device is not supported" }
            startActivity<DeviceIsNotSupported>()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 145) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                applicationContext.defaultSharedPreferences.edit().putBoolean("is_tts_checked", true).apply()
                td { "TTS IS AVAILABLE" }
            } else {
                td { "TTS IS UNAVAILABLE" }
                startActivity<DeviceIsNotSupported>("ttsUnavailable" to true)
            }
        }
    }

    class Anko : AnkoComponent<MainActivity> {
        override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
            relativeLayout {
                linearLayout {
                    orientation = LinearLayout.VERTICAL
                    textView("Android experiment") {
                        gravity = Gravity.CENTER
                        textSize = 30f
                    }
                    textView("Project shows the power of AccessibilityService") {
                        gravity = Gravity.CENTER
                        textSize = 20f
                        topPadding = dip(20)
                    }
                    textView("To get started, please:") {
                        gravity = Gravity.CENTER
                        topPadding = dip(15)
                    }
                    val nameEditText = editText {
                        topPadding = dip(20)
                        gravity = Gravity.CENTER
                        hint = "Enter your name"
                        textChangedListener {
                            onTextChanged { charSequence, i, i2, i3 -> TheGame.GameStats.username = if(charSequence != null) charSequence.toString() else ""}
                        }
                    }
                    var buttonGrant : Button? = null
                    var buttonStart : Button? = null

                    buttonStart = button("start") {
                        visibility = View.GONE
                        topPadding = dip(20)
                        onClick {
                            if (nameEditText.length() != 0) {
                                if(!isAccessibilityEnabled(context)){
                                    buttonGrant?.performClick()
                                    return@onClick
                                }
                                GameService.startGame(context)
                            } else {
                                nameEditText.error = ""
                            }
                        }
                    }

                    buttonGrant = button("and grant accessibility permission for \"${resources.getString(R.string.app_name)}\" app") {

                        visibility = View.GONE
                        topPadding = dip(20)
                        onClick {
                            if (nameEditText.length() != 0) {
                                if(isAccessibilityEnabled(context)){
                                    buttonStart?.performClick()
                                    return@onClick
                                }
                                context.startActivity(INTENT_ACCESSIBILITY)
                                GameService.listenForAccessibilityAndStart(context)
                            } else {
                                nameEditText.error = ""
                            }
                        }
                    }

                    if (isAccessibilityEnabled(context)) {
                        buttonStart.visibility = View.VISIBLE
                        buttonGrant.visibility = View.GONE
                    } else {
                        buttonStart.visibility = View.GONE
                        buttonGrant.visibility = View.VISIBLE
                    }
                }.lparams {
                    gravity = Gravity.CENTER
                }
            }
        }
    }
}



