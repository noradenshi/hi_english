package pl.edu.ur.db131403.hi_english

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import pl.edu.ur.db131403.hi_english.ui.theme.HiEnglishTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.db131403.hi_english.ui.learn.LearningModule
import pl.edu.ur.db131403.hi_english.ui.dictionary.DictionaryScreen
import pl.edu.ur.db131403.hi_english.ui.store.StorePage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.edu.ur.db131403.hi_english.data.local.DictionaryDatabase
import pl.edu.ur.db131403.hi_english.data.local.InventoryDatabase
import pl.edu.ur.db131403.hi_english.data.local.StoreDao
import pl.edu.ur.db131403.hi_english.data.repository.WordRepository
import pl.edu.ur.db131403.hi_english.data.repository.ProfileRepository
import pl.edu.ur.db131403.hi_english.ui.HouseScreen
import pl.edu.ur.db131403.hi_english.ui.settings.SettingsScreen
import pl.edu.ur.db131403.hi_english.ui.store.StoreViewModel
import pl.edu.ur.db131403.hi_english.ui.store.StoreViewModelFactory
import kotlin.getValue

class MainActivity : ComponentActivity() {
    private val dictionaryDb by lazy { DictionaryDatabase.getDatabase(this) }
    private val inventoryDb by lazy { InventoryDatabase.getDatabase(this) }
    private val wordRepository by lazy { WordRepository(dictionaryDb.wordDao(), this) }
    private val profileRepository by lazy { ProfileRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val channelId = "daily_reminder"
        val channel = NotificationChannel(
            channelId,
            "Przypomnienia o nauce",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        setContent {
            androidx.compose.runtime.LaunchedEffect(Unit) {
                wordRepository.checkAndInitializeDatabase()
            }

            HiEnglishTheme {
                HiEnglishApp(
                    wordRepository,
                    profileRepository,
                    inventoryDb.storeDao()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiEnglishApp(
    wordRepository: WordRepository,
    profileRepository: ProfileRepository,
    storeDao: StoreDao
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.EXERCISES) }

    BackHandler(enabled = currentDestination != AppDestinations.EXERCISES) {
        currentDestination = AppDestinations.EXERCISES
    }

    val points by profileRepository.userPoints.collectAsState(initial = 0)

    val storeViewModel: StoreViewModel = viewModel(
        factory = StoreViewModelFactory(storeDao, profileRepository)
    )

    val storeItems by storeViewModel.storeItems.collectAsState(initial = emptyList())
    val equippedItems by storeViewModel.equippedItems.collectAsState(initial = emptyMap())

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            imageVector = it.icon,
                            contentDescription = it.label,
                            tint = if (currentDestination == it)
                                MaterialTheme.colorScheme.onSecondaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Hi English!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        PointsDisplay(points = points)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentDestination) {
                    AppDestinations.SHOP -> StorePage(
                        items = storeItems,
                        points = points,
                        profileRepository = profileRepository,
                        onBuy = { item -> storeViewModel.handleItemClick(item, false) },
                        onSell = { item -> storeViewModel.sellItem(item) },
                    )

                    AppDestinations.PET -> HouseScreen(
                        equippedMap = equippedItems,
                        storeItems = storeItems,
                        onEquipToggle = { item, isEquipped ->
                            storeViewModel.handleItemClick(item, isEquipped)
                        }
                    )

                    AppDestinations.EXERCISES -> {
                        LearningModule(
                            wordRepository = wordRepository,
                            profileRepository = profileRepository,
                        )
                    }

                    AppDestinations.DICTIONARY -> DictionaryScreen(
                        profileRepository,
                        wordRepository
                    )

                    AppDestinations.SETTINGS -> SettingsScreen(
                        profileRepository,
                        storeDao
                    )
                }
            }
        }
    }
}

@Composable
fun PointsDisplay(points: Int) {
    Surface(
        modifier = Modifier.padding(end = 16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(100.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.MonetizationOn,
                contentDescription = "Points",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = points.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector
) {
    SHOP("Shop", Icons.Outlined.ShoppingBag),
    PET("Pet", Icons.Outlined.Pets),
    EXERCISES("Exercises", Icons.Outlined.AutoStories),
    DICTIONARY("Dictionary", Icons.Outlined.Book),
    SETTINGS("Settings", Icons.Outlined.Settings),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HiEnglishTheme {
        Greeting("Android")
    }
}