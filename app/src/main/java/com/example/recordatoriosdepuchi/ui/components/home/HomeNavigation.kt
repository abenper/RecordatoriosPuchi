package com.example.recordatoriosdepuchi.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recordatoriosdepuchi.ui.theme.BackRed
import com.example.recordatoriosdepuchi.ui.theme.NavigationOrange

@Composable
fun HomeNavigation(
    currentPage: Int,
    hasNextPage: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp) // Altura generosa para botones grandes
            .background(Color.White)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // BOTÓN ANTERIOR / VOLVER
        Button(
            onClick = onPrevious,
            colors = ButtonDefaults.buttonColors(containerColor = BackRed),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("VOLVER", fontSize = 21.sp, fontWeight = FontWeight.Black)
        }

        // BOTÓN SIGUIENTE (Solo si hay más)
        if (hasNextPage) {
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = NavigationOrange),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                Text("MÁS", fontSize = 22.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(36.dp))
            }
        } else {
            // Relleno vacío para mantener tamaño si no hay siguiente
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}