package com.example.pdfcreator.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PDFPrimary, // DodgerBlue
    secondary = PDFSecondary, // DeepSkyBlue
    tertiary = PDFAccent, // لون مميز
    background = PDFBackground, // خلفية مائلة للأزرق الغامق
    surface = PDFSurface, // سطح أزرق داكن
    surfaceVariant = PDFCardBackground, // خلفية البطاقات
    onPrimary = PDFOnPrimary, // أبيض
    onSecondary = PDFOnSecondary, // أسود
    onBackground = PDFOnBackground, // رمادي فاتح جداً
    onSurface = PDFOnSurface, // رمادي فاتح
    onSurfaceVariant = PDFTextSecondary, // نص ثانوي
    error = PDFError, // أحمر فاتح
    onError = PDFOnError, // أبيض
    outline = PDFBorder, // حدود DodgerBlue
    outlineVariant = PDFBorder.copy(alpha = 0.5f) // حدود شفافة
)

private val LightColorScheme = lightColorScheme(
    primary = PDFPrimary, // DodgerBlue
    secondary = PDFSecondary, // DeepSkyBlue
    tertiary = PDFAccent, // لون مميز
    background = PDFBackground, // خلفية مائلة للأزرق الغامق
    surface = PDFSurface, // سطح أزرق داكن
    surfaceVariant = PDFCardBackground, // خلفية البطاقات
    onPrimary = PDFOnPrimary, // أبيض
    onSecondary = PDFOnSecondary, // أسود
    onBackground = PDFOnBackground, // رمادي فاتح جداً
    onSurface = PDFOnSurface, // رمادي فاتح
    onSurfaceVariant = PDFTextSecondary, // نص ثانوي
    error = PDFError, // أحمر فاتح
    onError = PDFOnError, // أبيض
    outline = PDFBorder, // حدود DodgerBlue
    outlineVariant = PDFBorder.copy(alpha = 0.5f) // حدود شفافة
)

@Composable
fun PDFCreatorTheme(
    darkTheme: Boolean = true, // فرض Dark Mode دائماً
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // تعطيل الألوان الديناميكية
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme // استخدام Dark Mode دائماً

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}