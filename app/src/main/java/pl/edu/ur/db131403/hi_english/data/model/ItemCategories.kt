package pl.edu.ur.db131403.hi_english.data.model

object ItemCategories {
    const val HAT = "HAT"
    const val WALL_SKIN = "WALL_SKIN"
    const val PET_SKIN = "PET_SKIN"
    const val SCARF = "SCARF"
    const val GLASSES = "GLASSES"
    const val HOUSE_ACC = "HOUSE_ACC"

    // Lista używana do budowania zakładek w sklepie
    val all = listOf(
        HAT to "Czapki",
        WALL_SKIN to "Pokoje",
        PET_SKIN to "Zwierzątka",
        SCARF to "Szaliki",
        GLASSES to "Okulary",
        HOUSE_ACC to "Dodatki"
    )
}