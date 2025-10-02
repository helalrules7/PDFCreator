package com.example.pdfcreator

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.pdfcreator.utils.LanguageHelper
import java.util.*

abstract class BaseActivity : ComponentActivity() {
    
    override fun attachBaseContext(newBase: Context) {
        val language = LanguageHelper.getLanguage(newBase)
        val locale = Locale.forLanguageTag(language)
        Locale.setDefault(locale)
        
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }
}

@Composable
fun getLocalizedContext(): Context {
    return LocalContext.current
}
