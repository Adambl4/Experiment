package adambl4.experiment.challenge.utils.extensions

import adambl4.experiment.challenge.ChallengeApplication
import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import rx.Observable

/**
 * Created by Adambl4 on 05.04.2016.
 */

fun Context.getChallengeApplication(): ChallengeApplication = applicationContext as ChallengeApplication

val Context.rootInApplicationWindow: AccessibilityNodeInfo?
    get() {
        getChallengeApplication().controller.accessibilityService!!.windows.forEach {
            if(it.type == AccessibilityWindowInfo.TYPE_APPLICATION){
                //Logger.json(AccessibilityToJsonConverter().getJsonString(it.root))
                return it.root
            }
        }
        return null
    }

val Context.statusBarUI: AccessibilityNodeInfo?
    get() {

        getChallengeApplication().controller.accessibilityService!!.windows.forEach {
            Log.d("tag", "window $it")
            Log.d("tag", "window root ${it.root}")
        }

        getChallengeApplication().controller.accessibilityService!!.windows.forEach {
            if(it.type == AccessibilityWindowInfo.TYPE_SYSTEM){
                if(it.root != null && it.root.findAccessibilityNodeInfosByViewId("com.android.systemui:id/status_bar").isNotEmpty())
                return it.root
            }
        }
        return null
    }

val Context.navigationBarUI: AccessibilityNodeInfo?
    get() {
        getChallengeApplication().controller.accessibilityService!!.windows.forEach {
            if(it.type == AccessibilityWindowInfo.TYPE_SYSTEM){
                if(it.root != null){
                    if(it.root.findAccessibilityNodeInfosByViewId("com.android.systemui:id/nav_buttons").isNotEmpty()){
                        return it.root
                    }
                }
            }
        }
        return null
    }


val Context.accessibilityEventStream: Observable<AccessibilityEvent>
    get() = getChallengeApplication().controller.eventBus

val Context.keyEventStream: Observable<KeyEvent>
    get() = getChallengeApplication().controller.keyEventBus

val Context.lastAccessibilityPackage: CharSequence? get()  = getChallengeApplication().controller.lastPackage

fun Context.globalActionHome(){
    getChallengeApplication().controller.accessibilityService!!.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
}

fun Context.globalActionBack(){
    getChallengeApplication().controller.accessibilityService!!.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
}

fun Context.setActivityEnabled(isEnabled : Boolean, activityClass : Class<out Activity>){
    packageManager.setComponentEnabledSetting(ComponentName(this, activityClass),
            if(isEnabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED , PackageManager.DONT_KILL_APP);
}