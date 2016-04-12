package adambl4.experiment.challenge

import adambl4.experiment.challenge.accessibility.AccessibilityController
import adambl4.experiment.challenge.utils.SettingsStrings
import android.app.Application
import android.util.Log
import com.orhanobut.logger.LogLevel
import com.orhanobut.logger.Logger
import nl.komponents.kovenant.DirectDispatcher
import nl.komponents.kovenant.Kovenant

/**
 * Created by Adambl4 on 01.04.2016.
 */

class ChallengeApplication : Application() {
    lateinit var controller: AccessibilityController
    override fun onCreate() {
        super.onCreate()
        SettingsStrings.context = this
        GameConfig.context = this;
        controller = AccessibilityController();

        Kovenant.context {
            //TODO write a comment why DirectDispatcher
            workerContext.dispatcher = DirectDispatcher.instance
            callbackContext.dispatcher = DirectDispatcher.instance

            workerContext.errorHandler = { Log.d("tag", "THROW SYKA"); throw it}
            callbackContext.errorHandler = {Log.d("tag", "THROW FUCK"); throw it}
        }

        Logger.init("logger").methodCount(2).logLevel(LogLevel.FULL).methodOffset(5)


        Thread.setDefaultUncaughtExceptionHandler { thread, ex -> /*ignore*/ Log.d("tag", "HANDLER FUCK"); ex.printStackTrace()}
    }

}


//
/*
fun Context.doWhenAccessibilityEvent(
        eventPredicate: AccessibilityEventPredicate,
        callbackStopPredicate: (AccessibilityEvent) -> Boolean) {
    getChallengeApplication()
            .controller
            .eventBus
            .filter { eventPredicate.apply(it) }
            .takeUntil { callbackStopPredicate(it) }
            .subscribe()
}

fun Context.doWhenAccessibilityEvent(
        eventPredicate: AccessibilityEventPredicate): Promise<AccessibilityEvent, Exception> {
    val deferred = deferred<AccessibilityEvent, Exception>()
    val subscription = getChallengeApplication()
            .controller
            .eventBus
            .filter { eventPredicate.apply(it) }
            .first()
            .doOnError { deferred.reject(it as Exception) }
            .subscribe { deferred.resolve(it) }
    return deferred.promise fail { subscription.unsubscribe() }
}
*/
