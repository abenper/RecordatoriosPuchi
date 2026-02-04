package com.example.recordatoriosdepuchi.ui.components.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.recordatoriosdepuchi.data.local.entity.ContactEntity
import com.example.recordatoriosdepuchi.ui.theme.ActionGreen
import com.example.recordatoriosdepuchi.ui.theme.NavigationOrange
import com.example.recordatoriosdepuchi.ui.theme.PhotoBorder
import java.io.File

@Composable
fun TightMarginCard(
    contact: ContactEntity,
    greenColor: Color = ActionGreen,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale = if (isPressed) 0.96f else 1.0f

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(275.dp)
            .scale(pressScale)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxSize().padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(155.dp)
                    .border(4.dp, PhotoBorder, CircleShape)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                if (contact.photoUri.isNotEmpty()) {
                    AsyncImage(
                        model = File(contact.photoUri),
                        contentDescription = "Foto de ${contact.name}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.padding(20.dp).fillMaxSize())
                }
            }

            Text(
                text = contact.name.uppercase(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 34.sp,
                modifier = Modifier.padding(vertical = 0.dp)
            )

            Surface(
                color = greenColor,
                shape = RoundedCornerShape(50),
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth(0.98f).height(55.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(Icons.Default.Call, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("LLAMAR", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MorePeopleCard(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse), label = "scale"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(275.dp)
            .scale(scale)
            .border(3.dp, NavigationOrange, RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            Icon(Icons.Default.Groups, null, tint = NavigationOrange, modifier = Modifier.size(90.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("VER MÁS", fontSize = 30.sp, fontWeight = FontWeight.Black, color = NavigationOrange, textAlign = TextAlign.Center)
            Text("PERSONAS", fontSize = 29.sp, fontWeight = FontWeight.Black, color = NavigationOrange, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = NavigationOrange, modifier = Modifier.size(50.dp))
        }
    }
}