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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.edu.ur.db131403.hi_english.data.repository.ProfileRepository
import pl.edu.ur.db131403.hi_english.data.repository.WordRepository


// Define the data structure matching your React 'games' array
data class GameData(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val color: Color,
    val difficulty: String
)

// The mock data list from LearningModule.tsx
val games = listOf(
    GameData(
        id = "memory",
        title = "Pamięć: Zwierzęta",
        description = "Dopasuj pary angielskich słówek do obrazków zwierząt.",
        imageUrl = "https://images.unsplash.com/photo-1544391443-177047971b94?w=200&h=200&fit=crop",
        color = Color(0xFFD0E4FF),
        difficulty = "Łatwy"
    ),
    GameData(
        id = "match",
        title = "Dopasuj: Kolory",
        description = "Przeciągnij słowo do odpowiedniej plamy koloru.",
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
fun StatChip(icon: ImageVector, text: String) {
    Surface(
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
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
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun LearningMenuContent(onGameClick: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // We can add a "Daily Challenge" or "Recently Played" here to add bulk
        item {
            DailyProgressCard()
        }

        item {
            Text(
                text = "Kategorie Nauki",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(games) { game ->
            GameCard(game = game, onClick = { onGameClick(game.id) })
        }
    }
}

@Composable
fun LearningModule(
    wordRepository: WordRepository,
    profileRepository: ProfileRepository,
    onAddPoints: (Int) -> Unit
) {
    // Teraz tworzymy ViewModel TYLKO TUTAJ i z użyciem Fabryki
    val viewModel: LearningViewModel = viewModel(
        factory = LearningViewModelFactory(wordRepository, profileRepository)
    )

    val progress = viewModel.tasksCompletedInSession / 20f

    var selectedGameId by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = selectedGameId != null) {
        selectedGameId = null
    }

    if (selectedGameId == null) {
        DashboardLayout(onGameClick = { id -> selectedGameId = id })
    } else {
        GameTheater(
            gameId = selectedGameId!!,
            onExit = { selectedGameId = null },
            viewModel = viewModel
        )
    }
}

@Composable
fun GameCard(game: GameData, onClick: () -> Unit) { // Dodany parametr onClick
    Surface(
        onClick = onClick, // Przekazujemy onClick tutaj
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Game Icon Container - uses Secondary Container for contrast
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // You can still use the game-specific color as a tint if desired
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant // System muted text
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
fun DashboardLayout(onGameClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Witaj, Odkrywco!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Wybierz grę, aby zacząć naukę.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(24.dp))

        // Wywołujemy funkcję, którą przed chwilą stworzyliśmy
        DailyProgressCard()

        Spacer(modifier = Modifier.height(32.dp))

        // Nagłówek listy gier
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Twoje Gry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Pokaż wszystko", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(games) { game ->
                // Teraz GameCard przyjmuje onClick!
                GameCard(game = game, onClick = { onGameClick(game.id) })
            }
        }
    }
}

@Composable
fun GameTheater(
    gameId: String,
    onExit: () -> Unit,
    viewModel: LearningViewModel
) {
    // Obserwujemy stan gry z ViewModelu
    val gameState by viewModel.currentGameState

    // Uruchamiamy ładowanie danych przy wejściu do ekranu
    LaunchedEffect(gameId) {
        when (gameId) {
            "match" -> viewModel.startMatchGame()
            "spell" -> viewModel.startSpellGame() // Musisz dodać tę funkcję w VM
            // "memory" -> viewModel.startPictureQuiz()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Górny pasek z przyciskiem powrotu
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Wstecz")
            }
            Text(text = "Nauka: $gameId", modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val state = gameState) {
                is GameState.MatchingGame -> {
                    MatchingGameScreen(state = state, onMatchAttempt = { left, right ->
                        // Tutaj wywołaj logikę sprawdzania pary w ViewModelu
                    })
                }
                is GameState.ScrambledLetters -> {
                    // W GameTheater wywołujesz to tak:
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
fun DailyProgressCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("POSTĘP DNIA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Ukończono 3/5 gier", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { 0.6f }, // W nowszych wersjach Material3 używamy lambdy
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatChip(icon = Icons.Outlined.Schedule, text = "15 min")
                StatChip(icon = Icons.Default.CheckCircle, text = "30 słówek")
            }
        }
    }
}