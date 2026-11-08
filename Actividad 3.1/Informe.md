# Memoria de la Actividad 3.1: CRUD con Room, DataStore, ViewModel, Notificaciones y Despliegue

PMDM · 2º DAM · Informe UI/UX · Autor/a: Andrea Milena Vega Mora

## 1. Introducción

En esta actividad se ha ampliado la aplicación de la Actividad 2.2 (Login, Registro y Home con `NavHost` y `Scaffold`) para dotarla de persistencia real. La sesión del usuario se guarda con **DataStore**, la colección de videojuegos con **Room**, la interfaz se actualiza de forma reactiva con **Flow**, se añaden **notificaciones** con permiso en tiempo de ejecución y, por último, se genera un **APK firmado en modo release** que se instala en un dispositivo real.

Objetivos cubiertos:

- Persistir la sesión (email y «Recordar sesión») y ofrecer auto-login.
- Modelar la entidad `Videojuego` con CRUD completo en Room.
- Separar la lógica de la UI con ViewModel y repositorio.
- Mostrar la colección con `LazyColumn`, diálogos de creación, edición y borrado.
- Solicitar `POST_NOTIFICATIONS` en runtime sin romper la app si se rechaza.
- Empaquetar y desplegar el APK firmado.

Competencias trabajadas: CE.b (ventanas, menús, alertas y controles), CE.f (almacenes de datos), CE.h (empaquetado y despliegue), CE.i (documentación) y CE.j (permisos y seguridad).

## 2. Diagrama de navegación

La app tiene tres destinos. La ruta `home` ya no recibe el email como argumento: `HomeScreen` lo obtiene de DataStore a través del ViewModel.

```
                 ┌──────────────────────────────┐
   arranque ───► │ ¿rememberSession == true?    │
                 └───────┬───────────────┬──────┘
                    no   │               │  sí (auto-login)
                         ▼               ▼
                 ┌────────────┐     ┌────────────┐
                 │   LOGIN    │     │    HOME    │
                 │  (login)   │     │   (home)   │
                 └──┬──────┬──┘     └─────┬──────┘
         login OK   │      │ «Registrarse'│
   (guarda sesión)  │      ▼              │ «Cerrar sesión»
                    │ ┌────────────┐      │ (limpia DataStore)
                    │ │  REGISTRO  │      │
                    │ │ (register) │      │
                    │ └─────┬──────┘      │
                    │       │ registro OK │
                    ▼       ▼             ▼
                 ┌────────────┐     ┌────────────┐
                 │    HOME    │     │   LOGIN    │
                 └────────────┘     └────────────┘
```

| Ruta | Pantalla | Transición de entrada | Transición de salida |
| --- | --- | --- | --- |
| `login` | `LoginScreen` | Arranque sin sesión recordada, o tras cerrar sesión | A `home` (login correcto) o a `register` |
| `register` | `RegisterScreen` | Desde Login | A `home` (registro correcto, sesión guardada con `remember = true`) o vuelta atrás a Login |
| `home` | `HomeScreen` | Arranque con sesión recordada, o tras login/registro | A `login` al cerrar sesión (`popUpTo(0)`) |

Al entrar en Home se elimina Login de la pila (`popUpTo(Routes.LOGIN) { inclusive = true }`), de modo que el botón Atrás no devuelve al formulario de acceso.

## 3. Wireframes y capturas de pantalla

| Pantalla | Qué debe mostrarse | Captura |
| --- | --- | --- |
| Login | Campos de email y contraseña, casilla «Recordar sesión», botones Entrar y Registro | Captura 1 |
| Home con lista vacía | Cabecera con el email, título «Mis videojuegos», mensaje «Todavía no has añadido videojuegos» y FAB (+) | Captura 2b |
| Home con lista | Tarjetas con título, plataforma · género · año, nota, casilla de completado y papelera | Captura 2 |
| Diálogo nuevo videojuego | Formulario con título, plataforma, género, año, nota y «Completado» | Captura 3 |
| Diálogo editar videojuego | El mismo formulario con los datos precargados | Captura 4 |
| Diálogo confirmar borrado | Texto «¿Seguro que quieres borrar…?» con Borrar y Cancelar | Captura 5 |

