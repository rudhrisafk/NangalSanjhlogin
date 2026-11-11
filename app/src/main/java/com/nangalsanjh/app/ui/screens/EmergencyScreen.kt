package com.nangalsanjh.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class EmergencyContact(
    val name: String,
    val number: String,
    val emoji: String,
    val color: Color,
    val description: String
)

val emergencyContacts = listOf(
    EmergencyContact("Police", "100", "👮", Color(0xFF1565C0), "Nangal Police Station"),
    EmergencyContact("Ambulance", "108", "🚑", Color(0xFFB3101B), "Emergency Medical Service"),
    EmergencyContact("Fire Brigade", "101", "🚒", Color(0xFFE65100), "Fire & Rescue Service"),
    EmergencyContact("Disaster Management", "1077", "🆘", Color(0xFF6A1B9A), "State Disaster Helpline"),
    EmergencyContact("Women Helpline", "1091", "👩", Color(0xFFAD1457), "Women Safety & Support"),
    EmergencyContact("Child Helpline", "1098", "👶", Color(0xFF00695C), "Child Safety Helpline"),
    EmergencyContact("BBMB Control Room", "01887-220440", "🏭", Color(0xFF37474F), "Bhakra Beas Management Board"),
    EmergencyContact("PSPCL Helpline", "1912", "⚡", Color(0xFFF57F17), "Electricity Complaint"),
    EmergencyContact("Water Supply", "1800-180-2468", "💧", Color(0xFF0277BD), "Water & Sanitation"),
    EmergencyContact("Road Accident", "1073", "🚗", Color(0xFF558B2F), "Highway Accident Helpline"),
    EmergencyContact("Senior Citizen", "14567", "👴", Color(0xFF4E342E), "Elder Care Helpline"),
    EmergencyContact("Health Helpline", "104", "🏥", Color(0xFF00838F), "Medical Helpline"),
)

@Composable
fun EmergencyScreen(onBloodDonors: () -> Unit = {}) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(Color(0xFFB3101B))
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp)
        ) {
            Column {
                Text(
                    "Emergency contacts",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Nangal & Punjab Emergency Services",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFB3101B)),
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:112")
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🆘", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "SOS — Call 112",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "National Emergency Number",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFB3101B)),
                    onClick = onBloodDonors
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🩸", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Blood Donors",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Find donors by blood group",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Icon(Icons.Default.ArrowForward, null, tint = Color.White)
                    }
                }
            }

            item {
                Text(
                    "Emergency Numbers",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(emergencyContacts) { contact ->
                EmergencyContactCard(
                    contact = contact,
                    onCallClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${contact.number}")
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun EmergencyContactCard(contact: EmergencyContact, onCallClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = contact.color.copy(alpha = 0.15f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(contact.emoji, fontSize = 24.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    contact.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    contact.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    contact.number,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = contact.color
                )
            }
            Button(
                onClick = onCallClick,
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = contact.color),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Call, null, modifier = Modifier.size(18.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Call", fontSize = 13.sp, color = Color.White)
            }
        }
    }
}