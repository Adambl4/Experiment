package adambl4.experiment.challenge.utils.extensions

import android.os.Handler
import nl.komponents.kovenant.*

/**
 * Created by Adambl4 on 01.04.2016.
 */

fun unitDeferred(context: Context = Kovenant.context): Deferred<Unit, Exception> = Kovenant.deferred(context)

fun Deferred<Unit, Exception>.resolve() {
    resolve(Unit)
}

fun wait(duration: Long = 0): Promise<Unit, Exception> {
    if (duration.equals(0)) {
        return Promise.ofSuccess(Unit)
    } else {
        val h = Handler();
        val deferred = deferred<Unit, Exception>()
        h.postDelayed({ deferred.resolve() }, duration)
        return deferred.promise
    }
}

infix fun <V, E> Promise<V, E>.failAndThrow(callback: (error: E) -> Unit): Promise<V, E> = fail(context.callbackContext, callback) fail { throw it as Throwable }

infix fun <V, R> Promise<Promise<V, Exception>, Exception>.unwrapAndThen(bind: (V) -> R): Promise<R, Exception> = unwrap() then(bind)

fun test(){
    val l = task {
        Promise.ofSuccess<Unit, Exception>(Unit)
    } unwrapAndThen {
        Promise.ofSuccess<Unit, Exception>(Unit)
    } //.unwrap() //cannot do .unwrap() in chain

    l.unwrap() //but can do on value
}



