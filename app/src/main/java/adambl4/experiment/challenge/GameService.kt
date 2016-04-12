package adambl4.experiment.challenge

import adambl4.experiment.challenge.accessibility.*
import adambl4.experiment.challenge.utils.clearDeviceAdministration
import adambl4.experiment.challenge.utils.extensions.*
import adambl4.experiment.challenge.utils.isAccessibilityEnabled
import adambl4.experiment.challenge.view.AirplanChallengeView
import adambl4.experiment.challenge.view.CameraAlertDialogOverlay
import adambl4.experiment.challenge.view.RippleView
import adambl4.experiment.challenge.view.showOver
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.then
import nl.komponents.kovenant.unwrap
import org.jetbrains.anko.onClick
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import kotlin.properties.Delegates

class GameService : Service() {
    var promise: Promise<Unit, Exception> by Delegates.notNull()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action.equals(ACTION_START)) {
            if (!isAccessibilityEnabled()) {
                throw IllegalStateException("Accessibility isn't enabled")
            }
            startTheGame()
        }

        if (intent?.action.equals(ACTION_LISTEN)) {
            getChallengeApplication().controller.stateBus
                    .filter { it == AccessibilityController.STATE.CONNECTED }
                    .first()
                    .subscribe { startTheGame() }
        }

        return START_NOT_STICKY
    }

    private fun startTheGame() {
        val theGame = TheGame(this)
        promise = theGame.startTheGame()
        promise success { Log.d("tag", "GAME success") } failAndThrow  { Log.d("tag", "GAME ERROR") } success { clearDeviceAdministration(this) }
    }


    val h = Handler()

    private fun start() {


        fun olo() {
            waitEvent(or(CAMERA_DISABLED_EVENT, ALERT_DIALOG_EVENT)) then {
                Log.d("tag", "wait")
                wait(600) then { // need to wait the end of the window animation
                    Log.d("tag", "DISABLED EVENT");
                    val source = this@GameService.rootInApplicationWindow;
                    Log.d("tag", "source = " + source)
                    if (source != null) {
                        val button = source.findOneById("android:id/button3")
                        if (button != null) {
                            Log.d("tag", "button = $button")
                            val rippleView = RippleView(this)
                            rippleView.showOver(button.rectInScreen())

                            val cameraOverlay = CameraAlertDialogOverlay(this)

                            rippleView.onClick {
                                Log.d("tag", "click");
                                val content = source.findOneById("android:id/content")
                                if (content != null) {
                                    cameraOverlay.showOver(content.rectInScreen()) then {
                                        cameraOverlay.hide()
                                        this@GameService.globalActionBack()
                                    }
                                }
                            }

                            waitEvent(anyNewWindowExcept { CAMERA_DISABLED_EVENT }) then {
                                Log.d("tag", "hide")
                                button.recycle()
                                source.recycle()
                                rippleView.hide()
                                cameraOverlay.hide()
                            }

                        }
                    }
                    olo()
                } failAndThrow  {

                }
            } fail {
                Log.d("tag", "FAIL")
                throw it
            }
        }
        olo()
    }


    fun setupAirplaneChallenge(): Subscription {
        startActivity(INTENT_AIRPLANE_MODE)
        val composite = CompositeSubscription()

        fun isEventOutsideView(view: View, event: MotionEvent): Boolean {
            val l = intArrayOf(0, 0)
            view.getLocationOnScreen(l);
            val rx = event.rawX
            val ry = event.rawY
            val x = l[0];
            val y = l[1];
            val w = view.width;
            val h = view.height;

            if (rx < x || rx > x + w || ry < y || ry > y + h) {
                return false;
            }
            return true;
        }


        fun startChallenge(root: AccessibilityNodeInfo) {
            val listViewNode = root.findOneByIdOrThrow("android:id/list")

            //val coverNode = RippleView(this);
            //coverNode.showOver(listViewNode.getChild(0))

            Log.d("tag", "list node = " + listViewNode.getChild(0).rectInScreen())

            /*      coverNode.setOnTouchListener { view, motionEvent ->
                      if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                          if (isAirplaneModeEnabled()) doAccessibilityManipulation { toggleAirplane(it) } //turn off airplane
                      } else if (motionEvent.action == MotionEvent.ACTION_UP) {
                          if (!isAirplaneModeEnabled()) doAccessibilityManipulation { toggleAirplane(it) } //turn on airplane
                      } else if (!isEventOutsideView(coverNode, motionEvent)) {
                          if (!isAirplaneModeEnabled()) doAccessibilityManipulation { toggleAirplane(it) } //turn on airplane
                      }
                      return@setOnTouchListener false
                  }
      */
            val max = if (listViewNode.childCount > 4) 4 else listViewNode.childCount
            val multitouch = AirplanChallengeView(this)
            for (i in 0..max) {
                multitouch.addRect(listViewNode.getChild(i).rectInScreen())
            }

            multitouch.show()

            //coverNode.background = resources.getDrawable(R.drawable.border_clickable_drawable)
        }

        composite.add(accessibilityEvents()
                .filter { it.source != null }
                .filter { AIRPLANE_SCREEN_OPENED(it) }
                .doOnNext { startChallenge(it.source) }
                .subscribe())

        return composite
    }


    private fun startBrowserChallenge() {
        waitEvent(PASTEBIN_ANSWER_YES_EVENT) then  {
            val source = it.source
            val say_me_what_is_matter = StringBuilder().append(it.source.contentDescription).newLine(getString(R.string.say_me_what_is_matter)).toString();
            source.performSetText(say_me_what_is_matter)
            wait(1000).then {
                source.refresh()
                val android_or_ios = StringBuilder().append(source.contentDescription).newLine(getString(R.string.android_or_ios)).toString()
                source.performSetText(android_or_ios)

                val expectedDescription = android_or_ios + "1";
                val answer_android: (AccessibilityEvent) -> Boolean =
                        and(
                                eventWithType(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED),
                                eventWithClassName("android.widget.EditText"),
                                eventWithDescription(expectedDescription)
                        )


                waitEvent(answer_android)
            }.unwrap()
        } unwrapAndThen  {
            val source = it.source
            wait(500).then {
                val io_or_wwdc = StringBuilder().append(source.contentDescription).newLine(getString(R.string.io_or_wwdc)).toString()
                source.performSetText(io_or_wwdc)

                val expectedDescription = io_or_wwdc + "1";
                val answer_android: (AccessibilityEvent) -> Boolean =
                        and(
                                eventWithType(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED),
                                eventWithClassName("android.widget.EditText"),
                                eventWithDescription(expectedDescription)
                        )


                waitEvent(answer_android)
            }.unwrap()
        } unwrapAndThen {
            val source = it.source
            wait(500).then {
                val enum_or_intdef = StringBuilder().append(source.contentDescription).newLine(getString(R.string.enum_or_intdef)).toString()
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
                waitEvent(answer_android)
            }.unwrap()
        } unwrapAndThen {
            val source = it.source
            wait(500).then {
                val you_are_goddamn_right = StringBuilder().append(source.contentDescription).newLine(getString(R.string.you_are_goddamn_right)).toString()
                source.performSetText(you_are_goddamn_right)
                wait(500).then {
                    val refresh = source.refresh()
                    val press_enter_to_continue = StringBuilder().append(source.contentDescription).newLine(getString(R.string.press_enter_to_continue)).toString()
                    source.performSetText(press_enter_to_continue)
                }
            }
        } unwrapAndThen {
            wait(3000).then {
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

                waitEvent(enter_event)
            }.unwrap()
        } unwrapAndThen {
            Log.d("tag", "YEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEAH")
        }

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
