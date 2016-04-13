package adambl4.experiment.challenge.accessibility

import adambl4.experiment.challenge.BuildConfig
import adambl4.experiment.challenge.utils.extensions.getChallengeApplication
import adambl4.experiment.challenge.utils.ti
import android.accessibilityservice.AccessibilityService
import android.content.Context
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
    val blockedKeys = mutableSetOf<Int>()
    var isAllBlocked = false


    enum class STATE {CONNECTED, DISCONNECTED }

    private val accessibilityEventSubject: PublishSubject<AccessibilityEvent>
    private val keyEventSubject: PublishSubject<KeyEvent>
    private val accessibilityStateSubject: PublishSubject<STATE>

    private val eventToJson = AccessibilityToJsonConverter()
    var accessibilityService: AccessibilityService? = null

    init {
        accessibilityEventSubject = PublishSubject.create<AccessibilityEvent>()
        accessibilityStateSubject = PublishSubject.create<STATE>()
        keyEventSubject = PublishSubject.create<KeyEvent>()

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
        accessibilityEventSubject.onNext(event)
    }

    fun setState(state: STATE) {
        accessibilityStateSubject.onNext(state)
    }


    val eventBus: Observable<AccessibilityEvent>
        get() = accessibilityEventSubject.asObservable()

    val keyEventBus: Observable<KeyEvent>
        get() = keyEventSubject.asObservable()


    val stateBus: Observable<STATE>
        get() = accessibilityStateSubject.asObservable()

    fun getRootInActiveWindow(): AccessibilityNodeInfo? {
        val root = accessibilityService!!.rootInActiveWindow
        //Logger.json(eventToJson.getJsonString(root))
        return root
    }

    fun log(root: AccessibilityNodeInfo?) {
        //Logger.json(eventToJson.getJsonString(root))
    }

    fun onKeyEvent(event: KeyEvent): Boolean {
        keyEventSubject.onNext(event)
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

fun Context.blockHardwareButtons() {
    val cont = getChallengeApplication().controller;
    mainHardwareButtons.forEach {
        cont.blockedKeys.add(it)
    }
    ti { "block hardware buttons" }
}

fun Context.unblockHardwareButtons() {
    val cont = getChallengeApplication().controller;
    mainHardwareButtons.forEach {
        cont.blockedKeys.remove(it)
    }
    ti { "unblock hardware buttons" }
}

fun Context.setVolumeKeysBlocked(isBlocked: Boolean) {
    val cont = getChallengeApplication().controller;
    if (isBlocked) {
        cont.blockedKeys.add(KEYCODE_VOLUME_UP)
        cont.blockedKeys.add(KEYCODE_VOLUME_DOWN)
    } else {
        cont.blockedKeys.remove(KEYCODE_VOLUME_UP)
        cont.blockedKeys.remove(KEYCODE_VOLUME_DOWN)
    }
    ti { "set volume up button disabled $isBlocked" }
}