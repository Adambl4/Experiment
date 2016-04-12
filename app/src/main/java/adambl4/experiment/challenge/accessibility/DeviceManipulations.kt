package adambl4.experiment.challenge.accessibility

import adambl4.experiment.challenge.GameConfig
import adambl4.experiment.challenge.R
import adambl4.experiment.challenge.accessibility.*
import adambl4.experiment.challenge.utils.*
import adambl4.experiment.challenge.utils.extensions.*
import adambl4.experiment.challenge.view.LauncherActivity
import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import nl.komponents.kovenant.toSuccessVoid
import nl.komponents.kovenant.unwrap
import org.jetbrains.anko.audioManager

/**
 * Created by Adambl4 on 05.04.2016.
 */

fun enableAirplaneModeIfNeeded(context: Context = GameConfig.context): Promise<Unit, Exception> =
        if (!isAirplaneModeEnabled(context))
            toggleAirPlaneMode(context);
        else
            Promise.ofSuccess(Unit)

fun enableDeviceAdminIfNeeded(context: Context = GameConfig.context): Promise<Unit, Exception> =
        if (!isDeviceAdministrationEnabled())
            toggleDeviceAdministration()
        else {
            Promise.ofSuccess(Unit)
        }

fun becomeLauncherIfNeeded(context: Context = GameConfig.context): Promise<Unit, Exception> {
    context.setActivityEnabled(true, LauncherActivity::class.java)
    if (!isTheAppIsLauncher()) {
        return setTheAppAsLauncher(context)
    } else {
        return Promise.ofSuccess(Unit)
    }
}

fun muteIfNeeded(context: Context = GameConfig.context): Promise<Unit, Exception> {
    if (context.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != 0) {
        return mute(context)
    } else {
        return Promise.ofSuccess(Unit)
    }
}


fun toggleAirPlaneMode(context: Context = GameConfig.context): Promise<Unit, Exception> {
    return task {
        context.startActivity(INTENT_AIRPLANE_MODE)
        waitEvent(AIRPLANE_SCREEN_OPENED)
    }.unwrapAndThen {
        doAccessibilityManipulation { toggleAirplaneManipulation(it) }
    }.toSuccessVoid()
}

fun toggleDeviceAdministration(context: Context = GameConfig.activityContext): Promise<Unit, Exception> {
    if (context !is Activity) throw IllegalArgumentException("Context should be an activity")

    return task {
        context.startActivity(INTENT_DEVICE_ADMINISTRATION())
        waitEvent(ADMIN_ADD_SCREEN_OPENED)
    }.unwrapAndThen {
        doAccessibilityManipulation { toggleDeviceAdminManipulation(it) }
    }.toSuccessVoid()
}

fun setTheAppAsLauncher(context: Context = GameConfig.context): Promise<Unit, Exception> {
    return task {
        context.startActivity(INTENT_HOME)
        waitEvent(HOME_SCREEN_OPENED)
    }.unwrapAndThen {
        doAccessibilityManipulation { chooseLauncherManipulation(it, context.resources.getString(R.string.launcher_label)) }
    }.toSuccessVoid()
}


fun disableAutoRotateIfNeeded(context: Context = GameConfig.context): Promise<Unit, Exception> =
        if (isAutoRotateEnabled(context)) {
            toggleAutoRotate()
        } else {
            Promise.ofSuccess(Unit)
        }

fun mute(context: Context = GameConfig.context): Promise<Unit, Exception> =
        task {
            context.startActivity(INTENT_SOUND)
            waitEvent(SOUND_SCREEN_OPENED)
        }.unwrapAndThen {
            doAccessibilityManipulation { setMediaVolumeSeekBarPositionManipulation(it, 0) }
        }.toSuccessVoid()

fun toggleAutoRotate(context: Context = GameConfig.context): Promise<Unit, Exception> =
        task {
            context.startActivity(INTENT_ACCESSIBILITY)
            waitEvent(ACCESSIBILITY_SCREEN_OPENED)
        }.unwrapAndThen {
            val listview = it.source.findOneByIdOrThrow("android:id/list")
            listview.performScrollForward(500)
            doAccessibilityManipulation { toggleAutoRotateManipulation(it) }
        }.unwrap()

val toggleAirplaneManipulation = { node: AccessibilityNodeInfo ->
    task {
        val anchor = node.findOneByTextOrThrow(SettingsStrings.AIRPLANE_MODE)
        anchor.actionOnPredicate(AccessibilityNodeInfo.ACTION_CLICK, AIRPLANE_TOGGLE_TARGET, TRAVERSAL_PARENTS())
    }
}

val toggleDeviceAdminManipulation = { node: AccessibilityNodeInfo ->
    task {
        node.findOneByIdOrThrow("com.android.settings:id/action_button").click()
    }
}

val chooseLauncherManipulation = { node: AccessibilityNodeInfo, titleTarget: String ->
    task {
        val anchor = node.findOneWithIdAndTextOrThrow("android:id/title", titleTarget)
        anchor.actionOnPredicate(AccessibilityNodeInfo.ACTION_CLICK, LAUNCHER_ENABLE_CLICK_TARGET, TRAVERSAL_BACKWARD)
    }
}

val setMediaVolumeSeekBarPositionManipulation = { node: AccessibilityNodeInfo, position: Int ->
    task {
        val anchor = node.findOneWithIdAndTextOrThrow("android:id/title", SettingsStrings.MEDIA_VOLUME)
        val bundle = Bundle()
        bundle.putInt(ARGUMENT_SCROLL_COUNT, 25)

        anchor.actionOnPredicate(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD, SOUND_RANGE_ADJUST_TARGET, TRAVERSAL_NEXT, bundle)
    }
}

val toggleAutoRotateManipulation = { node: AccessibilityNodeInfo ->
    task {
        val anchor = node.findOneByTextOrThrow(SettingsStrings.AUTO_ROTATE_SCREEN)
        anchor.actionOnPredicate(AccessibilityNodeInfo.ACTION_CLICK, SETTINGS_2ND_LEVEL_CLICK_TARGET, TRAVERSAL_PARENTS())
    }
}
