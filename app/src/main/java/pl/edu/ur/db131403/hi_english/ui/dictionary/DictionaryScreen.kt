package pl.edu.ur.db131403.hi_english.ui.dictionary

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import pl.edu.ur.db131403.hi_english.data.model.WordEntity
import pl.edu.ur.db131403.hi_english.data.repository.ProfileRepository
import pl.edu.ur.db131403.hi_english.data.repository.WordRepository

@OptIn(ExperimentalFoundationApi::class) // Required for stickyHeader
@Composable
fun DictionaryScreen(
    profileRepository: ProfileRepository,
    wordRepository: WordRepository
) {
    val viewModel: WordViewModel = viewModel(
        factory = WordViewModel.Factory(wordRepository)
    )

    val isRoot by profileRepository.isRootAuthenticated.collectAsState(initial = false)

    LaunchedEffect(isRoot) {
        viewModel.updateRootMode(isRoot)
    }

    val words by viewModel.words.collectAsState()
    val query by viewModel.searchQuery.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val alphabet = ('A'..'Z').toList()

    // Group words by their first letter for the headers
    val groupedWords = remember(words) {
        words.groupBy {wordEntity ->
            wordEntity.word.trim().firstOrNull()?.uppercaseChar() ?: '#'
        }
            .toSortedMap()
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var wordToEdit by remember { mutableStateOf<WordEntity?>(null) }
    var selectedWord by remember { mutableStateOf<WordEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            if (isRoot) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Dodaj słowo")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { viewModel.onSearchChange(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            placeholder = { Text("Search words...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            shape = RoundedCornerShape(12.dp)
                        )

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            groupedWords.forEach { (initial, wordsInGroup) ->
                                // THE INDICATOR: Sticky Header for each letter

                                stickyHeader {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = initial.toString(),
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp,
                                                vertical = 4.dp
                                            ),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                items(
                                    wordsInGroup,
                                    key = { it.id!! }
                                ) { word ->
                                    SwipeableWordItem(
                                        word = word,
                                        isRoot = isRoot,
                                        onEdit = { wordToEdit = it },
                                        onToggleVisibility = { viewModel.toggleVisibility(it) },
                                        onClick = { selectedWord = word }
                                    )
                                }
                            }
                        }
                    }

                    // Alphabet Slider
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        alphabet.forEach { letter ->
                            Text(
                                text = letter.toString(),
                                modifier = Modifier
                                    .clickable {
                                        val wordsInGroupsBefore =
                                            groupedWords.filterKeys { it < letter }.values.sumOf { it.size }
                                        val headersBefore = groupedWords.keys.count { it < letter }

                                        val totalScrollIndex = wordsInGroupsBefore + headersBefore

                                        if (groupedWords.containsKey(letter)) {
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(totalScrollIndex)
                                            }
                                        }
                                    }
                                    .padding(vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (words.any {
                                            it.word.startsWith(
                                                letter,
                                                ignoreCase = true
                                            )
                                        })
                                        MaterialTheme.colorScheme.primary
                                    else Color.Gray.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }

                    // Show the popup if a word is selected
                    selectedWord?.let { word ->
                        WordInfoDialog(
                            word = word,
                            onDismiss = { selectedWord = null }
                        )
                    }
                }

                selectedWord?.let {
                    WordInfoDialog(word = it, onDismiss = { selectedWord = null })
                }

                wordToEdit?.let {
                    EditWordDialog(
                        word = it,
                        onDismiss = { wordToEdit = null },
                        onConfirm = { updated -> viewModel.updateWord(updated) },
                        onDelete = { wordToDelete -> viewModel.deleteWord(wordToDelete) }
                    )
                }
            }
        }

        if (showAddDialog) {
            EditWordDialog(
                word = WordEntity(
                    id = null,
                    word = "",
                    pos = null,
                    cefr = "A1",
                    translationPl = "",
                    description = null,
                    imageRes = null,
                    isVisible = true
                ),
                onDismiss = { showAddDialog = false },
                onConfirm = { newWord ->
                    viewModel.insertWord(newWord)
                    showAddDialog = false
                },
                onDelete = { }
            )
        }
    }
}

@Composable
fun WordInfoDialog(word: WordEntity, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cool!") }
        },
        shape = RoundedCornerShape(24.dp),
        // Używamy 'text' jako głównego kontenera, aby mieć pełną kontrolę nad układem
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(), // To usuwa pustą przestrzeń po prawej
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ikona/Obrazek
                Icon(
                    imageVector = Icons.Outlined.AutoStories,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Słowo angielskie
                Text(
                    text = word.word.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // NOWOŚĆ: Dymki części mowy (POS)
                // Wykorzystujemy stworzony wcześniej komponent
                PosTagRow(
                    posString = word.pos,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Tłumaczenie
                Text(
                    text = word.translationPl ?: "???",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Opis
                Text(
                    text = word.description ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Poziom CEFR
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = word.cefr ?: "A1",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    )
}