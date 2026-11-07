package org.adaway.helper

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import org.adaway.R
import org.adaway.model.adblocking.AdBlockMethod
import org.adaway.util.Constants
import org.adaway.vpn.VpnStatus

object PreferenceHelper {
    @JvmStatic
    fun getDarkThemeMode(context: Context): Int {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return when (
            prefs.getString(
                context.getString(R.string.pref_dark_theme_mode_key),
                context.getString(R.string.pref_dark_theme_mode_def)
            )
        ) {
            "MODE_NIGHT_NO" -> AppCompatDelegate.MODE_NIGHT_NO
            "MODE_NIGHT_YES" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }

    @JvmStatic
    fun setDarkThemeMode(context: Context, mode: String) {
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(context.getString(R.string.pref_dark_theme_mode_key), mode)
            .apply()
    }

    @JvmStatic
    fun getDynamicColorEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(
            context.getString(R.string.pref_dynamic_color_key),
            context.resources.getBoolean(R.bool.pref_dynamic_color_def)
        )
    }

    @JvmStatic
    fun getUpdateCheck(context: Context): Boolean {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(
            context.getString(R.string.pref_update_check_key),
            context.resources.getBoolean(R.bool.pref_update_check_def)
        )
    }

    @JvmStatic
    fun getNeverReboot(context: Context): Boolean {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(
            context.getString(R.string.pref_never_reboot_key),
            context.resources.getBoolean(R.bool.pref_never_reboot_def)
        )
    }

    @JvmStatic
    fun setNeverReboot(context: Context, value: Boolean) {
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(context.getString(R.string.pref_never_reboot_key), value)
            .apply()
    }

    @JvmStatic
    fun getEnableIpv6(context: Context): Boolean {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(
            context.getString(R.string.pref_enable_ipv6_key),
            context.resources.getBoolean(R.bool.pref_enable_ipv6_def)
        )
    }

    @JvmStatic
    fun getUpdateCheckAppStartup(context: Context): Boolean {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(
            context.getString(R.string.pref_update_check_app_startup_key),
            context.resources.getBoolean(R.bool.pref_update_check_app_startup_def)
        )
    }

    @JvmStatic
    fun getUpdateCheckAppDaily(context: Context): Boolean {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(
            context.getString(R.string.pref_update_check_app_daily_key),
            context.resources.getBoolean(R.bool.pref_update_check_app_daily_def)
        )
    }

    @JvmStatic
    fun getIncludeBetaReleases(context: Context): Boolean {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(
            context.getString(R.string.pref_update_include_beta_releases_key),
            context.resources.getBoolean(R.bool.pref_update_include_beta_releases_def)
        )
    }

    @JvmStatic
    fun getUpdateCheckHostsDaily(context: Context): Boolean {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(
            context.getString(R.string.pref_update_check_hosts_daily_key),
            context.resources.getBoolean(R.bool.pref_update_check_hosts_daily_def)
        )
    }

    @JvmStatic
    fun getAutomaticUpdateDaily(context: Context): Boolean {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(
            context.getString(R.string.pref_automatic_update_daily_key),
            context.resources.getBoolean(R.bool.pref_automatic_update_daily_def)
        )
    }

    @JvmStatic
    fun getUpdateOnlyOnWifi(context: Context): Boolean {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(
            context.getString(R.string.pref_update_only_on_wifi_key),
            context.resources.getBoolean(R.bool.pref_update_only_on_wifi_def)
        )
    }

    @JvmStatic
    fun getRedirectionIpv4(context: Context): String {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(
            context.getString(R.string.pref_redirection_ipv4_key),
            context.getString(R.string.pref_redirection_ipv4_def)
        ).orEmpty()
    }

    @JvmStatic
    fun getRedirectionIpv6(context: Context): String {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(
            context.getString(R.string.pref_redirection_ipv6_key),
            context.getString(R.string.pref_redirection_ipv6_def)
        ).orEmpty()
    }

    @JvmStatic
    fun getWebServerEnabled(context: Context): Boolean {
        val prefs = context.applicationContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(
            context.getString(R.string.pref_webserver_enabled_key),
            context.resources.getBoolean(R.bool.pref_webserver_enabled_def)
        )
    }

    @JvmStatic
    fun getWebServerIcon(context: Context): Boolean {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(
            context.getString(R.string.pref_webserver_icon_key),
            context.resources.getBoolean(R.bool.pref_webserver_icon_def)
        )
    }

