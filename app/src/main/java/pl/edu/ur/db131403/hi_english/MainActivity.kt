package pl.edu.ur.db131403.hi_english

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import pl.edu.ur.db131403.hi_english.ui.theme.HiEnglishTheme
import androidx.compose.ui.res.vectorResource
import pl.edu.ur.db131403.hi_english.ui.ExercisePage
import pl.edu.ur.db131403.hi_english.ui.shop.ShopPage


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HiEnglishTheme {
                HiEnglishApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun HiEnglishApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.EXERCISES) }
    var points by rememberSaveable { mutableIntStateOf(123) } // sample starting points

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            ImageVector.vectorResource(id = it.iconId),
                            contentDescription = it.label
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
            topBar = {
                if (currentDestination != AppDestinations.SETTINGS) {
                    TopBarWithPoints(points)
                }
            },
            modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.SHOP -> ShopPage(
                    modifier = Modifier.padding(innerPadding)
                )

                AppDestinations.EXERCISES -> ExercisePage(
                    modifier = Modifier.padding(innerPadding)
                )

                AppDestinations.PET -> Greeting(
                    name = "Pet",
                    modifier = Modifier.padding(innerPadding)
                )

                AppDestinations.SETTINGS -> Greeting(
                    name = "Settings",
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val iconId: Int,
) {
    SHOP("Shop", R.drawable.rounded_shopping_cart_24),
    EXERCISES("Exercises", R.drawable.rounded_cards_stack_24),
    PET("Pet", R.drawable.rounded_pets_24),
    SETTINGS("Settings", R.drawable.rounded_settings_24),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithPoints(points: Int) {
    TopAppBar(
        title = { Text("Points: $points") },
    )
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