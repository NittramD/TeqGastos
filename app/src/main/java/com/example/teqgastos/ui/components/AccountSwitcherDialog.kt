package com.example.teqgastos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teqgastos.model.UserRole
import com.example.teqgastos.ui.theme.TeqPrimary
import com.example.teqgastos.ui.theme.TeqSuccess

data class CorporateUserProfile(
    val role: UserRole,
    val name: String,
    val rut: String,
    val description: String,
    val icon: ImageVector
)

val TEQMED_PROFILES = listOf(
    CorporateUserProfile(
        role = UserRole.TECNICO,
        name = "Gerardo Galán",
        rut = "17.482.913-K",
        description = "Técnico de Terreno · Comisiones y Gastos",
        icon = Icons.Default.Build
    ),
    CorporateUserProfile(
        role = UserRole.SUPERVISOR,
        name = "Dra. Marcela Fuentes",
        rut = "12.839.210-4",
        description = "Jefatura de Servicio Técnico · Aprobaciones",
        icon = Icons.Default.SupervisorAccount
    ),
    CorporateUserProfile(
        role = UserRole.FINANZAS,
        name = "Roberto Cabrera",
        rut = "14.209.811-3",
        description = "Finanzas y Contabilidad · Pagos y ERP",
        icon = Icons.Default.AccountBalance
    )
)

/**
 * Diálogo Corporativo para Conmutación Rápida de Cuenta y Rol
 * Cumple con la Heurística de Nielsen #3 (Control y Libertad del Usuario)
 */
@Composable
fun AccountSwitcherDialog(
    currentRole: UserRole,
    onSelectRole: (UserRole) -> Unit,
    onLogoutToLogin: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                imageVector = Icons.Default.SwitchAccount,
                contentDescription = null,
                tint = TeqPrimary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Cambiar de Cuenta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Colaboradores TEQMED SpA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Selecciona el perfil corporativo con el que deseas operar:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TEQMED_PROFILES.forEach { profile ->
                    val isActive = profile.role == currentRole
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isActive) TeqPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, TeqPrimary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectRole(profile.role)
                                onDismissRequest()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) TeqPrimary else MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = profile.icon,
                                    contentDescription = null,
                                    tint = if (isActive) Color.White else TeqPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = profile.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isActive) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Activo",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TeqSuccess
                                        )
                                    }
                                }
                                Text(
                                    text = "RUT: ${profile.rut}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TeqPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = profile.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }

                            if (isActive) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Sesión Activa",
                                    tint = TeqPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                OutlinedButton(
                    onClick = {
                        onDismissRequest()
                        onLogoutToLogin()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ingresar con otra credencial / RUT",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar", fontWeight = FontWeight.SemiBold)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
