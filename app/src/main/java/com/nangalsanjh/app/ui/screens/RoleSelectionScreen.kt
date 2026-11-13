package com.nangalsanjh.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nangalsanjh.app.theme.NSButton
import com.nangalsanjh.app.theme.NSStatusBadge
import com.nangalsanjh.app.theme.Radius
import com.nangalsanjh.app.theme.Spacing

@Composable
fun RoleSelectionScreen(
    currentRole: String = "",
    onRoleSelected: (String) -> Unit,
) {
    var selectedRole by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .aurora(
                listOf(
                    androidx.compose.ui.graphics.Color(0xFF3A7BFF).copy(alpha = 0.10f),
                    androidx.compose.ui.graphics.Color(0xFF8E5BFF).copy(alpha = 0.08f),
                    androidx.compose.ui.graphics.Color(0xFF19C3B1).copy(alpha = 0.08f)
                ),
                periodMs = 9000
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.xxl)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(Spacing.huge))
            Text(
                text = tr("STEP 1 OF 1  ·  ਆਪਣੀ ਭੂਮਿਕਾ ਚੁਣੋ"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = tr("How will you use Nangal Seva?"),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = tr("You can switch your role later from your profile."),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.xxxl))

            RoleCard(
                number = "01",
                title = tr("Seeker"),
                subtitle = tr("Find trusted workers, post a job request and report civic issues."),
                icon = Icons.Default.Person,
                isSelected = selectedRole == "seeker",
                isCurrent = currentRole == "seeker",
            ) { selectedRole = "seeker" }

            Spacer(modifier = Modifier.height(Spacing.md))

            RoleCard(
                number = "02",
                title = tr("Worker"),
                subtitle = tr("Offer your skills, accept jobs nearby and build your rating."),
                icon = Icons.Default.Build,
                isSelected = selectedRole == "worker",
                isCurrent = currentRole == "worker",
            ) { selectedRole = "worker" }

            Spacer(modifier = Modifier.height(Spacing.md))

            RoleCard(
                number = "03",
                title = tr("Admin"),
                subtitle = tr("Municipal or government official managing the city."),
                icon = Icons.Default.AdminPanelSettings,
                isSelected = selectedRole == "admin",
                isCurrent = currentRole == "admin",
            ) { selectedRole = "admin" }

            Spacer(modifier = Modifier.height(Spacing.xl))
        }

        NSButton(
            text = tr("Continue"),
            onClick = { onRoleSelected(selectedRole) },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedRole.isNotEmpty() && selectedRole != currentRole
        )
        Spacer(modifier = Modifier.height(Spacing.xl))
    }
}

@Composable
fun RoleCard(
    number: String = "",
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    isCurrent: Boolean = false,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val borderColor = if (isSelected) primary else MaterialTheme.colorScheme.outline
    val container = if (isSelected) primary.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Radius.lg)
            .background(container)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, Radius.lg)
            .clickable(onClickLabel = "Select $title") { onClick() }
            .padding(Spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(Radius.md)
                .background(if (isSelected) primary else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(Spacing.lg))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (number.isNotEmpty()) {
                    Text(
                        number,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isCurrent) {
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    NSStatusBadge(text = tr("Active"), color = primary)
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isSelected) {
            Spacer(modifier = Modifier.width(Spacing.sm))
            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = primary)
        }
    }
}
