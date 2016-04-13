package adambl4.experiment.challenge.utils

import android.content.Context
import kotlin.properties.Delegates
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Created by Adambl4 on 02.04.2016.
 */


object SettingsStrings {
    var context : Context by Delegates.notNull()

    val SETTINGS: String by SettingsStringProperty("settings_label")
    val SETTINGS_MORE: String by SettingsStringProperty("radio_controls_title")
    val AIRPLANE_MODE: String by SettingsStringProperty("airplane_mode")
    val AUTO_ROTATE_SCREEN: String by SettingsStringProperty("accelerometer_title")
    val MEDIA_VOLUME: String by SettingsStringProperty("media_volume_option_title")
    val SOUND_AND_NOTIFICATION: String by SettingsStringProperty("notification_settings")
    val ACCESSIBILITY: String by SettingsStringProperty("accessibility_settings")
    val ACCESSIBILITY_SETTINGS: String by SettingsStringProperty("accessibility_settings_title")
    val DEVICE_ADMINISTRATORS: String by SettingsStringProperty("manage_device_admin")
    val HOME: String by SettingsStringProperty("home_settings")
    val WIFI: String by SettingsStringProperty("wifi_settings")
    val APP_INFO: String by SettingsStringProperty("application_info_label")
}

private class SettingsStringProperty(private val id : String) : ReadOnlyProperty<SettingsStrings, String> {
    override fun getValue(thisRef: SettingsStrings, property: KProperty<*>): String {
        val resources = thisRef.context.packageManager.getResourcesForApplication("com.android.settings")
        val resId = resources.getIdentifier(id, "string", "com.android.settings");
        return resources.getString(resId)
    }
}