package adambl4.experiment.challenge.utils.extensions

import adambl4.experiment.challenge.TheGame.GameConfig
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred
import rx.Observable

/**
 * Created by Adambl4 on 01.04.2016.
 */
val ARGUMENT_SCROLL_COUNT: String = "ARGUMENT_SCROLL_COUNT"

class ManipulationException(text: String) : Exception(text)

fun AccessibilityNodeInfo.click() {
    if (!performAction(AccessibilityNodeInfo.ACTION_CLICK)) throw ManipulationException("Cannot click node ${toString()}")
}


fun AccessibilityNodeInfo.performSetText(text : String) {
    val arguments = Bundle()
    arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
    if (!performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)) throw ManipulationException("Cannot set text to node ${toString()}")
}

fun AccessibilityNodeInfo.performScrollForward(count : Int = 1) {
    val arguments = Bundle()
    arguments.putInt(ARGUMENT_SCROLL_COUNT, count)
    if (!performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD, arguments)) throw ManipulationException("Cannot scroll forward node ${toString()}")
}

fun AccessibilityNodeInfo.performScrollBackward(count : Int = 1) {
    val arguments = Bundle()
    arguments.putInt(ARGUMENT_SCROLL_COUNT, count)
    if (!performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD, arguments)) throw ManipulationException("Cannot scroll backward node ${toString()}")
}

fun AccessibilityNodeInfo.findByIdOrThrow(id: String): List<AccessibilityNodeInfo> {
    val list = findAccessibilityNodeInfosByViewId(id);
    if (list.isEmpty())
        throw IllegalStateException("Cannot find node by id $id")

    return list;
}

fun AccessibilityNodeInfo.findByTextOrThrow(text: String): List<AccessibilityNodeInfo> {
    val list = findAccessibilityNodeInfosByText(text);
    if (list.isEmpty())  throw IllegalStateException("Cannot find node by text \"$text\"")
    return list;
}

fun AccessibilityNodeInfo.performOrThrow(action: Int) {
    if (!performAction(action)) throw ManipulationException("Cannot perform action $action")
}

fun AccessibilityNodeInfo.findOneByIdOrThrow(id: String): AccessibilityNodeInfo = findByIdOrThrow(id)[0];
fun AccessibilityNodeInfo.findOneById(id: String): AccessibilityNodeInfo? = try{findAccessibilityNodeInfosByViewId(id)[0]} catch (e : IndexOutOfBoundsException){null};

fun AccessibilityNodeInfo.findOneByTextOrThrow(text: String): AccessibilityNodeInfo = findByTextOrThrow(text)[0];

fun AccessibilityNodeInfo.findOneByText(text: String): AccessibilityNodeInfo? {
    val list = findAccessibilityNodeInfosByText(text)
    if(list.isNotEmpty()){
        return list[0]
    } else {
        return null;
    }
}

fun AccessibilityNodeInfo.findOneWithIdAndTextOrThrow(id: String, text: CharSequence): AccessibilityNodeInfo = findByIdOrThrow(id).first { it.text == text }

fun AccessibilityNodeInfo.clickNodeWithId(id: String) {
    performActionOnNodeWithId(id, AccessibilityNodeInfo.ACTION_CLICK)
}

fun AccessibilityNodeInfo.performActionOnNodeWithId(id: String, action: Int) {
    val list = findByIdOrThrow(id);
    if (list.size > 1) throw IllegalStateException("More then 1 node with id $id")
    list[0].performOrThrow(action)
}

fun AccessibilityNodeInfo.performActionOnNodeWithText(text: String, action: Int) {
    val list = findByTextOrThrow(text);
    if (list.size > 1) throw IllegalStateException("More then 1 node with text $text")
    list[0].performOrThrow(action)
}


fun AccessibilityNodeInfo.performActionOverrided(action: Int, bundle: Bundle?) {
    if (action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD || action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) {
        val count: Int = bundle?.getInt(ARGUMENT_SCROLL_COUNT) ?: 0

        for (i in 1..count) {
            performAction(action)
        }
    } else {
        performAction(action, bundle)
    }
}


