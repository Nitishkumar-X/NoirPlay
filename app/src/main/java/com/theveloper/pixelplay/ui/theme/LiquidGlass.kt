package com.theveloper.pixelplay.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
//  Liquid Glass Constants
// ─────────────────────────────────────────────────────────────────────────────

object LiquidGlassDefaults {
    // Glass fill — translucent white for the "frosted" base
    val GlassFill        = Color(0x14FFFFFF) // 8% white
    val GlassFillMedium  = Color(0x1EFFFFFF) // 12% white
    val GlassFillStrong  = Color(0x2AFFFFFF) // 16% white

    // Border — thin bright rim simulates the glass edge refraction
    val GlassBorder      = Color(0x28FFFFFF) // 16% white
    val GlassBorderBright= Color(0x50FFFFFF) // 32% white (selected / active)

    // Specular highlight — the bright strip at the top of a glass surface
    val SpecularTop      = Color(0x38FFFFFF) // 22% white
    val SpecularFade     = Color(0x00FFFFFF) // fully transparent

    // Glow — colored halos beneath elements
    val GlowBlur         = 24.dp
    val GlowAlpha        = 0.35f

    // Shape tokens
    val ShapePill        = RoundedCornerShape(50)
    val ShapePanel       = RoundedCornerShape(28.dp)
    val ShapePanelLarge  = RoundedCornerShape(36.dp)
    val ShapeButton      = RoundedCornerShape(20.dp)

    // Border widths
    val BorderThin       = 0.8.dp
    val BorderMedium     = 1.2.dp
}

// ─────────────────────────────────────────────────────────────────────────────
//  Modifier Extensions
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Applies the liquid glass frosted-panel look:
 * - Translucent white fill
 * - Thin bright border
 * - Optional specular sweep gradient at the top
 */
fun Modifier.liquidGlassPanel(
    shape: Shape = LiquidGlassDefaults.ShapePanel,
    fillColor: Color = LiquidGlassDefaults.GlassFillMedium,
    borderColor: Color = LiquidGlassDefaults.GlassBorder,
    borderWidth: Dp = LiquidGlassDefaults.BorderThin
): Modifier = this
    .clip(shape)
    .background(fillColor, shape)
    .border(borderWidth, borderColor, shape)

/**
 * Adds a specular highlight shimmer drawn at the top of the composable.
 * This creates the "wet glass" look without any blur pass.
 */
fun Modifier.glassSpecularHighlight(
    shape: Shape = LiquidGlassDefaults.ShapePanel
): Modifier = this.drawBehind {
    val gradient = Brush.linearGradient(
        colorStops = arrayOf(
            0.0f to LiquidGlassDefaults.SpecularTop,
            0.45f to LiquidGlassDefaults.SpecularFade,
        ),
        start = Offset(size.width * 0.1f, 0f),
        end   = Offset(size.width * 0.6f, size.height * 0.5f)
    )
    drawRoundRect(
        brush = gradient,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx())
    )
}

/**
 * Adds a soft colored glow / drop-shadow beneath the element.
 * Mid-range safe — uses the `shadow()` modifier (no RenderEffect).
 */
fun Modifier.liquidGlow(
    color: Color,
    elevation: Dp = 18.dp,
    shape: Shape = LiquidGlassDefaults.ShapePanel
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    ambientColor = color.copy(alpha = LiquidGlassDefaults.GlowAlpha),
    spotColor = color.copy(alpha = LiquidGlassDefaults.GlowAlpha),
    clip = false
)

// ─────────────────────────────────────────────────────────────────────────────
//  Reusable Composables
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A glass-style surface container.
 * Use this as the base for cards, panels, and sheets.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = LiquidGlassDefaults.ShapePanel,
    fillColor: Color = LiquidGlassDefaults.GlassFillMedium,
    borderColor: Color = LiquidGlassDefaults.GlassBorder,
    glowColor: Color? = null,
    glowElevation: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val baseModifier = if (glowColor != null) {
        modifier.liquidGlow(glowColor, glowElevation, shape)
    } else {
        modifier
    }

    Box(
        modifier = baseModifier
            .liquidGlassPanel(shape, fillColor, borderColor)
            .glassSpecularHighlight(shape),
        content = content
    )
}

/**
 * An interactive glass button with a spring press animation and glow.
 * Pill-shaped by default, matching the inspiration images.
 */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = LiquidGlassDefaults.ShapePill,
    fillColor: Color = LiquidGlassDefaults.GlassFillStrong,
    borderColor: Color = LiquidGlassDefaults.GlassBorderBright,
    glowColor: Color? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessHigh
        ),
        label = "GlassButtonPressScale"
    )

    val baseModifier = if (glowColor != null) {
        modifier.liquidGlow(glowColor, 14.dp, shape)
    } else {
        modifier
    }

    Box(
        modifier = baseModifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .liquidGlassPanel(shape, fillColor, borderColor)
            .glassSpecularHighlight(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.15f)),
                enabled = enabled,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * A small circular glass icon button — used for playback controls.
 */
@Composable
fun LiquidGlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    fillColor: Color = LiquidGlassDefaults.GlassFillStrong,
    borderColor: Color = LiquidGlassDefaults.GlassBorder,
    glowColor: Color? = null,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "GlassIconButtonPressScale"
    )

    val baseModifier = if (glowColor != null) {
        modifier.liquidGlow(glowColor, 10.dp, LiquidGlassDefaults.ShapePill)
    } else {
        modifier
    }

    Box(
        modifier = baseModifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .liquidGlassPanel(LiquidGlassDefaults.ShapePill, fillColor, borderColor)
            .glassSpecularHighlight(LiquidGlassDefaults.ShapePill)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.2f)),
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}