Elementos de interfaz utilizados: `Scaffold`, `TopAppBar` con menú overflow (Acerca de y Cerrar sesión), `LazyColumn`, `Card`, `FloatingActionButton`, `OutlinedTextField`, `Checkbox` y `AlertDialog`. Todos los textos se encuentran en `strings.xml` y se referencian con `stringResource`.

### Capturas de la aplicación

*Captura 1. Pantalla de registro: nombre, email, contraseña, confirmación y casilla «Recordar sesión».*

![Captura 1: Pantalla de registro](Capturas/1.jpeg)

*Captura 2. Home con la lista de videojuegos: plataforma, género, año, nota, casilla de completado y papelera.*

![Captura 2: Home con la lista de videojuegos](Capturas/2.jpeg)

*Captura 2b. Home con la lista vacía: mensaje «Todavía no has añadido videojuegos» y botón flotante (+).*

![Captura 2b: Home con la lista vacía](Capturas/2b.jpeg)

*Captura 3. Diálogo «Nuevo videojuego» con todos los campos vacíos.*

![Captura 3: Diálogo nuevo videojuego](Capturas/3.jpeg)

*Captura 4. Diálogo «Editar videojuego» con los datos precargados.*

![Captura 4: Diálogo editar videojuego](Capturas/4.jpeg)

*Captura 5. Diálogo de confirmación antes de borrar un videojuego.*

![Captura 5: Diálogo confirmar borrado](Capturas/5.jpeg)

*Captura 6. Opción «Acerca de» del menú overflow.*

![Captura 6: Acerca de](Capturas/6.png)

*Captura 7. Diálogo «¿Cerrar sesión?» del menú overflow.*

![Captura 7: Cerrar sesión](Capturas/7.jpeg)

*Captura 8. Menú overflow de Home con las opciones «Acerca de» y «Cerrar sesión».*

![Captura 8: Menú overflow de Home](Capturas/8.png)



## 4. Decisiones de diseño

La app sigue una arquitectura por capas con flujo de datos unidireccional:

```
HomeScreen  →  VideojuegoViewModel  →  VideojuegoRepository  →  Room (videojuegos)
                                                              →  DataStore (sesión)
```

### 4.1. ¿Por qué DataStore?

La sesión consiste en dos valores sencillos (email y flag «Recordar sesión»), por lo que el modelo clave-valor es suficiente. DataStore sustituye a `SharedPreferences` porque es asíncrono (no bloquea el hilo principal), expone los datos como `Flow` (la UI reacciona a los cambios) y evita errores de tipo en tiempo de ejecución.

### 4.2. ¿Por qué Room?

Los videojuegos son datos estructurados con varios campos y requieren consultas, ordenación y modificación individual. Room es un ORM sobre SQLite que ofrece comprobación de las consultas en compilación, menos código repetitivo que SQLite directo (sin `Cursor` ni `ContentValues`) y consultas reactivas que devuelven `Flow<List<Videojuego>>`, de modo que la lista se actualiza sola tras insertar, editar o borrar.

### 4.3. ¿Por qué ViewModel?

Al rotar la pantalla la Activity se destruye y se recrea; si la lógica viviera en el composable, el estado se perdería. El ViewModel sobrevive a los cambios de configuración, contiene la lógica de negocio, expone el estado como `StateFlow` (mediante `stateIn` con `SharingStarted.WhileSubscribed(5_000)`) y lanza las operaciones de escritura en `viewModelScope`, fuera del hilo principal. Se construye con una `ViewModelProvider.Factory` porque necesita el repositorio como dependencia.

### 4.4. ¿Por qué un repositorio?

El repositorio centraliza el acceso a Room y DataStore y oculta a la UI de dónde proceden los datos. Reduce el acoplamiento, evita duplicar código si varias pantallas necesitan los mismos datos y facilita las pruebas. Recibe el DAO y el `SessionDataStore` por constructor (inyección de dependencias manual) y la base de datos se obtiene mediante un singleton (`AppDatabase.getInstance`).

## 5. Estrategia de validación

La validación del formulario de videojuegos se ejecuta al pulsar «Guardar». Si alguna regla falla, se muestra el error bajo el campo correspondiente (`isError` y `supportingText`) y no se guarda nada. El error de cada campo se borra en cuanto el usuario vuelve a editarlo.

