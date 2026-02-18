package pl.edu.ur.db131403.hi_english.ui.dictionary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.ur.db131403.hi_english.data.model.WordEntity

@Composable
fun EditWordDialog(
    word: WordEntity,
    onDismiss: () -> Unit,
    onConfirm: (WordEntity) -> Unit,
    onDelete: (WordEntity) -> Unit
) {
    var editedWord by remember { mutableStateOf(word.word) }
    var editedTranslation by remember { mutableStateOf(word.translationPl ?: "") }
    var editedDescription by remember { mutableStateOf(word.description ?: "") }

    var editedCefr by remember { mutableStateOf(word.cefr ?: "A1") }
    var selectedPos by remember {
        mutableStateOf(word.pos?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet() ?: emptySet())
    }

    val cefrLevels = listOf("A1", "A2", "B1", "B2", "C1", "C2")
    val posOptions = listOf(
        "rzeczownik",
        "czasownik",
        "czasownik (być)",
        "czasownik (mieć)",
        "czasownik (posiłkowy)",
        "czasownik modalny",
        "przymiotnik",
        "przysłówek",
        "zaimek",
        "przyimek",
        "spójnik",
        "określnik",
        "liczebnik",
        "partykuła (to)",
        "wykrzyknik"
    )

    LaunchedEffect(word) {
        editedWord = word.word
        editedTranslation = word.translationPl ?: ""
        editedDescription = word.description ?: ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj słowo") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = editedWord, onValueChange = { editedWord = it }, label = { Text("Słowo (EN)") })
                OutlinedTextField(value = editedTranslation, onValueChange = { editedTranslation = it }, label = { Text("Tłumaczenie (PL)") })

                Text("Poziom CEFR", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    cefrLevels.forEach { level ->
                        FilterChip(
                            selected = editedCefr == level,
                            onClick = { editedCefr = level },
                            label = { Text(level) }
                        )
                    }
                }

                Text("Części mowy", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    posOptions.forEach { pos ->
                        val isSelected = selectedPos.contains(pos)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPos = if (isSelected) selectedPos - pos else selectedPos + pos
                            },
                            label = { Text(pos) }
                        )
                    }
                }

                OutlinedTextField(value = editedDescription, onValueChange = { editedDescription = it }, label = { Text("Opis/Przykład") }, modifier = Modifier.height(100.dp))
            }
        },
        confirmButton = {
            Button(
                enabled = editedWord.isNotBlank() && editedTranslation.isNotBlank(),
                onClick = {
                onConfirm(word.copy(
                    word = editedWord,
                    translationPl = editedTranslation,
                    cefr = editedCefr,
                    pos = selectedPos.joinToString(", "),
                    description = editedDescription
                ))
                onDismiss()
            }) { Text("Zapisz") }
        },
        dismissButton = {
            if (word.id != null) { // Ukryj "Usuń" jeśli dodajemy nowe słowo
                TextButton(
                    onClick = {
                        onDelete(word)
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Usuń")
                }
            }

            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}