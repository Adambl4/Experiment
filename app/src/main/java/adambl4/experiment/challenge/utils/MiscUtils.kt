package adambl4.experiment.challenge.utils

import android.speech.tts.TextToSpeech
import org.jetbrains.anko.bundleOf
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Created by Adambl4 on 12.04.2016.
 */

fun ttsToFile(text : String, file : File, tts : TextToSpeech){
    val map = bundleOf(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID to text)
    tts.synthesizeToFile(text, map, file, text);
}



fun Long.toTimeString(): CharSequence? {
    return String.format("%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(this),
            TimeUnit.MILLISECONDS.toMinutes(this),
            TimeUnit.MILLISECONDS.toSeconds(this) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this))
    );
}