| Campo | Regla | Mensaje de error |
| --- | --- | --- |
| Título | No puede estar vacío ni contener solo espacios (`isNotBlank`) | «El título no puede estar vacío» |
| Plataforma | Texto libre, sin validación obligatoria | (ninguno) |
| Género | Texto libre, sin validación obligatoria | (ninguno) |
| Año | Solo dígitos (el campo filtra cualquier otro carácter) y valor entre 1950 y 2100 | «Introduce un año válido» |
| Nota | Número decimal entre 0 y 10; se normaliza la coma a punto antes de convertir | «La nota debe estar entre 0 y 10» |
| Completado | Casilla booleana, valor por defecto `false` | (ninguno) |

Además, los teclados se adaptan al tipo de dato (`KeyboardType.Number` para el año y `KeyboardType.Decimal` para la nota), y los textos se recortan con `trim()` antes de guardarlos. La validación de Login y Registro es la implementada en la Actividad 2.2: \[describir brevemente las reglas de email y contraseña\].

## 6. Estrategia de persistencia

| Dato | Dónde se guarda | Motivo |
| --- | --- | --- |
| Email del usuario | DataStore (`session_prefs`, clave `email`) | Dato simple clave-valor, se lee como `Flow` para mostrarlo en Home |
| Flag «Recordar sesión» | DataStore (clave `remember_session`) | Decide el destino inicial: Home si es `true`, Login si no |
| Colección de videojuegos | Room (base de datos `login.db`, tabla `videojuegos`) | Datos estructurados con CRUD y consultas reactivas |

Comportamiento verificado:

- **Rotar la pantalla:** la lista se mantiene gracias al ViewModel.
- **Cerrar y reabrir la app:** los videojuegos siguen ahí porque están en disco (Room).
- **Cerrar sesión:** se limpia DataStore (`clearSession`), pero los videojuegos permanecen en la base de datos.
- **Auto-login:** al arrancar se lee `rememberFlow` con `collectAsState(initial = null)`; mientras vale `null` no se compone el `NavHost`, y después se elige el destino inicial.

La tabla `videojuegos` tiene las columnas `id` (clave primaria autogenerada), `titulo`, `plataforma`, `genero`, `anyo`, `nota` y `completado`.

## 7. Permisos: `POST_NOTIFICATIONS`

`POST_NOTIFICATIONS` es un permiso peligroso (Android 13 o superior) y, por tanto, se declara en el Manifest y se solicita en tiempo de ejecución.

- **Declaración:** `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />` en `AndroidManifest.xml`, antes de `<application>`. Declararlo no lo concede.
- **Canal:** `NotificationHelper.createChannel` crea el canal `login_videojuegos` al arrancar la app desde `MainActivity` (obligatorio desde Android 8; la operación es idempotente).
- **Solicitud:** al entrar en `HomeScreen`, un `LaunchedEffect(Unit)` comprueba con `ContextCompat.checkSelfPermission` si el permiso está concedido y, si no lo está y la versión es Android 13 o superior, lo pide con `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())`.
- **Uso:** solo se notifica al **crear** un videojuego (no al editar), y únicamente si el permiso está concedido. En versiones anteriores a Android 13 no se necesita permiso.
- **Si se rechaza:** la app sigue funcionando con normalidad; el videojuego se añade igualmente, simplemente no se muestra la notificación. Además, `mostrarNotificacion` captura `SecurityException` para que la app no se cierre.

| Versión de Android | Permiso | Resultado al añadir un videojuego |
| --- | --- | --- |
| 13 o superior | Concedido | Se inserta y se muestra la notificación |
| 13 o superior | Denegado | Se inserta, sin notificación |
| 12 o anterior | No necesario | Se inserta y se muestra la notificación |

*Captura 9. Diálogo del sistema que solicita el permiso de notificaciones al entrar en Home.*

![Captura 9: Solicitud del permiso de notificaciones](Capturas/9.jpeg)

*Captura 10. Ajustes del sistema con el permiso de notificaciones sin conceder; la app sigue funcionando (caso 15).*

![Captura 10: Notificaciones sin permiso concedido](Capturas/10.jpeg)

*Captura 11. Notificaciones «Videojuego añadido» con el permiso concedido.*

![Captura 11: Notificaciones Videojuego añadido](Capturas/11.jpeg)


## 8. Despliegue

