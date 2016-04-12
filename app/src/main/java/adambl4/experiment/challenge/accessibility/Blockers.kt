package adambl4.experiment.challenge.accessibility

import adambl4.experiment.challenge.GameConfig
import adambl4.experiment.challenge.R
import adambl4.experiment.challenge.utils.SettingsStrings
import adambl4.experiment.challenge.utils.extensions.*
import adambl4.experiment.challenge.view.*
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import nl.komponents.kovenant.then
import org.jetbrains.anko.newTask
import org.jetbrains.anko.onClick
import rx.Subscription
import rx.lang.kotlin.filterNotNull
import rx.subscriptions.CompositeSubscription
import java.util.*

/**
 * Created by Adambl4 on 12.04.2016.
 */

fun setupAccessibilityBlocker(context: Context = GameConfig.context): Subscription {
    val clickListener = { view: View? -> levelBasedBlockMethod(context) }

    val first = setupSettingsListViewClickInterceptor(context,
            context.getString(R.string.app_name),
            clickListener,
            ACCESSIBILITY_SCREEN_OPENED,
            SETTINGS_2ND_LEVEL_CLICK_TARGET)

    val second = setupScreenBlocker(ACCESSIBILITY_SERVICE_SCREEN_OPENED, { levelBasedBlockMethod(context, 2) })

    val comp = CompositeSubscription()
    comp.add(first)
    comp.add(second)
    return comp
}

fun setupWifiBlocker(context: Context = GameConfig.context): Subscription {
    val clickListener = { view: View? -> levelBasedBlockMethod(context) }

    val first = setupSettingsListViewClickInterceptor(context,
            SettingsStrings.WIFI,
            clickListener)

    val second = setupScreenBlocker(WIFI_SCREEN_OEPENED, { levelBasedBlockMethod(context, 2) })

    val comp = CompositeSubscription()
    comp.add(first)
    comp.add(second)
    return comp
}

fun setupDeviceAdminBlocker(context: Context = GameConfig.context): Subscription {
    val clickListener = { view: View? -> levelBasedBlockMethod(context) }

    val first = setupSettingsListViewClickInterceptor(context,
            context.getString(R.string.admin_label),
            clickListener,
            ADMIN_LIST_SCREEN_OPENED,
            SETTINGS_2ND_LEVEL_CLICK_TARGET)

    val second = setupScreenBlocker(ADMIN_ADD_SCREEN_OPENED, { levelBasedBlockMethod(context, 2) })

    val comp = CompositeSubscription()
    comp.add(first)
    comp.add(second)
    return comp
}

fun setupLauncherBlocker(context: Context = GameConfig.context): Subscription {
    val clickListener = { view: View? -> levelBasedBlockMethod(context) }

    val first = setupSettingsListViewClickInterceptor(context,
            SettingsStrings.HOME,
            clickListener)

    val second = setupScreenBlocker(HOME_SCREEN_OPENED, { levelBasedBlockMethod(context, 2) })

    val comp = CompositeSubscription()
    comp.add(first)
    comp.add(second)
    return comp
}

fun setupAirplaneBlocker(context: Context = GameConfig.context): Subscription {
    val clickListener = { view: View? -> levelBasedBlockMethod(context) }

    val first = setupSettingsListViewClickInterceptor(context,
            SettingsStrings.SETTINGS_MORE,
            clickListener)

    val second = setupScreenBlocker(AIRPLANE_SCREEN_OPENED, { levelBasedBlockMethod(context) })

    val comp = CompositeSubscription()
    comp.add(first)
    comp.add(second)
    return comp
}

