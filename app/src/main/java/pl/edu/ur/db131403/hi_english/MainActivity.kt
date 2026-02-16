package pl.edu.ur.db131403.hi_english

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.runtime.mutableIntStateOf
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
import pl.edu.ur.db131403.hi_english.ui.shop.ShopPage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import pl.edu.ur.db131403.hi_english.data.local.AppDatabase
import pl.edu.ur.db131403.hi_english.data.model.EquippedItems
import pl.edu.ur.db131403.hi_english.data.repository.WordRepository
import pl.edu.ur.db131403.hi_english.data.repository.ProfileRepository
import pl.edu.ur.db131403.hi_english.ui.HouseScreen
import pl.edu.ur.db131403.hi_english.ui.SettingsScreen
import kotlin.getValue

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val wordRepository by lazy { WordRepository(database.wordDao()) }
    private val profileRepository by lazy { ProfileRepository(database.profileDao()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HiEnglishTheme {
                HiEnglishApp(wordRepository, profileRepository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Required for TopAppBar
@Composable
fun HiEnglishApp(wordRepository: WordRepository, profileRepository: ProfileRepository) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.EXERCISES) }
    var points by rememberSaveable { mutableIntStateOf(123) }
    var inventory by rememberSaveable { mutableStateOf(listOf("item_default_walls")) }
    var equipped by rememberSaveable { mutableStateOf(EquippedItems()) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            imageVector = it.icon, // Using the Outlined ImageVector
                            contentDescription = it.label,
                            // Use onSurfaceVariant for unselected, onSecondaryContainer for selected
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
                        // This matches the Coin counter in the Figma design
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
                    AppDestinations.SHOP -> ShopPage(
                        points = points,
                        inventory = inventory,
                        onBuy = { item ->
                            if (points >= item.cost && !inventory.contains(item.id)) {
                                points -= item.cost
                                inventory = inventory + item.id
                                // You could add a Toast here: "Purchased item!"
                            }
                        })

                    AppDestinations.PET -> HouseScreen(
                        inventory = inventory,
                        equipped = equipped,
                        onEquip = { itemId, category ->
                            equipped = when (category) {
                                "hats" -> equipped.copy(hat = itemId)
                                "rooms" -> equipped.copy(background = itemId)
                                else -> equipped
                            }
                        }
                    )

                    AppDestinations.EXERCISES -> {
                        LearningModule(
                            wordRepository = wordRepository,
                            profileRepository = profileRepository,
                            onAddPoints = { points += it }
                        )
                    }

                    AppDestinations.DICTIONARY -> DictionaryScreen()
                    AppDestinations.SETTINGS -> SettingsScreen()
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
        shape = RoundedCornerShape(100.dp) // Fully rounded like Figma
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.MonetizationOn, // Or a custom Coin icon
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
    val icon: ImageVector, // Changed from iconId to use ImageVector directly
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