package com.example.pdfcreator.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LanguageHelper {
    private const val PREFS_NAME = "language_prefs"
    private const val LANGUAGE_KEY = "selected_language"
    
    fun setLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(LANGUAGE_KEY, languageCode).commit() // استخدام commit بدلاً من apply
    }
    
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(LANGUAGE_KEY, "ar") ?: "ar"
    }
    
    fun applyLanguage(context: Context): Context {
        val language = getLanguage(context)
        val locale = Locale.forLanguageTag(language)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            return context
        }
    }
    
    fun applyLanguageToActivity(activity: android.app.Activity) {
        val language = getLanguage(activity)
        val locale = Locale.forLanguageTag(language)
        Locale.setDefault(locale)
        
        val config = Configuration(activity.resources.configuration)
        config.setLocale(locale)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity.applyOverrideConfiguration(config)
        } else {
            @Suppress("DEPRECATION")
            activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
        }
    }
    
    fun getSupportedLanguages(): List<Pair<String, String>> {
        return listOf(
            "ar" to "العربية",
            "en" to "English"
        )
    }
}