fun setupSoundClickRedirecter(context: Context = GameConfig.context): Subscription {
    val randomSettingsActions = arrayOf(
            Settings.ACTION_APN_SETTINGS,
            Settings.ACTION_BATTERY_SAVER_SETTINGS,
            Settings.ACTION_CAST_SETTINGS,
            Settings.ACTION_DATE_SETTINGS,
            Settings.ACTION_DEVICE_INFO_SETTINGS,
            Settings.ACTION_DISPLAY_SETTINGS,
            Settings.ACTION_USAGE_ACCESS_SETTINGS,
            Settings.ACTION_INPUT_METHOD_SETTINGS,
            Settings.ACTION_INTERNAL_STORAGE_SETTINGS,
            Settings.ACTION_LOCALE_SETTINGS,
            Settings.ACTION_PRIVACY_SETTINGS,
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Settings.ACTION_LOCATION_SOURCE_SETTINGS)

    fun openRandomScreen() {
        try {
            context.startActivity(Intent(randomSettingsActions[Random().nextInt(randomSettingsActions.size - 1)]).newTask())
        } catch (e: Exception) {
            openRandomScreen()
        }
    }

    val clickListener = { view: View? -> openRandomScreen() }

    return setupSettingsListViewClickInterceptor(context,
            SettingsStrings.SOUND_AND_NOTIFICATION,
            clickListener)
}

fun setupStatusBarBlocker(context: Context = GameConfig.context): Subscription {
    val node = context.statusBarUI
    val nodeBlock = object : SystemAlertView(context) {}
    if (node != null) {
        nodeBlock.showOver(node.rectInScreen())
    } else {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            val height = context.resources.getDimensionPixelSize(resourceId);
            val width = context.resources.displayMetrics.widthPixels;
            nodeBlock.showOver(android.graphics.Rect(0, 0, width, height))
        }
    }

    return object : Subscription {
        override fun isUnsubscribed(): Boolean {
            return !nodeBlock.isAttachedToWindow
        }

        override fun unsubscribe() {
            nodeBlock.hide()
        }
    }
}


fun setupSettingsListViewClickInterceptor(context: Context,
                                          targetText: String,
                                          clickListener: (View?) -> Unit,
                                          eventPredicate: (AccessibilityEvent) -> Boolean = SETTINGS_SCREEN_OPENED,
                                          targetPredicate: (AccessibilityNodeInfo) -> Boolean = SETTINGS_1ST_LEVEL_CLICK_TARGET): Subscription {
    val handlerView = RippleView(context);
    handlerView.onClick(clickListener)
    var isShown = false;
    val nodeProvider = settingsClickTargetProvider(targetText, targetPredicate)

    val composite = CompositeSubscription()

    fun hide() {
        if (isShown) {
            handlerView.hide()
            isShown = false
        }
    }

    fun show(node: AccessibilityNodeInfo) {
        handlerView.followNode(node, { nodeProvider(context.rootInApplicationWindow) })
        isShown = true
        composite.add(accessibilityEvents()
                .filter { isShown }
                .first { (anyNewWindowExcept { eventPredicate })(it) }
                .doOnNext { hide() }
                .doOnUnsubscribe { hide() }
                .subscribe())
    }

    composite.add(accessibilityEvents()
            .filter { !isShown }
            .filter { eventPredicate(it) }
            .map { nodeProvider(it.source) }
            .filterNotNull()
            .doOnNext { show(it) }
            .subscribe())
    return composite
}

fun setupScreenBlocker(eventPredicate: (AccessibilityEvent) -> Boolean, blockMethod: (AccessibilityEvent) -> Unit): Subscription {
    return accessibilityEvents()
            .filter { eventPredicate(it) }
            .doOnNext(blockMethod)
            .subscribe()
}

fun levelBasedBlockMethod(context: Context, level: Int = 1) {
    val blockView = BillyTheAndroidView(context)
    blockView.show() then {
        for (i in 1..level) {
            context.globalActionBack()
        }
        context.globalActionHome();
        wait(500)
    } unwrapAndThen {
        blockView.hide()
    }
}


fun getSettingsListViewClickTarget(rootNode: AccessibilityNodeInfo?,
                                   text: String,
                                   predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo? {
    if (rootNode == null) return null
    val anchor = rootNode.findOneByText(text) ?: return null
    var target: AccessibilityNodeInfo? = null;
    TRAVERSAL_BACKWARD(anchor) {
        if (predicate(it)) {
            target = it;
            return@TRAVERSAL_BACKWARD true
        }
        return@TRAVERSAL_BACKWARD false
    }
    return target;
}

fun settingsClickTargetProvider(text: String, predicate: (AccessibilityNodeInfo) -> Boolean = SETTINGS_1ST_LEVEL_CLICK_TARGET): (AccessibilityNodeInfo?) -> AccessibilityNodeInfo? =
        { getSettingsListViewClickTarget(it, text, predicate) }

