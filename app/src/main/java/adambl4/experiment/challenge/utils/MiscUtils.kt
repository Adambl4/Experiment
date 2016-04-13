package adambl4.experiment.challenge.utils

import adambl4.experiment.challenge.R
import adambl4.experiment.challenge.TheGame
import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import org.jetbrains.anko.bundleOf
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Created by Adambl4 on 12.04.2016.
 */

fun ttsToFile(text: String, file: File, tts: TextToSpeech) {
    val map = bundleOf(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID to text)
    tts.synthesizeToFile(text, map, file, text);
}

fun synthesizeTape(context: Context = TheGame.GameConfig.context) {
    var tts: TextToSpeech? = null
    tts = TextToSpeech(context, TextToSpeech.OnInitListener {
        val file = TheGame.GameStats.tapeFile.value
        if (file.exists()) {
            td { "play_me.wav exist. Delete." }
            file.delete();
        }
        td { "file path ${file.absolutePath}" }
        val message = String.format(context.getString(R.string.tape_message), TheGame.GameStats.username)
        ttsToFile(message, file, tts!!);
        TheGame.GameStats.isTTSsynthesized = true
    })
}


fun drawText(bitmap: Bitmap, text: String, context: Context = TheGame.GameConfig.context): Bitmap? {
    val resources = context.resources;
    val scale = resources.displayMetrics.density;

    var bitmapConfig = bitmap.config;
    if (bitmapConfig == null) {
        bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
    }
    val bitmapCopy = bitmap.copy(bitmapConfig, true);
    val canvas = Canvas(bitmapCopy);
    val paint = Paint(Paint.ANTI_ALIAS_FLAG);
    paint.color = context.resources.getColor(android.R.color.holo_red_dark)
    paint.textSize = (40 * scale);

    val bounds = Rect();
    paint.getTextBounds(text, 0, text.length, bounds);
    val x = (bitmapCopy.width - bounds.width()) / 2;
    val y = (bitmapCopy.height + bounds.height()) / 2;

    canvas.drawText(text, x.toFloat(), y.toFloat(), paint);
    return bitmapCopy
}

fun getBitmapFromFile(file: File, context: Context = TheGame.GameConfig.context): Bitmap? {
    try {
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.fromFile(file));
        val ei = ExifInterface(file.path);
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        fun rotateImage(bitmap: Bitmap, angle : Float): Bitmap {
            td { "rotate image $angle" }
            val matrix = Matrix()
            matrix.setRotate(angle)
            return Bitmap.createBitmap(bitmap, 0,0, bitmap.width, bitmap.height, matrix, true)

        }
        td { "EXIF $orientation" }
        return when(orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f);
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f);
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f);
            else -> bitmap
        }

    } catch(e: Exception) {
        return null
    }
}

fun Long.toTimeString(): CharSequence? {
    return String.format("%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(this),
            TimeUnit.MILLISECONDS.toMinutes(this),
            TimeUnit.MILLISECONDS.toSeconds(this) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this))
    );
}
