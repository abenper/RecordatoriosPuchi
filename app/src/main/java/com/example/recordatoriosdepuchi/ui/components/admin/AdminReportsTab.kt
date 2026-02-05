package com.example.recordatoriosdepuchi.ui.components.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recordatoriosdepuchi.data.local.entity.CallLogEntity
import com.example.recordatoriosdepuchi.data.local.entity.CallType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminReportsTab(logs: List<CallLogEntity>) {
    var filterByIncoming by remember { mutableStateOf(false) }

    val totalCalls = logs.size
    val incomingCalls = logs.count { it.type == CallType.INCOMING }
    val outgoingCalls = logs.count { it.type == CallType.OUTGOING }

    val filteredLogs = if (filterByIncoming) logs.filter { it.type == CallType.INCOMING } else logs

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // --- SECCIÓN 1: RESUMEN ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem("Total", totalCalls.toString())
                StatItem("Entrantes", incomingCalls.toString(), Color(0xFF2E7D32))
                StatItem("Salientes", outgoingCalls.toString(), Color(0xFFC62828))
            }
        }

        // --- SECCIÓN 2: GRÁFICO ---
        Text("Actividad Reciente", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        SimpleBarChart(logs)
        Spacer(modifier = Modifier.height(16.dp))

        // --- SECCIÓN 3: LISTADO CON FILTROS ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Historial Detallado", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            FilterChip(
                selected = filterByIncoming,
                onClick = { filterByIncoming = !filterByIncoming },
                label = { Text("Solo Entrantes") },
                leadingIcon = {
                    if (filterByIncoming) Icon(Icons.AutoMirrored.Filled.CallReceived, null)
                }
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxHeight()
        ) {
            items(filteredLogs) { log ->
                LogItem(log)
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 12.sp)
    }
}

@Composable
fun LogItem(log: CallLogEntity) {
    val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    val isIncoming = log.type == CallType.INCOMING

    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                // USO DE ICONOS AUTO-MIRRORED CORREGIDOS
                imageVector = if (isIncoming) Icons.AutoMirrored.Filled.CallReceived else Icons.AutoMirrored.Filled.CallMade,
                contentDescription = null,
                tint = if (isIncoming) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(log.contactName, fontWeight = FontWeight.Bold)
                Text(dateFormat.format(Date(log.timestamp)), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun SimpleBarChart(logs: List<CallLogEntity>) {
    val recentLogs = logs.take(7)

    if (recentLogs.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
            Text("Sin datos suficientes para gráfica")
        }
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(top = 16.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        recentLogs.reversed().forEach { log ->
            val heightRatio = if (log.type == CallType.INCOMING) 0.8f else 0.5f
            val color = if (log.type == CallType.INCOMING) Color(0xFF66BB6A) else Color(0xFFEF5350)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .fillMaxHeight(heightRatio)
                        .background(color, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = SimpleDateFormat("dd", Locale.getDefault()).format(Date(log.timestamp)),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
    Text(
        "Últimas llamadas (Verde: Entrante / Rojo: Saliente)",
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.fillMaxWidth(),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}