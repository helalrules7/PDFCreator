package com.example.pdfcreator

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pdfcreator.ui.theme.PDFCreatorTheme
import com.example.pdfcreator.utils.LanguageAwareComposable
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image as ITextImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

class EditPDFActivity : BaseActivity() {
    
    private var pdfPath: String? = null
    private var pdfTitle: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        pdfPath = intent.getStringExtra("pdf_path")
        pdfTitle = intent.getStringExtra("pdf_title")
        
        setContent {
            LanguageAwareComposable {
                PDFCreatorTheme {
                    EditPDFScreen(
                        pdfPath = pdfPath ?: "",
                        pdfTitle = pdfTitle ?: "PDF",
                        onBackPressed = { finish() },
                        onSaveSuccess = { newPath ->
                            val intent = Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

data class PDFPageData(
    val pageNumber: Int,
    val bitmap: Bitmap?,
    val rotation: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPDFScreen(
    pdfPath: String,
    pdfTitle: String,
    onBackPressed: () -> Unit,
    onSaveSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var pages by remember { mutableStateOf<List<PDFPageData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf(pdfTitle) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    fun getString(@androidx.annotation.StringRes id: Int): String {
        return context.getString(id)
    }
    
    // Load PDF pages
    LaunchedEffect(pdfPath) {
        if (pdfPath.isNotEmpty()) {
            scope.launch {
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
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(getString(R.string.edit_pdf_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = getString(R.string.back)
                        )
                    }
                },
                actions = {
                    if (!isLoading && pages.isNotEmpty()) {
                        IconButton(
                            onClick = { showSaveDialog = true },
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = getString(R.string.save_pdf)
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
                isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = getString(R.string.loading_pdf_pages),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "❌",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = getString(R.string.error_loading_pdf),
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage!!,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                pages.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📄",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = getString(R.string.error_loading_pdf),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            items = pages,
                            key = { _, page -> page.pageNumber }
                        ) { index, page ->
                            PDFPageItem(
                                page = page,
                                index = index,
                                totalPages = pages.size,
                                onRotate = {
                                    pages = pages.toMutableList().apply {
                                        val oldRotation = this[index].rotation
                                        val newRotation = (oldRotation + 90) % 360
                                        android.util.Log.d("EditPDF", "Rotating page $index: $oldRotation -> $newRotation")
                                        this[index] = this[index].copy(
                                            rotation = newRotation
                                        )
                                    }
                                },
                                onMoveUp = {
                                    if (index > 0) {
                                        pages = pages.toMutableList().apply {
                                            val temp = this[index]
                                            this[index] = this[index - 1]
                                            this[index - 1] = temp
                                        }
                                    }
                                },
                                onMoveDown = {
                                    if (index < pages.size - 1) {
                                        pages = pages.toMutableList().apply {
                                            val temp = this[index]
                                            this[index] = this[index + 1]
                                            this[index + 1] = temp
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Save Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text(getString(R.string.save_pdf_title)) },
            text = {
                Column {
                    Text(getString(R.string.enter_new_title))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text(getString(R.string.pdf_title)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveDialog = false
                        isSaving = true
                        scope.launch {
                            try {
                                val newPath = saveEditedPDF(context, pages, newTitle)
                                isSaving = false
                                
                                // Navigate to MainActivity with the new PDF
                                val intent = Intent(context, com.example.pdfcreator.MainActivity::class.java).apply {
                                    putExtra("show_pdf_view", true)
                                    putExtra("pdf_path", newPath)
                                    putExtra("pdf_title", newTitle)
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                                context.startActivity(intent)
                                // Close this activity to go back to MainActivity
                                (context as? android.app.Activity)?.finish()
                                onSaveSuccess(newPath)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                errorMessage = e.message
                                isSaving = false
                            }
                        }
                    },
                    enabled = newTitle.isNotEmpty()
                ) {
                    Text(getString(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text(getString(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun PDFPageItem(
    page: PDFPageData,
    index: Int,
    totalPages: Int,
    onRotate: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Page preview
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                if (page.bitmap != null) {
                    val rotatedBitmap = remember(page.bitmap, page.rotation) {
                        rotateBitmap(page.bitmap, page.rotation.toFloat())
                    }
                    Image(
                        bitmap = rotatedBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "📄",
                        fontSize = 24.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Page info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = context.getString(R.string.page_number, page.pageNumber),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (page.rotation != 0) {
                    Text(
                        text = context.getString(R.string.rotated, page.rotation),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Action buttons
            Column {
                Row {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = context.getString(R.string.move_up)
                        )
                    }
                    
                    IconButton(onClick = onRotate) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = context.getString(R.string.rotate_page)
                        )
                    }
                }
                
                IconButton(
                    onClick = onMoveDown,
                    enabled = index < totalPages - 1
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = context.getString(R.string.move_down)
                    )
                }
            }
        }
    }
}

// Helper functions
private suspend fun loadPDFPages(context: Context, pdfPath: String): List<PDFPageData> = withContext(Dispatchers.IO) {
    val pages = mutableListOf<PDFPageData>()
    val file = File(pdfPath)
    
    try {
        // First, read rotations from PDF using iTextPDF
        val pdfReader = com.itextpdf.kernel.pdf.PdfReader(file)
        val pdfDoc = com.itextpdf.kernel.pdf.PdfDocument(pdfReader)
        val rotations = mutableListOf<Int>()
        
        for (i in 1..pdfDoc.numberOfPages) {
            val page = pdfDoc.getPage(i)
            rotations.add(page.rotation)
        }
        
        pdfDoc.close()
        pdfReader.close()
        
        // Then, render pages using PdfRenderer
        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(fileDescriptor)
        
        for (i in 0 until pdfRenderer.pageCount) {
            val page = pdfRenderer.openPage(i)
            
            // Create bitmap for preview
            val bitmap = Bitmap.createBitmap(
                page.width / 2,  // Reduced size for preview
                page.height / 2,
                Bitmap.Config.ARGB_8888
            )
            
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            
            pages.add(
                PDFPageData(
                    pageNumber = i + 1,
                    bitmap = bitmap,
                    rotation = rotations[i]  // Use rotation from PDF
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

private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    if (degrees == 0f) return bitmap
    
    val matrix = Matrix()
    matrix.postRotate(degrees)
    
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private suspend fun saveEditedPDF(
    context: Context,
    pages: List<PDFPageData>,
    title: String
): String = withContext(Dispatchers.IO) {
    // Save in the same directory as the original PDF (root external files dir)
    val fileName = "${context.getString(R.string.app_name)} - ${title}.pdf"
    val file = File(context.getExternalFilesDir(null), fileName)
    val outputPath = file.absolutePath
    
    try {
        val writer = PdfWriter(outputPath)
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)
        
        pages.forEachIndexed { index, page ->
            // Use original bitmap without rotating it
            val bitmap = page.bitmap!!
            
            // Convert bitmap to byte array using PNG (lossless, no borders)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            
            // Create image data
            val imageData = ImageDataFactory.create(byteArray)
            val pdfImage = ITextImage(imageData)
            
            // Remove ALL borders and strokes from image
            pdfImage.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            pdfImage.setBorderTop(com.itextpdf.layout.borders.Border.NO_BORDER)
            pdfImage.setBorderBottom(com.itextpdf.layout.borders.Border.NO_BORDER)
            pdfImage.setBorderLeft(com.itextpdf.layout.borders.Border.NO_BORDER)
            pdfImage.setBorderRight(com.itextpdf.layout.borders.Border.NO_BORDER)
            
            // تحسين حجم الصورة لتناسب الصفحة (same as original)
            val pageSize = pdfDocument.defaultPageSize
            val imageWidth = pdfImage.imageWidth
            val imageHeight = pdfImage.imageHeight
            val pageWidth = pageSize.width - 40 // هامش 20 من كل جانب
            val pageHeight = pageSize.height - 40
            
            val aspectRatio = imageWidth.toFloat() / imageHeight.toFloat()
            val pageAspectRatio = pageWidth / pageHeight
            
            val finalWidth: Float
            val finalHeight: Float
            
            if (aspectRatio > pageAspectRatio) {
                finalWidth = pageWidth
                finalHeight = pageWidth / aspectRatio
            } else {
                finalHeight = pageHeight
                finalWidth = pageHeight * aspectRatio
            }
            
            pdfImage.scaleToFit(finalWidth, finalHeight)
            pdfImage.setFixedPosition(20f, pageSize.height - finalHeight - 20f)
            
            document.add(pdfImage)
            
            // Add page break after image (except last one)
            if (index < pages.size - 1) {
                document.add(com.itextpdf.layout.element.AreaBreak())
            }
            
            // Apply rotation to the current page AFTER adding content
            if (page.rotation != 0) {
                val currentPage = pdfDocument.getPage(pdfDocument.numberOfPages)
                currentPage.setRotation(page.rotation)
            }
        }
        
        document.close()
        pdfDocument.close()
        writer.close()
        
        outputPath
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}

