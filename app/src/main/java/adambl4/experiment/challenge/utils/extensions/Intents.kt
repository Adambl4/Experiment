package adambl4.experiment.challenge.utils.extensions

import adambl4.experiment.challenge.GameConfig
import adambl4.experiment.challenge.utils.adminReceiverComponentName
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import org.jetbrains.anko.newTask

/**
 * Created by Adambl4 on 02.04.2016.
 */

val INTENT_AIRPLANE_MODE: Intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
        .newTask()
        .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)



val INTENT_ACCESSIBILITY: Intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        .newTask()
        .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)


val INTENT_HOME: Intent = Intent(Settings.ACTION_HOME_SETTINGS)
        .newTask()
        .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

val INTENT_SOUND: Intent = Intent(Settings.ACTION_SOUND_SETTINGS)
        .newTask()
        .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

val INTENT_LAUNCHER: Intent = Intent(Intent.ACTION_MAIN)
        .newTask()
        .addCategory(Intent.CATEGORY_HOME)

//don't use .newTask() here
fun INTENT_DEVICE_ADMINISTRATION(context: Context = GameConfig.context) : Intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiverComponentName(context))
        .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "nobody reads the manual")
