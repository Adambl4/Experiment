package adambl4.experiment.challenge.view

import adambl4.experiment.challenge.utils.getChromeApplicationInfo
import adambl4.experiment.challenge.utils.isDeviceApiSupported
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.LinearLayout
import org.jetbrains.anko.*

class DeviceIsNotSupported : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Anko(intent).setContentView(this)
    }

    class Anko(val intent: Intent) : AnkoComponent<DeviceIsNotSupported> {
        override fun createView(ui: AnkoContext<DeviceIsNotSupported>) = with(ui) {
            linearLayout{
                orientation = LinearLayout.VERTICAL
                textView("Device doesn't supported"){
                    textSize = 30f
                    gravity = Gravity.CENTER
                }

                if(!isDeviceApiSupported()){
                    textView("App support only Android 5"){
                        textSize = 25f
                        gravity = Gravity.CENTER
                    }
                } else {
                    if (intent.getBooleanExtra("ttsUnavailable", false)) {
                        button("install tts") {
                            onClick {
                                val installIntent = Intent()
                                installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA;
                                context.startActivity(installIntent); // clear the cache after installing
                            }
                        }
                    }

                    if (getChromeApplicationInfo() == null) {
                        button("Install chrome") {
                            onClick {
                                val appPackageName = "com.android.chrome"
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (e: Exception) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
