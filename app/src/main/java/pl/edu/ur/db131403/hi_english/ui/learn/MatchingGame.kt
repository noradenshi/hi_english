package pl.edu.ur.db131403.hi_english.ui.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun MatchingGameScreen(
    state: GameState.MatchingGame,
    onMatchAttempt: (String, Boolean) -> Unit
) {
    val englishWords = remember(state.pairs) { state.pairs.keys.shuffled() }
    val polishWords = remember(state.pairs) { state.pairs.values.shuffled() }

    Row(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Kolumna Angielska
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            englishWords.forEach { word ->
                MatchCard(
                    text = word,
                    isSelected = state.selectedLeft == word,
                    isMatched = state.matchedKeys.contains(word),
                    onClick = { onMatchAttempt(word, true) }
                )
            }
        }

        // Kolumna Polska
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            polishWords.forEach { word ->
                val isMatched = state.matchedKeys.any { state.pairs[it] == word }
                MatchCard(
                    text = word,
                    isSelected = state.selectedRight == word,
                    isMatched = isMatched,
                    onClick = { onMatchAttempt(word, false) }
                )
            }
        }
    }
}

@Composable
fun MatchCard(
    text: String,
    isSelected: Boolean,
    isMatched: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isMatched -> Color.Transparent // Ukrywamy obramowanie dla dopasowanych
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val backgroundColor = when {
        isMatched -> Color.LightGray.copy(alpha = 0.2f)
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isMatched) Color.Gray else MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = if (isMatched) ({}) else onClick,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = if (isSelected) 3.dp else 1.dp,
            color = borderColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}