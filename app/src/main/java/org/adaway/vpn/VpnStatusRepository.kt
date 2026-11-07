package org.adaway.vpn

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.CopyOnWriteArrayList

object VpnStatusRepository {
    private val _status = MutableStateFlow(VpnStatus.STOPPED)

    @JvmStatic
    val status: StateFlow<VpnStatus> = _status.asStateFlow()

    private val listeners = CopyOnWriteArrayList<VpnStatusListener>()

    /**
     * Register a listener called on every status transition, from any thread.
     *
     * Unlike [status], it does not replay the current value: callers use it for side effects
     * (e.g. keeping the ad-block model's "applied" state in sync with the real service state),
     * not to render the current state.
     */
    @JvmStatic
    fun addListener(listener: VpnStatusListener) {
        listeners.addIfAbsent(listener)
    }

    @JvmStatic
    fun removeListener(listener: VpnStatusListener) {
        listeners.remove(listener)
    }

    @JvmStatic
    fun update(status: VpnStatus) {
        _status.value = status
        listeners.forEach { it.onStatusChanged(status) }
    }
}

/**
 * Java-friendly equivalent of a `(VpnStatus) -> Unit` callback, so Java callers can use a
 * lambda without dealing with Kotlin's `Function1<..., Unit>` return type.
 */
fun interface VpnStatusListener {
    fun onStatusChanged(status: VpnStatus)
}
