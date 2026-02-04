package com.example.recordatoriosdepuchi.ui.components.admin

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.example.recordatoriosdepuchi.data.local.entity.ContactEntity
import com.example.recordatoriosdepuchi.ui.viewmodel.HomeViewModel
import com.example.recordatoriosdepuchi.utils.ImageHelper
import java.io.File
import java.io.FileOutputStream

@Composable
fun AdminFamilyTab(viewModel: HomeViewModel) {
    val contacts by viewModel.contacts.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var contactToEdit by remember { mutableStateOf<ContactEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Contactos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Usa las flechas para ordenar", color = Color.Gray, fontSize = 14.sp)
            }
            Button(
                onClick = { contactToEdit = null; showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("AÑADIR")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(contacts) { contact ->
                ContactAdminCard(
                    contact = contact,
                    onEdit = { contactToEdit = contact; showDialog = true },
                    onDelete = { viewModel.deleteContact(contact) },
                    onMoveUp = { viewModel.moveContactUp(contact, contacts) },
                    onMoveDown = { viewModel.moveContactDown(contact, contacts) }
                )
            }
        }
    }

    if (showDialog) {
        ContactFullScreenDialog(
            contact = contactToEdit,
            onDismiss = { showDialog = false },
            onSave = { name, number, uri ->
                if (contactToEdit == null) {
                    viewModel.addContact(name, number, uri)
                } else {
                    viewModel.updateContact(contactToEdit!!.copy(name = name, phoneNumber = number, photoUri = uri))
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun ContactAdminCard(
    contact: ContactEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(elevation = CardDefaults.cardElevation(4.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            // FLECHAS DE ORDEN
            Column {
                IconButton(onClick = onMoveUp, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.KeyboardArrowUp, "Subir") }
                IconButton(onClick = onMoveDown, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.KeyboardArrowDown, "Bajar") }
            }
            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) {
                if (contact.photoUri.isNotEmpty()) {
                    Image(painter = rememberAsyncImagePainter(model = File(contact.photoUri)), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.Person, null, tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(contact.phoneNumber, color = Color.Gray)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactFullScreenDialog(contact: ContactEntity?, onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var number by remember { mutableStateOf(contact?.phoneNumber ?: "") }
    var photoUri by remember { mutableStateOf(contact?.photoUri ?: "") }

    var showSourceDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val path = ImageHelper.saveImageToInternalStorage(context, it)
            if (path != null) photoUri = path
        }
    }

    // Cámara (Miniatura)
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            // Guardar el bitmap en un archivo temporal
            val file = File(context.filesDir, "cam_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                it.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            photoUri = file.absolutePath
        }
    }

    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Añadir foto") },
            text = { Text("Elige una opción:") },
            confirmButton = {
                TextButton(onClick = { galleryLauncher.launch("image/*"); showSourceDialog = false }) {
                    Icon(Icons.Default.PhotoLibrary, null); Spacer(Modifier.width(4.dp)); Text("Galería")
                }
            },
            dismissButton = {
                TextButton(onClick = { cameraLauncher.launch(null); showSourceDialog = false }) {
                    Icon(Icons.Default.PhotoCamera, null); Spacer(Modifier.width(4.dp)); Text("Cámara")
                }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (contact == null) "Nuevo Contacto" else "Editar Contacto") },
                    navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) } },
                    actions = { TextButton(onClick = { if (name.isNotBlank() && number.isNotBlank()) onSave(name, number, photoUri) }) { Text("GUARDAR", fontWeight = FontWeight.Bold) } }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(140.dp).clip(CircleShape).background(Color.LightGray).clickable { showSourceDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri.isNotEmpty()) {
                        Image(painter = rememberAsyncImagePainter(model = File(photoUri)), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(40.dp), tint = Color.DarkGray)
                            Text("Añadir foto", fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = number, onValueChange = { number = it }, label = { Text("Teléfono") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        }
    }
}