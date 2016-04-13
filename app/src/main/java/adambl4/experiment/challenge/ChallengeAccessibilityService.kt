package adambl4.experiment.challenge

import adambl4.experiment.challenge.accessibility.AccessibilityController
import adambl4.experiment.challenge.utils.extensions.getChallengeApplication
import adambl4.experiment.challenge.utils.td
import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

/**
 * Created by Adambl4 on 01.04.2016.
 */

class ChallengeAccessibilityService : AccessibilityService() {

    lateinit var controller: AccessibilityController

    override fun onCreate() {
        td { "Accessibility service onCreate" }
        super.onCreate()
        controller = getChallengeApplication().controller

    }

    override fun onServiceConnected() {
        td { "Accessibility service onServiceConnected" }
        super.onServiceConnected()
        controller.accessibilityService = this;
        controller.setState(AccessibilityController.STATE.CONNECTED)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        controller.onEvent(event)
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if(event == null) return false
        //td { "KeyEvent = $event" }
        val b  = controller.onKeyEvent(event)
        return b
    }

    override fun onInterrupt() {
        td { "Accessibility service onInterrupt" }
        controller.accessibilityService = null;
        controller.setState(AccessibilityController.STATE.DISCONNECTED)
    }

    override fun onDestroy() {
        td { "Accessibility service onDestroy" }
        super.onDestroy()
    }
}