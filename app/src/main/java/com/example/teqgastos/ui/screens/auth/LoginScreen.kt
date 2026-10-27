package com.example.teqgastos.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.teqgastos.model.UserRole
import com.example.teqgastos.ui.components.GlassmorphicButton
import com.example.teqgastos.ui.components.NielsenHelpDialog
import com.example.teqgastos.ui.components.NeumorphicCard
import com.example.teqgastos.ui.theme.NeuBackground
import com.example.teqgastos.ui.theme.NeuPressed
import com.example.teqgastos.ui.theme.NeuSurface
import com.example.teqgastos.ui.theme.TeqPrimary
import com.example.teqgastos.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: (UserRole) -> Unit
) {
    val session by authViewModel.userSession.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }

    if (showHelpDialog) {
        NielsenHelpDialog(onDismissRequest = { showHelpDialog = false })
    }

    Scaffold(
        containerColor = NeuBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Guía y Usabilidad (Leyes de Nielsen)",
                            tint = TeqPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Logo con relieve Neumorphic (Soft UI)
                NeumorphicCard(
                    modifier = Modifier.size(96.dp),
                    cornerRadius = 48.dp,
                    elevation = 6.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = TeqPrimary,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "TEQMED SpA",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TeqPrimary
                )
                Text(
                    text = "TeqGastos – Rendición en Terreno",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Mantenimiento y Equipamiento Médico · Macrozona Sur",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "Seleccione el perfil para ingresar al sistema:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Opciones de Perfiles con Tarjetas Neumórficas (Relieve 3D Soft UI)
            item {
                NeuRoleSelectionCard(
                    role = UserRole.TECNICO,
                    title = "Técnico de Terreno",
                    userName = "Gerardo Galán",
                    userRut = "17.482.913-K",
                    description = "Crea rendiciones, registra gastos y captura boletas offline en terreno.",
                    icon = Icons.Default.Build,
                    isSelected = session.rol == UserRole.TECNICO,
                    onSelect = { authViewModel.switchRole(UserRole.TECNICO) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                NeuRoleSelectionCard(
                    role = UserRole.SUPERVISOR,
                    title = "Jefatura de Servicio Técnico",
                    userName = "Dra. Marcela Fuentes",
                    userRut = "12.839.210-4",
                    description = "Aprueba, observa con feedback o rechaza rendiciones desde el móvil.",
                    icon = Icons.Default.SupervisorAccount,
                    isSelected = session.rol == UserRole.SUPERVISOR,
                    onSelect = { authViewModel.switchRole(UserRole.SUPERVISOR) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                NeuRoleSelectionCard(
                    role = UserRole.FINANZAS,
                    title = "Finanzas y Contabilidad",
                    userName = "Roberto Pinochet",
                    userRut = "14.209.811-1",
                    description = "Revisión final, registro de pagos de reembolsos y exportación a ERP.",
                    icon = Icons.Default.AccountBalance,
                    isSelected = session.rol == UserRole.FINANZAS,
                    onSelect = { authViewModel.switchRole(UserRole.FINANZAS) }
                )
                Spacer(modifier = Modifier.height(28.dp))
            }

            // Botón Principal Exclusivo con Efecto Glassmorphism (Vidrio Esmerilado translúcido)
            item {
                GlassmorphicButton(
                    onClick = { onLoginSuccess(session.rol) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    cornerRadius = 16.dp,
                    elevation = 6.dp
                ) {
                    Text(
                        text = "Ingresar como ${session.rol.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun NeuRoleSelectionCard(
    role: UserRole,
    title: String,
    userName: String,
    userRut: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        elevation = if (isSelected) 6.dp else 3.dp,
        backgroundColor = if (isSelected) NeuPressed else NeuSurface,
        onClick = onSelect
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) TeqPrimary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else TeqPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$userName ($userRut)",
                    style = MaterialTheme.typography.labelMedium,
                    color = TeqPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = TeqPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
