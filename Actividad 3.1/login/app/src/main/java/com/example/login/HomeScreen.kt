package com.example.login

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast //Reto 6: exportar lista a JSON
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

// NOTA: NotificationHelper está en el mismo paquete (com.example.login),
// por lo que NO hace falta importarlo.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: VideojuegoViewModel,
    onLogout: () -> Unit
) {
    // ---------------------------------------------------------
    // ESTADO LOCAL DE LA UI
    // ---------------------------------------------------------
    var menuExpanded by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showFormDialog by remember { mutableStateOf(false) }
    var videojuegoEditando by remember { mutableStateOf<Videojuego?>(null) }
    var videojuegoABorrar by remember { mutableStateOf<Videojuego?>(null) }

    // ---------------------------------------------------------
    // ESTADO DEL VIEWMODEL
    // ---------------------------------------------------------
    val userEmail by viewModel.emailUsuario.collectAsState()
    val videojuegos by viewModel.videojuegos.collectAsState()

    val context = LocalContext.current

    // Para leer strings dentro de callbacks sin que salte el lint
    // "Querying resource values using LocalContext.current"
    val resources = LocalResources.current

    // ---------------------------------------------------------
    // PERMISO POST_NOTIFICATIONS
    // ---------------------------------------------------------
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Si el usuario rechaza, la app sigue funcionando sin notificación.
    }

    // Reto 2: cambiar por filtro
    val filtro by viewModel.filtro.collectAsState()

    // Reto 1: Borrar todo
    var showBorrarTodoDialog by remember { mutableStateOf(false) }

    // Reto 4: ordenar por título, nota, año
    // (se declara ANTES de usarlo para calcular la lista)
    var ordenMenuExpanded by remember { mutableStateOf(false) }
    val ordenActual by viewModel.orden.collectAsState()

    // Reto 2 + Reto 4: filtrar por género y ordenar
    val videojuegosFiltrados = videojuegos
        .filter { it.genero.contains(filtro.trim(), ignoreCase = true) }
        .let { lista ->
            when (ordenActual) {
                Orden.TITULO -> lista.sortedBy { it.titulo.lowercase() }
                Orden.NOTA -> lista.sortedByDescending { it.nota }
                Orden.ANYO -> lista.sortedByDescending { it.anyo }
            }
        }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.app_name))
                },
                actions = {
                    // Reto 4: menú de orden
                    TextButton(onClick = { ordenMenuExpanded = true }) {
                        Text(stringResource(R.string.orden_boton))
                    }
                    DropdownMenu(
                        expanded = ordenMenuExpanded,
                        onDismissRequest = { ordenMenuExpanded = false }
                    ) {
                        Orden.entries.forEach { orden ->
                            val etiqueta = when (orden) {
                                Orden.TITULO -> R.string.orden_titulo
                                Orden.NOTA -> R.string.orden_nota
                                Orden.ANYO -> R.string.orden_anyo
                            }
                            DropdownMenuItem(
                                text = {
                                    // Marcamos la opción activa con una ✓
                                    Text(
                                        (if (orden == ordenActual) "✓ " else "") +
                                                stringResource(etiqueta)
                                    )
                                },
                                onClick = {
                                    viewModel.cambiarOrden(orden)
                                    ordenMenuExpanded = false
                                }
                            )
                        }
                    }

                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.abrir_menu)
                        )
                    }

                    // Reto 1: Borrar todo
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.menu_acerca_de))
                            },
                            onClick = {
                                menuExpanded = false
                                showAboutDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.menu_borrar_todo))
                            },
                            onClick = {
                                menuExpanded = false // cierra el menu
                                showBorrarTodoDialog = true // abre el dialogo
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.menu_cerrar_sesion))
                            },
                            onClick = {
                                menuExpanded = false
                                onLogout()
                            }
                        )

                        //Reto 6: exportar lista a JSON
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_exportar_json)) },
                            onClick = {
                                menuExpanded = false
                                viewModel.exportarJson(context) { ruta ->
                                    Toast.makeText(
                                        context,
                                        resources.getString(R.string.export_ok, ruta),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                videojuegoEditando = null
                showFormDialog = true
            }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.boton_add_videojuego)
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = if (userEmail.isNotEmpty()) {
                    stringResource(R.string.home_bienvenida, userEmail)
                } else {
                    stringResource(R.string.home_sin_email)
                },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text(
                text = stringResource(R.string.home_titulo_lista),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Reto 2: filtrar por genero
            OutlinedTextField(
                value = filtro,
                onValueChange = {
                    viewModel.cambiarFiltro(it)
                },
                label = {
                    Text(stringResource(R.string.hint_filtro_genero))
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            if (videojuegosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(
                            if (videojuegos.isEmpty()) {
                                R.string.home_lista_vacia
                            } else {
                                R.string.home_sin_resultados
                            }
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(videojuegosFiltrados, key = { it.id }) { videojuego ->
                        VideojuegoItem(
                            videojuego = videojuego,
                            onClick = {
                                videojuegoEditando = videojuego
                                showFormDialog = true
                            },
                            onDeleteClick = {
                                videojuegoABorrar = videojuego
                            },

                            // Reto 3: marcar completado y mostrar notificación
                            onToggleCompletado = {

                                // Calculamos el valor NUEVO (el contrario al actual)
                                val nuevoValor = !videojuego.completado

                                viewModel.actualizar(
                                    videojuego.copy(completado = nuevoValor)
                                )

                                // Solo notificamos al MARCAR (true),
                                // no al desmarcar, y solo con permiso
                                if (nuevoValor && tienePermisoNotificaciones(context)) {
                                    NotificationHelper.mostrarNotificacion(
                                        context = context,
                                        titulo = resources.getString(
                                            R.string.notif_completado_titulo
                                        ),
                                        mensaje = resources.getString(
                                            R.string.notif_completado_mensaje,
                                            videojuego.titulo
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------
    // DIÁLOGO "Acerca de"
    // ---------------------------------------------------------
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text(stringResource(R.string.dialog_acerca_titulo))
            },
            text = {
                Text(stringResource(R.string.dialog_acerca_texto))
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(stringResource(R.string.dialog_aceptar))
                }
            }
        )
    }

    // ---------------------------------------------------------
    // DIÁLOGO CREAR / EDITAR
    // ---------------------------------------------------------
    if (showFormDialog) {
        VideojuegoFormDialog(
            videojuegoInicial = videojuegoEditando,
            onDismiss = {
                showFormDialog = false
                videojuegoEditando = null
            },
            onSave = { videojuego ->

                if (videojuegoEditando == null) {
                    viewModel.insertar(videojuego)

                    val granted = tienePermisoNotificaciones(context)

                    if (granted) {
                        NotificationHelper.mostrarNotificacion(
                            context = context,
                            titulo = resources.getString(R.string.notif_titulo),
                            mensaje = resources.getString(
                                R.string.notif_mensaje,
                                videojuego.titulo
                            )
                        )
                    }
                } else {
                    viewModel.actualizar(videojuego)
                }

                showFormDialog = false
                videojuegoEditando = null
            }
        )
    }

    // ---------------------------------------------------------
    // DIÁLOGO CONFIRMAR BORRADO
    // ---------------------------------------------------------
    videojuegoABorrar?.let { juego ->
        AlertDialog(
            onDismissRequest = {
                videojuegoABorrar = null
            },
            title = {
                Text(stringResource(R.string.dialog_borrar_titulo))
            },
            text = {
                Text(stringResource(R.string.dialog_borrar_texto, juego.titulo))
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.borrar(juego)
                    videojuegoABorrar = null
                }) {
                    Text(stringResource(R.string.boton_borrar))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    videojuegoABorrar = null
                }) {
                    Text(stringResource(R.string.boton_cancelar))
                }
            }
        )
    }

    // ---------------------------------------------------------
    // RETO 1: BORRAR TODO
    // ---------------------------------------------------------
    if (showBorrarTodoDialog) {
        AlertDialog(
            onDismissRequest = {
                showBorrarTodoDialog = false
            },
            title = {
                Text(stringResource(R.string.dialog_borrar_todo_titulo))
            },
            text = {
                Text(stringResource(R.string.dialog_borrar_todo_texto))
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.borrarTodo() // ya existe en el ViewModel
                    showBorrarTodoDialog = false
                }) {
                    Text(stringResource(R.string.boton_borrar))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBorrarTodoDialog = false
                }) {
                    Text(stringResource(R.string.boton_cancelar))
                }
            }
        )
    }
}

// ---------------------------------------------------------
// ITEM DE LA LISTA: una fila por videojuego.
// ---------------------------------------------------------
@Composable
private fun VideojuegoItem(
    videojuego: Videojuego,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleCompletado: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = videojuego.completado,
                onCheckedChange = { onToggleCompletado() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = videojuego.titulo,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "${videojuego.plataforma} · ${videojuego.genero} · ${videojuego.anyo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Nota: ${videojuego.nota}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.cd_borrar_juego),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ---------------------------------------------------------
// DIÁLOGO DE FORMULARIO (crear o editar).
// ---------------------------------------------------------
@Composable
private fun VideojuegoFormDialog(
    videojuegoInicial: Videojuego?,
    onDismiss: () -> Unit,
    onSave: (Videojuego) -> Unit
) {
    val esEdicion = videojuegoInicial != null

    var titulo by remember { mutableStateOf(videojuegoInicial?.titulo ?: "") }
    var plataforma by remember { mutableStateOf(videojuegoInicial?.plataforma ?: "") }
    var genero by remember { mutableStateOf(videojuegoInicial?.genero ?: "") }
    var anyo by remember {
        mutableStateOf(videojuegoInicial?.anyo?.toString() ?: "")
    }
    var nota by remember {
        mutableStateOf(videojuegoInicial?.nota?.toString() ?: "")
    }
    var completado by remember {
        mutableStateOf(videojuegoInicial?.completado ?: false)
    }

    var tituloError by remember { mutableStateOf<String?>(null) }
    var anyoError by remember { mutableStateOf<String?>(null) }
    var notaError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (esEdicion) R.string.dialog_editar_titulo
                    else R.string.dialog_nuevo_titulo
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                OutlinedTextField(
                    value = titulo,
                    onValueChange = {
                        titulo = it
                        tituloError = null
                    },
                    label = {
                        Text(stringResource(R.string.hint_titulo_juego))
                    },
                    isError = tituloError != null,
                    supportingText = tituloError?.let {
                        { Text(it) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = plataforma,
                    onValueChange = { plataforma = it },
                    label = {
                        Text(stringResource(R.string.hint_plataforma))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = genero,
                    onValueChange = { genero = it },
                    label = {
                        Text(stringResource(R.string.hint_genero))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = anyo,
                    onValueChange = {
                        anyo = it.filter { c -> c.isDigit() }
                        anyoError = null
                    },
                    label = {
                        Text(stringResource(R.string.hint_anyo))
                    },
                    isError = anyoError != null,
                    supportingText = anyoError?.let {
                        { Text(it) }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nota,
                    onValueChange = {
                        nota = it
                        notaError = null
                    },
                    label = {
                        Text(stringResource(R.string.hint_nota))
                    },
                    isError = notaError != null,
                    supportingText = notaError?.let {
                        { Text(it) }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = completado,
                        onCheckedChange = { completado = it }
                    )
                    Text(stringResource(R.string.check_completado))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {

                val tituloOk = titulo.isNotBlank()
                tituloError = if (!tituloOk) {
                    "El título no puede estar vacío"
                } else {
                    null
                }

                val anyoInt = anyo.toIntOrNull()
                val anyoOk = anyoInt != null && anyoInt in 1950..2100
                anyoError = if (!anyoOk) {
                    "Introduce un año válido"
                } else {
                    null
                }

                val notaFloat = nota.replace(',', '.').toFloatOrNull()
                val notaOk = notaFloat != null && notaFloat in 0f..10f
                notaError = if (!notaOk) {
                    "La nota debe estar entre 0 y 10"
                } else {
                    null
                }

                // Al comprobar null aquí, Kotlin hace smart cast
                // y ya no hacen falta los "!!"
                if (tituloOk && anyoInt != null && anyoOk && notaFloat != null && notaOk) {
                    onSave(
                        Videojuego(
                            id = videojuegoInicial?.id ?: 0,
                            titulo = titulo.trim(),
                            plataforma = plataforma.trim(),
                            genero = genero.trim(),
                            anyo = anyoInt,
                            nota = notaFloat,
                            completado = completado
                        )
                    )
                }
            }) {
                Text(stringResource(R.string.boton_guardar))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.boton_cancelar))
            }
        }
    )
}

// ---------------------------------------------------------
// RETO 3: COMPROBAR PERMISO DE NOTIFICACIONES
// ---------------------------------------------------------

// Devuelve true si podemos mostrar notificaciones.
// En Android 12 o anterior no hace falta permiso.
private fun tienePermisoNotificaciones(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}