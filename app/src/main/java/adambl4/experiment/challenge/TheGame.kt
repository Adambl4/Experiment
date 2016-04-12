package adambl4.experiment.challenge

import adambl4.experiment.challenge.accessibility.*
import adambl4.experiment.challenge.utils.extensions.*
import adambl4.experiment.challenge.utils.isDeviceAdministrationEnabled
import adambl4.experiment.challenge.view.BlackoutView
import adambl4.experiment.challenge.view.FlashingLight
import android.content.Context
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import nl.komponents.kovenant.unwrap
import rx.subscriptions.CompositeSubscription
import kotlin.properties.Delegates

/**
 * Created by Adambl4 on 01.04.2016.
 */
object GameConfig {
    var context: Context by Delegates.notNull()
    var activityContext: Context by Delegates.notNull()

    var startTimeStamp = 0L
    var uninstallAttempts = 0
    var username: CharSequence? = null
}


class TheGame(context: Context) {
    val blockersSubscriptions = CompositeSubscription()

    init {
        GameConfig.context = context;
    }

    fun startTheGame(context: Context = GameConfig.context): Promise<Unit, Exception> {
        val blackout = BlackoutView(context)
        val promise =  task {
            context.blockMainHardwareButtons()
            blackout.show()
        } unwrapAndThen {
            doBeforeTheGamePreparations()
        }  unwrapAndThen   {
            context.globalActionHome()
            wait(2000)
        } unwrapAndThen {
            setupBlockers()
        } unwrapAndThen {
            context.unblockMainHardwareButtons()
            GameConfig.startTimeStamp = System.currentTimeMillis()
            blackout.hide()
        }
        return promise.unwrap()
    }


    private fun doBeforeTheGamePreparations(context: Context = GameConfig.context): Promise<Unit, Exception> {
        val promise = task {
            if (context.lastAccessibilityPackage == "com.android.settings") {
                //close the accessibility screen
                context.globalActionBack()
                context.globalActionBack()
            }
        } then {
            enableAirplaneModeIfNeeded(context)
        } unwrapAndThen {
            becomeLauncherIfNeeded(context)
        } unwrapAndThen {
            muteIfNeeded(context)
        } unwrapAndThen {
            disableAutoRotateIfNeeded(context)
        } unwrapAndThen  {
            //TODO explain why flashing light is needed
            if (!isDeviceAdministrationEnabled(context)) {
                val flashingLight = FlashingLight(context)
                flashingLight.show() then {
                    wait(2000)
                } unwrapAndThen {
                    toggleDeviceAdministration()
                } unwrapAndThen {
                    wait(2000)
                } unwrapAndThen {
                    flashingLight.hide()
                    Unit
                }
            } else {
                Promise.ofSuccess<Unit, Exception>(Unit)
            }
        }
        return promise.unwrap() //TODO why
    }

    //things to block
    //* status bar
    //* wifi settings
    //* launcher settings
    //* sound settings
    //* device admin settings
    //* airplane settings
    //* accessibility settings
    private fun setupBlockers(): Promise<Unit, Exception> {
        blockersSubscriptions.add(setupWifiBlocker())
        blockersSubscriptions.add(setupLauncherBlocker())
        blockersSubscriptions.add(setupSoundClickRedirecter())
        blockersSubscriptions.add(setupDeviceAdminBlocker())
        blockersSubscriptions.add(setupAccessibilityBlocker())
        blockersSubscriptions.add(setupAirplaneBlocker())
        blockersSubscriptions.add(setupStatusBarBlocker())
        return Promise.ofSuccess(Unit)
    }


    private fun doAfterTheGameCompletion(context: Context = GameConfig.context): Promise<Unit, Exception> =
            task {

            }
}