| Aspecto | Valor |
| --- | --- |
| Tipo de artefacto | APK firmado, variante `release`, esquema de firma V2 |
| Ruta del keystore | `C:\Users\[usuario]\keystores\login.jks` (fuera del proyecto, no subido a Git; la contraseña no se incluye en este documento) |
| Alias de la clave | `login` |
| Validez de la clave | 25 años |
| Ruta del APK | `app/release/app-release.apk` |
| Dispositivo utilizado | \[Marca, modelo y versión de Android\] |
| Método de instalación | \[USB con adb / transferencia de archivo / adb inalámbrico\] |

Generación del APK en Android Studio: `Build` → `Generate Signed Bundle / APK...` → `APK` → crear o seleccionar el keystore → alias `login` → variante `release` → `Create`.

*Captura 12. Asistente «Generate Signed App Bundle or APK»: se elige la opción APK.*

![Captura 12: Generar APK firmado](Capturas/12.png)

*Captura 13. Selección del keystore `login.jks` y del alias `login`.*

![Captura 13: Keystore y alias](Capturas/13.png)

*Captura 14. Carpeta de destino y variante `release` (App ID `com.example.login`).*

![Captura 14: Carpeta de destino y variante release](Capturas/14.png)

*Captura 14b. APK generado en `app/release`: `app-release.apk` (13.002 KB) junto a `baselineProfiles` y `output-metadata`.*

![Captura 14b: APK generado](Capturas/14b.png)

Verificación de la firma:

```
& "[ANDROID_HOME]\build-tools\36.0.0\apksigner.bat" verify --verbose app\build\outputs\apk\release\app-release.apk
```

Salida obtenida: `Verifies`, `Verified using v1 scheme (JAR signing): false` y `Verified using v2 scheme (APK Signature Scheme v2): true`. Solo el esquema v2 figura como verificado y hay un único firmante (Number of signers: 1).

*Captura 15. Verificación con `apksigner`: «Verifies», esquema v2 en true y un único firmante.*

![Captura 15: Verificación con apksigner](Capturas/15.png)

Instalación en el dispositivo real (depuración USB activada):

```
& "[ANDROID_HOME]\platform-tools\adb.exe" uninstall com.ejemplo.login
& "[ANDROID_HOME]\platform-tools\adb.exe" install -r app\build\outputs\apk\release\app-release.apk
```

