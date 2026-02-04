package com.example.recordatoriosdepuchi.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.recordatoriosdepuchi.ui.components.admin.AdminPasswordDialog
import com.example.recordatoriosdepuchi.ui.components.home.*
import com.example.recordatoriosdepuchi.ui.viewmodel.HomeViewModel
import com.example.recordatoriosdepuchi.utils.PreferenceHelper
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAdmin: () -> Unit
) {
    val contacts by viewModel.contacts.collectAsState()
    val isScreensaverTest by viewModel.isScreensaverTest.collectAsState()

    val context = LocalContext.current
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showScreensaver by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    // Si activamos el test desde Admin, forzamos que se muestre
    LaunchedEffect(isScreensaverTest) {
        if (isScreensaverTest) {
            showScreensaver = true
        }
    }

    // LÓGICA DE INACTIVIDAD
    val canShowScreensaver = contacts.isNotEmpty()
    LaunchedEffect(lastInteractionTime, canShowScreensaver, isScreensaverTest) {
        while (true) {
            delay(5000)
            val timeoutMillis = PreferenceHelper.getScreensaverTimeout(context) * 1000L
            if (!isScreensaverTest && canShowScreensaver && System.currentTimeMillis() - lastInteractionTime > timeoutMillis) {
                showScreensaver = true
            }
        }
    }

    if (showScreensaver && canShowScreensaver) {
        PuchiInteractiveScreensaver(
            contacts = contacts,
            testMode = isScreensaverTest,
            onWakeUp = {
                viewModel.stopScreensaverTest()
                lastInteractionTime = System.currentTimeMillis()
                showScreensaver = false
            }
        )
    } else {
        var currentPage by remember { mutableIntStateOf(0) }
        val gridCapacity = 4
        val startIndex = currentPage * 3
        val remainingContacts = if (startIndex < contacts.size) contacts.size - startIndex else 0
        val showMoreButton = remainingContacts > gridCapacity
        val itemsToShowCount = if (showMoreButton) 3 else minOf(remainingContacts, gridCapacity)
        val endIndex = startIndex + itemsToShowCount

        val contactsToShow = if (contacts.isNotEmpty() && startIndex < contacts.size) {
            contacts.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        // --- CORRECCIÓN IMPORTANTE AQUÍ ---
        fun makeSecureCall(contact: com.example.recordatoriosdepuchi.data.local.entity.ContactEntity) {
            try {
                // En lugar de abrir la pantalla directamente, ORDENAMOS la llamada al sistema.
                // El sistema avisará a nuestro PuchiCallService, y él abrirá la pantalla.
                val uri = Uri.parse("tel:${contact.phoneNumber}")
                val intent = Intent(Intent.ACTION_CALL, uri)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        Scaffold(
            containerColor = Color(0xFFF0F0F0),
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onPress = { lastInteractionTime = System.currentTimeMillis() },
                    onTap = { lastInteractionTime = System.currentTimeMillis() }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (currentPage == 0) {
                    HomeHeader(onAdminTriggered = { showPasswordDialog = true })
                } else {
                    HomeNavigation(
                        currentPage = currentPage,
                        hasNextPage = showMoreButton,
                        onPrevious = { currentPage-- },
                        onNext = { currentPage++ }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    if (contacts.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            userScrollEnabled = false,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(contactsToShow) { contact ->
                                TightMarginCard(
                                    contact = contact,
                                    greenColor = com.example.recordatoriosdepuchi.ui.theme.ActionGreen,
                                    onClick = {
                                        lastInteractionTime = System.currentTimeMillis()
                                        makeSecureCall(contact)
                                    }
                                )
                            }
                            if (showMoreButton) {
                                item { MorePeopleCard(onClick = { currentPage++ }) }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showPasswordDialog) {
        AdminPasswordDialog(onDismiss = { showPasswordDialog = false }, onSuccess = { showPasswordDialog = false; onNavigateToAdmin() })
    }
}