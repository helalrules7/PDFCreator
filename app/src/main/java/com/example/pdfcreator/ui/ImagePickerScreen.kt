package com.example.pdfcreator.ui

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.pdfcreator.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImagePickerScreen(
    viewModel: PDFCreatorViewModel,
    onNavigateToPDF: () -> Unit
) {
    val context = LocalContext.current
    val state = viewModel.state
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        try {
            uris.forEach { uri ->
                viewModel.addImage(uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // العنوان الرئيسي
        Text(
            text = getString(R.string.app_name),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // حقل إدخال عنوان PDF
        OutlinedTextField(
            value = state.pdfTitle,
            onValueChange = { viewModel.updatePDFTitle(it) },
            label = { Text(getString(R.string.pdf_title)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // زر إضافة الصور
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clickable {
                    imagePickerLauncher.launch("image/*")
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = getString(R.string.add_image),
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = getString(R.string.add_images),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = getString(R.string.add_images_subtitle),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // عرض الصور المختارة
        if (state.selectedImages.isNotEmpty()) {
            Text(
                text = getString(R.string.selected_images_count, state.selectedImages.size),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.selectedImages) { imageUri ->
                    ImageItem(
                        imageUri = imageUri,
                        onRemove = { viewModel.removeImage(imageUri) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // أزرار التحكم
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.clearImages() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(getString(R.string.clear_all))
                }

                Button(
                    onClick = { 
                        viewModel.createPDF(context)
                        onNavigateToPDF()
                    },
                    enabled = !state.isCreatingPDF,
                    modifier = Modifier.weight(1f)
                ) {
                    if (state.isCreatingPDF) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(getString(R.string.create_pdf))
                }
            }
        } else {
            // رسالة فارغة
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📷",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = getString(R.string.no_images_selected),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getString(R.string.no_images_instruction),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // عرض رسائل الخطأ
        state.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.clearError() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = getString(R.string.close_error),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImageItem(
    imageUri: Uri,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Image(
            painter = rememberAsyncImagePainter(imageUri),
            contentDescription = getString(R.string.selected_images),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // زر الحذف
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(
                    Color.Black.copy(alpha = 0.6f),
                    RoundedCornerShape(16.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = getString(R.string.remove_image),
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Helper function to get string resources
@Composable
private fun getString(@androidx.annotation.StringRes id: Int, vararg args: Any): String {
    val context = LocalContext.current
    return context.getString(id, *args)
}
