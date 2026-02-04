package com.example.recordatoriosdepuchi.ui.components.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recordatoriosdepuchi.ui.theme.HeaderBlue
import kotlinx.coroutines.delay

@Composable
fun HomeHeader(
    onAdminTriggered: () -> Unit
) {
    var isHeaderPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isHeaderPressed) {
        if (isHeaderPressed) {
            delay(3000)
            onAdminTriggered()
            isHeaderPressed = false
        }
    }

    Surface(
        color = HeaderBlue,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 50.dp, bottom = 20.dp, start = 4.dp, end = 4.dp)
        ) {
            Text(
                text = "Hola Puchi,",
                fontSize = 58.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Visible,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { isHeaderPressed = true; tryAwaitRelease(); isHeaderPressed = false }
                    )
                }
            )
            Text(
                text = "¿A quién llamamos?",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}