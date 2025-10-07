package com.tsavvy.pdfcreator.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import com.tsavvy.pdfcreator.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PDFViewScreen(
    viewModel: PDFCreatorViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val state = viewModel.state
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // حالة معاينة الصفحة الأولى
    var firstPagePreview by remember { mutableStateOf<Bitmap?>(null) }
    
    // متغير للتحكم في إعادة التحميل عند العودة للشاشة
    var reloadTrigger by remember { mutableStateOf(0) }
    
    // مراقبة lifecycle لإعادة تحميل الـ thumbnail عند العودة للشاشة
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // عند العودة للشاشة، نزيد counter لتحفيز إعادة التحميل
                reloadTrigger++
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // التحقق من وجود الملف والعودة للشاشة الرئيسية إذا تم حذفه
    LaunchedEffect(state.pdfPath, reloadTrigger) {
        if (state.pdfPath != null) {
            val file = File(state.pdfPath)
            if (!file.exists()) {
                // الملف تم حذفه - نمسح الحالة ونعود للشاشة الرئيسية
                viewModel.clearImages()
                onNavigateBack()
            }
        }
    }
    
    // تحميل معاينة الصفحة الأولى
    LaunchedEffect(state.pdfPath, reloadTrigger) {
        if (state.pdfPath != null) {
            val file = File(state.pdfPath)
            if (file.exists()) {
                firstPagePreview = null // إعادة تعيين لإظهار مؤشر التحميل
                firstPagePreview = loadFirstPagePreview(state.pdfPath)
            }
        }
    }

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
                // معاينة الصفحة الأولى أو أيقونة نجاح
                Card(
                    modifier = Modifier
                        .width(200.dp)
                        .height(280.dp)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (firstPagePreview != null) {
                            // عرض معاينة الصفحة الأولى
                            Image(
                                bitmap = firstPagePreview!!.asImageBitmap(),
                                contentDescription = "PDF Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            // عرض أيقونة تحميل أو نجاح
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "جاري التحميل...",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // عرض الرسالة المناسبة حسب نوع الملف
                if (state.isNewPDF) {
                    // ملف جديد - عرض رسالة النجاح
                    Text(
                        text = getString(R.string.pdf_created_successfully),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                } else {
                    // ملف موجود - عرض اسم الملف
                    val fileName = state.pdfPath?.substringAfterLast("/") ?: state.pdfTitle
                    Text(
                        text = fileName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // أزرار الإجراءات
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Open
                    Button(
                        onClick = {
                            openPDF(context, state.pdfPath ?: "")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(getString(R.string.open_pdf))
                    }

                    // 2. Share
                    Button(
                        onClick = {
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

                    // 3. Print
                    Button(
                        onClick = {
                            printPDF(context, state.pdfPath ?: "")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🖨️ ${getString(R.string.print_pdf)}")
                    }

                    // 4. Edit
                    Button(
                        onClick = {
                            val intent = Intent(context, com.tsavvy.pdfcreator.EditPDFActivity::class.java).apply {
                                putExtra("pdf_path", state.pdfPath)
                                putExtra("pdf_title", state.pdfTitle)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("✏️ ${getString(R.string.edit_pdf)}")
                    }

                    // 5. Create New
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
    try {
        val file = File(pdfPath)
        if (file.exists()) {
            // استخدام FileProvider للمشاركة
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.pdf_from_creator))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, context.getString(R.string.share_pdf_chooser))
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } else {
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
        if (!file.exists()) {
            android.util.Log.e("PDFCreator", "File not found: $pdfPath")
            return
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? android.print.PrintManager
            if (printManager == null) {
                android.util.Log.e("PDFCreator", "PrintManager not available")
                return
            }
            
            val jobName = "${context.getString(R.string.app_name)}_${file.nameWithoutExtension}"
            
            // استخدام PdfDocument.Page لإنشاء PrintDocumentAdapter
            val printAdapter = object : android.print.PrintDocumentAdapter() {
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
                    
                    try {
                        val pageCount = getPDFPageCount(file)
                        val info = android.print.PrintDocumentInfo.Builder(jobName)
                            .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .setPageCount(pageCount)
                            .build()
                        callback?.onLayoutFinished(info, true)
                    } catch (e: Exception) {
                        android.util.Log.e("PDFCreator", "Error in onLayout: ${e.message}")
                        callback?.onLayoutFailed(e.message)
                    }
                }
                
                override fun onWrite(
                    pages: Array<out android.print.PageRange>?,
                    destination: android.os.ParcelFileDescriptor?,
                    cancellationSignal: android.os.CancellationSignal?,
                    callback: android.print.PrintDocumentAdapter.WriteResultCallback?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onWriteCancelled()
                        return
                    }
                    
                    try {
                        file.inputStream().use { input ->
                            android.os.ParcelFileDescriptor.AutoCloseOutputStream(destination).use { output ->
                                input.copyTo(output)
                            }
                        }
                        callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        android.util.Log.e("PDFCreator", "Error in onWrite: ${e.message}", e)
                        callback?.onWriteFailed(e.message)
                    }
                }
            }
            
            // فتح نافذة الطباعة
            printManager.print(jobName, printAdapter, null)
            android.util.Log.d("PDFCreator", "Print dialog opened successfully")
        } else {
            // للنسخ الأقدم من Android (قبل KitKat)
            android.widget.Toast.makeText(
                context,
                "Printing requires Android 4.4 or higher",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    } catch (e: Exception) {
        android.util.Log.e("PDFCreator", "Error printing PDF: ${e.message}", e)
        android.widget.Toast.makeText(
            context,
            "Error: ${e.message}",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}

/**
 * تحميل معاينة للصفحة الأولى من ملف PDF
 * @param pdfPath مسار ملف PDF
 * @return Bitmap للصفحة الأولى أو null في حالة الفشل
 */
private suspend fun loadFirstPagePreview(pdfPath: String): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val file = File(pdfPath)
        if (!file.exists()) {
            android.util.Log.e("PDFViewScreen", "ملف PDF غير موجود: $pdfPath")
            return@withContext null
        }
        
        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(fileDescriptor)
        
        if (pdfRenderer.pageCount == 0) {
            android.util.Log.e("PDFViewScreen", "ملف PDF فارغ")
            pdfRenderer.close()
            fileDescriptor.close()
            return@withContext null
        }
        
        // فتح الصفحة الأولى
        val page = pdfRenderer.openPage(0)
        
        // إنشاء Bitmap بالحجم المناسب
        val bitmap = Bitmap.createBitmap(
            page.width * 2, // مضاعفة الدقة للحصول على جودة أفضل
            page.height * 2,
            Bitmap.Config.ARGB_8888
        )
        
        // رسم الصفحة على Bitmap
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        
        // إغلاق الموارد
        page.close()
        pdfRenderer.close()
        fileDescriptor.close()
        
        bitmap
    } catch (e: Exception) {
        android.util.Log.e("PDFViewScreen", "خطأ في تحميل معاينة PDF: ${e.message}", e)
        null
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
