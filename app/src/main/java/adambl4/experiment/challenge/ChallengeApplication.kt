package adambl4.experiment.challenge

import adambl4.experiment.challenge.TheGame.GameConfig
import adambl4.experiment.challenge.accessibility.AccessibilityController
import adambl4.experiment.challenge.utils.AwesomeDebugTree
import adambl4.experiment.challenge.utils.SettingsStrings
import adambl4.experiment.challenge.utils.td
import android.app.Application
import com.orhanobut.logger.LogLevel
import com.orhanobut.logger.Logger
import nl.komponents.kovenant.DirectDispatcher
import nl.komponents.kovenant.Kovenant
import timber.log.Timber


/**
 * Created by Adambl4 on 01.04.2016.
 */

class ChallengeApplication : Application() {
    lateinit var controller: AccessibilityController

    override fun onCreate() {
        super.onCreate()

        Logger
                .init("logger")
                .methodCount(1)
                .logLevel(LogLevel.FULL)
                .methodOffset(7)
        Timber.plant(AwesomeDebugTree())

        SettingsStrings.context = this
        GameConfig.context = this;
        controller = AccessibilityController();

        Kovenant.context {
            //TODO write a comment why DirectDispatcher
            workerContext.dispatcher = DirectDispatcher.instance
            callbackContext.dispatcher = DirectDispatcher.instance

            workerContext.errorHandler = { td {"WORKER ERROR HANDLER"}; it.printStackTrace(); throw it}
            callbackContext.errorHandler = { td {"CALLBACK ERROR HANDLER"}; it.printStackTrace(); throw it}
        }

        Thread.setDefaultUncaughtExceptionHandler { thread, ex -> /*ignore*/ td { "UNCAUGHT EXCEPTION" }; ex.printStackTrace()}


    }

}