package com.better.spark.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.graphics.vector.ImageVector
import com.better.spark.presentation.viewmodel.LifeCalendarState
import com.better.spark.presentation.viewmodel.LifeCalendarViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val viewModel = koinViewModel<LifeCalendarViewModel>()
    val state by viewModel.state.collectAsState()
    
    var showOnboarding by remember { mutableStateOf(false) }

    // Onboarding State
    var onboardingStep by remember { mutableStateOf(0) } // DOB, Gender, Habits, Exercise, Sleep, Screen
    var selectedDob by remember { mutableStateOf<Long?>(null) }
    var selectedGender by remember { mutableStateOf("Male") }
    
    // Habits
    var isSmoker by remember { mutableStateOf(false) }
    var alcoholDrinks by remember { mutableStateOf(0f) }
    var exerciseDays by remember { mutableStateOf(3f) }

    var selectedSleep by remember { mutableStateOf(7.5f) }
    var selectedScreen by remember { mutableStateOf(4.0f) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        when (val s = state) {
            is LifeCalendarState.Loading -> { }
            is LifeCalendarState.Onboarding -> {
                Text(
                    text = "setup your life.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { 
                    showOnboarding = true 
                    onboardingStep = 0
                }) {
                    Text("Start Setup")
                }
            }
            is LifeCalendarState.Display -> {
                // Calendar
                LifeCalendarCanvas(
                    weeksLived = s.weeksLived,
                    weeksSleep = s.weeksSleep,
                    weeksScreen = s.weeksScreenTime,
                    totalWeeks = s.totalWeeks
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Premium Legend / Stats
                PremiumLegend(
                    state = s,
                    onEditClick = {
                        // Pre-fill data
                        selectedDob = s.dobMillis
                        selectedGender = s.gender
                        selectedSleep = s.sleepHours
                        selectedScreen = s.screenHours
                        isSmoker = s.isSmoker
                        alcoholDrinks = s.alcoholDrinks.toFloat()
                        exerciseDays = s.exerciseDays.toFloat()
                        
                        onboardingStep = 0
                        showOnboarding = true
                    }
                )
            }
        }
    }

    if (showOnboarding) {
        val datePickerState = rememberDatePickerState(
            initialDisplayMode = DisplayMode.Picker,
            initialSelectedDateMillis = selectedDob
        )
        
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showOnboarding = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (onboardingStep) {
                            0 -> { // DOB
                                if (datePickerState.selectedDateMillis != null) {
                                    selectedDob = datePickerState.selectedDateMillis
                                    onboardingStep = 1
                                }
                            }
                            1 -> { onboardingStep = 2 } // Gender
                            2 -> { onboardingStep = 3 } // Habits
                            3 -> { onboardingStep = 4 } // Exercise
                            4 -> { onboardingStep = 5 } // Sleep
                            5 -> { // Screen & Finish
                                selectedDob?.let { dob ->
                                    viewModel.saveOnboardingData(
                                        dob, 
                                        selectedGender, 
                                        selectedSleep,
                                        selectedScreen,
                                        isSmoker,
                                        alcoholDrinks.toInt(),
                                        exerciseDays.toInt()
                                    )
                                }
                                showOnboarding = false
                            }
                        }
                    }
                ) {
                    Text(if (onboardingStep == 5) "Finish" else "Next")
                }
            },
            dismissButton = {
                if (onboardingStep > 0) {
                     TextButton(onClick = { onboardingStep-- }) { Text("Back") }
                } else {
                     TextButton(onClick = { showOnboarding = false }) { Text("Cancel") }
                }
            },
            text = {
                Column {
                    when (onboardingStep) {
                        0 -> {
                            Text("Select Date of Birth")
                            Spacer(modifier = Modifier.height(8.dp))
                            DatePicker(state = datePickerState)
                        }
                        1 -> {
                            Text("Select Gender")
                             Spacer(modifier = Modifier.height(16.dp))
                            listOf("Male", "Female", "Other").forEach { gender ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedGender = gender }
                                        .padding(8.dp)
                                ) {
                                    androidx.compose.material3.RadioButton(
                                        selected = (selectedGender == gender),
                                        onClick = { selectedGender = gender }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = gender)
                                }
                            }
                        }
                        2 -> {
                            Text("Habits")
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.Switch(checked = isSmoker, onCheckedChange = { isSmoker = it })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Do you smoke?")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Alcohol (Drinks/Week): ${alcoholDrinks.toInt()}")
                             androidx.compose.material3.Slider(
                                value = alcoholDrinks,
                                onValueChange = { alcoholDrinks = it },
                                valueRange = 0f..20f,
                                steps = 19
                            )
                        }
                        3 -> {
                             Text("Exercise (Days/Week): ${exerciseDays.toInt()}")
                             Spacer(modifier = Modifier.height(16.dp))
                             androidx.compose.material3.Slider(
                                 value = exerciseDays,
                                 onValueChange = { exerciseDays = it },
                                 valueRange = 0f..7f,
                                 steps = 6
                             )
                        }
                        4 -> {
                            Text("Avg Sleep: $selectedSleep hours")
                            Spacer(modifier = Modifier.height(16.dp))
                            androidx.compose.material3.Slider(
                                value = selectedSleep,
                                onValueChange = { selectedSleep = it },
                                valueRange = 4f..12f,
                                steps = 15 // 0.5 steps
                            )
                        }
                        5 -> {
                            Text("Avg Screen Time: $selectedScreen hours")
                            Spacer(modifier = Modifier.height(16.dp))
                             androidx.compose.material3.Slider(
                                value = selectedScreen,
                                onValueChange = { selectedScreen = it },
                                valueRange = 0f..12f,
                                steps = 23 // 0.5 steps
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun PremiumLegend(
    state: LifeCalendarState.Display,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Main Expectancy Header & Edit Button
        Row(
             modifier = Modifier.fillMaxWidth(),
             horizontalArrangement = Arrangement.SpaceBetween,
             verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Est. Life: ${state.lifeExpectancyYears} Years",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${state.totalWeeks} Total Weeks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            androidx.compose.material3.IconButton(onClick = onEditClick) {
                androidx.compose.material3.Icon(
                    imageVector = PencilIcon(),
                    contentDescription = "Edit Settings",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Grid-like Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(48.dp)) {
                // Left Column
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendStat(
                        label = "Lived", 
                        value = "${state.weeksLived} weeks", 
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    LegendStat(
                        label = "Screen Time", 
                        value = "${state.weeksScreenTime} weeks", 
                        color = Color(0xFFEF5350)
                    )
                }
                
                // Right Column
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendStat(
                        label = "Sleep", 
                        value = "${state.weeksSleep} weeks", 
                        color = Color(0xFF64B5F6)
                    )
                    LegendStat(
                        label = "Quality Life", 
                        value = "${state.weeksAwake} weeks", 
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun LegendStat(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = androidx.compose.ui.Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun LifeCalendarCanvas(
    weeksLived: Int,
    weeksSleep: Int,
    weeksScreen: Int,
    totalWeeks: Int
) {
    val livedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    val sleepColor = Color(0xFF64B5F6) // Blue
    val screenColor = Color(0xFFEF5350) // Red
    val awakeColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f) // Subtle
    
    // Canvas for performance
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.65f) 
    ) {
        val rows = 80 // Years
        val cols = 52 // Weeks
        
        val availableWidth = size.width
        val gap = 2.dp.toPx()
        val totalGap = (cols - 1) * gap
        val dotSize = (availableWidth - totalGap) / cols
        val effectiveDotSize = dotSize.coerceAtLeast(1f)
        
        for (i in 0 until totalWeeks) {
            val row = i / cols
            val col = i % cols
            
            val dotLeft = col * (effectiveDotSize + gap)
            val dotTop = row * (effectiveDotSize + gap)
            val cornerRadius = effectiveDotSize * 0.25f
            val color = when {
                i < weeksLived -> livedColor
                i < (weeksLived + weeksSleep) -> sleepColor
                i < (weeksLived + weeksSleep + weeksScreen) -> screenColor
                else -> awakeColor
            }

            drawRoundRect(
                color = color,
                topLeft = Offset(dotLeft, dotTop),
                size = androidx.compose.ui.geometry.Size(effectiveDotSize, effectiveDotSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
            )
        }
    }
}




