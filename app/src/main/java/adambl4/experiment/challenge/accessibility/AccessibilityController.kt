package adambl4.experiment.challenge.accessibility

import adambl4.experiment.challenge.BuildConfig
import adambl4.experiment.challenge.utils.extensions.getChallengeApplication
import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.orhanobut.logger.Logger
import rx.Observable
import rx.subjects.PublishSubject

/**
 * Created by Adambl4 on 01.04.2016.
 */


class AccessibilityController {
    var lastPackage: CharSequence? = null

    enum class STATE {CONNECTED, DISCONNECTED }

    private val accessibilityEventBus: PublishSubject<AccessibilityEvent>
    private val accessibilityStateBus: PublishSubject<STATE>

    private val eventToJson = AccessibilityToJsonConverter()
    var accessibilityService: AccessibilityService? = null

    init {
        accessibilityEventBus = PublishSubject.create<AccessibilityEvent>()
        accessibilityStateBus = PublishSubject.create<STATE>()

        if (BuildConfig.DEBUG) setLogs()

        eventBus.subscribe({ lastPackage = if (it.packageName != null) it.packageName else lastPackage })
    }

    private fun setLogs() {
        eventBus.subscribe() {
            if (it.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                Logger.json(eventToJson.getJsonString(it))
            }
        }
    }

    fun onEvent(event: AccessibilityEvent) {
        accessibilityEventBus.onNext(event)
    }

    fun setState(state: STATE) {
        accessibilityStateBus.onNext(state)
    }


    val eventBus: Observable<AccessibilityEvent>
        get() = accessibilityEventBus.asObservable()

    val stateBus: Observable<STATE>
        get() = accessibilityStateBus.asObservable()

    fun getRootInActiveWindow(): AccessibilityNodeInfo? {
        val root = accessibilityService!!.rootInActiveWindow
        //Logger.json(eventToJson.getJsonString(root))
        return root
    }

    fun log(root: AccessibilityNodeInfo?) {
        //Logger.json(eventToJson.getJsonString(root))
    }

    val blockedKeys = mutableSetOf<Int>()
    var isAllBlocked = false
    fun onKeyEvent(event: KeyEvent): Boolean {
        Log.d("tag", "keyEvent $event")
        return isAllBlocked || blockedKeys.contains(event.keyCode)
    }
}

fun Context.blockKey(keycode: Int) {
    getChallengeApplication().controller.blockedKeys.add(keycode)
}


fun Context.unblockKey(keycode: Int) {
    getChallengeApplication().controller.blockedKeys.remove(keycode)
}

fun Context.blockAll() {
    getChallengeApplication().controller.isAllBlocked = true
}

fun Context.unblockAll() {
    getChallengeApplication().controller.isAllBlocked = false
}

val mainHardwareButtons = arrayOf(
        KEYCODE_HOME, KEYCODE_BACK, KEYCODE_APP_SWITCH, KEYCODE_VOLUME_UP,
        KEYCODE_VOLUME_DOWN, KEYCODE_MUTE, KEYCODE_POWER)

fun Context.blockMainHardwareButtons() {
    val cont = getChallengeApplication().controller;
    mainHardwareButtons.forEach {
        cont.blockedKeys.add(it)
    }
}

fun Context.unblockMainHardwareButtons() {
    val cont = getChallengeApplication().controller;
    mainHardwareButtons.forEach {
        cont.blockedKeys.remove(it)
    }
}