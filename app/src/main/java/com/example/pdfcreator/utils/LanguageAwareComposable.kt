package com.example.pdfcreator.utils

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import java.util.*

@Composable
fun LanguageAwareComposable(
    content: @Composable () -> Unit
) {
    content()
}