Se desinstala primero la versión anterior para evitar el error `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, que aparece cuando la firma del APK nuevo no coincide con la de la app ya instalada. Al desinstalar se borran los datos locales de la app anterior.

*Captura 16. Instalación con adb: dispositivo detectado y `install -r` finalizado con «Success» (el `uninstall` previo devolvió `DELETE_FAILED_INTERNAL_ERROR`).*

![Captura 16: Instalación con adb](Capturas/16.png)

*Captura 17. MiApp instalada en el dispositivo real a partir del APK release.*

![Captura 17: MiApp instalada en el dispositivo](Capturas/17.jpeg)

Para evitar que ProGuard elimine clases en la versión release, se añadieron reglas para Room, corrutinas y DataStore en `proguard-rules.pro`.

## 9. Capturas de pruebas

Pruebas realizadas en el emulador:

| Caso | Descripción | Resultado esperado | Resultado obtenido | Captura |
| --- | --- | --- | --- | --- |
| 1 | Abrir la app por primera vez | Se muestra Login | OK | — |
| 2 | Login sin «Recordar sesión» | Navega a Home | [OK] | Captura 2 |
| 3 | Cerrar y abrir la app tras el login sin «Recordar sesión» | Vuelve a Login (no se recuerda la sesión) | OK | — |
| 4 | Login con «Recordar sesión» | Navega a Home | OK | — |
| 5 | Cerrar y abrir la app con «Recordar sesión» activo | Entra directamente a Home (auto-login) | [OK] | Captura 2 |
| 6 | Cerrar sesión | Vuelve a Login | OK | — |
| 7 | Cerrar y abrir la app tras cerrar sesión | Se muestra Login (sesión limpiada) | OK | — |
| 8 | Ir a Registro desde Login | Se muestra la pantalla de Registro | OK | — |
| 9 | Rellenar Registro con datos válidos | Navega a Home | OK | — |
| 10 | Añadir un videojuego | Aparece en la lista y se muestra la notificación | [OK] | Capturas 18 y 11 |
| 11 | Editar un videojuego | El cambio se refleja en la lista | OK | — |
| 12 | Borrar un videojuego | Diálogo de confirmación y desaparece de la lista | [OK] | Capturas 19 y 20 |
| 13 | Rotar la pantalla | El estado se mantiene | OK | — |
| 14 | Cerrar la app y volver a abrirla | Los videojuegos siguen ahí | OK | — |
| 15 | Rechazar el permiso de notificaciones | La app funciona sin notificación | [OK] | Capturas 9 y 10 |
| 16 | Probar con TalkBack | Todos los elementos se anuncian | OK | — |

Pruebas complementarias realizadas: edición de un videojuego sin notificación, rotación de pantalla (la lista se mantiene), cierre y reapertura de la app (los datos persisten), cierre de sesión (vuelve a Login y la sesión queda limpia) y prueba con TalkBack. En el dispositivo real se comprobó: instalación sin errores, login con «Recordar sesión», alta de videojuego con notificación, auto-login tras cerrar la app y cierre de sesión.

Caso 2:

*Captura 2. Home con la lista de videojuegos tras hacer login sin «Recordar sesión».*

![Captura 2: Home tras el login](Capturas/2.jpeg)

Caso 5:

*Captura 2. Home mostrado directamente al reabrir la app con «Recordar sesión» activo (auto-login).*

![Captura 2: Home tras el auto-login](Capturas/2.jpeg)

Caso 10:

*Captura 18. El videojuego añadido aparece en la lista.*

![Captura 18: Videojuego añadido a la lista](Capturas/18.jpeg)

*Captura 11. Notificación «mystic messenger se ha añadido a tu colección».*

![Captura 11: Notificación de videojuego añadido](Capturas/11.jpeg)

Caso 12:

*Captura 19. Videojuego selecionado para poder eliminarlo.*

![Captura 19: Eliminar videojuego](Capturas/19.jpeg)

*Captura 20. Lista tras confirmar el borrado (cyberpunk 2077 ya no aparece).*

![Captura 20: Lista tras borrar](Capturas/20.jpeg)

Caso 15:

*Captura 9. Diálogo del sistema que solicita el permiso de notificaciones.*

![Captura 9: Solicitud del permiso de notificaciones](Capturas/9.jpeg)

*Captura 10. Permiso de notificaciones sin conceder; la app sigue funcionando sin notificación.*

![Captura 10: Notificaciones sin permiso concedido](Capturas/10.jpeg)

Pruebas complementarias:

*Captura 21. Edición: «mystic messenger» con plataforma «móvil» y género «ril» antes de editarlo (el resultado se ve en la Captura 2).*

![Captura 21: Lista antes de editar](Capturas/21.jpeg)

*Captura 22. Rotación de pantalla: la lista se mantiene en horizontal.*

![Captura 22: Pantalla en horizontal](Capturas/22.jpeg)

## 10. Dificultades encontradas

Conservar únicamente las que realmente se hayan dado y completar con las propias.

| Dificultad | Causa | Solución aplicada |
| --- | --- | --- |
| Errores en rojo en `NavGraph.kt` durante los primeros pasos | El archivo usa clases (`AppDatabase`, repositorio, ViewModel, nuevo `HomeScreen`) que se crean en pasos posteriores | Continuar con los pasos y compilar al terminar el CRUD |
| Error de versión de KSP al sincronizar Gradle | KSP debe coincidir con la versión de Kotlin (y con AGP) | Actualizar KSP a una versión compatible (`2.3.6` o superior) |
| `Cannot create an instance of class VideojuegoViewModel` | No se había pasado la factory | Usar `viewModel(factory = factory)` |
| Al borrar un elemento se borraba otro | Faltaba `key` en `items` | Añadir `key = { it.id }` |
| La notificación no aparecía | Faltaba el canal o el permiso no estaba concedido | Llamar a `createChannel` en `MainActivity` y revisar el permiso |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` / «App not installed» | Firma distinta a la de la app ya instalada | Desinstalar la app antes de instalar el APK release |
| La app se cerraba en release al usar Room | ProGuard eliminaba clases | Añadir reglas de ProGuard para Room, corrutinas y DataStore |

## 11. Conclusiones

