package pl.edu.ur.db131403.hi_english.data.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.core.content.edit
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit
import pl.edu.ur.db131403.hi_english.data.notifications.NotificationWorker

class ProfileRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)

    private val _pointsFlow = MutableStateFlow(prefs.getInt("points", 0))
    val userPoints: Flow<Int> = _pointsFlow

    private val _totalPointsEverFlow = MutableStateFlow(prefs.getInt("total_points_ever", 0))
    val totalPointsEver: Flow<Int> = _totalPointsEverFlow

    private val _gamesCompletedFlow = MutableStateFlow(prefs.getInt("games_today", 0))
    val gamesCompletedToday: Flow<Int> = _gamesCompletedFlow

    private val GAMES_GOAL = 5

    val totalExercises = MutableStateFlow(prefs.getInt("total_exercises", 0))
    val dailyMinutes = MutableStateFlow(prefs.getInt("daily_minutes", 0))

    private val _isRootAuthenticated = MutableStateFlow(prefs.getBoolean("is_root_authenticated", false))
    val isRootAuthenticated: Flow<Boolean> = _isRootAuthenticated

    init {
        // Reset Root przy każdym uruchomieniu aplikacji
        setRootAuthenticated(false)
        checkAndResetDailyProgress()
    }

    fun addMinuteOfStudy() {
        val current = prefs.getInt("daily_minutes", 0)
        val newTotal = current + 1

        prefs.edit {
            putInt("daily_minutes", newTotal)
        }
        dailyMinutes.value = newTotal
    }

    fun getFirstExerciseDate(): String {
        var timestamp = prefs.getLong("first_exercise_date", 0L)

        // Jeśli nie ma daty (pierwsze uruchomienie), zapisz "teraz"
        if (timestamp == 0L) {
            timestamp = System.currentTimeMillis()
            prefs.edit { putLong("first_exercise_date", timestamp) }
        }

        val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    fun addPoints(amount: Int) {
        val currentWallet = prefs.getInt("points", 0)
        val currentLifetime = prefs.getInt("total_points_ever", 0)

        val newWallet = currentWallet + amount
        val newLifetime = if (amount > 0) currentLifetime + amount else currentLifetime

        prefs.edit {
            putInt("points", newWallet)
            putInt("total_points_ever", newLifetime)
        }

        _pointsFlow.value = newWallet
        _totalPointsEverFlow.value = newLifetime
    }

    fun subtractPoints(amount: Int) {
        addPoints(-amount)
    }

    fun getCurrentPoints(): Int {
        return prefs.getInt("points", 0)
    }

    fun getTotalPointsEver(): Int {
        return prefs.getInt("total_points_ever", 0)
    }

    fun incrementGamesCompleted() {
        val currentGames = prefs.getInt("games_today", 0) + 1
        val currentTotalEx = prefs.getInt("total_exercises", 0) + 1

        prefs.edit {
            putInt("games_today", currentGames)
            putInt("total_exercises", currentTotalEx)
            putLong("last_exercise_timestamp", System.currentTimeMillis())
            if (!prefs.contains("first_exercise_date")) {
                putLong("first_exercise_date", System.currentTimeMillis())
            }
        }

        _gamesCompletedFlow.value = currentGames
        totalExercises.value = currentTotalEx

        if (currentGames == GAMES_GOAL) {
            addPoints(20)
        }
    }

    init {
        checkAndResetDailyProgress()
    }

    fun checkAndResetDailyProgress() {
        val lastUpdate = prefs.getLong("last_daily_reset_millis", 0L)
        val now = Calendar.getInstance()
        val resetBoundary = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        if (now.before(resetBoundary)) resetBoundary.add(Calendar.DAY_OF_YEAR, -1)

        if (lastUpdate < resetBoundary.timeInMillis) {
            prefs.edit {
                putInt("games_today", 0)
                putInt("daily_minutes", 0)
                putLong("last_daily_reset_millis", System.currentTimeMillis())
            }
            _gamesCompletedFlow.value = 0
            dailyMinutes.value = 0
        }
    }

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_enabled", false))
    val notificationsEnabled: Flow<Boolean> = _notificationsEnabled

    private val _notificationTime = MutableStateFlow(prefs.getString("notification_time", "18:00") ?: "18:00")
    val notificationTime: Flow<String> = _notificationTime

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("notifications_enabled", enabled) }
        _notificationsEnabled.value = enabled

        if (enabled) {
            // Pobierz ostatnio zapisaną godzinę lub ustaw domyślną
            val time = _notificationTime.value.split(":")
            scheduleDailyNotification(time[0].toInt(), time[1].toInt())
        } else {
            WorkManager.getInstance(context).cancelUniqueWork("daily_reminder")
        }
    }

    fun scheduleDailyNotification(hour: Int, minute: Int) {
        val timeString = String.format("%02d:%02d", hour, minute)
        prefs.edit {
            putString("notification_time", timeString)
        }
        _notificationTime.value = timeString

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val delay = calendar.timeInMillis - System.currentTimeMillis()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag("daily_reminder_task")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.REPLACE, // UPDATE zamiast KEEP, by odświeżyć godzinę
            dailyWorkRequest
        )
    }

    fun updateRootPassword(newPass: String) {
        prefs.edit { putString("root_password", newPass) }
    }

    fun getRootPassword(): String {
        return prefs.getString("root_password", "root123") ?: "root123"
    }

    fun setRootAuthenticated(authenticated: Boolean) {
        prefs.edit {
            putBoolean("is_root_authenticated", authenticated)
        }
        _isRootAuthenticated.value = authenticated
    }

    fun checkRootPassword(password: String): Boolean {
        val isValid = password == getRootPassword()
        if (isValid) {
            setRootAuthenticated(true)
        }
        return isValid
    }

    fun logoutRoot() {
        setRootAuthenticated(false)
    }

    fun debugUpdatePoints(newAmount: Int) {
        prefs.edit { putInt("points", newAmount) }
        _pointsFlow.value = newAmount
    }

    fun debugUpdateTotalPointsEver(newAmount: Int) {
        prefs.edit { putInt("total_points_ever", newAmount) }
        _totalPointsEverFlow.value = newAmount
    }

    fun debugUpdateGamesCompleted(newAmount: Int) {
        prefs.edit { putInt("games_today", newAmount) }
        _gamesCompletedFlow.value = newAmount
    }

    fun getGamesCompletedToday(): Int = prefs.getInt("games_today", 0)

    fun getAllPrefs(): Map<String, *> {
        return prefs.all
    }

    fun clearAllPrefs() {
        prefs.edit { clear() }
    }
}