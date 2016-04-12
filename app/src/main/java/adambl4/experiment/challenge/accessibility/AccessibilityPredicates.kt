package adambl4.experiment.challenge.accessibility

import adambl4.experiment.challenge.GameConfig
import adambl4.experiment.challenge.R
import adambl4.experiment.challenge.utils.SettingsStrings
import adambl4.experiment.challenge.utils.extensions.TRAVERSAL_FORWARD
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Created by Adambl4 on 03.04.2016.
 */

fun <V> and(vararg predicate: (V) -> Boolean): (V) -> Boolean = { v: V -> predicate.all { it(v) } }

fun <V> or(vararg predicate: (V) -> Boolean): (V) -> Boolean = { v: V -> predicate.any { it(v) } }

fun eventPredicate(callback: AccessibilityEvent.() -> Boolean): (AccessibilityEvent) -> Boolean = { callback(it) }
fun nodePredicate(callback: AccessibilityNodeInfo.() -> Boolean): (AccessibilityNodeInfo) -> Boolean = { callback(it) }

fun eventWithText(text: String) = eventPredicate() { source?.findAccessibilityNodeInfosByText(text)?.isNotEmpty() ?: false }
fun eventWithPackage(packagee: String) = eventPredicate() { source?.packageName == packageName ?: false }

//shit
fun eventWithDescription(description: String) = eventPredicate() {
    if (source != null) {
        if (source.contentDescription == description) {
            return@eventPredicate true
        } else {
            var success = false;
            TRAVERSAL_FORWARD(source) {
                if (it.contentDescription == description) {
                    success = true
                    return@TRAVERSAL_FORWARD true
                } else {
                    return@TRAVERSAL_FORWARD false
                }
            }
            return@eventPredicate success
        }
    }
    return@eventPredicate false
}

fun eventWithClassName(className: String) = eventPredicate { this.className == className }
fun eventWithType(type: Int) = eventPredicate { this.eventType == type }

fun eventActivityOpening(name: String? = null) = eventPredicate() {
    eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && (name == null || className == name)
}

fun eventPackageOpening(name: String? = null) = eventPredicate() {
    eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && (name == null || packageName == name)
}

fun nodeWithText(text: String) = nodePredicate() { findAccessibilityNodeInfosByText(text).isNotEmpty() }
fun nodeWithClassName(classname: String) = nodePredicate() { className == classname }
fun nodeWithId(id: String) = nodePredicate() { viewIdResourceName == id }

fun anyNewWindowExcept(exceptPredicate: () -> (AccessibilityEvent) -> Boolean): (AccessibilityEvent) -> Boolean = {
    eventActivityOpening()(it) && !exceptPredicate()(it)
}

//EVENT PREDICATES
val SUB_SETTINGS_OPENED: (AccessibilityEvent) -> Boolean = eventActivityOpening("com.android.settings.SubSettings")

val AIRPLANE_SCREEN_OPENED: (AccessibilityEvent) -> Boolean =
        or(
                eventActivityOpening("com.android.settings.Settings\$WirelessSettingsActivity"),
                and(
                        SUB_SETTINGS_OPENED,
                        //eventWithText(SettingsStrings.SETTINGS_MORE),
                        eventWithText(SettingsStrings.AIRPLANE_MODE)
                )
        )

val ADMIN_ADD_SCREEN_OPENED: (AccessibilityEvent) -> Boolean = eventActivityOpening("com.android.settings.DeviceAdminAdd")

val ADMIN_LIST_SCREEN_OPENED: (AccessibilityEvent) -> Boolean =
        and(
                SUB_SETTINGS_OPENED,
                eventWithText(SettingsStrings.DEVICE_ADMINISTRATORS)
        )

val HOME_SCREEN_OPENED: (AccessibilityEvent) -> Boolean =
        or(
                eventActivityOpening("com.android.settings.Settings\$HomeSettingsActivity"),
                and(
                        SUB_SETTINGS_OPENED,
                        eventWithText(GameConfig.context.getString(R.string.launcher_label))
                )
        )

val SOUND_SCREEN_OPENED: (AccessibilityEvent) -> Boolean =
        or(
                eventActivityOpening("com.android.settings.Settings\$NotificationSettingsActivity"),
                and(
                        SUB_SETTINGS_OPENED,
                        eventWithText(SettingsStrings.SOUND_AND_NOTIFICATION)
                )
        )

val ACCESSIBILITY_SERVICE_SCREEN_OPENED: (AccessibilityEvent) -> Boolean =
        and(
                SUB_SETTINGS_OPENED,
                eventWithText(GameConfig.context.getString(R.string.accessibility_description))
        )

val WIFI_SCREEN_OEPENED: (AccessibilityEvent) -> Boolean =
        and(
                SUB_SETTINGS_OPENED,
                eventWithText(SettingsStrings.WIFI)
        )

val ACCESSIBILITY_SCREEN_OPENED: (AccessibilityEvent) -> Boolean =
        or(
                eventActivityOpening("com.android.settings.Settings\$AccessibilitySettingsActivity"),
                and(
                        SUB_SETTINGS_OPENED,
                        or(
                                //not sure which one is used, so check for both
                                eventWithText(SettingsStrings.ACCESSIBILITY),
                                eventWithText(SettingsStrings.ACCESSIBILITY_SETTINGS)
                        )
                )
        )


val SETTINGS_SCREEN_OPENED: (AccessibilityEvent) -> Boolean =
        and(
                eventActivityOpening("com.android.settings.Settings"),
                eventWithText(SettingsStrings.SETTINGS)
        )

val PASTEBIN_ANSWER_YES_EVENT: (AccessibilityEvent) -> Boolean = and(
        eventWithType(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED),
        eventWithClassName("android.widget.EditText"),
        or(
                //TODO this is shit
                eventWithDescription(GameConfig.context.getString(R.string.pastebin_text) + "Y"),
                eventWithDescription(GameConfig.context.getString(R.string.pastebin_text) + " Y"),
                eventWithDescription(GameConfig.context.getString(R.string.pastebin_text) + "y"),
                eventWithDescription(GameConfig.context.getString(R.string.pastebin_text) + " y")
        )
)

val CAMERA_DISABLED_EVENT: (AccessibilityEvent) -> Boolean =
        and(
                eventActivityOpening("android.app.AlertDialog"),
                eventWithPackage("com.android.camera2")
        )


val ALERT_DIALOG_EVENT: (AccessibilityEvent) -> Boolean = eventActivityOpening("android.app.AlertDialog")


//NODE PREDICATES
val AIRPLANE_TOGGLE_TARGET: (AccessibilityNodeInfo) -> Boolean = nodeWithClassName("android.widget.LinearLayout")

val LAUNCHER_ENABLE_CLICK_TARGET: (AccessibilityNodeInfo) -> Boolean = nodeWithId("com.android.settings:id/home_app_pref")

val SOUND_RANGE_ADJUST_TARGET: (AccessibilityNodeInfo) -> Boolean = nodeWithId("android:id/seekbar")

val SETTINGS_1ST_LEVEL_CLICK_TARGET: (AccessibilityNodeInfo) -> Boolean = and(nodeWithClassName("android.widget.FrameLayout"), nodePredicate { isClickable == true })

val SETTINGS_2ND_LEVEL_CLICK_TARGET: (AccessibilityNodeInfo) -> Boolean = and(nodeWithClassName("android.widget.LinearLayout"), nodePredicate { isClickable == true })