La actividad ha permitido pasar de una app con estado temporal a una con persistencia real y una arquitectura clara: DataStore para la sesión, Room para los datos, ViewModel para sobrevivir a los cambios de configuración y repositorio como capa intermedia. El uso de `Flow` simplifica la interfaz, ya que la lista se actualiza automáticamente sin necesidad de recargarla manualmente. También se ha comprobado la importancia de pedir permisos en tiempo de ejecución y de que la app siga funcionando si el usuario los rechaza, así como el proceso completo de firma y despliegue de un APK release en un dispositivo real.

## Anexo: Retos opcionales

Se han realizado los siete retos opcionales. Cada uno funciona en el emulador, se explica a continuación (qué se ha cambiado y por qué) e incluye su captura.

| Reto | Dificultad | Archivos modificados | Captura |
| --- | --- | --- | --- |
| 1. Botón «Borrar todo» | Fácil | `HomeScreen.kt`, `strings.xml` | Reto 1 y 1.2 |
| 2. Filtro por género | Medio | `VideojuegoDao.kt`, `VideojuegoRepository.kt`, `VideojuegoViewModel.kt`, `HomeScreen.kt` | Reto 2 y 2.2 |
| 3. Notificación al completar | Medio | `HomeScreen.kt`, `strings.xml` | Reto 3 |
| 4. Ordenar por nota o año | Difícil | `VideojuegoDao.kt`, `VideojuegoRepository.kt`, `VideojuegoViewModel.kt`, `HomeScreen.kt` | Reto 4 |
| 5. Persistir el filtro con DataStore | Difícil | `SessionDataStore.kt`, `VideojuegoRepository.kt`, `VideojuegoViewModel.kt`, `NavGraph.kt` | Reto 5 y 5.2 |
| 6. Exportar la lista a JSON | Muy difícil | `Videojuego.kt`, `VideojuegoRepository.kt`, `VideojuegoViewModel.kt`, `HomeScreen.kt`, `build.gradle.kts` | Reto 6 |
| 7. Tests unitarios del repositorio | Muy difícil | `app/src/test/java/...`, `build.gradle.kts` | Reto 7 |

### Reto 1: Botón «Borrar todo» en el menú overflow

Se ha añadido un `DropdownMenuItem` «Borrar todo» al menú de la `TopAppBar`. Al pulsarlo se activa un nuevo estado `showBorrarTodoDialog` que muestra un `AlertDialog` de confirmación, reutilizando el patrón del diálogo de borrado individual. Si el usuario acepta, se llama a `viewModel.borrarTodo()`, que ya existía y llega hasta `dao.deleteAll()`, por lo que no hizo falta tocar el repositorio ni el ViewModel. Room emite entonces una lista vacía por el `Flow` y la pantalla vuelve a mostrar el mensaje de lista vacía sin ningún código extra. Los textos del menú y del diálogo se han añadido a `strings.xml`. Se ha elegido pedir confirmación porque la acción es destructiva e irreversible.

Capturas del reto 1 (menú y diálogo de confirmación):

*Reto 1: opción «Borrar todo» en el menú overflow (emulador).*

![Reto 1: Borrar todo en el menú](Capturas/reto1.png)

*Reto 1: diálogo de confirmación «Borrar todos los videojuegos» (emulador).*

![Reto 1: Diálogo Borrar todos los videojuegos](Capturas/reto1.2.png)

### Reto 2: Filtro por género

Se ha añadido un `OutlinedTextField` en la parte superior de Home que filtra la lista mientras se escribe. En el DAO se creó la consulta `getByGenero(filtro)` con `WHERE genero LIKE '%' || :filtro || '%'`, que con el campo vacío devuelve todos los videojuegos. El repositorio guarda el texto en un `MutableStateFlow<String>` y usa `flatMapLatest` para cambiar de consulta cada vez que el filtro varía, cancelando la anterior. El ViewModel expone el filtro como `StateFlow` y una función para actualizarlo, y `HomeScreen` solo muestra el campo y notifica los cambios. Así la lógica de filtrado queda fuera de la UI y la lista sigue siendo reactiva.

Capturas del reto 2 (filtro por género):

*Reto 2: filtro «novela» y su resultado.*

![Reto 2: Filtro novela](Capturas/reto2.2.jpeg)

*Reto 2: filtro «aventura» sin coincidencias, con el mensaje de lista vacía.*

![Reto 2: Filtro aventura sin resultados](Capturas/reto2.jpeg)

