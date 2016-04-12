package adambl4.experiment.challenge.view

import adambl4.experiment.challenge.R
import adambl4.experiment.challenge.utils.extensions.TRAVERSAL_PARENTS
import adambl4.experiment.challenge.utils.extensions.rectInScreen
import adambl4.experiment.challenge.utils.extensions.unitDeferred
import adambl4.experiment.challenge.utils.extensions.unwrapAndThen
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import nl.komponents.kovenant.*
import org.jetbrains.anko.*
import java.util.*

/**
 * Created by Adambl4 on 05.04.2016.
 */

abstract class SystemAlertView(context: Context) : FrameLayout(context) {
    open val params: WindowManager.LayoutParams by lazy {
        defaultParams()
    }

    open fun show(): Promise<Unit, Exception> {
        addToWindowManager(params)
        return Promise.ofSuccess(Unit)
    }

    open fun hide(): Promise<Unit, Exception> {
        removeFromWindowManager()
        return Promise.ofSuccess(Unit)
    }
}

class BlackoutView(context: Context, val duration: Long = 2000) : SystemAlertView(context) {
    init {
        alpha = 0f
        backgroundResource = android.R.color.black
    }

    override fun show(): Promise<Unit, Exception> {
        val deferred = deferred<Unit, Exception>()
        super.show() then {
            animate().alpha(1f).setDuration(duration).withEndAction { deferred.resolve(Unit) }.start()
        }
        return deferred.promise
    }

    override fun hide(): Promise<Unit, Exception> {
        val deferred = deferred<Unit, Exception>()
        animate().alpha(0f).setDuration(duration).withEndAction {
            super.hide() then {
                deferred.resolve(Unit)
            }
        }.start()
        return deferred.promise
    }
}

class FlashingLight(context: Context) : SystemAlertView(context) {
    init {
        visibility = INVISIBLE
        imageView {
            image = resources.getDrawable(R.drawable.lamp, null)
        }
        backgroundResource = android.R.color.white
    }

    val random = Random()
    fun flash() {
        visibility = VISIBLE
        postDelayed({
            visibility = INVISIBLE
            postDelayed({ flash() }, random.nextInt(500).toLong())
        }, 200)
    }


    override fun show(): Promise<Unit, Exception> {
        flash()
        return super.show()
    }

    override fun hide(): Promise<Unit, Exception> {
        handler.removeCallbacksAndMessages(null)
        return super.hide()
    }
}


class BillyTheAndroidView(context: Context, val duration: Long = 1000) : SystemAlertView(context) {
    init {
        alpha = 0f
        imageView {
            image = context.getDrawable(R.drawable.billy_the_android)
            backgroundColor = context.resources.getColor(android.R.color.black)
        }
    }

    override fun show(): Promise<Unit, Exception> =
            super.show().then() {
                val deferred = unitDeferred()
                animate().alpha(1f).setDuration(duration / 3 * 2).withEndAction { deferred.resolve(Unit) }.start()
                deferred.promise
            }.unwrap()

    override fun hide(): Promise<Unit, Exception> =
            task {
                val deferred = unitDeferred()
                animate().alpha(0f).setDuration(duration / 3 * 1).withEndAction { deferred.resolve(Unit) }.start()
                deferred.promise
            }.unwrapAndThen() { super.hide() }.unwrap()
}


class RippleView(context: Context) : SystemAlertView(context) {
    init {
        isClickable = true
        foreground = resources.getDrawable(R.drawable.transparent_ripple_like_in_settings_app)
        backgroundColor = resources.getColor(android.R.color.holo_red_dark)
    }
}


class CameraAlertDialogOverlay(context: Context) : SystemAlertView(context) {
    val FADE_IN_DURATION = 1000L

    init {
        alpha = 0f
        backgroundResource = android.R.color.black
    }


    override fun show(): Promise<Unit, Exception> {
        val deferred = deferred<Unit, Exception>()
        super.show() then {
            animate().alpha(1f).setDuration(FADE_IN_DURATION).withEndAction { deferred.resolve(Unit) }.start()
        }
        return deferred.promise
    }
}

val MotionEvent.currentId: Int get() = getPointerId(actionIndex)

class AirplanChallengeView(context: Context) : SystemAlertView(context) {
    val listOfRect = mutableListOf<Rect>()
    val isRectPressed = mutableListOf<Boolean>()
    val rectPaintPressed = Paint()
    val rectPaintNotPressed = Paint()
    var hhandler = Handler()
    val random = Random()
    var hareRect: Rect? = null

    val showRunnable = Runnable {
        val randomX = random.nextInt(height)
        val randomY = random.nextInt(width)

        hareRect = Rect(0, 0, 500, 500)

        (hareRect as Rect).offsetTo(randomX, randomY)
        invalidate()

        hhandler.postDelayed(hideRunnale, 920)
    }

    val hideRunnale = Runnable {
        hareRect = null;
        invalidate()
        startHareCoursing()
    }

