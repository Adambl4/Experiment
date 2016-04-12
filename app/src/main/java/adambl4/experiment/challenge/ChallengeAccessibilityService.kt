package adambl4.experiment.challenge

import adambl4.experiment.challenge.accessibility.AccessibilityController
import adambl4.experiment.challenge.utils.extensions.getChallengeApplication
import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

/**
 * Created by Adambl4 on 01.04.2016.
 */

class ChallengeAccessibilityService : AccessibilityService() {

    lateinit var controller: AccessibilityController

    override fun onCreate() {
        super.onCreate()
        controller = getChallengeApplication().controller

    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        controller.accessibilityService = this;
        controller.setState(AccessibilityController.STATE.CONNECTED)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        controller.onEvent(event)

        //Log.d("tag", "event = ${event.eventType}")
        if(event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            //Log.d("tag", "event = " + event.eventType);
        }
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if(event == null) return false
        return controller.onKeyEvent(event)
    }

    override fun onInterrupt() {
        controller.accessibilityService = null;
        controller.setState(AccessibilityController.STATE.DISCONNECTED)
    }

}