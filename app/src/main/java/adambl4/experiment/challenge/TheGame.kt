package adambl4.experiment.challenge

import adambl4.experiment.challenge.accessibility.*
import adambl4.experiment.challenge.utils.*
import adambl4.experiment.challenge.utils.extensions.*
import adambl4.experiment.challenge.view.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Environment
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import nl.komponents.kovenant.unwrap
import org.jetbrains.anko.*
import rx.Subscription
import rx.lang.kotlin.PublishSubject
import rx.subscriptions.CompositeSubscription
import java.io.File
import kotlin.properties.Delegates

/**
 * Created by Adambl4 on 01.04.2016.
 */


object TheGame {
    val blockersSubscriptions = CompositeSubscription()
    const val UNINSTALL_ATTEMPTS_BEFORE_EVENT = 2
    const val SOUND_FIX_ATTEMPTS_BEFORE_EVENT = 3
    const val PASTEBIN_CAMERA_UNLOCKER_LINK = "http://pastebin.com/ZQndFGey"

    object GameConfig {
        var context: Context by Delegates.notNull()
        var activityContext: Context by Delegates.notNull()
    }

    object GameStats {
        var startTimeStamp: Long = System.currentTimeMillis()
        var username: String = ""
        var attemptsToUninstall: Int by Delegates.observable(0) { prop, old, new -> attemptToUninstall(new) }
        var attemptsToFixSound: Int by Delegates.observable(0) { prop, old, new -> attemptToFixSound(new) }
        var isTTSsynthesized = false
        var isCameraChecked = false
        val cameraFile = lazy { File(Environment.getExternalStorageDirectory(), "$username reflection.jpg") }
        val tapeFile = lazy { File(Environment.getExternalStorageDirectory(), "play_me.wav") }
        var isGameWin = false
        var endTimestamp = 0L


        fun clear() {
            startTimeStamp = System.currentTimeMillis()
            username = ""
            attemptsToUninstall = 0
            attemptsToFixSound = 0
            isTTSsynthesized = false
            isCameraChecked = false
            isGameWin = false
            endTimestamp = 0
        }

        private fun attemptToFixSound(attempt: Int) {
            if (attempt == SOUND_FIX_ATTEMPTS_BEFORE_EVENT) {
                sendEvent(GameEvent.ATTEMPT_TO_OPEN_SOUND_SETTINGS)
            }
        }


        fun attemptToUninstall(attempt: Int) {
            if (attempt == UNINSTALL_ATTEMPTS_BEFORE_EVENT) {
                td { "sendEvent(GameEvent.ATTEMPT_TO_UNINSTALL)" }
                sendEvent(GameEvent.ATTEMPT_TO_UNINSTALL)
            }
        }

    }

