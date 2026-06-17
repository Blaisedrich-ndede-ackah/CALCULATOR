package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CalculationHistory

// Elegant Dark Theme Color Palette
val ObsidianBg = Color(0xFF1C1B1F)      // Screen base background
val KeypadBg = Color(0xFF2B2930)        // Keypad container background
val ActionControlBg = Color(0xFF4A4458) // Special keys background (AC, ( ), %)
val NumericKeyBg = Color(0xFF1C1B1F)    // Numeric keys background
val OperatorPurple = Color(0xFF4F378B)  // Operators background (÷, ×, -, +)
val EqualLavender = Color(0xFFD0BCFF)   // Equals background (=)

val TextPrimary = Color(0xFFFFFFFF)     // High emphasis text / evaluation result
val TextSecondary = Color(0xFF938F99)   // Medium emphasis / formula text / logs
val TextAccent = Color(0xFFD0BCFF)      // Highlight/links text
val ActionControlText = Color(0xFFD0BCFF) // Action control text color
val NumericalText = Color(0xFFE6E1E5)   // Numeric keys text color
val OperatorText = Color(0xFFEADDFF)    // Operator keys text color
val EqualText = Color(0xFF381E72)       // Equals key text color
val ClearRed = Color(0xFFF2B8B5)        // Clear (C) red-pink color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val expression by viewModel.expression.collectAsStateWithLifecycle()
    val realTimeResult by viewModel.realTimeResult.collectAsStateWithLifecycle()
    val useRadians by viewModel.useRadians.collectAsStateWithLifecycle()
    val history by viewModel.historyState.collectAsStateWithLifecycle()

    var showHistory by remember { mutableStateOf(false) }
    var showScientific by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBg)
            ) {
                // 1. TOP PORTION (Header + Display) - takes remaining space
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    // Top Options Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CALCULATOR",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                color = TextAccent
                            )
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Degree / Radian toggle
                            TextButton(
                                onClick = { viewModel.toggleAngleUnit() },
                                modifier = Modifier
                                    .testTag("btn_unit_toggle")
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ActionControlBg)
                            ) {
                                Text(
                                    text = if (useRadians) "RAD" else "DEG",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextAccent
                                    )
                                )
                            }

                            // Scientific toggle
                            IconButton(
                                onClick = { showScientific = !showScientific },
                                modifier = Modifier
                                    .testTag("btn_sci_toggle")
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (showScientific) ActionControlBg else Color.Transparent)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = "Toggle Scientific Panel",
                                    tint = if (showScientific) TextAccent else TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // History toggle
                            IconButton(
                                onClick = { showHistory = !showHistory },
                                modifier = Modifier
                                    .testTag("btn_history_toggle")
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (showHistory) ActionControlBg else Color.Transparent)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Calculation History",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Display Area for formula and result values
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.End
                    ) {
                        // Formula Expression (Dynamic auto-scaling based on character lengths)
                        val formulaTextSize = when {
                            expression.length < 10 -> 44.sp
                            expression.length < 18 -> 32.sp
                            expression.length < 25 -> 24.sp
                            else -> 18.sp
                        }

                        Text(
                            text = expression.ifEmpty { "0" },
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = formulaTextSize,
                                fontWeight = FontWeight.Light,
                                fontFamily = FontFamily.SansSerif,
                                color = TextSecondary,
                                textAlign = TextAlign.End
                            ),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Real-time or Evaluated Result
                        AnimatedVisibility(
                            visible = realTimeResult.isNotEmpty(),
                            enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
                            exit = fadeOut()
                        ) {
                            Text(
                                text = realTimeResult,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 60.sp, // Large 7xl matches Elegant Dark look
                                    fontWeight = FontWeight.Light,
                                    fontFamily = FontFamily.SansSerif,
                                    color = if (realTimeResult == "Error" || realTimeResult == "Infinity") ClearRed else TextPrimary,
                                    textAlign = TextAlign.End
                                ),
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // 2. BOTTOM PORTION (Keypad Area Container)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(KeypadBg)
                        .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Scientific keypad grid overlay (Only visible if showScientific is true)
                        AnimatedVisibility(
                            visible = showScientific,
                            enter = expandVertically(animationSpec = spring()) + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Row S1: sin, cos, tan, log, ln
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    ScientificButton("sin", "sin(", Modifier.weight(1f), viewModel)
                                    ScientificButton("cos", "cos(", Modifier.weight(1f), viewModel)
                                    ScientificButton("tan", "tan(", Modifier.weight(1f), viewModel)
                                    ScientificButton("log", "log(", Modifier.weight(1f), viewModel)
                                    ScientificButton("ln", "ln(", Modifier.weight(1f), viewModel)
                                }
                                // Row S2: sqrt, power(^), %, pi(π), e
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    ScientificButton("√", "√(", Modifier.weight(1f), viewModel, "btn_sqrt")
                                    ScientificButton("^", "^", Modifier.weight(1f), viewModel, "btn_pow")
                                    ScientificButton("%", "%", Modifier.weight(1f), viewModel, "btn_percent")
                                    ScientificButton("π", "π", Modifier.weight(1f), viewModel, "btn_pi")
                                    ScientificButton("e", "e", Modifier.weight(1f), viewModel, "btn_e")
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }

                        // Row 1: C, ( ), %, ÷
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ActionControlKey("C", "btn_clear", Modifier.weight(1f), viewModel, ClearRed)
                            ActionControlKey("( )", "btn_bracket", Modifier.weight(1f), viewModel, TextAccent)
                            ActionControlKey("⌫", "btn_backspace", Modifier.weight(1f), viewModel, TextAccent)
                            OperatorKey("÷", "btn_divide", Modifier.weight(1f), viewModel)
                        }

                        // Row 2: 7, 8, 9, ×
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            NumericalKey("7", "btn_7", Modifier.weight(1f), viewModel)
                            NumericalKey("8", "btn_8", Modifier.weight(1f), viewModel)
                            NumericalKey("9", "btn_9", Modifier.weight(1f), viewModel)
                            OperatorKey("×", "btn_multiply", Modifier.weight(1f), viewModel)
                        }

                        // Row 3: 4, 5, 6, −
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            NumericalKey("4", "btn_4", Modifier.weight(1f), viewModel)
                            NumericalKey("5", "btn_5", Modifier.weight(1f), viewModel)
                            NumericalKey("6", "btn_6", Modifier.weight(1f), viewModel)
                            OperatorKey("−", "btn_minus", Modifier.weight(1f), viewModel)
                        }

                        // Row 4: 1, 2, 3, +
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            NumericalKey("1", "btn_1", Modifier.weight(1f), viewModel)
                            NumericalKey("2", "btn_2", Modifier.weight(1f), viewModel)
                            NumericalKey("3", "btn_3", Modifier.weight(1f), viewModel)
                            OperatorKey("+", "btn_plus", Modifier.weight(1f), viewModel)
                        }

                        // Row 5: +/-, 0, . , =
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            NumericalKey("+/-", "btn_negate", Modifier.weight(1f), viewModel)
                            NumericalKey("0", "btn_0", Modifier.weight(1f), viewModel)
                            NumericalKey(".", "btn_dot", Modifier.weight(1f), viewModel)
                            EqualKey("=", "btn_equal", Modifier.weight(1f), viewModel)
                        }
                    }
                }
            }

            // 3. History Sliding Drawer Panel (at the top)
            AnimatedVisibility(
                visible = showHistory,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f)
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(KeypadBg)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CALCULATION LOG",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = TextSecondary
                                )
                            )

                            Row {
                                if (history.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.clearAllHistory() },
                                        modifier = Modifier.testTag("btn_history_clear")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Clear History Logo",
                                            tint = ClearRed
                                        )
                                    }
                                }
                                TextButton(onClick = { showHistory = false }) {
                                    Text("CLOSE", color = TextAccent)
                                }
                            }
                        }

                        HorizontalDivider(color = ActionControlBg, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                        if (history.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Your computation logs appear here",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(history, key = { it.id }) { item ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(ObsidianBg.copy(alpha = 0.5f))
                                            .clickable {
                                                viewModel.selectHistoryItem(item)
                                                showHistory = false
                                            }
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = item.expression,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = FontFamily.SansSerif
                                            ),
                                            color = TextSecondary,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Start
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "= ${item.result}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = TextPrimary
                                            ),
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Keypad Button Helpers

