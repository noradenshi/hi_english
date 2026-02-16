package pl.edu.ur.db131403.hi_english.ui.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun MatchingGameScreen(
    state: GameState.MatchingGame,
    onMatchAttempt: (String, String) -> Unit
) {
    var selectedLeft by remember { mutableStateOf<String?>(null) }
    var selectedRight by remember { mutableStateOf<String?>(null) }

    Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Angielskie słowa
        Column(modifier = Modifier.weight(1f)) {
            state.pairs.keys.forEach { word ->
                val isMatched = state.matchedKeys.contains(word)
                GameButton(
                    text = word,
                    isSelected = selectedLeft == word,
                    isMatched = isMatched,
                    onClick = { selectedLeft = word }
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Polskie tłumaczenia (pomieszane)
        val shuffledTranslations = remember(state.pairs) { state.pairs.values.shuffled() }
        Column(modifier = Modifier.weight(1f)) {
            shuffledTranslations.forEach { translation ->
                val isMatched = state.matchedKeys.any { state.pairs[it] == translation }
                GameButton(
                    text = translation,
                    isSelected = selectedRight == translation,
                    isMatched = isMatched,
                    onClick = { selectedRight = translation }
                )
            }
        }
    }

    // Automatyczna próba dopasowania po wybraniu obu stron
    LaunchedEffect(selectedLeft, selectedRight) {
        if (selectedLeft != null && selectedRight != null) {
            onMatchAttempt(selectedLeft!!, selectedRight!!)
            selectedLeft = null
            selectedRight = null
        }
    }
}

@Composable
fun GameButton(
    text: String,
    isSelected: Boolean,
    isMatched: Boolean,
    onClick: () -> Unit
) {
    // Dynamiczna zmiana kolorów w zależności od stanu
    val backgroundColor = when {
        isMatched -> Color(0xFF4CAF50).copy(alpha = 0.2f) // Zielony dla dopasowanych
        isSelected -> MaterialTheme.colorScheme.primaryContainer // Kolor zaznaczenia
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isMatched -> Color(0xFF4CAF50)
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val textColor = when {
        isMatched -> Color(0xFF2E7D32)
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(56.dp)
            .clickable(enabled = !isMatched) { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected || isMatched) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}