    fun startTheGame(context: Context): Promise<Unit, Exception> {
        val blackout = BlackoutView(context)

        val promise = task {
            context.blockHardwareButtons()
            blackout.show()
        } unwrapAndThen {
            doBeforeTheGamePreparations()
        }  unwrapAndThen   {
            context.globalActionHome()
            wait(2000)
        } unwrapAndThen {
            listenKeyEvents()
            setupBlockers()
        } unwrapAndThen {
            GameStats.startTimeStamp = System.currentTimeMillis()
            blackout.hide()
        } unwrapAndThen{
            context.unblockHardwareButtons()
            context.setVolumeKeysBlocked(true)
            td { "wait 10000" }
            wait(10000).then {
                td { "show help " }
                showHelp(String.format(context.resources.getString(R.string.what_happens), GameStats.username))
            }.then {
                td { "wait attempt to uninstall " }
                waitGameEvent(GameEvent.ATTEMPT_TO_UNINSTALL)
            }.unwrap()
        } unwrapAndThen {
            td { "synthesizeTape" }
            synthesizeTape()
            wait(5000).then {
                showHelp(GameConfig.context.getString(R.string.look_into_pocked_help_text));
                td { "waitGameEvent(GameEvent.AUDIO_APP_OPENED)" }
                waitGameEvent(GameEvent.AUDIO_APP_OPENED)
            }.unwrap()
        } unwrapAndThen {
            td { "waitGameEvent(GameEvent.ATTEMPT_TO_TURN_VOLUME_UP_WITH_BUTTON)" }
            waitGameEvent(GameEvent.ATTEMPT_TO_TURN_VOLUME_UP_WITH_BUTTON).then {
                wait(1500)
            }.unwrapAndThen {
                showHelp(context.getString(R.string.it_disabled_the_volume_help_text))
            }
        } unwrapAndThen {
            td { "waitGameEvent(GameEvent.ATTEMPT_TO_OPEN_SOUND_SETTINGS)" }
            waitGameEvent(GameEvent.ATTEMPT_TO_OPEN_SOUND_SETTINGS) then { wait(5000) } unwrapAndThen  {
                showHelp(context.getString(R.string.try_to_find_another_way_help_text))
            }
        } unwrapAndThen {
            listenForSoundScreenOpening()
            td { "waitGameEvent(GameEvent.SOUND_SETTINGS_OPENED)" }
            waitGameEvent(GameEvent.SOUND_SETTINGS_OPENED)
        } unwrapAndThen {
            td { "SOUND SETTINGS SCREEN OPENED" }
            //TODO show help about "reflection"
            setupHelperForDisabledCamera()
            td { "waitGameEvent(GameEvent.CAMERA_OPENED)" }
            waitGameEvent(GameEvent.CAMERA_OPENED)
        } unwrapAndThen {
            td { " waitGameEvent(GameEvent.CHROME_OPENED)" }
            waitGameEvent(GameEvent.CHROME_OPENED)
        } unwrapAndThen {
            wait(1500).then {
                showHelp(context.getString(R.string.need_to_disable_airplane_help_text))
                airplaneBlocker?.unsubscribe()
                setupAirplaneChallenge()
                td { "waitGameEvent(GameEvent.AIRPLANE_DISABLED)" }
                waitGameEvent(GameEvent.AIRPLANE_DISABLED)
            }.unwrap()
        } unwrapAndThen {
            Toast.makeText(context, "Airplane disabled", Toast.LENGTH_SHORT).show()
            startBrowserChallenge()
            td { "waitGameEvent(GameEvent.BROWSER_CHALLENGE_WIN)" }
            waitGameEvent(GameEvent.BROWSER_CHALLENGE_WIN)
        } unwrapAndThen {
            setCameraDisabled(false)
            context.startActivity(TAKE_PHOTO_INTENT(context))
            td { "waitGameEvent(GameEvent.PHOTO_TAKEN)" }
            waitGameEvent(GameEvent.PHOTO_TAKEN)
        } unwrapAndThen {//
            val view: SystemAlertView;
            val bitmap = getBitmapFromFile(GameStats.cameraFile.value)
            if (bitmap != null) {
                val bitmapWithText = drawText(bitmap, "TURN THE LIGHTS OFF")
                view = object : SystemAlertView(context) {
                    init {
                        backgroundResource = android.R.color.black
                        imageView {
                            imageBitmap = bitmapWithText
                        }
                    }
                }
            } else {
                view = object : SystemAlertView(context) {
                    init {
                        backgroundResource = android.R.color.black
                        textView("TURN THE LIGHTS OFF") {
                            textSize = 30f
                            textColor = resources.getColor(android.R.color.holo_red_dark)
                        }
                    }
                }
            }
            view.show()
            listenForScreenOffOn()
            waitGameEvent(GameEvent.SCREEN_IS_OFF).then {
                td { "SCREEN_IS_OFF" }
                view.hide()
            }.unwrap()
        } unwrapAndThen {
            td { "WIN" }
            gamewin()
            //TODO WIN
        }
        return promise
    }

    private fun gamewin() {
        GameStats.isGameWin = true
        GameStats.endTimestamp = System.currentTimeMillis()
        GameStats.isTTSsynthesized = false;
        GameStats.isCameraChecked = false
    }


