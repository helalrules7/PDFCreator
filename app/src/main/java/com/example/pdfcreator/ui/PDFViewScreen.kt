package com.example.pdfcreator.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import com.example.pdfcreator.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PDFViewScreen(
    viewModel: PDFCreatorViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val state = viewModel.state

    Scaffold(
        topBar = {
            TopAppBar(
                    title = { Text(getString(R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = getString(R.string.back)
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.pdfCreated && state.pdfPath != null) {
                // أيقونة نجاح
                Card(
                    modifier = Modifier
                        .size(120.dp)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            fontSize = 48.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = getString(R.string.pdf_created_successfully),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // أزرار الإجراءات
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            android.util.Log.d("PDFCreator", "Share button clicked, PDF path: ${state.pdfPath}")
                            sharePDF(context, state.pdfPath ?: "")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(getString(R.string.share_pdf))
                    }

                    Button(
                        onClick = {
                            android.util.Log.d("PDFCreator", "Print button clicked, PDF path: ${state.pdfPath}")
                            printPDF(context, state.pdfPath ?: "")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🖨️ ${getString(R.string.print_pdf)}")
                    }

                    OutlinedButton(
                        onClick = {
                            android.util.Log.d("PDFCreator", "Open button clicked, PDF path: ${state.pdfPath}")
                            openPDF(context, state.pdfPath ?: "")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(getString(R.string.open_pdf))
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.clearImages()
                            onNavigateBack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(getString(R.string.create_new_pdf))
                    }
                }
            } else {
                // حالة التحميل أو الخطأ
                if (state.isCreatingPDF) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = getString(R.string.creating_pdf),
                        fontSize = 18.sp
                    )
                } else {
                    Text(
                        text = getString(R.string.error_creating_pdf),
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun sharePDF(context: Context, pdfPath: String) {
    android.util.Log.d("PDFCreator", "sharePDF called with path: $pdfPath")
    try {
        val file = File(pdfPath)
        android.util.Log.d("PDFCreator", "File exists: ${file.exists()}, size: ${file.length()}")
        if (file.exists()) {
            // استخدام FileProvider للمشاركة
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            android.util.Log.d("PDFCreator", "URI created: $uri")
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.pdf_from_creator))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            android.util.Log.d("PDFCreator", "Starting share intent")
            val chooser = Intent.createChooser(intent, context.getString(R.string.share_pdf_chooser))
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } else {
            android.util.Log.e("PDFCreator", "File does not exist: $pdfPath")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        android.util.Log.e("PDFCreator", "Error sharing PDF: ${e.message}")
    }
}

private fun openPDF(context: Context, pdfPath: String) {
    try {
        val file = File(pdfPath)
        if (file.exists()) {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } else {
            android.util.Log.e("PDFCreator", "File does not exist: $pdfPath")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        android.util.Log.e("PDFCreator", "Error opening PDF: ${e.message}")
    }
}

private fun printPDF(context: Context, pdfPath: String) {
    try {
        val file = File(pdfPath)
        if (file.exists()) {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            // محاولة استخدام PrintManager للطباعة المباشرة (Android 4.4+)
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as android.print.PrintManager
                    val jobName = "PDF_${System.currentTimeMillis()}"
                    
                    // حساب عدد صفحات PDF
                    val pageCount = getPDFPageCount(file)
                    android.util.Log.d("PDFCreator", "PDF has $pageCount pages")
                    
                    // إنشاء PrintDocumentAdapter محسن
                    val printAdapter = object : android.print.PrintDocumentAdapter() {
                        override fun onWrite(
                            pages: Array<out android.print.PageRange>?,
                            destination: android.os.ParcelFileDescriptor?,
                            cancellationSignal: android.os.CancellationSignal?,
                            callback: android.print.PrintDocumentAdapter.WriteResultCallback?
                        ) {
                            try {
                                val input = file.inputStream()
                                val output = android.os.ParcelFileDescriptor.AutoCloseOutputStream(destination)
                                input.copyTo(output)
                                input.close()
                                output.close()
                                
                                // إرجاع جميع الصفحات المطلوبة
                                val pageRanges = pages ?: arrayOf(android.print.PageRange.ALL_PAGES)
                                callback?.onWriteFinished(pageRanges)
                            } catch (e: Exception) {
                                android.util.Log.e("PDFCreator", "Error writing PDF: ${e.message}")
                                callback?.onWriteFailed(e.message)
                            }
                        }
                        
                        override fun onLayout(
                            oldAttributes: android.print.PrintAttributes?,
                            newAttributes: android.print.PrintAttributes?,
                            cancellationSignal: android.os.CancellationSignal?,
                            callback: android.print.PrintDocumentAdapter.LayoutResultCallback?,
                            extras: android.os.Bundle?
                        ) {
                            if (cancellationSignal?.isCanceled == true) {
                                callback?.onLayoutCancelled()
                                return
                            }
                            
                            val info = android.print.PrintDocumentInfo.Builder(jobName)
                                .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                                .setPageCount(pageCount) // استخدام العدد الصحيح للصفحات
                                .build()
                            callback?.onLayoutFinished(info, true)
                        }
                    }
                    
                    printManager.print(jobName, printAdapter, null)
                } else {
                    // للأنظمة الأقدم، استخدم Intent عادي
                    val printIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.print_pdf_subject))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val printChooser = Intent.createChooser(printIntent, context.getString(R.string.print_pdf_chooser))
                    context.startActivity(printChooser)
                }
            } catch (e: Exception) {
                android.util.Log.e("PDFCreator", "Error with PrintManager: ${e.message}")
                // في حالة فشل الطباعة المباشرة، استخدم مشاركة عادية
                val printIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.print_pdf_subject))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val shareIntent = Intent.createChooser(printIntent, context.getString(R.string.print_pdf_chooser_alt))
                context.startActivity(shareIntent)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        android.util.Log.e("PDFCreator", "Error printing PDF: ${e.message}")
    }
}

private fun getPDFPageCount(file: File): Int {
    return try {
        val reader = com.itextpdf.kernel.pdf.PdfReader(file)
        val pdfDocument = com.itextpdf.kernel.pdf.PdfDocument(reader)
        val pageCount = pdfDocument.numberOfPages
        pdfDocument.close()
        reader.close()
        pageCount
    } catch (e: Exception) {
        android.util.Log.e("PDFCreator", "Error reading PDF page count: ${e.message}")
        1 // fallback to 1 page
    }
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1 -> "%.1f MB".format(mb)
        kb >= 1 -> "%.1f KB".format(kb)
        else -> "$bytes B"
    }
}

private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
    return formatter.format(date)
}

// Helper function to get string resources
@Composable
private fun getString(@androidx.annotation.StringRes id: Int): String {
    val context = LocalContext.current
    return context.getString(id)
}