    @JvmStatic
    fun getAdBlockMethod(context: Context): AdBlockMethod {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return AdBlockMethod.fromCode(
            prefs.getInt(
                context.getString(R.string.pref_ad_block_method_key),
                context.resources.getInteger(R.integer.pref_ad_block_method_key_def)
            )
        )
    }

    @JvmStatic
    fun setAbBlockMethod(context: Context, method: AdBlockMethod) {
        context.applicationContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(context.getString(R.string.pref_ad_block_method_key), method.toCode())
            .apply()
    }

    @JvmStatic
    fun getVpnServiceStatus(context: Context): VpnStatus {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return VpnStatus.fromCode(
            prefs.getInt(
                context.getString(R.string.pref_vpn_service_status_key),
                context.resources.getInteger(R.integer.pref_vpn_service_status_def)
            )
        )
    }

    @JvmStatic
    fun setVpnServiceStatus(context: Context, status: VpnStatus) {
        context.applicationContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(context.getString(R.string.pref_vpn_service_status_key), status.toCode())
            .apply()
    }

    /**
     * Get whether the user has explicitly enabled the VPN (user intent).
     *
     * This is distinct from the runtime service status: it tracks what the user has asked
     * for, not whether the Android service is currently running. Background paths (host
     * updates, connection monitor, sticky resurrection) must use this signal, never the
     * runtime status, to decide whether to (re)start the VPN.
     *
     * When unset (existing installs / first read), this falls back to the persisted runtime
     * status to avoid breaking users who already had the VPN running.
     */
    @JvmStatic
    fun getVpnServiceUserEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val key = context.getString(R.string.pref_vpn_service_user_enabled_key)
        if (prefs.contains(key)) {
            return prefs.getBoolean(key, false)
        }
        // Migration fallback: derive from existing service status pref.
        return getVpnServiceStatus(context).isStarted
    }

    @JvmStatic
    fun setVpnServiceUserEnabled(context: Context, userEnabled: Boolean) {
        context.applicationContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(context.getString(R.string.pref_vpn_service_user_enabled_key), userEnabled)
            .apply()
    }

    @JvmStatic
    fun getVpnServiceOnBoot(context: Context): Boolean {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(
            context.getString(R.string.pref_vpn_service_on_boot_key),
            context.resources.getBoolean(R.bool.pref_vpn_service_on_boot_def)
        )
    }

    @JvmStatic
    fun getVpnWatchdogEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(
            context.getString(R.string.pref_vpn_watchdog_enabled_key),
            context.resources.getBoolean(R.bool.pref_vpn_watchdog_enabled_def)
        )
    }

    /**
     * Whether the diagnostic log is allowed to record events. Off by default: the user must
     * explicitly opt in (Preferences > VPN > Record diagnostic log) before anything is captured.
     */
    @JvmStatic
    fun getVpnDiagnosticLogEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(
            context.getString(R.string.pref_vpn_diagnostic_log_enabled_key),
            context.resources.getBoolean(R.bool.pref_vpn_diagnostic_log_enabled_def)
        )
    }

    /**
     * Whether the TV first-run wizard has already been handled for this install, either by
     * actually completing it or by having been detected as an existing install that already
     * had ad-blocking working before the wizard existed.
     */
    @JvmStatic
    fun isTvWelcomeDone(context: Context): Boolean {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(
            context.getString(R.string.pref_tv_welcome_done_key),
            context.resources.getBoolean(R.bool.pref_tv_welcome_done_def)
        )
    }

    @JvmStatic
    fun setTvWelcomeDone(context: Context, done: Boolean) {
        context.applicationContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(context.getString(R.string.pref_tv_welcome_done_key), done)
            .apply()
    }

    @JvmStatic
    fun getDebugEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(
            context.getString(R.string.pref_enable_debug_key),
            context.resources.getBoolean(R.bool.pref_enable_debug_def)
        )
    }

    @JvmStatic
    fun getVpnExcludedSystemApps(context: Context): String {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(
            context.getString(R.string.pref_vpn_excluded_system_apps_key),
            context.getString(R.string.pref_vpn_excluded_system_apps_default)
        ).orEmpty()
    }

    @JvmStatic
    fun getVpnExcludedApps(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(
            context.getString(R.string.pref_vpn_excluded_user_apps_key),
            emptySet()
        ).orEmpty()
    }

    @JvmStatic
    fun setVpnExcludedApps(context: Context, excludedApplicationPackageNames: Set<String>) {
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(context.getString(R.string.pref_vpn_excluded_user_apps_key), excludedApplicationPackageNames)
            .apply()
    }
}
