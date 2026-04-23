@file:kotlin.OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.theveloper.pixelplay.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.size.Size
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.ui.theme.GoogleSansRounded
import com.theveloper.pixelplay.ui.theme.LiquidGlassDefaults
import com.theveloper.pixelplay.ui.theme.LiquidGlassIconButton

internal val LocalMaterialTheme = staticCompositionLocalOf<ColorScheme> { error("No ColorScheme provided") }

val MiniPlayerHeight = 64.dp
const val ANIMATION_DURATION_MS = 255
val MiniPlayerBottomSpacer = 8.dp

@Composable
fun getNavigationBarHeight(): Dp {
    val insets = WindowInsets.safeDrawing.asPaddingValues()
    return insets.calculateBottomPadding()
}

@Composable
internal fun MiniPlayerContentInternal(
    song: Song,
    isPlaying: Boolean,
    isCastConnecting: Boolean,
    isPreparingPlayback: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    cornerRadiusAlb: Dp,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val controlsEnabled = !isCastConnecting && !isPreparingPlayback
    val scheme = LocalMaterialTheme.current

    // Liquid glass specular gradient for the top of the mini player
    val specularGradient = remember {
        Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to Color(0x30FFFFFF),
                0.5f to Color(0x00FFFFFF)
            ),
            start = Offset(0f, 0f),
            end   = Offset(400f, 100f)
        )
    }

    Box(modifier = modifier.fillMaxWidth().height(MiniPlayerHeight)) {
        // Glass specular shimmer overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(brush = specularGradient)
                }
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Album Art ──────────────────────────────────────────────────
            val albumArtModel = song.albumArtUriString?.takeIf { it.isNotBlank() }
            Box(contentAlignment = Alignment.Center) {
                key(song.id) {
                    SmartImage(
                        model = albumArtModel,
                        contentDescription = "Album art: ${song.title}",
                        shape = RoundedCornerShape(12.dp),
                        targetSize = Size(150, 150),
                        modifier = Modifier.size(44.dp),
                        placeholderModel = if (albumArtModel?.startsWith("telegram_art") == true) {
                            "$albumArtModel?quality=thumb"
                        } else null
                    )
                }
                if (isCastConnecting) {
                    CircularProgressIndicator(
                        modifier  = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color     = scheme.onPrimaryContainer
                    )
                } else if (isPreparingPlayback) {
                    CircularWavyProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ── Title + Artist ─────────────────────────────────────────────
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                val titleStyle = MaterialTheme.typography.titleSmall.copy(
                    fontSize     = 14.sp,
                    fontWeight   = FontWeight.SemiBold,
                    letterSpacing = (-0.3).sp,
                    fontFamily   = GoogleSansRounded,
                    color        = Color.White
                )
                val artistStyle = MaterialTheme.typography.bodySmall.copy(
                    fontSize     = 12.sp,
                    letterSpacing = 0.sp,
                    fontFamily   = GoogleSansRounded,
                    color        = Color.White.copy(alpha = 0.6f)
                )
                AutoScrollingText(
                    text = when {
                        isCastConnecting     -> "Connecting to device…"
                        isPreparingPlayback  -> "Preparing playback…"
                        else                 -> song.title
                    },
                    style = titleStyle,
                    gradientEdgeColor = Color.Transparent
                )
                AutoScrollingText(
                    text = if (isPreparingPlayback) "Loading audio…" else song.displayArtist,
                    style = artistStyle,
                    gradientEdgeColor = Color.Transparent
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // ── Controls — glass icon buttons ──────────────────────────────
            LiquidGlassIconButton(
                onClick  = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onPrevious()
                },
                modifier  = Modifier.size(36.dp),
                fillColor = LiquidGlassDefaults.GlassFillMedium,
                borderColor = LiquidGlassDefaults.GlassBorder,
                enabled  = controlsEnabled
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "Previous",
                    tint     = Color.White.copy(alpha = if (controlsEnabled) 0.9f else 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Play / Pause — highlighted with primary glow
            LiquidGlassIconButton(
                onClick  = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onPlayPause()
                },
                modifier    = Modifier.size(42.dp),
                fillColor   = scheme.primary.copy(alpha = 0.30f),
                borderColor = scheme.primary.copy(alpha = 0.60f),
                glowColor   = if (isPlaying) scheme.primary else null,
                enabled     = controlsEnabled
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint     = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            LiquidGlassIconButton(
                onClick  = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onNext()
                },
                modifier    = Modifier.size(36.dp),
                fillColor   = LiquidGlassDefaults.GlassFillMedium,
                borderColor = LiquidGlassDefaults.GlassBorder,
                enabled     = controlsEnabled
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Next",
                    tint     = Color.White.copy(alpha = if (controlsEnabled) 0.9f else 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