@Composable
fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    contentColor: Color,
    shape: androidx.compose.ui.graphics.Shape = CircleShape,
    testTag: String
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .testTag(testTag)
            .aspectRatio(1.15f) // Tactile physical key aspect ratio
            .clip(shape)
            .background(backgroundColor)
            .clickable { onClick() }
            .minimumInteractiveComponentSize()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            ),
            color = contentColor
        )
    }
}

@Composable
fun NumericalKey(
    value: String,
    testTag: String,
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel
) {
    KeypadButton(
        text = value,
        onClick = { viewModel.handleKeyPress(value) },
        backgroundColor = NumericKeyBg,
        contentColor = NumericalText,
        shape = CircleShape, // Fully rounded-full as per HTML design requirements
        testTag = testTag,
        modifier = modifier
    )
}

@Composable
fun OperatorKey(
    value: String,
    testTag: String,
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel
) {
    KeypadButton(
        text = value,
        onClick = { viewModel.handleKeyPress(value) },
        backgroundColor = OperatorPurple,
        contentColor = OperatorText,
        shape = RoundedCornerShape(16.dp), // modern 2xl active scaling look
        testTag = testTag,
        modifier = modifier
    )
}

@Composable
fun ActionControlKey(
    value: String,
    testTag: String,
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel,
    contentColor: Color = ActionControlText
) {
    KeypadButton(
        text = value,
        onClick = { viewModel.handleKeyPress(value) },
        backgroundColor = ActionControlBg,
        contentColor = contentColor,
        shape = RoundedCornerShape(16.dp), // modern rounded-2xl look
        testTag = testTag,
        modifier = modifier
    )
}

@Composable
fun EqualKey(
    value: String,
    testTag: String,
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel
) {
    KeypadButton(
        text = value,
        onClick = { viewModel.handleKeyPress(value) },
        backgroundColor = EqualLavender,
        contentColor = EqualText,
        shape = RoundedCornerShape(16.dp), // matches operator buttons shape
        testTag = testTag,
        modifier = modifier
    )
}

@Composable
fun ScientificButton(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel,
    customTag: String? = null
) {
    val tag = customTag ?: "btn_${label.lowercase()}"
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .testTag(tag)
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ActionControlBg)
            .clickable { viewModel.handleKeyPress(value) }
            .minimumInteractiveComponentSize()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            ),
            color = ActionControlText
        )
    }
}
