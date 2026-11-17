package com.example.teqgastos.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.teqgastos.data.security.BiometricAuthManager
import com.example.teqgastos.model.UserRole
import com.example.teqgastos.ui.components.GlassmorphicButton
import com.example.teqgastos.ui.components.NeumorphicButton
import com.example.teqgastos.ui.components.NeumorphicCard
import com.example.teqgastos.ui.components.NielsenHelpDialog
import com.example.teqgastos.ui.components.RutValidator
import com.example.teqgastos.ui.components.TeqGastosBrandLogo
import com.example.teqgastos.ui.theme.NeuBackground
import com.example.teqgastos.ui.theme.NeuPressed
import com.example.teqgastos.ui.theme.NeuSurface
import com.example.teqgastos.ui.theme.TeqPrimary
import com.example.teqgastos.ui.viewmodel.AuthState
import com.example.teqgastos.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: (UserRole) -> Unit
) {
    val session by authViewModel.userSession.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var showHelpDialog by remember { mutableStateOf(false) }
    var rutInput by remember { mutableStateOf(session.rut) }
    var passwordInput by remember { mutableStateOf("teqmed2026") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isRutValid by remember { mutableStateOf(RutValidator.isValid(session.rut)) }

    val isBiometricAvailable = remember(context) {
        BiometricAuthManager.canAuthenticate(context)
    }

    // Monitoreo reactivo de autenticación para navegación fluida
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess((authState as AuthState.Success).session.rol)
        }
    }

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
            // 1. Barra Superior con Guía Nielsen
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "Guía y Usabilidad (Leyes de Nielsen)",
                            tint = TeqPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Logo Oficial Corporativo TEQMED TeqGastos
                TeqGastosBrandLogo(
                    size = 96.dp,
                    animated = true
                )

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

                Spacer(modifier = Modifier.height(20.dp))
            }

            // 2. Banner de Feedback de Error Animado (AnimatedVisibility)
            item {
                AnimatedVisibility(
                    visible = authState is AuthState.Error,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    if (authState is AuthState.Error) {
                        val errorMessage = (authState as AuthState.Error).message
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = errorMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { authViewModel.resetAuthState() }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cerrar error",
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Tarjeta Neumórfica de Credenciales Corporativas (Formulario Validado)
            item {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    elevation = 5.dp
                ) {
                    Text(
                        text = "Acceso Seguro con JWT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TeqPrimary
                    )
                    Text(
                        text = "Ingrese sus credenciales de colaborador o use biometría:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo RUT con auto-formateo y validación de Módulo 11
                    OutlinedTextField(
                        value = rutInput,
                        onValueChange = { input ->
                            val formatted = RutValidator.formatRut(input)
                            rutInput = formatted
                            isRutValid = RutValidator.isValid(formatted)
                        },
                        label = { Text("RUT Colaborador") },
                        placeholder = { Text("12.345.678-K") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = TeqPrimary
                            )
                        },
                        trailingIcon = {
                            if (rutInput.isNotEmpty()) {
                                if (isRutValid) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "RUT Válido",
                                        tint = Color(0xFF2E7D32)
                                    )
                                } else if (rutInput.length >= 8) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = "RUT Inválido",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        },
                        isError = rutInput.length >= 8 && !isRutValid,
                        supportingText = {
                            if (rutInput.length >= 8 && !isRutValid) {
                                Text(
                                    text = "Dígito verificador incorrecto (Módulo 11)",
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else if (isRutValid) {
                                Text(
                                    text = "RUT válido",
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TeqPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedContainerColor = NeuSurface,
                            unfocusedContainerColor = NeuSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Campo Contraseña con Toggle de Visibilidad
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Contraseña") },
                        placeholder = { Text("Mínimo 4 caracteres") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = TeqPrimary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ocultar clave" else "Mostrar clave",
                                    tint = TeqPrimary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                authViewModel.loginWithCredentials(rutInput, passwordInput)
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TeqPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedContainerColor = NeuSurface,
                            unfocusedContainerColor = NeuSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botón Principal Glassmorphism para Iniciar Sesión
                    val isLoading = authState is AuthState.Loading
                    GlassmorphicButton(
                        onClick = {
                            focusManager.clearFocus()
                            authViewModel.loginWithCredentials(rutInput, passwordInput)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        cornerRadius = 16.dp,
                        elevation = 6.dp
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Autenticando...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Iniciar Sesión Segura",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Botón de Acceso Biométrico (Huella Digital)
                    if (isBiometricAvailable) {
                        Spacer(modifier = Modifier.height(12.dp))
                        NeumorphicButton(
                            onClick = {
                                focusManager.clearFocus()
                                authViewModel.loginWithBiometrics(context, onSuccess = {})
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            cornerRadius = 14.dp,
                            elevation = 3.dp,
                            backgroundColor = NeuSurface
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Huella Digital",
                                tint = TeqPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Ingreso con Huella Digital",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = TeqPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 4. Perfiles de Prueba Corporativa y Selector Rápido de Roles
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "  Perfiles Corporativos TEQMED  ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Medium
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Seleccione un perfil para simular el rol en terreno:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    onSelect = {
                        authViewModel.switchRole(UserRole.TECNICO)
                        rutInput = "17.482.913-K"
                        passwordInput = "teqmed2026"
                        isRutValid = true
                    }
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
                    onSelect = {
                        authViewModel.switchRole(UserRole.SUPERVISOR)
                        rutInput = "12.839.210-4"
                        passwordInput = "teqmed2026"
                        isRutValid = true
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                NeuRoleSelectionCard(
                    role = UserRole.FINANZAS,
                    title = "Finanzas y Contabilidad",
                    userName = "Roberto Cabrera",
                    userRut = "14.209.811-3",
                    description = "Revisión final, registro de pagos de reembolsos y exportación a ERP.",
                    icon = Icons.Default.AccountBalance,
                    isSelected = session.rol == UserRole.FINANZAS,
                    onSelect = {
                        authViewModel.switchRole(UserRole.FINANZAS)
                        rutInput = "14.209.811-3"
                        passwordInput = "teqmed2026"
                        isRutValid = true
                    }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Botón de Acceso Directo con Rol Activo
            item {
                GlassmorphicButton(
                    onClick = { onLoginSuccess(session.rol) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    cornerRadius = 16.dp,
                    elevation = 4.dp
                ) {
                    Text(
                        text = "Entrar directo como ${session.rol.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
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
