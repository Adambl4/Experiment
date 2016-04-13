package adambl4.experiment.challenge

import adambl4.experiment.challenge.accessibility.AccessibilityController
import adambl4.experiment.challenge.utils.extensions.failAndThrow
import adambl4.experiment.challenge.utils.extensions.getChallengeApplication
import adambl4.experiment.challenge.utils.isAccessibilityEnabled
import adambl4.experiment.challenge.utils.ti
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import nl.komponents.kovenant.Promise
import rx.Subscription
import kotlin.properties.Delegates

class GameService : Service() {
    var promise: Promise<Unit, Exception> by Delegates.notNull()

    var stateSubscription : Subscription? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action.equals(ACTION_START)) {
            if (!isAccessibilityEnabled()) {
                throw IllegalStateException("Accessibility isn't enabled")
            }
            startTheGame()
        }

        if (intent?.action.equals(ACTION_LISTEN) && stateSubscription == null) {
            ti {"Listen for accessibility enabling"}
            stateSubscription = getChallengeApplication().controller.stateBus
                    .filter { it == AccessibilityController.STATE.CONNECTED }
                    .first()
                    .subscribe { startTheGame() }
        }


        if (intent?.action.equals(ACTION_STOP)) {
            TheGame.doAfterTheGameCompletion()
        }

        return START_NOT_STICKY
    }

    private fun startTheGame() {
        ti {"START THE GAME"}
        promise = TheGame.startTheGame(applicationContext)
        promise success { ti {"GAME SUCCESS"} } failAndThrow  { ti {"GAME FAIL"}}
    }


    companion object {
        const val ACTION_START: String = "start"
        const val ACTION_STOP: String = "stop"
        const val ACTION_LISTEN: String = "listen"
        fun startGame(context: Context) {
            context.startService(Intent(context, GameService::class.java).setAction(ACTION_START))
        }

        fun stopGame(context: Context) {
            context.startService(Intent(context, GameService::class.java).setAction(ACTION_STOP))
        }

        fun listenForAccessibilityAndStart(context: Context) {
            context.startService(Intent(context, GameService::class.java).setAction(ACTION_LISTEN))
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }
}
