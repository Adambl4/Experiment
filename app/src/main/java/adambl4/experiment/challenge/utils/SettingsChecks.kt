package adambl4.experiment.challenge.utils

import adambl4.experiment.challenge.GameConfig
import adambl4.experiment.challenge.utils.extensions.getChallengeApplication
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import org.jetbrains.anko.devicePolicyManager

/**
 * Created by Adambl4 on 04.04.2016.
 */

fun isAirplaneModeEnabled(context: Context = GameConfig.context) =
    Settings.Global.getInt(context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, 0) != 0;


fun isDeviceAdministrationEnabled(context: Context = GameConfig.context): Boolean {
    val policyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    return policyManager.isAdminActive(adminReceiverComponentName());
}

fun adminReceiverComponentName(context: Context = GameConfig.context) = ComponentName(context, AdminReceiver::class.java)

fun isAccessibilityEnabled(context: Context = GameConfig.context) = context.getChallengeApplication().controller.accessibilityService != null


class AdminReceiver : DeviceAdminReceiver()

fun clearDeviceAdministration(context: Context = GameConfig.context){
    context.devicePolicyManager.removeActiveAdmin(adminReceiverComponentName(context))
}

fun isAutoRotateEnabled(context: Context = GameConfig.context) : Boolean =
        Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION) == 1;

fun isTheAppIsLauncher(context: Context = GameConfig.context): Boolean {
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_HOME);
    val resolve = context.packageManager.resolveActivity(intent, 0);
    return resolve.activityInfo.packageName.equals(context.packageName)
}