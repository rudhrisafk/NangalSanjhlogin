package com.nangalsanjh.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class EmergencyContact(
    val name: String,
    val number: String,
    val icon: ImageVector,
    val color: Color,
    val description: String
)

val emergencyContacts = listOf(
    EmergencyContact("Police", "100", Icons.Default.LocalPolice, Color(0xFF1F5FBF), "Nangal Police Station"),
    EmergencyContact("Ambulance", "108", Icons.Default.MedicalServices, Color(0xFFD92D3A), "Emergency Medical Service"),
    EmergencyContact("Fire Brigade", "101", Icons.Default.LocalFireDepartment, Color(0xFFD9480F), "Fire & Rescue Service"),
    EmergencyContact("Disaster Management", "1077", Icons.Default.Warning, Color(0xFF6A1B9A), "State Disaster Helpline"),
    EmergencyContact("Women Helpline", "1091", Icons.Default.Woman, Color(0xFFAD1457), "Women Safety & Support"),
    EmergencyContact("Child Helpline", "1098", Icons.Default.ChildCare, Color(0xFF00695C), "Child Safety Helpline"),
    EmergencyContact("BBMB Control Room", "01887-220440", Icons.Default.Factory, Color(0xFF455A64), "Bhakra Beas Management Board"),
    EmergencyContact("PSPCL Helpline", "1912", Icons.Default.ElectricBolt, Color(0xFFB7791F), "Electricity Complaint"),
    EmergencyContact("Water Supply", "1800-180-2468", Icons.Default.WaterDrop, Color(0xFF0277BD), "Water & Sanitation"),
    EmergencyContact("Road Accident", "1073", Icons.Default.CarCrash, Color(0xFF558B2F), "Highway Accident Helpline"),
    EmergencyContact("Senior Citizen", "14567", Icons.Default.Elderly, Color(0xFF6D4C41), "Elder Care Helpline"),
    EmergencyContact("Health Helpline", "104", Icons.Default.HealthAndSafety, Color(0xFF00838F), "Medical Helpline"),
)

private val topThree = setOf("100", "108", "101")
private val SosRed = Color(0xFFB3101B)
private val SosRedDeep = Color(0xFF7A0A12)

private fun dial(context: android.content.Context, number: String) {
    context.startActivity(Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$number") })
}

@Composable
fun EmergencyScreen(onBloodDonors: () -> Unit = {}, onClose: () -> Unit = {}) {
    val context = LocalContext.current
    val quick = emergencyContacts.filter { it.number in topThree }
    val others = emergencyContacts.filter { it.number !in topThree }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Header with SOS ───────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(Brush.linearGradient(listOf(SosRed, SosRedDeep)))
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 22.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "EMERGENCY · ਐਮਰਜੈਂਸੀ",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            "Help is one tap away",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(44.dp).glass(CircleShape, fillAlpha = 0.18f)
                    ) { Icon(Icons.Default.Close, "Close", tint = Color.White) }
                }
                Spacer(Modifier.height(16.dp))
                // SOS call card (glass)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp)
                        .glass(RoundedCornerShape(20.dp), fillAlpha = 0.16f)
                        .clickable(onClickLabel = "Call 112") { dial(context, "112") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.White),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Call, null, tint = SosRed, modifier = Modifier.size(26.dp)) }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("SOS · Call 112", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            "National emergency number",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.8f))
                }
            }
        }

        // ── Police / Ambulance / Fire ─────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                quick.forEach { c ->
                    Surface(
                        onClick = { dial(context, c.number) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 14.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.size(44.dp).clip(CircleShape).background(c.color.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) { Icon(c.icon, null, tint = c.color, modifier = Modifier.size(24.dp)) }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                c.name.substringBefore(' '),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            Text(c.number, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ── Blood donors ──────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(SosRed, SosRedDeep)))
                    .clickable(onClickLabel = "Blood donors") { onBloodDonors() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(48.dp).glass(RoundedCornerShape(14.dp), fillAlpha = 0.18f),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.WaterDrop, null, tint = Color.White, modifier = Modifier.size(26.dp)) }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Blood donors", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Find donors by blood group", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                }
                Icon(Icons.Default.ArrowForward, null, tint = Color.White)
            }
        }

        item {
            Text(
                "HELPLINES · ਹੈਲਪਲਾਈਨ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        items(others) { contact ->
            EmergencyContactCard(
                contact = contact,
                onCallClick = { dial(context, contact.number) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun EmergencyContactCard(
    contact: EmergencyContact,
    onCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape).background(contact.color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) { Icon(contact.icon, null, tint = contact.color, modifier = Modifier.size(24.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    contact.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    contact.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    contact.number,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = onCallClick,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Icon(Icons.Default.Call, contentDescription = "Call ${contact.name}", modifier = Modifier.size(22.dp)) }
        }
    }
}
