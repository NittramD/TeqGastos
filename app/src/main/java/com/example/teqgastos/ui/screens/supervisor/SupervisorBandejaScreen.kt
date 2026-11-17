package com.example.teqgastos.ui.screens.supervisor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teqgastos.model.UserRole
import com.example.teqgastos.ui.components.AccountSwitcherDialog
import com.example.teqgastos.ui.components.EstadoRendicionBadge
import com.example.teqgastos.ui.components.NielsenHelpDialog
import com.example.teqgastos.ui.components.TeqConfirmationDialog
import com.example.teqgastos.ui.components.formatClp
import com.example.teqgastos.ui.theme.TeqPrimary
import com.example.teqgastos.ui.theme.TeqSuccess
import com.example.teqgastos.ui.viewmodel.SupervisorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorBandejaScreen(
    viewModel: SupervisorViewModel,
    onNavigateToRevision: (Long) -> Unit,
    onNavigateToConfiguraciones: (() -> Unit)? = null,
    onLogout: () -> Unit,
    onSwitchAccount: ((UserRole) -> Unit)? = null
) {
    val rendiciones by viewModel.rendicionesPendientes.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }
    var showAccountSwitcherDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showHelpDialog) {
        NielsenHelpDialog(onDismissRequest = { showHelpDialog = false })
    }

    if (showAccountSwitcherDialog) {
        AccountSwitcherDialog(
            currentRole = UserRole.SUPERVISOR,
            onSelectRole = { role ->
                showAccountSwitcherDialog = false
                onSwitchAccount?.invoke(role)
            },
            onLogoutToLogin = {
                showAccountSwitcherDialog = false
                onLogout()
            },
            onDismissRequest = { showAccountSwitcherDialog = false }
        )
    }

    if (showLogoutDialog) {
        TeqConfirmationDialog(
            title = "¿Cerrar Sesión?",
            message = "¿Deseas cerrar la sesión activa de la Dra. Marcela Fuentes (Jefatura de Servicio Técnico)? Se revocarán los tokens de acceso.",
            confirmText = "Cerrar Sesión",
            cancelText = "Cancelar",
            isDestructive = true,
            icon = Icons.AutoMirrored.Filled.Logout,
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Bandeja de Aprobación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TeqPrimary)
                        Text("Jefatura de Servicio Técnico · TEQMED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { showAccountSwitcherDialog = true }) {
                        Icon(Icons.Default.SwitchAccount, contentDescription = "Cambiar de Cuenta", tint = TeqPrimary)
                    }
                    onNavigateToConfiguraciones?.let { navConf ->
                        IconButton(onClick = navConf) {
                            Icon(Icons.Default.Settings, contentDescription = "Configuraciones", tint = TeqPrimary)
                        }
                    }
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Ayuda y Políticas", tint = TeqPrimary)
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar Sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Rendiciones Pendientes de tu Equipo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Revisa gastos en terreno y aprueba o devuelve con observaciones.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (rendiciones.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = TeqSuccess, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("¡Estás al día!", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("No tienes rendiciones pendientes de revisión.", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            } else {
                items(rendiciones, key = { it.rendicion.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToRevision(item.rendicion.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = TeqPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(item.rendicion.tecnicoNombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                }
                                EstadoRendicionBadge(estado = item.rendicion.estado)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(item.rendicion.titulo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(item.rendicion.centroCostoNombre, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text("Total a Revisar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text(item.totalRendidoClp.formatClp(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = TeqPrimary)
                                }
                                Text(
                                    text = "${item.gastos.size} gastos registrados",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
