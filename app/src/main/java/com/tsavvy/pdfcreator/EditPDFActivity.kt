package com.tsavvy.pdfcreator

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tsavvy.pdfcreator.ui.theme.PDFCreatorTheme
import com.tsavvy.pdfcreator.utils.LanguageAwareComposable
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * ==========================================
 * محرر PDF - Activity الرئيسية
 * ==========================================
 * هذا الـ Activity مسؤول عن تحرير ملفات PDF
 * يسمح بإعادة ترتيب الصفحات وتدويرها
 * مع حفظ تلقائي وحفظ يدوي
 */
class EditPDFActivity : BaseActivity() {
    
    private var pdfPath: String? = null
    private var pdfTitle: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // استقبال بيانات PDF من Intent
        pdfPath = intent.getStringExtra("pdf_path")
        pdfTitle = intent.getStringExtra("pdf_title")
        
        setContent {
            LanguageAwareComposable {
                PDFCreatorTheme {
                    EditPDFScreen(
                        pdfPath = pdfPath ?: "",
                        pdfTitle = pdfTitle ?: "PDF",
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }
}

/**
 * ==========================================
 * بيانات صفحة PDF
 * ==========================================
 * Data class تحتوي على معلومات كل صفحة
 */
data class PDFPageData(
    val pageNumber: Int,          // رقم الصفحة الأصلي
    val bitmap: Bitmap?,          // صورة معاينة الصفحة
    val originalRotation: Int = 0,// التدوير الأصلي في الملف
    val rotation: Int = 0,        // درجة التدوير الحالية (0, 90, 180, 270)
    val isDragging: Boolean = false  // هل يتم سحب الصفحة حالياً
)

/**
 * ==========================================
 * شاشة تحرير PDF
 * ==========================================
 * الشاشة الرئيسية لتحرير ملف PDF
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPDFScreen(
    pdfPath: String,
    pdfTitle: String,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // ==================== الحالات (States) ====================
    var pages by remember { mutableStateOf<List<PDFPageData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isAutoSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var lastModificationTime by remember { mutableStateOf(0L) }
    var needsReload by remember { mutableStateOf(false) }
    var reloadTrigger by remember { mutableStateOf(0) }
    
    // ==================== تحميل صفحات PDF ====================
    LaunchedEffect(pdfPath) {
        if (pdfPath.isNotEmpty()) {
            try {
                val loadedPages = loadPDFPages(context, pdfPath)
                pages = loadedPages
                isLoading = false
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = e.message
                isLoading = false
            }
        }
    }
    
    // ==================== إعادة تحميل الصفحات بعد الحفظ ====================
    LaunchedEffect(reloadTrigger) {
        if (reloadTrigger > 0 && needsReload) {
            try {
                val reloadedPages = loadPDFPages(context, pdfPath)
                pages = reloadedPages
                needsReload = false
            } catch (e: Exception) {
                e.printStackTrace()
                needsReload = false
            }
        }
    }
    
    // ==================== الحفظ التلقائي ====================
    // تم تعطيل الحفظ التلقائي - الحفظ يتم يدوياً فقط
    
    // ==================== دالة مساعدة للحصول على النصوص ====================
    fun getString(@androidx.annotation.StringRes id: Int): String {
        return context.getString(id)
    }
    
    // ==================== التصميم الرئيسي ====================
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(getString(R.string.edit_pdf_title))
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = getString(R.string.back)
                        )
                    }
                },
                actions = {
                    // زر الحفظ اليدوي
                    if (!isLoading && pages.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                isSaving = true
                                scope.launch {
                                    try {
                                        savePDFToOriginalPath(context, pdfPath, pages)
                                        hasUnsavedChanges = false
                                        
                                        isSaving = false
                                        
                                        // عرض رسالة Toast
                                        val fileName = pdfPath.substringAfterLast("/")
                                        withContext(Dispatchers.Main) {
                                            android.widget.Toast.makeText(
                                                context,
                                                "تم حفظ الملف $fileName بنجاح",
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        
                                        // الانتقال إلى الشاشة الرئيسية
                                        delay(500) // انتظار قصير لعرض Toast
                                        withContext(Dispatchers.Main) {
                                            val intent = Intent(context, MainActivity::class.java).apply {
                                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                            }
                                            context.startActivity(intent)
                                            (context as? android.app.Activity)?.finish()
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("EditPDF", "❌ فشل الحفظ اليدوي: ${e.message}")
                                        e.printStackTrace()
                                        
                                        withContext(Dispatchers.Main) {
                                            android.widget.Toast.makeText(
                                                context,
                                                "فشل في حفظ الملف: ${e.message}",
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        
                                        errorMessage = e.message
                                        isSaving = false
                                    }
                                }
                            },
                            enabled = !isSaving && !needsReload
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = getString(R.string.save_pdf),
                                    tint = if (hasUnsavedChanges) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // ==================== حالة التحميل ====================
                isLoading -> {
                    LoadingView()
                }
                // ==================== حالة الخطأ ====================
                errorMessage != null -> {
                    ErrorView(errorMessage = errorMessage)
                }
                // ==================== حالة فارغة ====================
                pages.isEmpty() -> {
                    EmptyView()
                }
                // ==================== عرض الصفحات ====================
                else -> {
                    PDFPagesListView(
                        pages = pages,
                        onPagesChanged = { newPages ->
                            pages = newPages
                            hasUnsavedChanges = true
                            lastModificationTime = System.currentTimeMillis()
                        }
                    )
                }
            }
        }
    }
}

/**
 * ==========================================
 * عرض قائمة صفحات PDF
 * ==========================================
 * يعرض جميع صفحات PDF مع إمكانية التحرير
 */
@Composable
private fun PDFPagesListView(
    pages: List<PDFPageData>,
    onPagesChanged: (List<PDFPageData>) -> Unit
) {
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            items = pages,
            key = { _, page -> page.pageNumber }
        ) { index, page ->
            PDFPageItemCard(
                page = page,
                index = index,
                totalPages = pages.size,
                isDragging = draggedIndex == index,
                onRotate = {
                    // تدوير الصفحة بمقدار 90 درجة
                    val newPages = pages.toMutableList()
                    val oldRotation = newPages[index].rotation
                    val newRotation = (oldRotation + 90) % 360
                    newPages[index] = newPages[index].copy(rotation = newRotation)
                    newPages.forEachIndexed { i, p -> 
                    }
                    onPagesChanged(newPages)
                },
                onMoveUp = {
                    // نقل الصفحة للأعلى
                    if (index > 0) {
                        val newPages = pages.toMutableList()
                        val temp = newPages[index]
                        newPages[index] = newPages[index - 1]
                        newPages[index - 1] = temp
                        newPages.forEachIndexed { i, p -> 
                        }
                        onPagesChanged(newPages)
                    }
                },
                onMoveDown = {
                    // نقل الصفحة للأسفل
                    if (index < pages.size - 1) {
                        val newPages = pages.toMutableList()
                        val temp = newPages[index]
                        newPages[index] = newPages[index + 1]
                        newPages[index + 1] = temp
                        newPages.forEachIndexed { i, p -> 
                        }
                        onPagesChanged(newPages)
                    }
                },
                onDragStart = {
                    draggedIndex = index
                },
                onDragEnd = {
                    draggedIndex = null
                }
            )
        }
    }
}

/**
 * ==========================================
 * بطاقة صفحة PDF
 * ==========================================
 * تعرض معاينة الصفحة مع أزرار التحكم
 */
@Composable
private fun PDFPageItemCard(
    page: PDFPageData,
    index: Int,
    totalPages: Int,
    isDragging: Boolean,
    onRotate: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit
) {
    val context = LocalContext.current
    val elevation by animateDpAsState(if (isDragging) 8.dp else 2.dp)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation)
            .pointerInput(Unit) {
                // إضافة إمكانية السحب بالضغط المطول
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() },
                    onDrag = { _, _ -> }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ==================== معاينة الصفحة ====================
            PagePreview(page = page)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // ==================== معلومات الصفحة ====================
            PageInfo(
                page = page,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // ==================== أزرار التحكم ====================
            ControlButtons(
                index = index,
                totalPages = totalPages,
                onRotate = onRotate,
                onMoveUp = onMoveUp,
                onMoveDown = onMoveDown
            )
        }
    }
}

