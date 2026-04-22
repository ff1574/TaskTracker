package com.better.spark.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*

object TaskAppearance {

    val colors = listOf(
        "#F44336", "#E57373", // Red
        "#E91E63", "#F06292", // Pink
        "#9C27B0", "#BA68C8", // Purple
        "#673AB7", "#9575CD", // Deep Purple
        "#3F51B5", "#7986CB", // Indigo
        "#2196F3", "#64B5F6", // Blue
        "#03A9F4", "#4FC3F7", // Light Blue
        "#00BCD4", "#4DD0E1", // Cyan
        "#009688", "#4DB6AC", // Teal
        "#4CAF50", "#81C784", // Green
        "#8BC34A", "#AED581", // Light Green
        "#CDDC39", "#DCE775", // Lime
        "#FFEB3B", "#FFF176", // Yellow
        "#FFC107", "#FFD54F", // Amber
        "#FF9800", "#FFB74D", // Orange
        "#FF5722", "#FF8A65", // Deep Orange
        "#795548", "#A1887F", // Brown
        "#9E9E9E", "#BDBDBD", // Grey
        "#607D8B", "#90A4AE"  // Blue Grey
    )

    fun getColor(hex: String): Color {
        return try {
            Color(hex.removePrefix("#").toLong(16) or 0x00000000FF000000)
        } catch (e: Exception) {
            Color(0xFFBB86FC)
        }
    }