    init {
        foreground = resources.getDrawable(R.drawable.transparent_ripple_like_in_settings_app)
        rectPaintPressed.color = resources.getColor(android.R.color.holo_green_dark)
        rectPaintPressed.strokeWidth = 3f
        rectPaintPressed.style = Paint.Style.STROKE

        rectPaintNotPressed.color = resources.getColor(android.R.color.holo_red_dark)
        rectPaintNotPressed.strokeWidth = 6f
        rectPaintNotPressed.style = Paint.Style.STROKE
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return super.onTouchEvent(event)


        if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_DOWN || event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_POINTER_DOWN) {
            if (hareRect != null) {
                if ((hareRect as Rect).contains(event.getX(event.pointerCount - 1).toInt(), event.getY(event.pointerCount - 1).toInt())) {
                    Log.d("tag", "OOOOOOOOOOOPPPPPPP");
                }
            }

        }

        if (event.currentId > listOfRect.size - 1 || event.currentId >= event.pointerCount || event.pointerCount > listOfRect.size) return false

        if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_DOWN) {
            for (i in 1..isRectPressed.size - 1) {
                isRectPressed[i] = false
            }
        }

        when (event.action and MotionEvent.ACTION_MASK) {

            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (listOfRect[event.currentId].contains(event.getX(event.currentId).toInt(), event.getY(event.currentId).toInt())) {
                    isRectPressed[event.currentId] = true
                    invalidate()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                for (i in 0..event.pointerCount - 1) {
                    val id = event.getPointerId(i)
                    if (id > listOfRect.size - 1 || id > event.pointerCount - 1) return false;

                    if (listOfRect[id].contains(event.getX(id).toInt(), event.getY(id).toInt())) {
                        isRectPressed[id] = true
                    } else {
                        isRectPressed[id] = false
                    }
                }
                invalidate()
            }

            MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
                isRectPressed[event.currentId] = false
                invalidate()
            }
        }

        if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_POINTER_DOWN && event.pointerCount == listOfRect.size) startHareCoursing()
        if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_POINTER_UP && hareRect != null) stopHareCoursing()


        return super.onTouchEvent(event)
    }

    private fun startHareCoursing() {
        hhandler.postDelayed(showRunnable, random.nextInt(1000).toLong())
    }


    private fun stopHareCoursing() {
        if (hareRect != null) {
            hhandler.removeCallbacksAndMessages(null)
            hareRect = null;
            invalidate()
        }
    }

    fun addRect(rect: Rect) {
        rect.inset(-1, -1)
        listOfRect.add(rect)
        isRectPressed.add(false)
    }

    override fun onDraw(canvas: Canvas?) {
        if (isRectPressed[0] == false) {
            canvas?.drawRect(listOfRect[0], rectPaintNotPressed)
        } else {
            listOfRect.forEach {
                canvas?.drawRect(it, if (isRectPressed[listOfRect.indexOf(it)] == true) rectPaintPressed else rectPaintNotPressed)
            }
        }

        if (hareRect != null) {
            canvas?.drawRect(hareRect, rectPaintNotPressed)
        }
        super.onDraw(canvas)
    }
}

fun SystemAlertView.followNode(node: AccessibilityNodeInfo, freshNodeProvider: () -> AccessibilityNodeInfo?, frequency: Long = 5) {
    val handler = Handler()
    var nodeToFollow = node;
    visibility = View.VISIBLE

    fun update() {
        if (nodeToFollow.refresh()) {
            TRAVERSAL_PARENTS()(nodeToFollow) {
                it.recycle()
                true
            }
            fitParamsToRect(nodeToFollow.rectInScreen(), params)
            if (params.width > 0 && params.height > 0) {
                if (!isAttachedToWindow){
                    show()
                } else {
                    updateLayoutParams(params)
                }
            }
            handler.postDelayed(::update, frequency)
        } else {
            Log.d("tag", "CANNOT REFRESH NODE = $nodeToFollow")
            visibility = View.GONE
            val newNode = freshNodeProvider()
            if (newNode != null) {
                visibility = View.VISIBLE
                nodeToFollow = newNode;
                handler.postDelayed(::update, frequency)
            } else {
                hide()
            }
        }
    }

    onAttachStateChangeListener {
        onViewDetachedFromWindow {
            handler.removeCallbacksAndMessages(null)
            removeOnAttachStateChangeListener(this)
        }
    }

    update()
}


fun SystemAlertView.showOver(rect : Rect): Promise<Unit, Exception> {
    fitParamsToRect(rect, params)
    return show()
}

fun fitParamsToRect(rect : Rect, params: WindowManager.LayoutParams) {
    params.width = rect.width()
    params.height = rect.height()
    params.x = rect.left
    params.y = rect.top
    params.gravity = Gravity.TOP or Gravity.LEFT
}

private fun defaultParams(): WindowManager.LayoutParams {
    val layoutParams = WindowManager.LayoutParams()
    layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
    layoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
    layoutParams.format = PixelFormat.TRANSPARENT
    return layoutParams
}

fun View.addToWindowManager(params: WindowManager.LayoutParams) {
    try {
        context.windowManager.addView(this, params)
    } catch(ignore: Exception) {
        ignore.printStackTrace()
    }
}

fun View.updateLayoutParams(params: WindowManager.LayoutParams) {
    context.windowManager.updateViewLayout(this, params)
}

fun View.removeFromWindowManager() {
    try {
        context.windowManager.removeView(this)
    } catch(ignore: Exception) {
        ignore.printStackTrace()
    }
}

