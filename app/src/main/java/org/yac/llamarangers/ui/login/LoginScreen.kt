package org.yac.llamarangers.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.yac.llamarangers.data.local.entity.RangerProfileEntity
import org.yac.llamarangers.domain.model.enums.RangerRole
import org.yac.llamarangers.ui.theme.AvatarDefault
import org.yac.llamarangers.ui.theme.AvatarSelected
import org.yac.llamarangers.ui.theme.AvatarSelectedBorder
import org.yac.llamarangers.ui.theme.HeroGradientBottom
import org.yac.llamarangers.ui.theme.HeroGradientTop
import org.yac.llamarangers.ui.theme.PinEmpty
import org.yac.llamarangers.ui.theme.PinFilled

/**
 * Login screen with ranger selection and PIN keypad.
 * Ports iOS LoginView with spring animations, text shadows,
 * auto-scroll to PIN, per-dot animation, and avatar scale feedback.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel()
) {
    val rangers by viewModel.rangers.collectAsState()
    val selectedRanger by viewModel.selectedRanger.collectAsState()
    val enteredPIN by viewModel.enteredPIN.collectAsState()
    val loginError by viewModel.loginError.collectAsState()
    val scrollState = rememberScrollState()

    // Spring-animated hero height transition (matches iOS .spring(response: 0.4, dampingFraction: 0.8))
    val heroHeight by animateDpAsState(
        targetValue = if (selectedRanger == null) 300.dp else 180.dp,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "heroHeight"
    )

    // Auto-scroll to PIN section when a ranger is selected
    LaunchedEffect(selectedRanger) {
        if (selectedRanger != null) {
            // Small delay to let the AnimatedVisibility expand first
            kotlinx.coroutines.delay(150)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Hero section with spring-animated height
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroHeight)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(HeroGradientTop, HeroGradientBottom)
                        )
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = "Leaf",
                        tint = Color.White.copy(alpha = 0.95f),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Title with shadow (matches iOS .shadow(color: .black.opacity(0.5), radius: 4))
                    Text(
                        text = "Lama Lama Rangers",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = TextStyle(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(0f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                    // Subtitle with shadow (matches iOS .shadow(color: .black.opacity(0.4), radius: 2))
                    Text(
                        text = "Yintjingga Aboriginal Corporation",
                        style = TextStyle(
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            color = Color.White.copy(alpha = 0.75f),
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.4f),
                                offset = Offset(0f, 1f),
                                blurRadius = 2f
                            )
                        )
                    )
                }
            }

            // Card section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                // Ranger selection
                Text(
                    text = "Who are you?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rangers.forEach { ranger ->
                        RangerAvatarCard(
                            ranger = ranger,
                            isSelected = selectedRanger?.id == ranger.id,
                            onClick = { viewModel.selectRanger(ranger) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // PIN entry
                AnimatedVisibility(
                    visible = selectedRanger != null,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // "Enter PIN" label
                        Text(
                            text = "Enter PIN",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // PIN dots with per-dot spring color animation
                        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            repeat(4) { i ->
                                val dotColor by animateColorAsState(
                                    targetValue = if (i < enteredPIN.length) PinFilled else PinEmpty,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "pinDot$i"
                                )
                                // Scale pop on fill
                                val dotScale by animateFloatAsState(
                                    targetValue = if (i < enteredPIN.length) 1.15f else 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "pinDotScale$i"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .graphicsLayer {
                                            scaleX = dotScale
                                            scaleY = dotScale
                                        }
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))

                        // Keypad
                        PINKeypad(
                            onDigit = { viewModel.appendPINDigit(it) },
                            onDelete = { viewModel.deletePINDigit() }
                        )
                    }
                }

                // Error message
                loginError?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Footer
        Text(
            text = "31265 Communications for IT Professionals  \u00B7  EWB Challenge 2026",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun RangerAvatarCard(
    ranger: RangerProfileEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initials = (ranger.displayName ?: "R")
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.toString() }
        .joinToString("")

    val roleLabel = RangerRole.fromValue(ranger.role ?: "ranger").displayName

    val firstName = ranger.displayName?.split(" ")?.firstOrNull() ?: "Ranger"

    // Avatar selection scale feedback (1f -> 1.05f with spring)
    val cardScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "avatarScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) AvatarSelected.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .then(
                if (isSelected) Modifier.border(
                    1.5.dp, AvatarSelectedBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp)
                ) else Modifier
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp)
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isSelected) AvatarSelected else AvatarDefault)
                .then(
                    if (isSelected) Modifier.border(2.5.dp, AvatarSelectedBorder, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = firstName,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = roleLabel,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PINKeypad(
    onDigit: (String) -> Unit,
    onDelete: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { key ->
                    when {
                        key.isEmpty() -> {
                            Spacer(modifier = Modifier.weight(1f).height(64.dp))
                        }
                        key == "DEL" -> {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable(onClick = onDelete),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .clickable { onDigit(key) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