    /**
     * Complete curated icon list using Phosphor icons.
     * Organized by category for the search/scroll picker.
     */
    val iconEntries: List<Pair<String, String>> = listOf(
        // ── Mindfulness & Wellness ─────────────────────────────────────────────
        "TaiChi"          to "Mindfulness",
        "Brain"          to "Mindfulness",
        "Wind"           to "Mindfulness",
        "Yin"            to "Mindfulness",
        "Flower"         to "Mindfulness",
        "FlowerLotus"    to "Mindfulness",
        "Leaf"           to "Mindfulness",
        "Tree"           to "Mindfulness",
        "SunHorizon"     to "Mindfulness",
        "Moon"           to "Mindfulness",
        "Sun"            to "Mindfulness",
        "Snowflake"      to "Mindfulness",
        "Drop"           to "Mindfulness",
        "Waves"          to "Mindfulness",
        "HandsPraying"   to "Mindfulness",
        "SmileyWink"     to "Mindfulness",
        "Peace"          to "Mindfulness",
        "Eye"            to "Mindfulness",
        "Heartbeat"      to "Mindfulness",
        // ── Bad Habits ────────────────────────────────────────────────────────
        "Cigarette"      to "Habits",
        "CigaretteSlash" to "Habits",
        "Wine"           to "Habits",
        "BeerBottle"     to "Habits",
        "Martini"        to "Habits",
        "Pill"           to "Habits",
        "Syringe"        to "Habits",
        "DeviceMobile"   to "Habits",
        "Monitor"        to "Habits",
        "Hamburger"      to "Habits",
        "Pizza"          to "Habits",
        // ── Health & Fitness ───────────────────────────────────────────────────
        "Heart"          to "Health",
        "Barbell"        to "Health",
        "PersonSimpleRun" to "Health",
        "PersonSimpleBike" to "Health",
        "PersonSimpleSwim" to "Health",
        "PersonSimpleHike" to "Health",
        "PersonSimpleWalk" to "Health",
        "Tooth"          to "Health",
        "FirstAid"       to "Health",
        "Bed"            to "Health",
        "ForkKnife"      to "Health",
        "Bowl"           to "Health",
        "Coffee"         to "Health",
        "Timer"          to "Health",
        "Thermometer"    to "Health",
        "Scales"         to "Health",
        // ── Mind & Education ──────────────────────────────────────────────────
        "BookOpen"       to "Education",
        "Book"           to "Education",
        "Books"          to "Education",
        "GraduationCap"  to "Education",
        "Notebook"       to "Education",
        "Note"           to "Education",
        "Pencil"         to "Education",
        "Article"        to "Education",
        "Code"           to "Education",
        "Laptop"         to "Education",
        "MagnifyingGlass" to "Education",
        "Lightbulb"      to "Education",
        // ── Work & Finance ────────────────────────────────────────────────────
        "Briefcase"      to "Finance",
        "ChartLine"      to "Finance",
        "ChartBar"       to "Finance",
        "CurrencyDollar" to "Finance",
        "Wallet"         to "Finance",
        "PiggyBank"      to "Finance",
        "Receipt"        to "Finance",
        "TrendUp"        to "Finance",
        "Calculator"     to "Finance",
        "Buildings"      to "Finance",
        // ── Home & Lifestyle ──────────────────────────────────────────────────
        "House"          to "Home",
        "Broom"          to "Home",
        "Bathtub"        to "Home",
        "Couch"          to "Home",
        "Cookie"         to "Home",
        "Plant"          to "Home",
        "PawPrint"       to "Home",
        "Baby"           to "Home",
        "ShoppingCart"   to "Home",
        "ShoppingBag"    to "Home",
        "Toolbox"        to "Home",
        "Wrench"         to "Home",
        "Jar"            to "Home",
        // ── Social & Community ────────────────────────────────────────────────
        "Users"          to "Social",
        "UserCircle"     to "Social",
        "HandHeart"      to "Social",
        "Handshake"      to "Social",
        "ChatCircle"     to "Social",
        "Phone"          to "Social",
        "Envelope"       to "Social",
        "Bell"           to "Social",
        "HandWaving"     to "Social",
        "Gift"           to "Social",
        "Smiley"         to "Social",
        // ── Travel & Places ───────────────────────────────────────────────────
        "Airplane"       to "Travel",
        "Car"            to "Travel",
        "Bicycle"        to "Travel",
        "Bus"            to "Travel",
        "Train"          to "Travel",
        "MapPin"         to "Travel",
        "Compass"        to "Travel",
        "Mountains"      to "Travel",
        "Umbrella"       to "Travel",
        "Tent"           to "Travel",
        // ── Entertainment & Creativity ────────────────────────────────────────
        "MusicNote"      to "Creative",
        "Headphones"     to "Creative",
        "Microphone"     to "Creative",
        "Camera"         to "Creative",
        "FilmSlate"      to "Creative",
        "Palette"        to "Creative",
        "PencilLine"     to "Creative",
        "GameController" to "Creative",
        "TelevisionSimple" to "Creative",
        "Podcast"        to "Creative",
        "Radio"            to "Creative",
        // ── Goals & Productivity ──────────────────────────────────────────────
        "Star"           to "Goals",
        "Trophy"         to "Goals",
        "Medal"          to "Goals",
        "Crown"            to "Goals",
        "Target"         to "Goals",
        "Flag"           to "Goals",
        "Fire"           to "Goals",
        "Lightning"      to "Goals",
        "Rocket"         to "Goals",
        "CheckSquare"    to "Goals",
        "ListChecks"     to "Goals",
        "CalendarCheck"  to "Goals",
        "Alarm"          to "Goals",
        "Hourglass"      to "Goals",
        "Sparkle"        to "Goals",
        "ArrowUp"        to "Goals",
        "Infinity"       to "Goals"
    )

    val iconNames: List<String> get() = iconEntries.map { it.first }

    val categories: List<String> get() = iconEntries.map { it.second }.distinct()

