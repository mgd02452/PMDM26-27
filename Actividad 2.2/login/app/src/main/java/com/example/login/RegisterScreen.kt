package com.example.login

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
// [RETO 5]: import explícito de derivedStateOf (ya lo cubre runtime.*, se deja por claridad)
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
// [RETO 5]: Color para el contador en verde
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------
// [RETO 5]: reglas de validación como funciones PURAS
// (no tocan estados de error, solo devuelven un Boolean)
// ---------------------------------------------------------
private val PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$".toRegex()

private fun emailValido(e: String) =
    e.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(e.trim()).matches()

private fun passwordValida(p: String) = PASSWORD_REGEX.matches(p)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    // [RETO 3 - NOMBRE]: ahora recibe (email, nombre) para pasarlos a Home
    onRegisterSuccess: (String, String) -> Unit,
    onBackToLogin: () -> Unit
) {
    // ---------------------------------------------------------
    // 1. ESTADO
    // ---------------------------------------------------------
    // [RETO 3 - NOMBRE]: nuevo estado para el nombre y su error
    var nombre by remember { mutableStateOf("") }
    var nombreError by remember { mutableStateOf<String?>(null) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptTerms by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    var termsError by remember { mutableStateOf<String?>(null) }

    var showPassword by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    // [RETO 5]: estado DERIVADO. Se recalcula SOLO si cambia alguno de los estados
    // que se leen dentro del bloque. Sin remember se recrearía en cada recomposición.
    // [RETO 3 - NOMBRE]: el nombre también cuenta, porque es obligatorio.
    val formularioValido by remember {
        derivedStateOf {
            nombre.isNotBlank() &&
                    emailValido(email) &&
                    passwordValida(password) &&
                    confirmPassword == password && confirmPassword.isNotEmpty() &&
                    acceptTerms
        }
    }

    // ---------------------------------------------------------
    // 2. FOCO
    // ---------------------------------------------------------
    val focusManager = LocalFocusManager.current
    // [RETO 3 - NOMBRE]: el foco inicial pasa al campo nombre (es el primero)
    val nombreFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        nombreFocusRequester.requestFocus()
    }

    // ---------------------------------------------------------
    // 3. VALIDACIONES (escriben el mensaje de error)
    // ---------------------------------------------------------
    // [RETO 3 - NOMBRE]: el nombre es obligatorio
    fun validarNombre(): Boolean {
        val ok = nombre.isNotBlank()
        nombreError = if (!ok) "Introduce tu nombre" else null
        return ok
    }

    fun validarEmail(): Boolean {
        // [RETO 5]: reutiliza la función pura
        val ok = emailValido(email)
        emailError = if (!ok) "Introduce un email válido" else null
        return ok
    }

    fun validarPassword(): Boolean {
        // [RETO 5]: reutiliza la función pura
        val ok = passwordValida(password)
        passwordError = if (!ok) {
            "Mínimo 8 caracteres, una mayúscula y un número"
        } else null
        return ok
    }

    fun validarConfirm(): Boolean {
        val ok = confirmPassword == password && confirmPassword.isNotEmpty()
        confirmError = if (!ok) "Las contraseñas no coinciden" else null
        return ok
    }

    fun validarTerminos(): Boolean {
        termsError = if (!acceptTerms) "Debes aceptar los términos" else null
        return acceptTerms
    }

    // ---------------------------------------------------------
    // 4. UI
    // ---------------------------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_registro)) },
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.volver)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.titulo_bienvenida),
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            // [RETO 3 - NOMBRE]: campo NOMBRE (antes del email para que el foco fluya bien)
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    if (it.isNotEmpty()) validarNombre() else nombreError = null
                },
                label = { Text(stringResource(R.string.hint_nombre)) },
                isError = nombreError != null,
                supportingText = nombreError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(nombreFocusRequester)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Email ---
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (it.isNotEmpty()) validarEmail()
                },
                label = { Text(stringResource(R.string.hint_email)) },
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                // [RETO 3 - NOMBRE]: se quita el focusRequester del email (ahora está en nombre)
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Contraseña ---
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (it.isNotEmpty()) validarPassword()
                    // Si ya había confirmación escrita, revalidamos
                    if (confirmPassword.isNotEmpty()) validarConfirm()
                },
                label = { Text(stringResource(R.string.hint_password)) },
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it) } },
                visualTransformation = if (showPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) {
                                Icons.Filled.Visibility
                            } else {
                                Icons.Filled.VisibilityOff
                            },
                            contentDescription = stringResource(R.string.toggle_password)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // [RETO 5]: contador "n/8" en tiempo real, justo debajo de la contraseña
            val cumple = password.length >= 8
            Text(
                text = stringResource(R.string.contador_password, password.length),
                style = MaterialTheme.typography.bodySmall,
                // Rojo si no llega a 8, verde si ya cumple.
                // Accesibilidad: el color no es lo único que informa, el texto "n/8" también.
                color = if (cumple) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Confirmar contraseña ---
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (it.isNotEmpty()) validarConfirm()
                },
                label = { Text(stringResource(R.string.hint_confirm_password)) },
                isError = confirmError != null,
                supportingText = confirmError?.let { { Text(it) } },
                visualTransformation = if (showConfirm) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { showConfirm = !showConfirm }) {
                        Icon(
                            imageVector = if (showConfirm) {
                                Icons.Filled.Visibility
                            } else {
                                Icons.Filled.VisibilityOff
                            },
                            contentDescription = stringResource(R.string.toggle_password)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- Checkbox términos ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = acceptTerms,
                    onCheckedChange = {
                        acceptTerms = it
                        if (it) termsError = null
                    }
                )
                Text(stringResource(R.string.acepto_terminos))
            }
            termsError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Botón Crear cuenta ---
            Button(
                onClick = {
                    // Las validaciones se mantienen dentro del onClick como red de seguridad
                    // [RETO 3 - NOMBRE]: validamos también el nombre
                    val nombreOk = validarNombre()
                    val emailOk = validarEmail()
                    val passOk = validarPassword()
                    val confirmOk = validarConfirm()
                    val termsOk = validarTerminos()
                    if (nombreOk && emailOk && passOk && confirmOk && termsOk) {
                        focusManager.clearFocus()
                        // [RETO 3 - NOMBRE]: pasamos email y nombre
                        onRegisterSuccess(email.trim(), nombre.trim())
                    }
                },
                // [RETO 5]: deshabilitado hasta que todo el formulario sea válido
                enabled = formularioValido,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.boton_crear_cuenta))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Volver a Login ---
            TextButton(onClick = onBackToLogin) {
                Text(stringResource(R.string.ya_tienes_cuenta))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    MaterialTheme {
        // [RETO 3 - NOMBRE]: el callback ahora tiene dos parámetros
        RegisterScreen(onRegisterSuccess = { _, _ -> }, onBackToLogin = {})
    }
}