package org.adaway.ui.hosts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.adaway.db.AppDatabase
import org.adaway.db.dao.HostsSourceDao
import org.adaway.db.entity.HostsSource

class HostsSourcesViewModel(application: Application) : AndroidViewModel(application) {
    private val hostsSourceDao: HostsSourceDao = AppDatabase.getInstance(application).hostsSourceDao()

    val hostsSources: StateFlow<List<HostsSource>> = hostsSourceDao.loadAll()
        .asFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(FLOW_STOP_TIMEOUT_MILLIS), emptyList())

    /**
     * Set on a real user edit only (toggle, or add/edit/delete via [notifyModelChanged]),
     * unlike [hostsSources]: that one is the raw, reactive query result, which re-emits on any
     * write to the table, background syncs included. Driving an "Apply" prompt off it would
     * show the prompt (and, on TV, leave the header button stuck) after a sync the user never
     * asked to apply.
     */
    private val modelChanged = MutableLiveData(false)

    // LiveData bridges for the Android TV sources screen, which still uses the View system.
    fun observeHostsSources(): LiveData<List<HostsSource>> = hostsSources.asLiveData()

    fun observeModelChanged(): LiveData<Boolean> = modelChanged

    /**
     * Mark a change as pending. For an edit made outside this ViewModel: adding, editing or
     * deleting a source goes through the source edit activities directly, not through here, so
     * the screen that launched one calls this once it reports back that a save or delete
     * actually happened.
     */
    fun notifyModelChanged() {
        modelChanged.postValue(true)
    }

    fun toggleSourceEnabled(source: HostsSource) {
        viewModelScope.launch(Dispatchers.IO) {
            hostsSourceDao.toggleEnabled(source)
            modelChanged.postValue(true)
        }
    }

    companion object {
        private const val FLOW_STOP_TIMEOUT_MILLIS = 5_000L
    }
}
