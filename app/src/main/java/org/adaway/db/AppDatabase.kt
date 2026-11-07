package org.adaway.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import org.adaway.R
import org.adaway.db.Migrations.MIGRATION_1_2
import org.adaway.db.Migrations.MIGRATION_2_3
import org.adaway.db.Migrations.MIGRATION_3_4
import org.adaway.db.Migrations.MIGRATION_4_5
import org.adaway.db.Migrations.MIGRATION_5_6
import org.adaway.db.Migrations.MIGRATION_6_7
import org.adaway.db.converter.ListTypeConverter
import org.adaway.db.converter.ZonedDateTimeConverter
import org.adaway.db.dao.HostEntryDao
import org.adaway.db.dao.HostListItemDao
import org.adaway.db.dao.HostsSourceDao
import org.adaway.db.entity.HostEntry
import org.adaway.db.entity.HostListItem
import org.adaway.db.entity.HostsSource
import org.adaway.db.entity.HostsSource.USER_SOURCE_ID
import org.adaway.db.entity.HostsSource.USER_SOURCE_URL
import org.adaway.util.CoroutineDispatchers
import timber.log.Timber
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask

@Database(entities = [HostsSource::class, HostListItem::class, HostEntry::class], version = 7)
@TypeConverters(ListTypeConverter::class, ZonedDateTimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hostsSourceDao(): HostsSourceDao

    abstract fun hostsListItemDao(): HostListItemDao

    abstract fun hostEntryDao(): HostEntryDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /**
         * Set from [Callback.onCreate], on a fresh install only. See [awaitInitialization].
         */
        @Volatile
        private var initializationTask: FutureTask<Unit>? = null

        @JvmStatic
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(AppDatabase::class.java) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app.db"
                )
                    .addCallback(
                        object : Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                val task = FutureTask<Unit> {
                                    initialize(context, requireNotNull(instance))
                                }
                                initializationTask = task
                                CoroutineDispatchers.ioExecutor().execute(task)
                            }
                        }
                    )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7
                    )
                    .build()
                    .also { instance = it }
            }
        }

        /**
         * Block the calling thread until the default hosts sources seeded by [Callback.onCreate]
         * have actually landed, if that seeding is still in flight.
         *
         * On a fresh install, the app's very first real query is what makes Room open the
         * database file and fire `onCreate` - on whichever thread issued that query. `onCreate`
         * itself only hands the insert off to a separate background executor and returns
         * immediately, so without this, that same first caller could read the hosts source table
         * before its own seeding had landed, see zero rows, and (for a sync) wrongly conclude
         * there was nothing to do - exactly what a fresh Android TV install's first sync did.
         *
         * [getWritableDatabase] forces that same lazy open to happen right here if it has not
         * happened yet, so [initializationTask] is guaranteed to already be set (on a fresh
         * install) by the time it is read below, instead of this method racing that open itself.
         * A cheap no-op on every call after the first: the open is cached by Room, and
         * [FutureTask.get] on an already-finished task returns immediately.
         */
        @JvmStatic
        fun awaitInitialization(context: Context) {
            getInstance(context).openHelper.writableDatabase
            val task = initializationTask ?: return
            try {
                task.get()
            } catch (e: ExecutionException) {
                Timber.w(e, "Database initialization failed.")
            } catch (e: InterruptedException) {
                Timber.w(e, "Interrupted while waiting for database initialization.")
                Thread.currentThread().interrupt()
            }
        }

        private fun initialize(context: Context, database: AppDatabase) {
            val hostsSourceDao = database.hostsSourceDao()
            if (hostsSourceDao.all.isNotEmpty()) {
                return
            }

            val userSource = HostsSource().apply {
                label = context.getString(R.string.hosts_user_source)
                id = USER_SOURCE_ID
                url = USER_SOURCE_URL
                setAllowEnabled(true)
                setRedirectEnabled(true)
            }
            hostsSourceDao.insert(userSource)

            hostsSourceDao.insert(
                HostsSource().apply {
                    label = context.getString(R.string.hosts_adaway_source)
                    url = "https://adaway.org/hosts.txt"
                }
            )
            hostsSourceDao.insert(
                HostsSource().apply {
                    label = context.getString(R.string.hosts_stevenblack_source)
                    url = "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts"
                }
            )
            hostsSourceDao.insert(
                HostsSource().apply {
                    label = context.getString(R.string.hosts_peterlowe_source)
                    url = "https://pgl.yoyo.org/adservers/serverlist.php?hostformat=hosts&showintro=0&mimetype=plaintext"
                }
            )
        }
    }
}