### Reto 3: Notificación al marcar como completado

Al pulsar la casilla de un videojuego, `onToggleCompletado` calcula el nuevo valor, actualiza el videojuego con `viewModel.actualizar(...)` y, solo si el nuevo valor es `true`, muestra la notificación «¡\[título\] completado!». Si se desmarca no se muestra nada. Se reutiliza `NotificationHelper.mostrarNotificacion` y, antes de notificar, se comprueba que el permiso `POST_NOTIFICATIONS` está concedido (en Android 13 o superior), de modo que la app sigue funcionando si el usuario lo ha rechazado. Los nuevos textos se añadieron a `strings.xml`.

Captura del reto 3 (notificación al completar):

*Reto 3: notificación «¡mystic messenger completado!» al marcarlo como completado.*

![Reto 3: Notificación al completar](Capturas/reto3.jpeg)

### Reto 4: Ordenar la lista por nota o por año

Se ha creado una enumeración `Orden` con tres valores: título (por defecto, ascendente), nota (descendente) y año (descendente). El DAO tiene una consulta por cada criterio con su `ORDER BY`. El repositorio mantiene un `MutableStateFlow<Orden>` y, con `flatMapLatest`, elige la consulta adecuada en cada momento. El ViewModel expone el orden actual y un método para cambiarlo, y la `TopAppBar` incluye un `DropdownMenu` con las tres opciones. Como el orden vive en el ViewModel, se conserva al rotar la pantalla y mientras la app permanece abierta.

Captura del reto 4 (menú de ordenación):

*Reto 4: menú «Ordenar» con las opciones título, nota y año.*

![Reto 4: Menú Ordenar](Capturas/reto4.jpeg)

### Reto 5: Persistir el filtro con DataStore

Se ha ampliado `SessionDataStore` con una nueva clave `filtro_genero`, un `Flow<String>` para leerla y una función `suspend fun saveFiltro(filtro: String)`. Cuando el usuario cambia el filtro del Reto 2, el ViewModel lo guarda en DataStore dentro de `viewModelScope`, y al arrancar la pantalla se lee el valor guardado para inicializar el campo. De este modo, al cerrar y abrir la app el filtro sigue aplicado. Se ha usado DataStore y no Room porque es un único valor de preferencia de la interfaz, no un dato de la colección.

Capturas del reto 5 (filtro persistido):

*Reto 5: el filtro «aventura» sigue aplicado tras reabrir la app.*

![Reto 5: Filtro persistido](Capturas/reto5.jpeg)

*Reto 5: con el filtro vacío la lista vuelve a mostrar todos los videojuegos.*

![Reto 5: Filtro vacío](Capturas/reto5.2.jpeg)

### Reto 6: Exportar la lista a JSON

Se ha añadido la dependencia `kotlinx-serialization-json` y su plugin en `build.gradle.kts`, y se ha marcado la entidad `Videojuego` con `@Serializable`. Una nueva opción del menú overflow llama a `exportarJson` en el ViewModel, que obtiene la lista actual, la serializa con `prettyPrint` activado para que sea legible y la escribe en el archivo `videojuegos.json` dentro de `context.getExternalFilesDir(null)`. Este directorio es propio de la app, por lo que no necesita permisos de almacenamiento. La escritura se hace en una corrutina fuera del hilo principal y al terminar se avisa al usuario.

Captura del reto 6: archivo `videojuegos.json` exportado:

*Reto 6: mensaje con la ruta del archivo exportado.*

![Reto 6: Exportación a JSON](Capturas/reto6.jpeg)

### Reto 7: Tests unitarios del repositorio

Se han añadido las dependencias `room-testing` y `kotlinx-coroutines-test`. Los tests usan `Room.inMemoryDatabaseBuilder`, de modo que cada prueba parte de una base de datos vacía que no toca el disco, y un `FakeSessionDataStore` para no depender de DataStore. Las funciones `suspend` se prueban con `runTest`. Se cubren cuatro casos: insertar un videojuego y comprobar que se lee, actualizarlo y comprobar que el cambio persiste, borrarlo y comprobar que desaparece, y borrar todo y comprobar que la lista queda vacía.

Captura del reto 7 (tests en verde):

*Reto 7: los cuatro tests de `VideojuegoRepositoryTest` en verde.*

![Reto 7: Tests en verde](Capturas/reto7.png)


