package com.example.pdfcreator.ui

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import com.example.pdfcreator.R

data class PDFCreatorState(
    val selectedImages: List<Uri> = emptyList(),
    val isCreatingPDF: Boolean = false,
    val pdfCreated: Boolean = false,
    val errorMessage: String? = null,
    val pdfPath: String? = null,
    val pdfTitle: String = "PDF_${System.currentTimeMillis()}"
)

class PDFCreatorViewModel : ViewModel() {
    var state by mutableStateOf(PDFCreatorState())
        private set

    fun addImage(uri: Uri) {
        state = state.copy(
            selectedImages = state.selectedImages + uri,
            errorMessage = null
        )
    }

    fun removeImage(uri: Uri) {
        state = state.copy(
            selectedImages = state.selectedImages.filter { it != uri }
        )
    }

    fun clearImages() {
        state = state.copy(
            selectedImages = emptyList(),
            pdfCreated = false,
            pdfPath = null
        )
    }

    fun updatePDFTitle(title: String) {
        state = state.copy(pdfTitle = title)
    }

    fun reorderImages(fromIndex: Int, toIndex: Int) {
        val images = state.selectedImages.toMutableList()
        if (fromIndex in images.indices && toIndex in images.indices) {
            val item = images.removeAt(fromIndex)
            images.add(toIndex, item)
            state = state.copy(selectedImages = images)
        }
    }

    fun createPDF(context: Context) {
        if (state.selectedImages.isEmpty()) {
            state = state.copy(errorMessage = context.getString(R.string.please_select_at_least_one_image))
            return
        }

        state = state.copy(isCreatingPDF = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val pdfPath = withContext(Dispatchers.IO) {
                    createPDFFile(context, state.selectedImages)
                }
                state = state.copy(
                    isCreatingPDF = false,
                    pdfCreated = true,
                    pdfPath = pdfPath
                )
            } catch (e: Exception) {
                e.printStackTrace() // إضافة log للخطأ
                state = state.copy(
                    isCreatingPDF = false,
                    errorMessage = context.getString(R.string.error_creating_pdf_detailed, e.message ?: context.getString(R.string.unknown_error))
                )
            }
        }
    }

    private suspend fun createPDFFile(context: Context, imageUris: List<Uri>): String {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "${context.getString(R.string.app_name)} - ${state.pdfTitle}.pdf"
                val file = File(context.getExternalFilesDir(null), fileName)
                val outputStream = FileOutputStream(file)

                val pdfWriter = PdfWriter(outputStream)
                val pdfDocument = PdfDocument(pdfWriter)
                val document = Document(pdfDocument)

                try {
                    for (uri in imageUris) {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        if (inputStream != null) {
                            val imageData = ImageDataFactory.create(inputStream.readBytes())
                            val image = Image(imageData)
                            
                            // تحسين حجم الصورة لتناسب الصفحة
                            val pageSize = pdfDocument.defaultPageSize
                            val imageWidth = image.imageWidth
                            val imageHeight = image.imageHeight
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
                            
                            image.scaleToFit(finalWidth, finalHeight)
                            image.setFixedPosition(20f, pageSize.height - finalHeight - 20f)
                            
                            document.add(image)
                            if (imageUris.indexOf(uri) < imageUris.size - 1) {
                                document.add(com.itextpdf.layout.element.AreaBreak())
                            }
                        }
                    }
                } finally {
                    document.close()
                    pdfDocument.close()
                    pdfWriter.close()
                    outputStream.close()
                }

                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }

    fun clearError() {
        state = state.copy(errorMessage = null)
    }
}
