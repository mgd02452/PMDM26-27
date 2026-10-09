package com.example.login

import android.content.res.Configuration
import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
// [RETO 3]: Importación de los nuevos iconos de candado
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.login.ui.theme.LoginTheme

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    // Estado inicial de campos
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // [RETO 5]: Nuevos estados para la confirmación de contraseña
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var showPassword by remember { mutableStateOf(false) }
    var recordar by rememberSaveable { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val emailFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        emailFocusRequester.requestFocus()
    }

    // [RETO 4]: Funciones puras de comprobación
    fun esEmailValido(textoEmail: String): Boolean {
        return textoEmail.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(textoEmail.trim()).matches()
    }

    fun esPasswordValida(textoPassword: String): Boolean {
        val regex = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$".toRegex()
        return regex.matches(textoPassword)
    }

    // [RETO 5]: Comprobación de que ambas contraseñas coincidan y no estén vacías
    fun esConfirmPasswordValida(pass: String, confirmPass: String): Boolean {
        return confirmPass.isNotEmpty() && pass == confirmPass
    }

    // Funciones de validación para asignar o limpiar mensajes de error
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

    // [RETO 5]: Asignar mensaje de error de confirmación
    fun validarConfirmPassword(): Boolean {
        val coincide = esConfirmPasswordValida(password, confirmPassword)
        confirmPasswordError = if (!coincide) {
            "Las contraseñas no coinciden"
        } else null
        return coincide
    }

    // [RETO 4 y 5]: Estado derivado que determina si todo el formulario es válido (los 3 campos)
    val isFormValid by remember(email, password, confirmPassword) {
        derivedStateOf {
            esEmailValido(email) && esPasswordValida(password) && esConfirmPasswordValida(password, confirmPassword)
        }
    }

    // [RETO 7]: Envolvemos la pantalla en un Surface con el color de fondo del tema
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
                // [RETO 7]: Tinte del icono según el color primario del tema
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.titulo_bienvenida),
                style = MaterialTheme.typography.headlineSmall,
                // [RETO 7]: Color dinámico adaptado al tema (onBackground)
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo 1: EMAIL
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
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(emailFocusRequester)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo 2: CONTRASEÑA
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (it.isNotEmpty()) validarPassword()
                    // [RETO 5]: Revalidar la confirmación si se cambia la contraseña principal
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
                            // [RETO 3]: Uso de los iconos personalizados de candado
                            imageVector = if (showPassword) Icons.Filled.LockOpen else Icons.Filled.Lock,
                            contentDescription = stringResource(R.string.toggle_password)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next // [RETO 5]: Cambiado a Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo 3: [RETO 5] CONFIRMAR CONTRASEÑA
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
                    onDone = {
                        focusManager.clearFocus()
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = recordar,
                    onCheckedChange = { recordar = it }
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
                    if (isFormValid) {
                        focusManager.clearFocus()
                        onLoginSuccess()
                    }
                },
                // [RETO 4]: Bloqueado hasta que el formulario sea totalmente válido
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.boton_entrar))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BOTÓN "¿Olvidaste tu contraseña?"
            TextButton(
                onClick = { /* Acción */ },
                // [RETO 1]: Mover el botón alineándolo a la derecha (Alignment.End)
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.olvidaste_password))
            }
        }
    }
}
// [RETO 7]: Previsualización en Modo Claro
@Preview(name = "Light Mode", showBackground = true)
@Composable
fun LoginScreenLightPreview() {
    LoginTheme { // Cambiado MaterialTheme por LoginTheme (o el tema de tu app)
        LoginScreen(onLoginSuccess = {})
    }
}

// [RETO 7]: Previsualización en Modo Oscuro
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun LoginScreenDarkPreview() {
    LoginTheme { // Cambiado MaterialTheme por LoginTheme (o el tema de tu app)
        LoginScreen(onLoginSuccess = {})
    }
}