    private fun doBeforeTheGamePreparations(context: Context = GameConfig.context): Promise<Unit, Exception> {
        ti { "Do before the game preparations" }
        val promise = task {
            if (context.lastAccessibilityPackage == "com.android.settings") {
                ti { "current package is \"com.android.settings\", do BACK twice" }
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
            //when "add device admin" screen opened, all view overlays disappear
            //so take user attention off using flashing light
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
                }
            } else {
                Promise.ofSuccess<Unit, Exception>(Unit)
            }
        } unwrapAndThen {
            setCameraDisabled(true)
        }
        return promise
    }

    //things to block
    //* status bar
    //* wifi settings
    //* launcher settings
    //* sound settings
    //* device admin settings
    //* airplane settings
    //* app info screen
    //* accessibility settings
    var airplaneBlocker: Subscription? = null

    private fun setupBlockers(): Promise<Unit, Exception> {
        ti { "Setup blockers" }
        //TODO explain
        blockersSubscriptions.add(setupWifiBlocker())
        blockersSubscriptions.add(setupLauncherBlocker())
        blockersSubscriptions.add(setupSoundClickRedirecter())
        blockersSubscriptions.add(setupDeviceAdminBlocker())
        blockersSubscriptions.add(setupAccessibilityBlocker())
        airplaneBlocker = setupAirplaneBlocker()
        blockersSubscriptions.add(airplaneBlocker)
        blockersSubscriptions.add(setupAppInfoScreenBlocker())
        blockersSubscriptions.add(setupStatusBarBlocker())
        return Promise.ofSuccess(Unit)
    }


    fun setupHelperForDisabledCamera(context: Context = GameConfig.context): Subscription {
        fun doWorkaround() {
            Log.d("tag", "SHOW CAMERA HELP")
            showHelp(context.getString(R.string.camera_disabled_help_text))
            GameStats.isCameraChecked = true
        }

        return accessibilityEvents()
                .filter { or(CAMERA_DISABLED_EVENT, ALERT_DIALOG_EVENT)(it) }
                .subscribe { doWorkaround(); sendEvent(GameEvent.CAMERA_OPENED) }
    }

    fun setupAirplaneChallenge(context: Context = GameConfig.context): Subscription {
        val composite = CompositeSubscription()

        fun startChallenge(root: AccessibilityNodeInfo) {
            val listViewNode = root.findOneByIdOrThrow("android:id/list")

            var legalUnbloicked = false;
            val max = if (listViewNode.childCount > 4) 4 else listViewNode.childCount
            val pointerCallback = { pointer: Int, event: MotionEvent ->
                if (pointer == 0) {
                    if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_DOWN) {
                        td { "DISABLE ACCESSIBILITY" }
                        if (isAirplaneModeEnabled()) doAccessibilityManipulation { toggleAirplaneManipulation(it) } //turn off airplane
                        context.blockHardwareButtons()
                    } else if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_UP) {
                        if (!legalUnbloicked) {
                            td { "ENABLE ACCESSIBILITY" }
                            if (!isAirplaneModeEnabled()) doAccessibilityManipulation { toggleAirplaneManipulation(it) } //turn on airplane
                        }
                        context.unblockHardwareButtons()
                    }
                }
            }
            val hareCallback = {
                td { "sendEvent(GameEvent.AIRPLANE_DISABLED);" };
                sendEvent(GameEvent.AIRPLANE_DISABLED);
                composite.unsubscribe()
                context.unblockHardwareButtons()
                context.globalActionHome()
                Unit
            }
            val multitouch = AirplanChallengeView(context, pointerCallback, hareCallback)
            for (i in 0..max) {
                multitouch.addRect(listViewNode.getChild(i).rectInScreen())
            }

            multitouch.show()

            composite.add(accessibilityEvents()
                    .filter { it.source != null }
                    .filter { anyNewWindowExcept { AIRPLANE_SCREEN_OPENED }(it) }
                    .doOnNext { multitouch.hide() }
                    .doOnUnsubscribe { multitouch.hide() }
                    .subscribe())
        }

        composite.add(accessibilityEvents()
                .filter { it.source != null }
                .filter { AIRPLANE_SCREEN_OPENED(it) }
                .doOnNext { startChallenge(it.source) }
                .subscribe())

        return composite
    }


    //shit
    fun startBrowserChallenge(context: Context = GameConfig.context) {
        td { "startBrowserChallenge" }

        val PASTEBIN_ANSWER_YES_EVENT_TEXT_CHANGED: (AccessibilityEvent) -> Boolean = and(
                eventWithType(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED),
                eventWithClassName("android.widget.EditText"),
                or(
                        //TODO fffuuuuu
                        eventWithDescription(TheGame.GameConfig.context.getString(R.string.pastebin_text) + "Y"),
                        eventWithDescription(TheGame.GameConfig.context.getString(R.string.pastebin_text) + " Y"),
                        eventWithDescription(TheGame.GameConfig.context.getString(R.string.pastebin_text) + "y"),
                        eventWithDescription(TheGame.GameConfig.context.getString(R.string.pastebin_text) + " y")
                )
        )


        waitAccessibilityEvent(PASTEBIN_ANSWER_YES_EVENT_TEXT_CHANGED) then  {
            td { "PASTEBIN ANSWER Y" }
            val source = it.source
            val say_me_what_is_matter = StringBuilder().append(it.source.contentDescription).newLine(context.getString(R.string.say_me_what_is_matter)).toString();
            source.performSetText(say_me_what_is_matter)
            wait(1000).then {
                source.refresh()
                val android_or_ios = StringBuilder().append(source.contentDescription).newLine(context.getString(R.string.android_or_ios)).toString()
                source.performSetText(android_or_ios)

                val expectedDescription = android_or_ios + "1";
                val answer_android: (AccessibilityEvent) -> Boolean =
                        and(
                                eventWithType(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED),
                                eventWithClassName("android.widget.EditText"),
                                eventWithDescription(expectedDescription)
                        )


                waitAccessibilityEvent(answer_android)
            }.unwrap()
        } unwrapAndThen  {
            val source = it.source
            wait(500).then {
                val io_or_wwdc = StringBuilder().append(source.contentDescription).newLine(context.getString(R.string.io_or_wwdc)).toString()
                source.performSetText(io_or_wwdc)

                val expectedDescription = io_or_wwdc + "1";
                val answer_android: (AccessibilityEvent) -> Boolean =
                        and(
                                eventWithType(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED),
                                eventWithClassName("android.widget.EditText"),
                                eventWithDescription(expectedDescription)
                        )


                waitAccessibilityEvent(answer_android)
            }.unwrap()
        } unwrapAndThen {
            val source = it.source
            wait(500).then {
                val enum_or_intdef = StringBuilder().append(source.contentDescription).newLine(context.getString(R.string.enum_or_intdef)).toString()
                source.performSetText(enum_or_intdef)

                val expectedDescription1 = enum_or_intdef + "1";
                val expectedDescription2 = enum_or_intdef + "2";
                val answer_android: (AccessibilityEvent) -> Boolean =
                        and(
                                eventWithType(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED),
                                eventWithClassName("android.widget.EditText"),
                                or(
                                        eventWithDescription(expectedDescription1),
                                        eventWithDescription(expectedDescription2)
                                )
                        )
                waitAccessibilityEvent(answer_android)
            }.unwrap()
        } unwrapAndThen {
            val source = it.source
            wait(500).then {
                val you_are_goddamn_right = StringBuilder().append(source.contentDescription).newLine(context.getString(R.string.you_are_goddamn_right)).toString()
                source.performSetText(you_are_goddamn_right)
                wait(500).then {
                    val refresh = source.refresh()
                    val press_enter_to_continue = StringBuilder().append(source.contentDescription).newLine(context.getString(R.string.press_enter_to_continue)).toString()
                    source.performSetText(press_enter_to_continue)
                }
            }
        } unwrapAndThen {
            wait(1500).then {
                val enter_event: (AccessibilityEvent) -> Boolean =
                        or(
                                and(
                                        eventWithType(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED),
                                        eventWithClassName("android.widget.EditText")
                                ),
                                and(
                                        eventWithType(AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED),
                                        eventWithClassName("android.widget.EditText")
                                )
                        )

                waitAccessibilityEvent(enter_event)
            }.unwrap()
        } unwrapAndThen {
            td { "sendEvent(GameEvent.BROWSER_CHALLENGE_WIN)" }
            sendEvent(GameEvent.BROWSER_CHALLENGE_WIN)
        }

    }

    fun showHelp(helpText: String, context: Context = GameConfig.context) {
        HelpView(GameConfig.context, helpText).show()
    }

    private fun listenForSoundScreenOpening() {
        accessibilityEvents()
                .first(SOUND_SCREEN_OPENED)
                .subscribe { sendEvent(GameEvent.SOUND_SETTINGS_OPENED) }
    }

    private fun listenKeyEvents(context: Context = GameConfig.context) {
        ti { "Start listening for key events" }
        context.keyEventStream
                .subscribe {
                    if (it.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                        sendEvent(GameEvent.ATTEMPT_TO_TURN_VOLUME_UP_WITH_BUTTON)
                    }
                }

    }


    fun listenForScreenOffOn(context: Context = GameConfig.context) {
        val filter = IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("tag", "INTENT ${intent?.action}")
                if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                    sendEvent(GameEvent.SCREEN_IS_OFF)
                } else if (intent?.action == Intent.ACTION_SCREEN_ON) {
                    sendEvent(GameEvent.SCREEN_IS_ON)
                }
            }
        }
        context.registerReceiver(receiver, filter);

    }


    enum class GameEvent {
        ATTEMPT_TO_UNINSTALL, AUDIO_APP_OPENED, ATTEMPT_TO_TURN_VOLUME_UP_WITH_BUTTON,
        ATTEMPT_TO_OPEN_SOUND_SETTINGS, SOUND_SETTINGS_OPENED, CHROME_OPENED, CAMERA_OPENED,
        AIRPLANE_DISABLED, BROWSER_CHALLENGE_WIN, PHOTO_TAKEN, SCREEN_IS_OFF, SCREEN_IS_ON
    }

    val eventBus = PublishSubject<GameEvent>()
    private fun waitGameEvent(event: GameEvent): Promise<Unit, Exception> {
        val deferred = unitDeferred()
        val subscription = eventBus
                .asObservable()
                .first { it == event }
                .subscribe { deferred.resolve(Unit) }
        return deferred.promise fail { subscription.unsubscribe() }
    }

    fun sendEvent(event: GameEvent) {
        eventBus.onNext(event)
    }

    fun doAfterTheGameCompletion(context: Context = GameConfig.context): Promise<Unit, Exception> =
            task {
                blockersSubscriptions.unsubscribe()
                context.setActivityEnabled(false, LauncherActivity::class.java)
                setCameraDisabled(false)
                clearDeviceAdministration()
                context.globalActionHome()
                GameStats.clear()
            }
}



