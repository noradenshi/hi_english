package pl.edu.ur.db131403.hi_english.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import pl.edu.ur.db131403.hi_english.data.local.StoreDao
import pl.edu.ur.db131403.hi_english.data.notifications.NotificationSetter
import pl.edu.ur.db131403.hi_english.data.repository.ProfileRepository

@Composable
fun SettingsScreen(
    profileRepository: ProfileRepository,
    storeDao: StoreDao
) {
    val totalExercises by profileRepository.totalExercises.collectAsState()
    val dailyMinutes by profileRepository.dailyMinutes.collectAsState()
    val totalPoints by profileRepository.totalPointsEver.collectAsState(initial = 0)
    val purchasedItemsCount by storeDao.getPurchasedItemsCount().collectAsState(initial = 0)

    // Stany powiadomień
    val notificationsEnabled by profileRepository.notificationsEnabled.collectAsState(initial = false)
    val notificationTime by profileRepository.notificationTime.collectAsState(initial = "18:00")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {

        Text(
            "STATYSTYKI",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        StatsGrid(
            exercises = totalExercises,
            points = totalPoints,
            items = purchasedItemsCount,
            lastSession = dailyMinutes,
            startDate = profileRepository.getFirstExerciseDate()
        )

        Spacer(Modifier.height(24.dp))
        Text(
            "POWIADOMIENIA",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Przypomnienie o nauce", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = if (notificationsEnabled) "Ustawione na: $notificationTime" else "Powiadomienia są wyłączone",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (notificationsEnabled) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray
                )
            }
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { profileRepository.setNotificationsEnabled(it) }
            )
        }

        if (notificationsEnabled) {
            NotificationSetter(profileRepository)
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "ADMINISTRACJA",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        RootSection(profileRepository)
    }
}

@Composable
fun StatsGrid(
    exercises: Int,
    points: Int,
    items: Int,
    lastSession: Int,
    startDate: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Ćwiczenia", exercises.toString(), Icons.Outlined.HistoryEdu, Modifier.weight(1f))
            StatCard("Zdobyte Punkty", points.toString(), Icons.Outlined.MonetizationOn, Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Przedmioty", items.toString(), Icons.Outlined.ShoppingBag, Modifier.weight(1f))
            StatCard("Sesja dzisiaj", "$lastSession min", Icons.Outlined.Timer, Modifier.weight(1f))
        }
        StatCard("Rozpoczęcie nauki", startDate, Icons.Outlined.CalendarToday, Modifier.fillMaxWidth())
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RootSection(profileRepository: ProfileRepository) {
    var passwordInput by remember { mutableStateOf("") }
    var isLoggedIn by remember { mutableStateOf(false) }

    var newPass by remember { mutableStateOf("") }
    var repeatPass by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val allPrefs = remember(isLoggedIn) { profileRepository.getAllPrefs() }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("ROOT ACCESS", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.ExtraBold)

            if (!isLoggedIn) {
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        if (it == profileRepository.getRootPassword()) isLoggedIn = true
                    },
                    label = { Text("Enter Root Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else {
                Text("System Preferences Debugger", style = MaterialTheme.typography.titleSmall)

                Surface(
                    color = Color.Black.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp).verticalScroll(rememberScrollState())) {
                        allPrefs.forEach { (key, value) ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                Text(key, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), color = Color.Gray)
                                Text(value.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Button(
                    onClick = { profileRepository.clearAllPrefs() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("NUKE ALL PREFS")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Zmień hasło Root", style = MaterialTheme.typography.titleSmall)

                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("Nowe hasło") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = repeatPass,
                    onValueChange = { repeatPass = it },
                    label = { Text("Powtórz hasło") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = repeatPass.isNotEmpty() && newPass != repeatPass
                )

                if (message.isNotEmpty()) {
                    Text(message, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = {
                        if (newPass == repeatPass && newPass.isNotEmpty()) {
                            profileRepository.updateRootPassword(newPass)
                            message = "Hasło zmienione!"
                            newPass = ""
                            repeatPass = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    enabled = newPass == repeatPass && newPass.isNotEmpty()
                ) {
                    Text("Zapisz hasło")
                }
            }
        }
    }
}