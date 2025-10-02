package com.example.pdfcreator

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pdfcreator.ui.theme.PDFCreatorTheme
import com.example.pdfcreator.utils.LanguageAwareComposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MyPDFsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            LanguageAwareComposable {
                PDFCreatorTheme {
                    MyPDFsScreen(
                        onBackPressed = { finish() },
                        onViewPDF = { pdfFile ->
                            // فتح MainActivity مع PDFViewScreen
                            val intent = Intent(this, MainActivity::class.java).apply {
                                putExtra("show_pdf_view", true)
                                putExtra("pdf_path", pdfFile.absolutePath)
                                putExtra("pdf_title", pdfFile.nameWithoutExtension.removePrefix("H PDF Creator - "))
                            }
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

data class PDFFileInfo(
    val file: File,
    val name: String,
    val size: String,
    val date: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPDFsScreen(
    onBackPressed: () -> Unit,
    onViewPDF: (File) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var pdfFiles by remember { mutableStateOf<List<PDFFileInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<PDFFileInfo?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    
    fun getString(@androidx.annotation.StringRes id: Int, vararg formatArgs: Any): String {
        return if (formatArgs.isEmpty()) {
            context.getString(id)
        } else {
            context.getString(id, *formatArgs)
        }
    }
    
    // تحميل ملفات PDF
    fun loadPDFFiles() {
        scope.launch {
            try {
                isLoading = true
                val files = withContext(Dispatchers.IO) {
                    val directory = context.getExternalFilesDir(null)
                    directory?.listFiles { file ->
                        file.isFile && file.extension.equals("pdf", ignoreCase = true)
                    }?.map { file ->
                        val sizeInKB = file.length() / 1024
                        val sizeInMB = sizeInKB / 1024.0
                        val sizeText = if (sizeInMB >= 1.0) {
                            String.format("%.2f MB", sizeInMB)
                        } else {
                            String.format("%d KB", sizeInKB)
                        }
                        
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        val dateText = dateFormat.format(Date(file.lastModified()))
                        
                        PDFFileInfo(
                            file = file,
                            name = file.nameWithoutExtension.removePrefix("H PDF Creator - "),
                            size = sizeText,
                            date = dateText
                        )
                    }?.sortedByDescending { it.file.lastModified() } ?: emptyList()
                }
                pdfFiles = files
                isLoading = false
            } catch (e: Exception) {
                e.printStackTrace()
                isLoading = false
            }
        }
    }
    
    // تحميل الملفات عند بدء الشاشة
    LaunchedEffect(Unit) {
        loadPDFFiles()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(getString(R.string.my_pdfs_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = getString(R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = {
            if (snackbarMessage != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { snackbarMessage = null }) {
                            Text(getString(R.string.close))
                        }
                    }
                ) {
                    Text(snackbarMessage!!)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = getString(R.string.loading_pdfs),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                pdfFiles.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📄",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = getString(R.string.no_pdfs_found),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = getString(R.string.no_pdfs_message),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pdfFiles, key = { it.file.absolutePath }) { pdfInfo ->
                            PDFFileCard(
                                pdfInfo = pdfInfo,
                                onView = { onViewPDF(pdfInfo.file) },
                                onDelete = {
                                    fileToDelete = pdfInfo
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // حوار تأكيد الحذف
    if (showDeleteDialog && fileToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(getString(R.string.delete_pdf)) },
            text = { 
                Column {
                    Text(getString(R.string.delete_confirmation))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = fileToDelete!!.name,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val deleted = withContext(Dispatchers.IO) {
                                    fileToDelete!!.file.delete()
                                }
                                if (deleted) {
                                    snackbarMessage = context.getString(R.string.pdf_deleted)
                                    loadPDFFiles() // إعادة تحميل القائمة
                                } else {
                                    snackbarMessage = context.getString(R.string.error_deleting_pdf)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                snackbarMessage = context.getString(R.string.error_deleting_pdf)
                            }
                            showDeleteDialog = false
                            fileToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(getString(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    fileToDelete = null
                }) {
                    Text(getString(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun PDFFileCard(
    pdfInfo: PDFFileInfo,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    
    fun getString(@androidx.annotation.StringRes id: Int): String {
        return context.getString(id)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // أيقونة PDF
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📄",
                    fontSize = 32.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // معلومات الملف
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = pdfInfo.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📊 ${pdfInfo.size}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "📅 ${pdfInfo.date}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // أزرار الإجراءات
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // زر الاستعراض
                Button(
                    onClick = onView,
                    modifier = Modifier.width(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = getString(R.string.view_pdf),
                        fontSize = 12.sp
                    )
                }
                
                // زر الحذف
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.width(100.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = getString(R.string.delete_pdf),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

