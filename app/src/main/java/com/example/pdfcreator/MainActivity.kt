package com.example.pdfcreator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.example.pdfcreator.ui.PDFCreatorViewModel
import com.example.pdfcreator.ui.PDFViewScreen
import com.example.pdfcreator.ui.ImagePickerScreen
import com.example.pdfcreator.ui.theme.PDFCreatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PDFCreatorTheme {
                PDFCreatorApp()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PDFCreatorApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: PDFCreatorViewModel = viewModel()
    var showPDFScreen by remember { mutableStateOf(false) }

    // طلب الصلاحيات المطلوبة
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.READ_MEDIA_IMAGES
        )
    )

    // طلب الصلاحيات عند بدء التطبيق (مرة واحدة فقط)
    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        if (!permissionsState.allPermissionsGranted) {
            // رسالة طلب الصلاحيات
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                    Text(
                        text = "🔒 Rania PDF Creator",
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "هذا التطبيق يحتاج صلاحية الوصول للصور لتحويلها إلى PDF\n\nاضغط على الزر أدناه لمنح الصلاحيات",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { 
                        try {
                            android.util.Log.d("PDFCreator", "Requesting permissions...")
                            permissionsState.launchMultiplePermissionRequest()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            android.util.Log.e("PDFCreator", "Error requesting permissions: ${e.message}")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🔓 منح الصلاحيات")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                androidx.compose.material3.OutlinedButton(
                    onClick = { 
                        try {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = android.net.Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("⚙️ فتح إعدادات التطبيق")
                }
            }
        } else if (showPDFScreen) {
            PDFViewScreen(
                viewModel = viewModel,
                onNavigateBack = { showPDFScreen = false }
            )
        } else {
            ImagePickerScreen(
                viewModel = viewModel,
                onNavigateToPDF = { showPDFScreen = true }
            )
        }
    }
}