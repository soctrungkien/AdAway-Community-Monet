package org.adaway.ui.adblocking

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.adaway.AdAwayApplication
import org.adaway.R
import org.adaway.model.error.HostErrorException
import org.adaway.util.ExpressiveSnackbar

class ApplyConfigurationSnackbar(
    private val view: View,
    private val syncSources: Boolean,
    private val ignoreEventDuringInstall: Boolean
) {
    private val notifySnackbar: Snackbar = Snackbar
        .make(view, R.string.notification_configuration_changed, LENGTH_INDEFINITE)
        .setAction(R.string.notification_configuration_changed_action) { apply() }
    private val waitSnackbar: Snackbar = Snackbar
        .make(view, R.string.notification_configuration_installing, LENGTH_INDEFINITE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var update = false
    private var skipUpdate = false

    init {
        ExpressiveSnackbar.style(notifySnackbar)
        ExpressiveSnackbar.style(waitSnackbar)
        appendViewToSnackbar(waitSnackbar, ProgressBar(view.context))
    }

    fun notifyUpdateAvailable() {
        if (notifySnackbar.isShown) {
            return
        }
        if (waitSnackbar.isShown) {
            update = true
            return
        }
        if (skipUpdate) {
            skipUpdate = false
            return
        }
        notifySnackbar.show()
        update = false
    }

    /**
     * Create an [Observer] with the same "ignore the first (initialization) event" rule as the
     * mobile screens, but calling back instead of showing the snackbar's own action button.
     * For a TV screen whose "Apply" control is a plain header button: a Snackbar sits outside
     * the normal focus order, so a D-pad cannot reach its action.
     */
    fun <T> createPendingObserver(onPending: Runnable): Observer<T> {
        return object : Observer<T> {
            private var firstUpdate = true

            override fun onChanged(value: T) {
                if (value == null || (value is Collection<*> && value.isEmpty())) {
                    return
                }
                if (firstUpdate) {
                    firstUpdate = false
                    return
                }
                onPending.run()
            }
        }
    }

    /**
     * Apply the pending configuration change. Also the snackbar action's own target on a screen
     * that keeps using this class's Snackbar; a screen driving its own header button through
     * [createPendingObserver] calls this directly instead.
     */
    fun apply() {
        showLoading()
        scope.launch {
            val successful = withContext(Dispatchers.IO) {
                val application = view.context.applicationContext as AdAwayApplication
                val sourceModel = application.sourceModel
                val adBlockModel = application.adBlockModel
                try {
                    if (syncSources) {
                        sourceModel.retrieveHostsSources()
                    } else {
                        sourceModel.syncHostEntries()
                    }
                    // Applying a configuration change must not silently re-enable ad blocking
                    // after the user explicitly turned it off.
                    adBlockModel.applyIfActive()
                    true
                } catch (_: HostErrorException) {
                    false
                }
            }
            endLoading(successful)
        }
    }

    private fun showLoading() {
        notifySnackbar.dismiss()
        waitSnackbar.show()
    }

    private fun endLoading(successfulInstall: Boolean) {
        waitSnackbar.dismiss()
        if (!successfulInstall) {
            val failureSnackbar = Snackbar.make(view, R.string.notification_configuration_failed, LENGTH_LONG)
            ExpressiveSnackbar.style(failureSnackbar)
            val icon = ImageView(view.context).apply {
                setImageResource(R.drawable.ic_error_outline_24dp)
            }
            appendViewToSnackbar(failureSnackbar, icon)
            failureSnackbar.show()
        } else if (update) {
            if (ignoreEventDuringInstall) {
                skipUpdate = true
            } else {
                notifyUpdateAvailable()
            }
        }
    }

    private fun appendViewToSnackbar(snackbar: Snackbar, view: View) {
        val viewGroup = snackbar.view
            .findViewById<View>(com.google.android.material.R.id.snackbar_text)
            .parent as ViewGroup
        viewGroup.addView(view)
    }
}
