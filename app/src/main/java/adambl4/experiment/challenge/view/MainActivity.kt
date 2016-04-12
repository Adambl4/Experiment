package adambl4.experiment.challenge.view

import adambl4.experiment.challenge.GameConfig
import adambl4.experiment.challenge.GameService
import adambl4.experiment.challenge.R
import adambl4.experiment.challenge.utils.extensions.INTENT_ACCESSIBILITY
import adambl4.experiment.challenge.utils.isAccessibilityEnabled
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.LinearLayout
import org.jetbrains.anko.*
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    var tts: TextToSpeech by Delegates.notNull()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Anko().setContentView(this)

        //GameService.startGame(this)
        GameConfig.activityContext = this;

/*
        tts = TextToSpeech(this, TextToSpeech.OnInitListener {
            Log.d("tag", "INIT")
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            val file = File(path, "play_me.mp3")
            ttsToFile("OLOLOLOLO AHAHAHHA LOLOLOL WTF MAN", file, tts);
        })*/

        //devicePolicyManager.setCameraDisabled(adminReceiverComponentName(), true)


        //FlashingLight(this).show()
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
                            onTextChanged { charSequence, i, i2, i3 -> GameConfig.username = charSequence }
                        }
                    }
                    if (isAccessibilityEnabled(context)) {
                        button("start") {
                            topPadding = dip(20)
                            onClick {
                                if (nameEditText.length() != 0) {
                                    GameService.startGame(context)
                                } else {
                                    nameEditText.error = ""
                                }
                            }
                        }
                    } else {
                        topPadding = dip(20)
                        button("and grant accessibility permission for \"${resources.getString(R.string.app_name)}\" app") {
                            onClick {
                                if (nameEditText.length() != 0) {
                                    context.startActivity(INTENT_ACCESSIBILITY)
                                    GameService.listenForAccessibilityAndStart(context)
                                } else {
                                    nameEditText.error = ""
                                }
                            }
                        }
                    }
                }.lparams {
                    gravity = Gravity.CENTER
                }
            }
        }
    }
}