    @Composable
    fun getIcon(name: String): ImageVector {
        return when (name) {
            // Mindfulness & Wellness
            "TaiChi"           -> PhosphorIcons.Regular.PersonSimpleTaiChi
            "Brain"            -> PhosphorIcons.Regular.Brain
            "Wind"             -> PhosphorIcons.Regular.Wind
            "Yin"              -> PhosphorIcons.Regular.YinYang
            "Flower"           -> PhosphorIcons.Regular.Flower
            "FlowerLotus"      -> PhosphorIcons.Regular.FlowerLotus
            "Leaf"             -> PhosphorIcons.Regular.Leaf
            "Tree"             -> PhosphorIcons.Regular.Tree
            "SunHorizon"       -> PhosphorIcons.Regular.SunHorizon
            "Moon"             -> PhosphorIcons.Regular.Moon
            "Sun"              -> PhosphorIcons.Regular.Sun
            "Snowflake"        -> PhosphorIcons.Regular.Snowflake
            "Drop"             -> PhosphorIcons.Regular.Drop
            "Waves"            -> PhosphorIcons.Regular.Waves
            "HandsPraying"     -> PhosphorIcons.Regular.HandsPraying
            "SmileyWink"       -> PhosphorIcons.Regular.SmileyWink
            "Peace"            -> PhosphorIcons.Regular.Peace
            "Eye"              -> PhosphorIcons.Regular.Eye
            "Heartbeat"        -> PhosphorIcons.Regular.Heartbeat
            // Bad Habits
            "Cigarette"        -> PhosphorIcons.Regular.Cigarette
            "CigaretteSlash"   -> PhosphorIcons.Regular.CigaretteSlash
            "Wine"             -> PhosphorIcons.Regular.Wine
            "BeerBottle"       -> PhosphorIcons.Regular.BeerBottle
            "Martini"          -> PhosphorIcons.Regular.Martini
            "Pill"             -> PhosphorIcons.Regular.Pill
            "Syringe"          -> PhosphorIcons.Regular.Syringe
            "DeviceMobile"     -> PhosphorIcons.Regular.DeviceMobile
            "Hamburger"        -> PhosphorIcons.Regular.Hamburger
            "Pizza"            -> PhosphorIcons.Regular.Pizza
            // Health & Fitness
            "Heart"            -> PhosphorIcons.Regular.Heart
            "Barbell"          -> PhosphorIcons.Regular.Barbell
            "PersonSimpleRun"  -> PhosphorIcons.Regular.PersonSimpleRun
            "PersonSimpleBike" -> PhosphorIcons.Regular.PersonSimpleBike
            "PersonSimpleSwim" -> PhosphorIcons.Regular.PersonSimpleSwim
            "PersonSimpleHike" -> PhosphorIcons.Regular.PersonSimpleHike
            "PersonSimpleWalk" -> PhosphorIcons.Regular.PersonSimpleWalk
            "Tooth"            -> PhosphorIcons.Regular.Tooth
            "FirstAid"         -> PhosphorIcons.Regular.FirstAid
            "Bed"              -> PhosphorIcons.Regular.Bed
            "ForkKnife"        -> PhosphorIcons.Regular.ForkKnife
            "Bowl"             -> PhosphorIcons.Regular.BowlFood
            "Coffee"           -> PhosphorIcons.Regular.Coffee
            "Timer"            -> PhosphorIcons.Regular.Timer
            "Thermometer"      -> PhosphorIcons.Regular.Thermometer
            "Scales"           -> PhosphorIcons.Regular.Scales
            // Mind & Education
            "BookOpen"         -> PhosphorIcons.Regular.BookOpen
            "Book"             -> PhosphorIcons.Regular.Book
            "Books"            -> PhosphorIcons.Regular.Books
            "GraduationCap"    -> PhosphorIcons.Regular.GraduationCap
            "Notebook"         -> PhosphorIcons.Regular.Notebook
            "Note"             -> PhosphorIcons.Regular.Note
            "Pencil"           -> PhosphorIcons.Regular.Pencil
            "Article"          -> PhosphorIcons.Regular.Article
            "Code"             -> PhosphorIcons.Regular.Code
            "Monitor"          -> PhosphorIcons.Regular.Monitor
            "Laptop"           -> PhosphorIcons.Regular.Laptop
            "MagnifyingGlass"  -> PhosphorIcons.Regular.MagnifyingGlass
            "Lightbulb"        -> PhosphorIcons.Regular.Lightbulb
            // Work & Finance
            "Briefcase"        -> PhosphorIcons.Regular.Briefcase
            "ChartLine"        -> PhosphorIcons.Regular.ChartLine
            "ChartBar"         -> PhosphorIcons.Regular.ChartBar
            "CurrencyDollar"   -> PhosphorIcons.Regular.CurrencyDollar
            "Wallet"           -> PhosphorIcons.Regular.Wallet
            "PiggyBank"        -> PhosphorIcons.Regular.PiggyBank
            "Receipt"          -> PhosphorIcons.Regular.Receipt
            "TrendUp"          -> PhosphorIcons.Regular.TrendUp
            "Calculator"       -> PhosphorIcons.Regular.Calculator
            "Buildings"        -> PhosphorIcons.Regular.Buildings
            // Home & Lifestyle
            "House"            -> PhosphorIcons.Regular.House
            "Broom"            -> PhosphorIcons.Regular.Broom
            "Bathtub"          -> PhosphorIcons.Regular.Bathtub
            "Couch"            -> PhosphorIcons.Regular.Couch
            "Cookie"           -> PhosphorIcons.Regular.Cookie
            "Plant"            -> PhosphorIcons.Regular.Plant
            "PawPrint"         -> PhosphorIcons.Regular.PawPrint
            "Baby"             -> PhosphorIcons.Regular.Baby
            "ShoppingCart"     -> PhosphorIcons.Regular.ShoppingCart
            "ShoppingBag"      -> PhosphorIcons.Regular.ShoppingBag
            "Toolbox"          -> PhosphorIcons.Regular.Toolbox
            "Wrench"           -> PhosphorIcons.Regular.Wrench
            "Jar"              -> PhosphorIcons.Regular.Jar
            // Social & Community
            "Users"            -> PhosphorIcons.Regular.Users
            "UserCircle"       -> PhosphorIcons.Regular.UserCircle
            "HandHeart"        -> PhosphorIcons.Regular.HandHeart
            "Handshake"        -> PhosphorIcons.Regular.Handshake
            "ChatCircle"       -> PhosphorIcons.Regular.ChatCircle
            "Phone"            -> PhosphorIcons.Regular.Phone
            "Envelope"         -> PhosphorIcons.Regular.Envelope
            "Bell"             -> PhosphorIcons.Regular.Bell
            "HandWaving"       -> PhosphorIcons.Regular.HandWaving
            "Gift"             -> PhosphorIcons.Regular.Gift
            "Smiley"           -> PhosphorIcons.Regular.Smiley
            // Travel & Places
            "Airplane"         -> PhosphorIcons.Regular.Airplane
            "Car"              -> PhosphorIcons.Regular.Car
            "Bicycle"          -> PhosphorIcons.Regular.Bicycle
            "Bus"              -> PhosphorIcons.Regular.Bus
            "Train"            -> PhosphorIcons.Regular.Train
            "MapPin"           -> PhosphorIcons.Regular.MapPin
            "Compass"          -> PhosphorIcons.Regular.Compass
            "Mountains"        -> PhosphorIcons.Regular.Mountains
            "Umbrella"         -> PhosphorIcons.Regular.Umbrella
            "Tent"             -> PhosphorIcons.Regular.Tent
            // Entertainment & Creativity
            "MusicNote"        -> PhosphorIcons.Regular.MusicNote
            "Headphones"       -> PhosphorIcons.Regular.Headphones
            "Microphone"       -> PhosphorIcons.Regular.Microphone
            "Camera"           -> PhosphorIcons.Regular.Camera
            "FilmSlate"        -> PhosphorIcons.Regular.FilmSlate
            "Palette"          -> PhosphorIcons.Regular.Palette
            "PencilLine"       -> PhosphorIcons.Regular.PencilLine
            "GameController"   -> PhosphorIcons.Regular.GameController
            "TelevisionSimple" -> PhosphorIcons.Regular.TelevisionSimple
            "Radio"            -> PhosphorIcons.Regular.Radio
            // Goals & Productivity
            "Star"             -> PhosphorIcons.Regular.Star
            "Trophy"           -> PhosphorIcons.Regular.Trophy
            "Medal"            -> PhosphorIcons.Regular.Medal
            "Crown"            -> PhosphorIcons.Regular.Crown
            "Target"           -> PhosphorIcons.Regular.Target
            "Flag"             -> PhosphorIcons.Regular.Flag
            "Fire"             -> PhosphorIcons.Regular.Fire
            "Lightning"        -> PhosphorIcons.Regular.Lightning
            "Rocket"           -> PhosphorIcons.Regular.Rocket
            "CheckSquare"      -> PhosphorIcons.Regular.CheckSquare
            "ListChecks"       -> PhosphorIcons.Regular.ListChecks
            "CalendarCheck"    -> PhosphorIcons.Regular.CalendarCheck
            "Alarm"            -> PhosphorIcons.Regular.Alarm
            "Hourglass"        -> PhosphorIcons.Regular.Hourglass
            "Sparkle"          -> PhosphorIcons.Regular.Sparkle
            "ArrowUp"          -> PhosphorIcons.Regular.ArrowUp
            "Infinity"         -> PhosphorIcons.Regular.Infinity
            else               -> PhosphorIcons.Regular.Star
        }
    }
}
