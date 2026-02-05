package com.example.recordatoriosdepuchi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment // Icono para informes
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Importante para observar flujos
import com.example.recordatoriosdepuchi.ui.components.admin.AdminFamilyTab
import com.example.recordatoriosdepuchi.ui.components.admin.AdminReminderTab
import com.example.recordatoriosdepuchi.ui.components.admin.AdminReportsTab // <--- IMPORTA ESTO
import com.example.recordatoriosdepuchi.ui.components.admin.AdminSettingsTab
import com.example.recordatoriosdepuchi.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    // Observamos los logs desde el ViewModel
    val callLogs by viewModel.callLogs.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administración") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.startScreensaverTest()
                        onBack()
                    }) {
                        Icon(Icons.Default.PlayCircle, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PROBAR ASISTENTE")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                // TAB 0: FAMILIA
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Familia") },
                    icon = { Icon(Icons.Default.People, null) }
                )

                // TAB 1: RECORDATORIOS
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Avisos") },
                    icon = { Icon(Icons.Default.Mic, null) }
                )

                // TAB 2: INFORMES (NUEVO)
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    label = { Text("Informes") },
                    icon = { Icon(Icons.Default.Assessment, null) }
                )

                // TAB 3: AJUSTES
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    label = { Text("Ajustes") },
                    icon = { Icon(Icons.Default.Settings, null) }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> AdminFamilyTab(viewModel)
                1 -> AdminReminderTab(viewModel)
                2 -> AdminReportsTab(logs = callLogs)
                3 -> AdminSettingsTab()
            }
        }
    }
}