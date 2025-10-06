package com.tsavvy.pdfcreator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tsavvy.pdfcreator.ui.theme.PDFCreatorTheme
import com.tsavvy.pdfcreator.utils.LanguageAwareComposable
import java.io.Serializable

data class PageNumberSettings(
    val position: PageNumberPosition = PageNumberPosition.FOOTER_CENTER,
    val startNumber: Int = 1,
    val fontSize: Float = 12f,
    val showOnFirstPage: Boolean = true,
    val format: String = "{page}" // يمكن أن يكون "{page}" أو "صفحة {page}" أو "{page} of {total}"
) : Serializable

enum class PageNumberPosition {
    HEADER_LEFT,
    HEADER_CENTER,
    HEADER_RIGHT,
    FOOTER_LEFT,
    FOOTER_CENTER,
    FOOTER_RIGHT
}

class PageNumberSettingsActivity : BaseActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val currentSettings = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("current_settings", PageNumberSettings::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("current_settings") as? PageNumberSettings
        }
        
        setContent {
            LanguageAwareComposable {
                PDFCreatorTheme {
                    PageNumberSettingsScreen(
                        currentSettings = currentSettings,
                        onApply = { settings ->
                            val resultIntent = Intent().apply {
                                putExtra("page_number_settings", settings)
                            }
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        },
                        onCancel = {
                            setResult(Activity.RESULT_CANCELED)
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
fun PageNumberSettingsScreen(
    currentSettings: PageNumberSettings?,
    onApply: (PageNumberSettings) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    
    var selectedPosition by remember {
        mutableStateOf(currentSettings?.position ?: PageNumberPosition.FOOTER_CENTER) 
    }
    var startNumber by remember { 
        mutableStateOf((currentSettings?.startNumber ?: 1).toString()) 
    }
    var fontSize by remember { 
        mutableStateOf(currentSettings?.fontSize ?: 12f) 
    }
    var showOnFirstPage by remember { 
        mutableStateOf(currentSettings?.showOnFirstPage ?: true) 
    }
    var selectedFormat by remember { 
        mutableStateOf(currentSettings?.format ?: "{page}") 
    }
    
    fun getString(@androidx.annotation.StringRes id: Int): String {
        return context.getString(id)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(getString(R.string.page_numbers_title)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = getString(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = getString(R.string.page_number_position),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Header
                    Text(
                        text = getString(R.string.header),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PositionButton(
                            text = getString(R.string.left),
                            isSelected = selectedPosition == PageNumberPosition.HEADER_LEFT,
                            onClick = { selectedPosition = PageNumberPosition.HEADER_LEFT },
                            modifier = Modifier.weight(1f)
                        )
                        PositionButton(
                            text = getString(R.string.center),
                            isSelected = selectedPosition == PageNumberPosition.HEADER_CENTER,
                            onClick = { selectedPosition = PageNumberPosition.HEADER_CENTER },
                            modifier = Modifier.weight(1f)
                        )
                        PositionButton(
                            text = getString(R.string.right),
                            isSelected = selectedPosition == PageNumberPosition.HEADER_RIGHT,
                            onClick = { selectedPosition = PageNumberPosition.HEADER_RIGHT },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Footer
                    Text(
                        text = getString(R.string.footer),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PositionButton(
                            text = getString(R.string.left),
                            isSelected = selectedPosition == PageNumberPosition.FOOTER_LEFT,
                            onClick = { selectedPosition = PageNumberPosition.FOOTER_LEFT },
                            modifier = Modifier.weight(1f)
                        )
                        PositionButton(
                            text = getString(R.string.center),
                            isSelected = selectedPosition == PageNumberPosition.FOOTER_CENTER,
                            onClick = { selectedPosition = PageNumberPosition.FOOTER_CENTER },
                            modifier = Modifier.weight(1f)
                        )
                        PositionButton(
                            text = getString(R.string.right),
                            isSelected = selectedPosition == PageNumberPosition.FOOTER_RIGHT,
                            onClick = { selectedPosition = PageNumberPosition.FOOTER_RIGHT },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = getString(R.string.additional_settings),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = startNumber,
                        onValueChange = { 
                            if (it.isEmpty() || it.toIntOrNull() != null) {
                                startNumber = it
                            }
                        },
                        label = { Text(getString(R.string.start_number)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "${getString(R.string.font_size)}: ${fontSize.toInt()}",
                        fontSize = 14.sp
                    )
                    Slider(
                        value = fontSize,
                        onValueChange = { fontSize = it },
                        valueRange = 8f..20f,
                        steps = 11,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showOnFirstPage = !showOnFirstPage },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getString(R.string.show_on_first_page),
                            fontSize = 14.sp
                        )
                        Switch(
                            checked = showOnFirstPage,
                            onCheckedChange = { showOnFirstPage = it }
                        )
                    }
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = getString(R.string.numbering_format),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    FormatOption(
                        format = "{page}",
                        example = "1, 2, 3...",
                        isSelected = selectedFormat == "{page}",
                        onClick = { selectedFormat = "{page}" }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    FormatOption(
                        format = "صفحة {page}",
                        example = "صفحة 1, صفحة 2...",
                        isSelected = selectedFormat == "صفحة {page}",
                        onClick = { selectedFormat = "صفحة {page}" }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    FormatOption(
                        format = "{page} / {total}",
                        example = "1 / 10, 2 / 10...",
                        isSelected = selectedFormat == "{page} / {total}",
                        onClick = { selectedFormat = "{page} / {total}" }
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(getString(R.string.cancel))
                }
                
                Button(
                    onClick = {
                        val settings = PageNumberSettings(
                            position = selectedPosition,
                            startNumber = startNumber.toIntOrNull() ?: 1,
                            fontSize = fontSize,
                            showOnFirstPage = showOnFirstPage,
                            format = selectedFormat
                        )
                        onApply(settings)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(getString(R.string.apply))
                }
            }
        }
    }
}

@Composable
fun PositionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) 
                MaterialTheme.colorScheme.onPrimary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(text, fontSize = 12.sp)
    }
}

@Composable
fun FormatOption(
    format: String,
    example: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else 
                    Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = format,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = example,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
    }
}

