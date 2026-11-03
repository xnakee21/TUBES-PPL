package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Branding: Sleek Midnight & Cyber Biometric Cyan
val MidnightDark = Color(0xFF080D1A)
val MidnightDarker = Color(0xFF040711)
val MidnightSurface = Color(0xFF0F172A)
val MidnightSurfaceVariant = Color(0xFF1E293B)
val MidnightCardBg = Color(0xFF131D31)
val MidnightBorder = Color(0xFF26354D)
val MidnightBorderGlow = Color(0x4000E5FF)

val CyanAccent = Color(0xFF00E5FF)
val CyanAccentDark = Color(0xFF00B4D8)
val CyanAccentGlow = Color(0x3300E5FF)
val ElectricBlue = Color(0xFF38BDF8)
val SoftIndigo = Color(0xFF6366F1)
val NeonPurple = Color(0xFF8B5CF6)

// Verification Verdict Colors
val GenuineGreen = Color(0xFF10B981)
val GenuineGreenLight = Color(0xFF34D399)
val GenuineGreenBg = Color(0x1F10B981)
val GenuineGreenGlow = Color(0x4D10B981)

val ForgeryRed = Color(0xFFEF4444)
val ForgeryRedLight = Color(0xFFF87171)
val ForgeryRedBg = Color(0x1FEF4444)
val ForgeryRedGlow = Color(0x4DEF4444)

val WarningAmber = Color(0xFFF59E0B)
val WarningAmberBg = Color(0x1FF59E0B)

val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
val TextTertiaryDark = Color(0xFF64748B)

// Gradients
val PrimaryCyberGradient = Brush.horizontalGradient(
    listOf(Color(0xFF00E5FF), Color(0xFF2563EB))
)

val GenuineVerdictGradient = Brush.linearGradient(
    listOf(Color(0xFF10B981), Color(0xFF059669))
)

val ForgeryVerdictGradient = Brush.linearGradient(
    listOf(Color(0xFFFF3366), Color(0xFFDC2626))
)

val CardGlassGradient = Brush.verticalGradient(
    listOf(Color(0xCC1E293B), Color(0xE60F172A))
)

val HeroGlowGradient = Brush.radialGradient(
    listOf(Color(0x2E00E5FF), Color(0x00000000))
)

// Light Theme Equivalents
val SurfaceLight = Color(0xFFF8FAFC)
val SurfaceLightCard = Color(0xFFFFFFFF)
val SurfaceLightBorder = Color(0xFFE2E8F0)
val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)
