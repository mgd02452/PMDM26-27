package com.example.login

import android.content.res.Configuration
import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first

@Composable
fun LoginScreen(
    onLoginSuccess: (String, Boolean) -> Unit,   // email + remember
    onGoToRegister: () -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var nombreError by remember { mutableStateOf<String?>(null) }

    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var showPassword by remember { mutableStateOf(false) }
    var rememberMe by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    val focusManager = LocalFocusManager.current
    val nombreFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        nombreFocusRequester.requestFocus()
    }

    LaunchedEffect(Unit) {
        val saved = UserPreferences.emailFlow(context).first()
        if (saved.isNotEmpty() && email.isEmpty()) {
            email = saved
            rememberMe = true
        }
    }

    fun esNombreValido(textoNombre: String): Boolean {
        return textoNombre.isNotBlank()
    }

    fun validarNombre(): Boolean {
        val valido = esNombreValido(nombre)
        nombreError = if (!valido) "Introduce tu nombre" else null
        return valido
    }

    fun esEmailValido(textoEmail: String): Boolean {
        return textoEmail.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(textoEmail.trim()).matches()
    }

    fun esPasswordValida(textoPassword: String): Boolean {
        val regex = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$".toRegex()
        return regex.matches(textoPassword)
    }

    fun esConfirmPasswordValida(pass: String, confirmPass: String): Boolean {
        return confirmPass.isNotEmpty() && pass == confirmPass
    }

    fun validarEmail(): Boolean {
        val valido = esEmailValido(email)
        emailError = if (!valido) "Introduce un email válido" else null
        return valido
    }

    fun validarPassword(): Boolean {
        val valida = esPasswordValida(password)
        passwordError = if (!valida) {
            "Mínimo 8 caracteres, una mayúscula y un número"
        } else null
        return valida
    }

    fun validarConfirmPassword(): Boolean {
        val coincide = esConfirmPasswordValida(password, confirmPassword)
        confirmPasswordError = if (!coincide) {
            "Las contraseñas no coinciden"
        } else null
        return coincide
    }

    val isFormValid by remember(nombre, email, password, confirmPassword) {
        derivedStateOf {
            esNombreValido(nombre) &&
                    esEmailValido(email) &&
                    esPasswordValida(password) &&
                    esConfirmPasswordValida(password, confirmPassword)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(R.string.logo_desc),
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.titulo_bienvenida),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            // NOMBRE
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

            // EMAIL
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
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CONTRASEÑA
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (it.isNotEmpty()) validarPassword()
                    if (confirmPassword.isNotEmpty()) validarConfirmPassword()
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
                            imageVector = if (showPassword) Icons.Filled.LockOpen else Icons.Filled.Lock,
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

            Spacer(modifier = Modifier.height(16.dp))

            // CONFIRMAR CONTRASEÑA
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (it.isNotEmpty()) {
                        validarConfirmPassword()
                    } else {
                        confirmPasswordError = null
                    }
                },
                label = { Text(stringResource(R.string.hint_confirm_password)) },
                isError = confirmPasswordError != null,
                supportingText = confirmPasswordError?.let { { Text(it) } },
                visualTransformation = if (showConfirmPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            imageVector = if (showConfirmPassword) Icons.Filled.LockOpen else Icons.Filled.Lock,
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

            // RECORDAR SESIÓN
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it }
                )
                Text(
                    text = stringResource(R.string.recordar_sesion),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BOTÓN ENTRAR
            Button(
                onClick = {
                    val emailOk = validarEmail()
                    val passOk = validarPassword()
                    if (emailOk && passOk) {
                        focusManager.clearFocus()
                        onLoginSuccess(email.trim(), rememberMe)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.boton_entrar))
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { /* Acción */ },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.olvidaste_password))
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onGoToRegister) {
                Text(stringResource(R.string.ya_tienes_cuenta))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(onLoginSuccess = { _, _ -> }, onGoToRegister = {})
    }
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun LoginScreenDarkPreview() {
    MaterialTheme {
        LoginScreen(onLoginSuccess = { _, _ -> }, onGoToRegister = {})
    }
}