fun AccessibilityNodeInfo.actionOnPredicate(action: Int,
                                            predicate: (AccessibilityNodeInfo) -> Boolean,
                                            traversal: (AccessibilityNodeInfo, (AccessibilityNodeInfo) -> Boolean) -> Unit = TRAVERSAL_NONE, bundle: Bundle? = null): Unit {

    var success = false
    traversal(this) {
        if (predicate(it)) {
            it.performActionOverrided(action, bundle)
            success = true
            return@traversal true
        }
        return@traversal false
    }

    if (success) return
    throw ManipulationException("Cannot satisfy predicate")
}


//TODO rewrite all traversals

val TRAVERSAL_NONE = { node: AccessibilityNodeInfo, callback: (AccessibilityNodeInfo) -> Boolean -> callback(node); Unit }

fun TRAVERSAL_PARENTS() = { node: AccessibilityNodeInfo, callback: (AccessibilityNodeInfo) -> Boolean ->
    tailrec fun getParent(node: AccessibilityNodeInfo, callback: (AccessibilityNodeInfo) -> Boolean) {
        if (node.parent != null) {
            if (callback(node.parent)) return
            getParent(node.parent, callback)
        }
    }
    getParent(node, callback)
}

val TRAVERSAL_NEXT = { node: AccessibilityNodeInfo, callback: (AccessibilityNodeInfo) -> Boolean ->
    fun getNext(node: AccessibilityNodeInfo?, callback: (AccessibilityNodeInfo) -> Boolean) {
        if (node == null) return
        traversal_forward(node, callback);

        if (node.parent != null) {
            getNext(node.parent.getNext(node), callback)
        }
    }

    getNext(node, callback)
}

fun AccessibilityNodeInfo.getNext(current: AccessibilityNodeInfo): AccessibilityNodeInfo? {
    for (i in 0..childCount - 1) {
        if (getChild(i) == current) {
            if (i != childCount - 1) {
                return getChild(i + 1)
            } else {
                if (parent != null) {
                    return parent.getNext(this)
                } else {
                    return null;
                }
            }
        }
    }
    throw Exception()
}


val TRAVERSAL_FORWARD = { node: AccessibilityNodeInfo, callback: (AccessibilityNodeInfo) -> Boolean ->
    traversal_forward(node, callback)
}


val TRAVERSAL_BACKWARD = { node: AccessibilityNodeInfo, callback: (AccessibilityNodeInfo) -> Boolean ->
    traversal_backward(node, callback)
}


fun traversal_forward(node: AccessibilityNodeInfo?, callback: (AccessibilityNodeInfo) -> Boolean) {
    if(node == null) return
    if (callback(node)) return
    if (node.childCount > 0) {
        for (i in 0..node.childCount - 1) {
            traversal_forward(node.getChild(i), callback);
        }
    }
}

fun traversal_backward(node: AccessibilityNodeInfo?, callback: (AccessibilityNodeInfo) -> Boolean) {
    if (node == null || node.parent == null) return

    if (callback(node)) return

    for (i in node.parent.childCount - 1 downTo 0) {
        if (node.parent.getChild(i) == node) {
            if (i != 0) {
                traversal_backward(node.parent.getChild(i - 1), callback)
            } else {
                traversal_backward(node.parent, callback);
            }
        }
    }
}


fun AccessibilityNodeInfo.rectInScreen(): Rect {
    val rect = Rect()
    getBoundsInScreen(rect)
    return rect
}

fun accessibilityEvents(context: Context = GameConfig.context): Observable<AccessibilityEvent> =
        context.accessibilityEventStream

fun waitAccessibilityEvent(eventPredicate: (AccessibilityEvent) -> Boolean, context: Context = GameConfig.context): Promise<AccessibilityEvent, Exception> {
    val deferred = deferred<AccessibilityEvent, Exception>()
    val subscription = context.accessibilityEventStream
            .filter { eventPredicate(it) }
            .first()
            .subscribe({ deferred.resolve(it) }, { deferred.reject(it as Exception) })

    return deferred.promise fail { subscription.unsubscribe() }
}

fun doAccessibilityManipulation(event: AccessibilityEvent? = null,
                                context: Context = GameConfig.context,
                                node: AccessibilityNodeInfo = event?.source ?: context.rootInApplicationWindow!!,
                                manipulation: (AccessibilityNodeInfo) -> Promise<Unit, Exception>) = manipulation(node)
