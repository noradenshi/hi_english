package pl.edu.ur.db131403.hi_english.ui.learn

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight // Use this for Chevron
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.edu.ur.db131403.hi_english.data.repository.ProfileRepository
import pl.edu.ur.db131403.hi_english.data.repository.WordRepository

data class GameData(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val color: Color,
    val difficulty: String
)

val games = listOf(
//    GameData(
//        id = "memory",
//        title = "Pamięć: Zwierzęta",
//        description = "Dopasuj pary angielskich słówek do obrazków zwierząt.",
//        imageUrl = "https://images.unsplash.com/photo-1544391443-177047971b94?w=200&h=200&fit=crop",
//        color = Color(0xFFD0E4FF),
//        difficulty = "Łatwy"
//    ),
    GameData(
        id = "match",
        title = "Dopasuj Słowa",
        description = "Połącz w pary słowa z ich tłumaczeniami.",
        imageUrl = "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=200&h=200&fit=crop",
        color = Color(0xFFC4EED0),
        difficulty = "Łatwy"
    ),
    GameData(
        id = "spell",
        title = "Mistrz Pisowni",
        description = "Ułóż słowo z rozsypanych liter alfabetu.",
        imageUrl = "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=200&h=200&fit=crop",
        color = Color(0xFFFFDCC0),
        difficulty = "Średni"
    )
)

sealed class LearningScreen {
    object Menu : LearningScreen()
    data class Playing(val gameId: String) : LearningScreen()
}

@Composable
fun LearningModule(
    wordRepository: WordRepository,
    profileRepository: ProfileRepository
) {
    val viewModel: LearningViewModel = viewModel(
        factory = LearningViewModelFactory(wordRepository, profileRepository)
    )

    val completedGames by profileRepository.gamesCompletedToday.collectAsState(initial = 0)

    val progress = viewModel.tasksCompletedInSession / 20f

    val gameState by viewModel.currentGameState
    var selectedGameId by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = selectedGameId != null) {
        viewModel.clearSession()
        selectedGameId = null
    }

    LaunchedEffect(gameState) {
        if (gameState == null) {
            selectedGameId = null
        }
    }

    if (selectedGameId == null) {
        DashboardLayout(
            completedGames = completedGames,
            onGameClick = { id -> selectedGameId = id }
        )
    } else {
        DisposableEffect(selectedGameId) {
            onDispose {
                viewModel.clearSession()
            }
        }

        GameTheater(
            gameId = selectedGameId!!,
            onExit = {
                viewModel.clearSession()
            },
            viewModel = viewModel,
            profileRepository = profileRepository
        )
    }
}

@Composable
fun GameCard(game: GameData, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = game.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Difficulty Badge
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = CircleShape
                    ) {
                        Text(
                            text = game.difficulty.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
                Text(
                    text = game.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun DashboardLayout(onGameClick: (String) -> Unit, completedGames: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Witaj, Odkrywco!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Wybierz grę, aby zacząć naukę.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(24.dp))

        DailyProgressCard(completedGames)

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Dostępne Gry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
//            Text("Pokaż wszystko", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(games) { game ->
                GameCard(game = game, onClick = { onGameClick(game.id) })
            }
        }
    }
}

@Composable
fun GameTheater(
    gameId: String,
    onExit: () -> Unit,
    viewModel: LearningViewModel,
    profileRepository: ProfileRepository
) {
    val gameState by viewModel.currentGameState

    LaunchedEffect(gameId) {
        when (gameId) {
            "match" -> viewModel.startMatchGame()
            "spell" -> viewModel.startSpellGame()
            // "memory" -> viewModel.startPictureQuiz()
        }
    }

    LaunchedEffect(Unit) {
        while(true) {
            kotlinx.coroutines.delay(60_000) // 60 sekund
            profileRepository.addMinuteOfStudy()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LinearProgressIndicator(
            progress = { viewModel.tasksCompletedInSession / 20f },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Wstecz")
            }
            Text(
                text = "Zadanie ${viewModel.tasksCompletedInSession + 1} / 20",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val state = gameState) {
                is GameState.MatchingGame -> {
                    MatchingGameScreen(state = state, onMatchAttempt = { word, isEnglish ->
                        viewModel.onMatchSelected(word, isEnglish)
                    })
                }
                is GameState.ScrambledLetters -> {
                    ScrambledLettersScreen(
                        state = state,
                        onLetterClick = { slot -> viewModel.onLetterClicked(slot) },
                        onReset = { viewModel.resetSpellGame() }
                    )
                }
                null -> CircularProgressIndicator() // Ładowanie danych z bazy
                else -> Text("Gra w budowie...")
            }
        }
    }
}

@Composable
fun StatChip(icon: ImageVector, text: String, color: Color? = null) {
    Surface(
        color = (color ?: MaterialTheme.colorScheme.onPrimaryContainer).copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color ?: MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color ?: MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun DailyProgressCard(completedGames: Int) {
    val totalGoal = 5
    val progress = (completedGames.toFloat() / totalGoal).coerceIn(0f, 1f)
    val isGoalReached = completedGames >= totalGoal

    val themeColor = MaterialTheme.colorScheme.primary
    val containerColor = MaterialTheme.colorScheme.primaryContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = themeColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "POSTĘP DNIA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = themeColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isGoalReached) "Cel osiągnięty! 🎉" else "Ukończono $completedGames/$totalGoal gier",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatChip(icon = Icons.Outlined.Schedule, text = "15 min")

                StatChip(
                    icon = if (isGoalReached) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    text = if (isGoalReached) "+20 punktów" else "Zdobądź 20 pkt",
                    color = if (isGoalReached) themeColor else null
                )
            }
        }
    }
}