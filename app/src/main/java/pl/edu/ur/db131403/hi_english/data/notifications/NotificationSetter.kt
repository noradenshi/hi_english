package pl.edu.ur.db131403.hi_english.data.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import pl.edu.ur.db131403.hi_english.data.repository.ProfileRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSetter(profileRepository: ProfileRepository) {
    var showTimePicker by remember { mutableStateOf(false) }

    val currentTime by profileRepository.notificationTime.collectAsState(initial = "14:00")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Godzina przypomnienia")
        Button(onClick = { showTimePicker = true }) {
            Text("Zmień")
        }
    }

    if (showTimePicker) {
        val parts = currentTime.split(":")
        val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 14
        val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val state = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    profileRepository.scheduleDailyNotification(state.hour, state.minute)
                    showTimePicker = false
                }) { Text("Zapisz") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Anuluj") }
            },
            text = { TimePicker(state = state) }
        )
    }
}