package com.example.teqgastos.ui.screens.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teqgastos.TeqGastosApp
import com.example.teqgastos.data.datastore.ThemeMode
import com.example.teqgastos.model.UserRole
import com.example.teqgastos.ui.components.AccountSwitcherDialog
import com.example.teqgastos.ui.components.TEQMED_PROFILES
import com.example.teqgastos.ui.components.GlassmorphicButton
import com.example.teqgastos.ui.components.NeumorphicCard
import com.example.teqgastos.ui.components.ShimmerGastoCard
import com.example.teqgastos.ui.components.ShimmerRendicionCard
import com.example.teqgastos.ui.components.TeqConfirmationDialog
import com.example.teqgastos.ui.components.TeqPulsingLoader
import com.example.teqgastos.ui.theme.LocalIsDarkTheme
import com.example.teqgastos.ui.theme.TeqPrimary
import com.example.teqgastos.ui.theme.TeqSuccess
import com.example.teqgastos.ui.theme.neuBackgroundColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionesScreen(
    app: TeqGastosApp,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit = {},
    onSwitchAccount: ((UserRole) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = LocalIsDarkTheme.current

    val currentTheme by app.userPreferences.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
    val session by app.userPreferences.userSessionFlow.collectAsState(initial = null)
    val isOfflineSimulated by app.userPreferences.offlineSimulationFlow.collectAsState(initial = false)
    val isBiometricsEnabled by app.userPreferences.biometricsEnabledFlow.collectAsState(initial = true)

    var showShimmerDemo by remember { mutableStateOf(false) }
    var showAccountSwitcherDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showAccountSwitcherDialog && session != null) {
        AccountSwitcherDialog(
            currentRole = session!!.rol,
            onSelectRole = { role ->
                if (onSwitchAccount != null) {
                    onSwitchAccount(role)
                } else {
                    coroutineScope.launch {
                        val profile = TEQMED_PROFILES.firstOrNull { it.role == role }
                        if (profile != null) {
                            app.userPreferences.saveUserSession(profile.name, profile.rut, profile.role)
                            snackbarHostState.showSnackbar("Cuenta cambiada a ${profile.name} (${profile.role.displayName})")
                        }
                    }
                }
            },
            onLogoutToLogin = onLogout,
            onDismissRequest = { showAccountSwitcherDialog = false }
        )
    }

    if (showLogoutDialog) {
        TeqConfirmationDialog(
            title = "¿Cerrar Sesión Activa?",
            message = "¿Estás seguro de que deseas cerrar la sesión de ${session?.nombre ?: "este usuario"}? Se revocarán los tokens de acceso y deberás autenticarte de nuevo.",
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
        containerColor = neuBackgroundColor(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configuración y Preferencias",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = neuBackgroundColor())
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // SECCIÓN 1: APARIENCIA Y MODO OSCURO
            item {
                Text(
                    text = "Apariencia y Tema",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = TeqPrimary,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            item {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    elevation = 4.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(TeqPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = TeqPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Modo de Visualización",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Adapta las sombras Neumorphic y colores M3",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionChip(
                            label = "Claro",
                            icon = Icons.Default.LightMode,
                            isSelected = currentTheme == ThemeMode.LIGHT,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                coroutineScope.launch {
                                    app.userPreferences.setThemeMode(ThemeMode.LIGHT)
                                    snackbarHostState.showSnackbar("Tema claro activado.")
                                }
                            }
                        )

                        ThemeOptionChip(
                            label = "Oscuro",
                            icon = Icons.Default.DarkMode,
                            isSelected = currentTheme == ThemeMode.DARK,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                coroutineScope.launch {
                                    app.userPreferences.setThemeMode(ThemeMode.DARK)
                                    snackbarHostState.showSnackbar("Tema oscuro activado.")
                                }
                            }
                        )

                        ThemeOptionChip(
                            label = "Sistema",
                            icon = Icons.Default.BrightnessAuto,
                            isSelected = currentTheme == ThemeMode.SYSTEM,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                coroutineScope.launch {
                                    app.userPreferences.setThemeMode(ThemeMode.SYSTEM)
                                    snackbarHostState.showSnackbar("Tema ajustado automáticamente al sistema operativo.")
                                }
                            }
                        )
                    }
                }
            }

            // SECCIÓN 2: CONECTIVIDAD & MODO TERRENO OFFLINE
            item {
                Text(
                    text = "Conectividad y Sincronización",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = TeqPrimary,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            item {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    elevation = 4.dp
                ) {
                    // Switch de Modo Terreno Offline
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isOfflineSimulated) Color(0xFFFFEDD5) else Color(0xFFDCFCE7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isOfflineSimulated) Icons.Default.CloudOff else Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = if (isOfflineSimulated) Color(0xFFEA580C) else TeqSuccess,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Simulador Modo Offline",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isOfflineSimulated) "Operando exclusivamente con Room SQLite" else "Sincronización online habilitada",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        Switch(
                            checked = isOfflineSimulated,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    app.userPreferences.setOfflineSimulation(enabled)
                                    snackbarHostState.showSnackbar(
                                        if (enabled) "Modo Offline forzado para demostración." else "Modo de red restaurado."
                                    )
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFEA580C))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Sincronización Manual Inmediata
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sincronizar con ERP Central",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Envía gastos pendientes mediante WorkManager",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val result = app.repository.forzarSincronizacionManual()
                                    if (result.isSuccess) {
                                        val summary = result.getOrNull()
                                        snackbarHostState.showSnackbar("Sincronización completada (${summary?.gastosSincronizados ?: 0} gastos procesados).")
                                    } else {
                                        snackbarHostState.showSnackbar("Error al sincronizar: ${result.exceptionOrNull()?.message}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = "Sincronizar", tint = TeqPrimary)
                        }
                    }
                }
            }

            // SECCIÓN 3: SEGURIDAD Y SESIÓN ACTIVA
            item {
                Text(
                    text = "Seguridad y Credenciales",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = TeqPrimary,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            item {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    elevation = 4.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TeqPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = TeqPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Acceso con Biometría",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Huella digital o reconocimiento facial",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        Switch(
                            checked = isBiometricsEnabled,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    app.userPreferences.setBiometricsEnabled(enabled)
                                    snackbarHostState.showSnackbar(
                                        if (enabled) "Biometría habilitada." else "Biometría deshabilitada."
                                    )
                                }
                            }
                        )
                    }

                    if (session != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Usuario Activo: ${session?.nombre}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "RUT: ${session?.rut} · Rol: ${session?.rol?.displayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showAccountSwitcherDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwitchAccount,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cambiar Cuenta",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { showLogoutDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cerrar Sesión",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // SECCIÓN 4: ANIMACIONES Y EXPERIENCIA VISUAL (IE 2.2.1 / IE 2.2.2)
            item {
                Text(
                    text = "Animaciones y Demostración UX",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = TeqPrimary,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            item {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    elevation = 4.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Previsualizar Shimmer Skeletons",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Efecto de carga animado continuo (Heurística Nielsen #1)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Switch(
                            checked = showShimmerDemo,
                            onCheckedChange = { showShimmerDemo = it }
                        )
                    }

                    AnimatedVisibility(
                        visible = showShimmerDemo,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Esqueleto Shimmer para Rendiciones:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TeqPrimary
                            )
                            ShimmerRendicionCard()

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Esqueleto Shimmer para Gastos:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TeqPrimary
                            )
                            ShimmerGastoCard()

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Cargador Pulsante de Ondas:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TeqPrimary
                            )
                            TeqPulsingLoader(message = "Simulando lectura y sincronización...")
                        }
                    }
                }
            }

            // SECCIÓN 5: INFORMACIÓN DE APLICACIÓN Y BASE DE DATOS
            item {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    elevation = 3.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = TeqPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "TeqGastos · TEQMED SpA",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Versión 1.2.0 · Evaluación Parcial 3 (DSY1105)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "Persistencia: SQLite 3 vía Room Database con KSP",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeOptionChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) TeqPrimary else MaterialTheme.colorScheme.outline
            )
        },
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = TeqPrimary.copy(alpha = 0.15f),
            selectedLabelColor = TeqPrimary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected) TeqPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    )
}
