package com.example.pdfcreator.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
// Removed icons import - using text emojis instead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// Removed ImageVector import - using emojis instead
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pdfcreator.MainActivity
import com.example.pdfcreator.SettingsActivity
import com.example.pdfcreator.R

data class DrawerItem(
    val title: String,
    val emoji: String,
    val action: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawer(
    isOpen: Boolean,
    onClose: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(if (isOpen) DrawerValue.Open else DrawerValue.Closed)
    
    val drawerItems = listOf(
        DrawerItem(
            title = getString(R.string.menu_home),
            emoji = "🏠",
            action = { onClose() }
        ),
        DrawerItem(
            title = getString(R.string.menu_settings),
            emoji = "⚙️",
            action = { 
                onClose()
                onNavigateToSettings()
            }
        ),
        DrawerItem(
            title = getString(R.string.menu_about),
            emoji = "ℹ️",
            action = { 
                onClose()
                onNavigateToAbout()
            }
        ),
        DrawerItem(
            title = getString(R.string.menu_help),
            emoji = "❓",
            action = { 
                onClose()
                onNavigateToHelp()
            }
        )
    )
    
    if (isOpen) {
        AlertDialog(
            onDismissRequest = onClose,
            title = { 
                Text(
                    text = "🔒 ${getString(R.string.app_name)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    drawerItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { item.action() }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.emoji,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onClose) {
                    Text(getString(R.string.close))
                }
            }
        )
    }
}

@Composable
private fun DrawerMenuItem(
    item: DrawerItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.emoji,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// Helper function to get string resources
@Composable
private fun getString(@androidx.annotation.StringRes id: Int): String {
    val context = LocalContext.current
    return context.getString(id)
}
