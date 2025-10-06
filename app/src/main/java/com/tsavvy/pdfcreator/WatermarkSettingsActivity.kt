package com.tsavvy.pdfcreator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tsavvy.pdfcreator.ui.theme.PDFCreatorTheme
import com.tsavvy.pdfcreator.utils.LanguageAwareComposable

/**
 * إعدادات العلامة المائية
 */
data class WatermarkSettings(
    val text: String = "",
    val color: Color = Color.Gray,
    val fontSize: Float = 48f,
    val opacity: Float = 0.3f,
    val rotation: Int = -45 // درجة الميل
) : java.io.Serializable

/**
 * Activity لإعدادات العلامة المائية
 */
class WatermarkSettingsActivity : BaseActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val currentSettings = intent.getSerializableExtra("current_settings") as? WatermarkSettings
        
        setContent {
            LanguageAwareComposable {
                PDFCreatorTheme {
                    WatermarkSettingsScreen(
                        currentSettings = currentSettings,
                        onApply = { settings ->
                            val resultIntent = Intent().apply {
                                putExtra("watermark_settings", settings)
                            }
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        },
                        onBack = {
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatermarkSettingsScreen(
    currentSettings: WatermarkSettings?,
    onApply: (WatermarkSettings) -> Unit,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var text by rememberSaveable { mutableStateOf(currentSettings?.text ?: "") }
    var selectedColor by remember { mutableStateOf(currentSettings?.color ?: Color.Gray) }
    var fontSize by rememberSaveable { mutableStateOf(currentSettings?.fontSize ?: 48f) }
    var opacity by rememberSaveable { mutableStateOf(currentSettings?.opacity ?: 0.3f) }
    var rotation by rememberSaveable { mutableStateOf(currentSettings?.rotation ?: -45) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.watermark_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = context.getString(R.string.back)
                        )
                    }
                },
                actions = {
                    // زر التطبيق
                    TextButton(
                        onClick = {
                            if (text.isNotBlank()) {
                                onApply(
                                    WatermarkSettings(
                                        text = text,
                                        color = selectedColor,
                                        fontSize = fontSize,
                                        opacity = opacity,
                                        rotation = rotation
                                    )
                                )
                            }
                        },
                        enabled = text.isNotBlank()
                    ) {
                        Text(
                            text = context.getString(R.string.apply),
                            fontWeight = FontWeight.Bold
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // نص العلامة المائية
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(context.getString(R.string.watermark_text)) },
                placeholder = { Text(context.getString(R.string.watermark_text_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )
            
            // معاينة العلامة المائية
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (text.isNotBlank()) {
                        Text(
                            text = text,
                            fontSize = (fontSize / 3).sp, // scaled down for preview
                            color = selectedColor.copy(alpha = opacity),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.rotate(rotation.toFloat())
                        )
                    } else {
                        Text(
                            text = context.getString(R.string.watermark_preview),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            
            // اختيار اللون
            Text(
                text = context.getString(R.string.watermark_color),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            ColorPicker(
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it }
            )
            
            // حجم الخط
            Text(
                text = context.getString(R.string.watermark_font_size),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${fontSize.toInt()}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = fontSize,
                    onValueChange = { fontSize = it },
                    valueRange = 20f..100f,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                )
            }
            
            // الشفافية
            Text(
                text = context.getString(R.string.watermark_opacity),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${(opacity * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = opacity,
                    onValueChange = { opacity = it },
                    valueRange = 0.1f..1f,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                )
            }
            
            // زاوية الميل
            Text(
                text = context.getString(R.string.watermark_rotation),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${rotation}°",
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = rotation.toFloat(),
                    onValueChange = { rotation = it.toInt() },
                    valueRange = -90f..90f,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun ColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val colors = listOf(
        Color.Black to context.getString(R.string.color_black),
        Color.Gray to context.getString(R.string.color_gray),
        Color.Red to context.getString(R.string.color_red),
        Color.Blue to context.getString(R.string.color_blue),
        Color.Green to context.getString(R.string.color_green),
        Color(0xFFFF9800) to context.getString(R.string.color_orange),
        Color(0xFF9C27B0) to context.getString(R.string.color_purple)
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            colors.take(4).forEach { (color, name) ->
                ColorButton(
                    color = color,
                    name = name,
                    isSelected = selectedColor == color,
                    onClick = { onColorSelected(color) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            colors.drop(4).forEach { (color, name) ->
                ColorButton(
                    color = color,
                    name = name,
                    isSelected = selectedColor == color,
                    onClick = { onColorSelected(color) },
                    modifier = Modifier.weight(1f)
                )
            }
            // مساحة فارغة للمحاذاة
            repeat(4 - colors.drop(4).size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ColorButton(
    color: Color,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (isSelected) {
                        Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    } else {
                        Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    }
                )
        )
        
        Text(
            text = name,
            fontSize = 10.sp,
            color = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