/**
 * ==========================================
 * معاينة الصفحة
 * ==========================================
 */
@Composable
private fun PagePreview(page: PDFPageData) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        if (page.bitmap != null) {
            // حساب التدوير الإضافي فقط (الفرق بين التدوير الحالي والأصلي)
            val additionalRotation = (page.rotation - page.originalRotation + 360) % 360
            val rotatedBitmap = remember(page.bitmap, additionalRotation) {
                if (additionalRotation == 0) {
                    page.bitmap
                } else {
                    rotateBitmap(page.bitmap, additionalRotation.toFloat())
                }
            }
            Image(
                bitmap = rotatedBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = "📄",
                fontSize = 32.sp
            )
        }
    }
}

/**
 * ==========================================
 * معلومات الصفحة
 * ==========================================
 */
@Composable
private fun PageInfo(
    page: PDFPageData,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Column(modifier = modifier) {
        Text(
            text = context.getString(R.string.page_number, page.pageNumber),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        if (page.rotation != 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = context.getString(R.string.rotated, page.rotation),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * ==========================================
 * أزرار التحكم
 * ==========================================
 */
@Composable
private fun ControlButtons(
    index: Int,
    totalPages: Int,
    onRotate: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val context = LocalContext.current
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // زر تحريك للأعلى
        IconButton(
            onClick = onMoveUp,
            enabled = index > 0
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = context.getString(R.string.move_up),
                tint = if (index > 0) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
        
        // زر التدوير
        FilledTonalIconButton(onClick = onRotate) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = context.getString(R.string.rotate_page)
            )
        }
        
        // زر تحريك للأسفل
        IconButton(
            onClick = onMoveDown,
            enabled = index < totalPages - 1
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = context.getString(R.string.move_down),
                tint = if (index < totalPages - 1) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

/**
 * ==========================================
 * عرض حالة التحميل
 * ==========================================
 */
@Composable
private fun LoadingView() {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = context.getString(R.string.loading_pdf_pages),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * ==========================================
 * عرض حالة الخطأ
 * ==========================================
 */
@Composable
private fun ErrorView(errorMessage: String?) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "❌",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = context.getString(R.string.error_loading_pdf),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * ==========================================
 * عرض حالة فارغة
 * ==========================================
 */
@Composable
private fun EmptyView() {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📄",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = context.getString(R.string.error_loading_pdf),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==========================================
// دوال مساعدة - Helper Functions
// ==========================================

/**
 * تحميل صفحات PDF من الملف
 * @param context السياق
 * @param pdfPath مسار ملف PDF
 * @return قائمة بصفحات PDF
 */
private suspend fun loadPDFPages(
    context: Context, 
    pdfPath: String
): List<PDFPageData> = withContext(Dispatchers.IO) {
    
    val pages = mutableListOf<PDFPageData>()
    val file = File(pdfPath)
    
    try {
        // قراءة دوران الصفحات من PDF باستخدام iTextPDF
        val pdfReader = PdfReader(file)
        val pdfDoc = PdfDocument(pdfReader)
        val rotations = mutableListOf<Int>()
        
        
        for (i in 1..pdfDoc.numberOfPages) {
            val page = pdfDoc.getPage(i)
            val rotation = page.rotation
            rotations.add(rotation)
        }
        
        pdfDoc.close()
        pdfReader.close()
        
        // عرض الصفحات باستخدام PdfRenderer
        val fileDescriptor = ParcelFileDescriptor.open(
            file, 
            ParcelFileDescriptor.MODE_READ_ONLY
        )
        val pdfRenderer = PdfRenderer(fileDescriptor)
        
        
        for (i in 0 until pdfRenderer.pageCount) {
            val page = pdfRenderer.openPage(i)
            
            // إنشاء صورة معاينة للصفحة
            val bitmap = Bitmap.createBitmap(
                page.width / 2,  // حجم أصغر للمعاينة
                page.height / 2,
                Bitmap.Config.ARGB_8888
            )
            
            page.render(
                bitmap, 
                null, 
                null, 
                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
            )
            
            val pageNumber = i + 1
            val pageRotation = rotations[i]
            
            pages.add(
                PDFPageData(
                    pageNumber = pageNumber,
                    bitmap = bitmap,
                    originalRotation = pageRotation,
                    rotation = pageRotation
                )
            )
            
            
            page.close()
        }
        
        pdfRenderer.close()
        fileDescriptor.close()
        
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
    
    pages
}

/**
 * تدوير صورة bitmap
 * @param bitmap الصورة الأصلية
 * @param degrees درجة التدوير
 * @return الصورة المدورة
 */
private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    if (degrees == 0f) return bitmap
    
    val matrix = Matrix()
    matrix.postRotate(degrees)
    
    return Bitmap.createBitmap(
        bitmap, 
        0, 
        0, 
        bitmap.width, 
        bitmap.height, 
        matrix, 
        true
    )
}

/**
 * حفظ PDF تلقائياً في نفس المسار
 * @param context السياق
 * @param originalPath المسار الأصلي للملف
 * @param pages قائمة الصفحات المعدلة
 */
private suspend fun autoSavePDF(
    context: Context,
    originalPath: String,
    pages: List<PDFPageData>
) = withContext(Dispatchers.IO) {
    savePDFToOriginalPath(context, originalPath, pages)
}

/**
 * حفظ PDF في نفس المسار الأصلي (Overwrite)
 * @param context السياق
 * @param originalPath المسار الأصلي للملف
 * @param pages قائمة الصفحات المعدلة
 */
private suspend fun savePDFToOriginalPath(
    context: Context,
    originalPath: String,
    pages: List<PDFPageData>
) = withContext(Dispatchers.IO) {
    val originalFile = File(originalPath)
    val tempFile = File(originalFile.parent, "${originalFile.name}.tmp")
    
    pages.forEachIndexed { index, page ->
    }
    
    try {
        // الخطوة 1: إنشاء PDF مؤقت جديد مع التعديلات
        createModifiedPDF(originalPath, tempFile.absolutePath, pages)
        
        // الخطوة 2: حذف الملف الأصلي
        if (originalFile.exists()) {
            originalFile.delete()
        }
        
        // الخطوة 3: إعادة تسمية الملف المؤقت ليحل محل الأصلي
        tempFile.renameTo(originalFile)
        
    } catch (e: Exception) {
        android.util.Log.e("EditPDF", "❌ خطأ في الحفظ: ${e.message}")
        e.printStackTrace()
        
        // في حالة حدوث خطأ، احذف الملف المؤقت
        if (tempFile.exists()) {
            tempFile.delete()
        }
        throw e
    }
}

/**
 * إنشاء PDF معدل بالترتيب والدوران الجديد
 * @param sourcePath مسار PDF الأصلي
 * @param destPath مسار PDF الناتج
 * @param pages قائمة الصفحات المعدلة
 */
private fun createModifiedPDF(
    sourcePath: String,
    destPath: String,
    pages: List<PDFPageData>
) {
    
    val reader = PdfReader(FileInputStream(sourcePath))
    val sourcePdf = PdfDocument(reader)
    
    val totalPagesInSource = sourcePdf.numberOfPages
    
    val writer = PdfWriter(FileOutputStream(destPath))
    val destPdf = PdfDocument(writer)
    
    try {
        pages.forEachIndexed { index, pageData ->
            
            if (pageData.pageNumber < 1 || pageData.pageNumber > totalPagesInSource) {
                val errorMsg = "رقم الصفحة ${pageData.pageNumber} خارج النطاق! (النطاق: 1-$totalPagesInSource)"
                throw IllegalArgumentException(errorMsg)
            }
            
            
            sourcePdf.copyPagesTo(
                pageData.pageNumber,
                pageData.pageNumber,
                destPdf
            )
            
            // الحصول على آخر صفحة مضافة
            val copiedPage = destPdf.getLastPage()
            
            // قراءة التدوير الحالي للصفحة المنسوخة
            val currentRotation = copiedPage.rotation
            
            // تطبيق التدوير المطلوب مباشرة (setRotation يستبدل القيمة القديمة)
            copiedPage.setRotation(pageData.rotation)
            
        }
        
    } catch (e: Exception) {
        android.util.Log.e("EditPDF", ">> ❌ خطأ في createModifiedPDF: ${e.message}")
        e.printStackTrace()
        throw e
    } finally {
        // إغلاق المستندات
        destPdf.close()
        sourcePdf.close()
        writer.close()
        reader.close()
    }
}
