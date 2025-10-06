package com.tsavvy.pdfcreator

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
// Removed icons import - using text emoji instead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tsavvy.pdfcreator.ui.theme.PDFCreatorTheme
import com.tsavvy.pdfcreator.utils.LanguageHelper
import com.tsavvy.pdfcreator.utils.LanguageAwareComposable

class SettingsActivity : BaseActivity() {
    
    private fun restartApp() {
        // إنشاء Intent لإعادة فتح MainActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        
        // بدء MainActivity وإنهاء جميع الأنشطة السابقة
        startActivity(intent)
        finishAffinity()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            LanguageAwareComposable {
                PDFCreatorTheme {
                    SettingsScreen(
                        onBackPressed = { finish() },
                    onLanguageChanged = { languageCode ->
                        LanguageHelper.setLanguage(this@SettingsActivity, languageCode)
                        // تأخير قصير جداً لضمان حفظ اللغة قبل إعادة التشغيل
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            restartApp()
                        }, 200)
                    }
                    )
                }
            }
        }
    }
    
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    onLanguageChanged: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedLanguage by remember { 
        mutableStateOf(LanguageHelper.getLanguage(context))
    }
    
    // Helper function to get string resources
    fun getString(@androidx.annotation.StringRes id: Int): String {
        return context.getString(id)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(getString(R.string.settings_title)) },
                navigationIcon = {
                    TextButton(onClick = onBackPressed) {
                        Text(
                            text = "←",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Language Settings Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = getString(R.string.language_settings),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = getString(R.string.select_language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Language Selection
                    Column(
                        modifier = Modifier.selectableGroup()
                    ) {
                        // Arabic Option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedLanguage == "ar",
                                    onClick = {
                                        selectedLanguage = "ar"
                                        onLanguageChanged("ar")
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == "ar",
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = getString(R.string.language_arabic),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // English Option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedLanguage == "en",
                                    onClick = {
                                        selectedLanguage = "en"
                                        onLanguageChanged("en")
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == "en",
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = getString(R.string.language_english),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Success Message
            if (selectedLanguage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "✅",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = getString(R.string.language_changed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// Helper function to get string resources
@Composable
private fun getString(@androidx.annotation.StringRes id: Int): String {
    val context = LocalContext.current
    return context.getString(id)
}
