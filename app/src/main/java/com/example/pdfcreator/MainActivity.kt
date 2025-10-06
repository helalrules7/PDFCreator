package com.example.pdfcreator

import android.content.Intent
import android.os.Bundle
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
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.example.pdfcreator.ui.PDFCreatorViewModel
import com.example.pdfcreator.ui.PDFViewScreen
import com.example.pdfcreator.ui.ImagePickerScreen
import com.example.pdfcreator.ui.NavigationDrawer
import com.example.pdfcreator.ui.theme.PDFCreatorTheme
import com.example.pdfcreator.utils.LanguageAwareComposable
// Removed icons import - using text emoji instead
import androidx.compose.material3.*

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            LanguageAwareComposable {
                PDFCreatorTheme {
                    PDFCreatorApp()
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PDFCreatorApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? MainActivity
    val viewModel: PDFCreatorViewModel = viewModel()
    
    // Get intent from activity
    val intent = activity?.intent
    
    // Check if we should show PDF view from intent
    val showPdfView = intent?.getBooleanExtra("show_pdf_view", false) ?: false
    val pdfPath = intent?.getStringExtra("pdf_path")
    val pdfTitle = intent?.getStringExtra("pdf_title")
    
    // Initialize view model state if coming from EditPDFActivity
    // Use pdfPath as key to trigger recomposition when it changes
    LaunchedEffect(pdfPath) {
        if (showPdfView && pdfPath != null && pdfTitle != null) {
            android.util.Log.d("PDFCreator", "Updating PDF: path=$pdfPath, title=$pdfTitle")
            viewModel.updatePDFCreated(pdfPath, pdfTitle)
        }
    }
    
    var showPDFScreen by remember { mutableStateOf(false) }
    
    // Update showPDFScreen when intent changes
    LaunchedEffect(showPdfView, pdfPath) {
        if (showPdfView && pdfPath != null) {
            android.util.Log.d("PDFCreator", "Showing PDF screen")
            showPDFScreen = true
        }
    }
    
    var showDrawer by remember { mutableStateOf(false) }
    
    val permissions = remember {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            listOf(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            listOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    // طلب الصلاحيات المطلوبة
    val permissionsState = rememberMultiplePermissionsState(
        permissions = permissions
    )

    // طلب الصلاحيات عند بدء التطبيق (مرة مرة فقط)
    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            if (permissionsState.allPermissionsGranted) {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text("H PDF Creator") },
                    navigationIcon = {
                        TextButton(onClick = { showDrawer = true }) {
                            Text(
                                text = "☰",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        // Navigation Drawer
        NavigationDrawer(
            isOpen = showDrawer,
            onClose = { showDrawer = false },
            onNavigateToHome = {
                // إعادة تعيين الشاشة لإنشاء PDF جديد
                showPDFScreen = false
                // مسح جميع الصور المحددة
                viewModel.clearImages()
            },
            onNavigateToMyPDFs = {
                val intent = Intent(context, MyPDFsActivity::class.java)
                context.startActivity(intent)
            },
            onNavigateToSettings = {
                val intent = Intent(context, SettingsActivity::class.java)
                context.startActivity(intent)
            },
            onNavigateToAbout = {
                val intent = Intent(context, AboutActivity::class.java)
                context.startActivity(intent)
            },
            onNavigateToHelp = {
                val intent = Intent(context, HelpActivity::class.java)
                context.startActivity(intent)
            }
        )
        
        if (!permissionsState.allPermissionsGranted) {
            // رسالة طلب الصلاحيات
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                    Text(
                        text = "🔒 H PDF Creator",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "هذا التطبيق يحتاج صلاحية الوصول للصور لتحويلها إلى PDF\n\nاضغط على الزر أدناه لمنح الصلاحيات",
                    style = MaterialTheme.typography.bodyMedium,
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

                OutlinedButton(
                    onClick = { 
                        try {
                            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
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