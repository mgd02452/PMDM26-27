# Memoria de la Actividad 2.2: Registro, Navegación con NavHost, Scaffold y Diálogos

PMDM · 2º DAM · Jetpack Compose

Alumno/a: \[Andrea Milena Vega Mora\] 

Repositorio Git: \[https://github.com/mgd02452/PMDM26-27.git\]

## 1. Introducción y objetivos

Esta actividad continúa la Actividad 2.1 (Login con Compose). Partiendo de una app con una única pantalla funcional (Login) y navegación por `Intent`, el objetivo es convertirla en una app con tres pantallas navegables (Login, Registro y Home) y con la estructura habitual de una aplicación real: barra superior, menú de opciones y diálogo informativo.

Objetivos específicos:

- Crear una pantalla de Registro con validaciones (email, contraseña, confirmación y aceptación de términos).
- Sustituir la navegación por `Intent` por `NavHost` + `NavController`.
- Envolver las pantallas en un `Scaffold` con `TopAppBar`.
- Añadir un menú overflow (⋮) con las opciones «Acerca de» y «Cerrar sesión».
- Implementar un `AlertDialog` «Acerca de».
- Pasar datos entre destinos (el email de Login/Registro a Home).

Competencias trabajadas: CE.a (estructura y estrategia de clases), CE.b (clases de interfaz gráfica), CE.g (pruebas de interacción en emulador) y CE.i (documentación del desarrollo).

## 2. Entorno y estructura del proyecto

Herramientas: Android Studio \[versión\], emulador AVD \[modelo y versión de Android\], Kotlin, Jetpack Compose con Material 3 y la dependencia `androidx.navigation:navigation-compose:2.8.4`.

Se mantiene el paquete plano `com.ejemplo.login`. Cambios respecto a la Actividad 2.1:

| Archivo | Acción | Motivo |
| --- | --- | --- |
| `NavGraph.kt` | Creado | Centraliza rutas y destinos. |
| `RegisterScreen.kt` | Creado | Nueva pantalla de registro. |
| `LoginScreen.kt` | Modificado | Nueva firma: recibe el email y navega a Registro. |
| `HomeScreen.kt` | Modificado | Añade `Scaffold`, `TopAppBar`, menú y diálogo. |
| `MainActivity.kt` | Modificado | Solo carga `AppNavGraph()`. |
| `HomeActivity.kt` | Eliminado | Home pasa a ser un destino del grafo. |
| `AndroidManifest.xml` | Modificado | Se elimina la entrada de `HomeActivity`. |
| `strings.xml` | Modificado | Nuevos textos (regla: ningún texto en el código). |

## 3. Desarrollo

### 3.1. Dependencias

Se añadió `navigation-compose` al catálogo de versiones (`libs.versions.toml`) y se referenció en `app/build.gradle.kts`. Tras sincronizar Gradle, los imports de `NavHost` y `rememberNavController` se resolvieron correctamente.

### 3.2. Pantalla de Registro

`RegisterScreen` tiene cuatro campos de estado (email, contraseña, confirmación y términos) y un estado de error por cada uno. Las validaciones son funciones locales: email con `Patterns.EMAIL_ADDRESS`, contraseña con la expresión regular `^(?=.*[A-Z])(?=.*[0-9]).{8,}$`, confirmación igual a la contraseña y checkbox marcado. La navegación entre campos usa `ImeAction.Next` y `FocusManager.moveFocus`, y el foco inicial se pide en un `LaunchedEffect`. El contenido tiene `verticalScroll` para que se vea completo al rotar la pantalla.

### 3.3. Grafo de navegación

`NavGraph.kt` define un objeto `Routes` con las rutas (`login`, `register`, `home/{email}`) y una función `home(email)` que codifica el email con `Uri.encode`. `AppNavGraph()` crea el `NavController` con `rememberNavController()` y declara los tres destinos. Puntos clave:

- Al llegar a Home desde Login o Registro se usa `popUpTo(Routes.LOGIN) { inclusive = true }`, equivalente al `finish()` de la versión con `Intent`.
- Al cerrar sesión se usa `popUpTo(0) { inclusive = true }`, que vacía toda la pila.
- El argumento `email` se declara con `navArgument` de tipo `StringType` y valor por defecto.

### 3.4. Home con Scaffold, menú y diálogo

`HomeScreen` usa `Scaffold` con `TopAppBar`. En `actions` hay un `IconButton` con `Icons.Filled.MoreVert` que abre un `DropdownMenu` controlado por `menuExpanded`. «Acerca de» activa `showAboutDialog`, que compone un `AlertDialog`; «Cerrar sesión» llama a `onLogout()`. El contenido aplica siempre `Modifier.padding(innerPadding)`.

### 3.5. MainActivity y Manifest

`MainActivity` queda reducida a `setContent { MaterialTheme { AppNavGraph() } }`. En el Manifest solo permanece `MainActivity`, con `windowSoftInputMode="adjustResize"`.

## 4. Wireframes y capturas de las pantallas

![Captura 1: pantalla de Login](Capturas/1.png)

![Captura 2: pantalla de Registro](Capturas/2.png)

![Captura 3: Home con el menú overflow abierto](Capturas/6.png)

La app recorre este flujo: Login ⇄ Registro; Login o Registro correctos → Home (con TopAppBar y menú ⋮) → AlertDialog «Acerca de» o vuelta a Login al cerrar sesión.

## 5. Decisiones de diseño

### 5.1. ¿Por qué NavHost en lugar de Intent?

Con `Intent`, cada pantalla es una `Activity`: más código repetido, más coste de memoria, paso de datos verboso (`putExtra` / `getStringExtra`) y un flujo de navegación disperso. Con `NavHost` hay una única Activity y cada pantalla es un `@Composable`. Todo el flujo vive en un solo archivo (`NavGraph.kt`), se lee y se modifica con facilidad, los argumentos se pasan en la ruta y el control de la pila (`popUpTo`, `popBackStack`) es explícito. Es además la forma recomendada por Google para Compose.

### 5.2. ¿Por qué Scaffold?

`Scaffold` aporta la estructura estándar de Material 3 (`topBar`, `bottomBar`, `floatingActionButton`, `snackbarHost` y `content`) y calcula el `innerPadding` para que el contenido no se solape con las barras. Así la barra superior y el menú quedan en el sitio que el usuario espera y la app resulta coherente y ampliable (por ejemplo, con una barra inferior o un botón flotante en futuras actividades).

### 5.3. Otras decisiones

- Las rutas se centralizan en `object Routes` para evitar errores tipográficos.
- El email se codifica con `Uri.encode`, porque caracteres como `@` o `+` pueden romper la ruta.
- Todos los textos van en `strings.xml` y se leen con `stringResource`.
- Se ofrece contenido accesible: `contentDescription` en la flecha atrás, el icono de visibilidad y el menú; los `IconButton` mantienen 48 dp; y los errores usan `MaterialTheme.colorScheme.error`.

## 6. Pruebas en el emulador

| Nº | Caso de prueba | Resultado esperado | Resultado |
| --- | --- | --- | --- |
| 1 | Abrir la app | Login con foco en email | \[OK / KO\] |
| 2 | «¿Ya tienes cuenta?» | Navega a Registro | \[OK / KO\] |
| 3 | Flecha atrás en Registro | Vuelve a Login | \[OK / KO\] |
| 4 | Email inválido en Registro | Error de email | \[OK / KO\] |
| 5 | Contraseñas distintas | «Las contraseñas no coinciden» | \[OK / KO\] |
| 6 | CREAR sin aceptar términos | «Debes aceptar los términos» | \[OK / KO\] |
| 7 | Registro correcto | Home con el email | \[OK / KO\] |
| 8 | Menú ⋮ en Home | Dos opciones | \[OK / KO\] |
| 9 | «Acerca de» | Se abre el AlertDialog | \[OK / KO\] |
| 10 | «Cerrar sesión» | Vuelve a Login sin poder retroceder | \[OK / KO\] |
| 11 | Atrás del sistema en Home | Sale de la app | \[OK / KO\] |
| 12 | Rotar en Registro | Contenido completo con scroll | \[OK / KO\] |
| 13 | TalkBack | Elementos anunciados | \[OK / KO\] |

Capturas obligatorias para el informe:

![Captura caso 2: Login → Registro](Capturas/4.png)

![Captura caso 5: error «Las contraseñas no coinciden»](Capturas/3.png)

![Captura caso 7: Home tras el registro correcto](Capturas/1.png)

![Captura caso 9: AlertDialog «Acerca de»](Capturas/7.png)

![Captura caso 10: vuelta a Login tras cerrar sesión](Capturas/8.png)

Casos límite probados: email con `+` (por ejemplo `alumno+test@ejemplo.com`), rotación en Home con el menú y retroceso desde Registro tras varias navegaciones. 

![Captura con + en el correo](Capturas/11.png)

![Captura con rotacion en el home](Capturas/10.png)

## 7. Dificultades encontradas y soluciones

Escribe aquí los errores que hayas tenido realmente. Estos son los más habituales en esta actividad; deja solo los que te hayan pasado y explícalos con tus palabras:

| Dificultad | Causa | Solución aplicada |
| --- | --- | --- |
| `Unresolved reference: NavHost` | Faltaba la dependencia o la sincronización de Gradle | Añadir `navigation-compose` y pulsar «Sync Now». |
| El contenido se solapaba con la TopAppBar | No se aplicaba `innerPadding` | `Modifier.padding(innerPadding)` en el contenido. |
| `Type mismatch` en el `@Preview` de Login | Cambió la firma de `onLoginSuccess` a `(String) -> Unit` | Usar `onLoginSuccess = { _ -> }`. |
| La app se cerraba al ir a Home con ciertos emails | Email sin codificar en la ruta | Usar `Uri.encode` en `Routes.home(email)`. |
| Al pulsar atrás en Home volvía a Login | Faltaba `popUpTo` | `popUpTo(Routes.LOGIN) { inclusive = true }`. |
| `HomeActivity not found` | Seguía declarada en el Manifest | Borrar la entrada y el archivo. |

## 8. Retos adicionales realizados (opcional)

Por cada reto completado, añade entre 5 y 10 líneas explicando qué has cambiado y por qué, y una captura del resultado.

### Reto 1: \[Diálogo de confirmación al cerrar sesión\]

Antes, la opción «Cerrar sesión» del menú overflow llamaba directamente a onLogout() y la sesión se cerraba sin avisar. He añadido un segundo estado booleano, showLogoutDialog, independiente de showAboutDialog, y ahora el ítem del menú solo cierra el menú y lo pone a true. Con ese estado a true se compone un segundo AlertDialog con título, texto y dos botones: «Aceptar» cierra el diálogo y llama a onLogout(), y «Cancelar» (o tocar fuera, vía onDismissRequest) solo lo cierra. Usar dos booleanos distintos permite que los diálogos «Acerca de» y «Cerrar sesión» no se solapen ni se abran a la vez; con un único estado compartido, abrir uno afectaría al otro. Los textos nuevos están en strings.xml y se reutilizan dialog_aceptar y dialog_cancelar. Así se evita cerrar la sesión por un toque accidental y se sigue la pauta de pedir confirmación en acciones destructivas.

[Insertar captura del diálogo de confirmación]

![Captura reto 1: diálogo de confirmación](Capturas/reto1.png)

### Reto 2: \[Icono «Acerca de» en la TopAppBar\]

He añadido en actions de la TopAppBar un IconButton con Icons.Filled.Info, colocado antes del botón ⋮ porque el orden dentro de actions es el orden visual de izquierda a derecha. Su onClick pone showAboutDialog = true, así que reutiliza el mismo estado y el mismo AlertDialog que el menú; no he duplicado el diálogo. El icono lleva un contentDescription («Acerca de») en strings.xml para que TalkBack lo anuncie. El menú overflow conserva sus dos opciones. Mi reflexión sobre duplicar la acción: aporta acceso rápido, pero «Acerca de» es una acción poco frecuente y Material recomienda reservar la barra superior para las más usadas, de modo que duplicarla añade ruido y ocupa espacio. En una app real lo dejaría solo en el menú; aquí lo he hecho porque lo pedía el reto.

[Insertar captura de la TopAppBar con el icono y el menú]

![Captura reto 2: TopAppBar con el icono y el menú](Capturas/reto2.png)

### Reto 3: \[Argumento adicional «nombre» en Home\]
 
He añadido un campo «Nombre» en Login y Registro y lo paso a Home como segundo argumento de la ruta. La ruta pasa a ser home/{email}/{nombre}, declaro dos navArgument de tipo StringType con valor por defecto, y Routes.home(email, nombre) codifica ambos valores con Uri.encode, porque el nombre puede llevar espacios o tildes. Los callbacks onLoginSuccess y onRegisterSuccess pasan de (String) a (String, String), y HomeScreen recibe un nuevo parámetro userName que muestra con el texto «¡Bienvenido, %1$s!». Tuve que actualizar también los @Preview, que si no daban error de tipos. Al probar con «Ana María» comprobé que el NavController decodifica el valor automáticamente al recuperarlo. Con este reto he practicado cambiar la firma de varias funciones de forma coherente, trabajando de dentro hacia fuera y compilando en cada paso.

[Insertar captura de Home con nombre y email]

![Captura reto 3: Home con nombre y email](Capturas/reto3.png)

### Reto 4: \[BottomAppBar con navegaciónr\]

He añadido una barra inferior con «Inicio» y «Perfil» mediante el parámetro bottomBar de Scaffold. Para no duplicar código la he extraído a un composable reutilizable, AppBottomBar, que recibe qué destino está seleccionado y dos callbacks; he usado NavigationBar porque marca el elemento activo, aunque BottomAppBar con IconButton también sería válido. He creado ProfileScreen.kt y la ruta profile/{email}, y desde Perfil el botón «Inicio» hace popBackStack() para volver a Home sin acumular copias en la pila. Cada pantalla tiene su propio Scaffold y su propia TopAppBar: así pueden tener títulos y acciones distintos, a costa de repetir un poco de estructura. La alternativa, un Scaffold único sobre el NavHost, es más elegante pero más compleja. Con este reto he aprendido a dar a una app varios puntos de entrada y a decidir cuándo una pantalla necesita su propio Scaffold.

[Insertar captura de Home y Perfil con la barra inferior]

![Captura reto 4: Home y Perfil con la barra inferior](Capturas/reto4.png)

### Reto 5: \[Validación en tiempo real con derivedStateOf\]
 
El botón «CREAR CUENTA» ahora está deshabilitado hasta que el formulario es válido. Como las funciones validarX() escriben mensajes de error y no sirven para calcular un booleano sin efectos secundarios, he creado versiones puras (emailValido, passwordValida). Con ellas he definido formularioValido mediante by remember { derivedStateOf { ... } }, que depende de email, contraseña, confirmación y términos, y lo paso a enabled del botón. Las validaciones siguen dentro del onClick como red de seguridad. Debajo del campo de contraseña muestro un contador «n/8 caracteres mínimos» en rojo (colorScheme.error) mientras no se llega a 8 y en verde después; el número se muestra siempre en texto para no depender solo del color. derivedStateOf evita recomponer lo que depende del resultado cuando el resultado no cambia, aunque cambie alguno de los estados de entrada.

[Insertar captura del botón deshabilitado y del contador]

![Captura reto 5:botón deshabilitado y del contador](Capturas/reto5.png)

### Reto 6: \[Animaciones de transición entre pantallas\]

He configurado las transiciones a nivel de NavHost, de modo que valen para todas las pantallas. Al avanzar, enterTransition hace que la pantalla nueva entre desde la derecha con slideInHorizontally y exitTransition desplaza la actual un tercio hacia la izquierda; al retroceder, popEnterTransition y popExitTransition invierten el movimiento. La duración, 300 ms, está en una constante (tween(DURACION)) para cambiarla en un solo sitio. El lambda { fullWidth -> fullWidth } devuelve el desplazamiento en píxeles: positivo es hacia la derecha y negativo hacia la izquierda. Distinguir enter/exit de popEnter/popExit es importante para que el movimiento tenga sentido: avanzar y retroceder deben sentirse como direcciones opuestas. Un caso especial es el cierre de sesión, que es un navigate y no un pop, por lo que Login entra por la derecha; para suavizarlo se puede definir una transición propia en ese destino, ya que los parámetros del composable sobrescriben los del NavHost. Con más de 300 ms la app se siente lenta.

[Insertar captura o enlace a un vídeo corto de la transición]

[Ver vídeo de la transicion](Capturas/reto6.mp4)

### Reto 7: \[Recordar el email con DataStore\]

He añadido una casilla «Recordar sesión» en Login. Creé UserPreferences.kt con un DataStore<Preferences> (una sola instancia por proceso, mediante la extensión preferencesDataStore) y tres operaciones: leer el email como Flow, guardarlo y borrarlo. En Login, un LaunchedEffect(Unit) lee el valor guardado con first() al abrir la pantalla; si existe, rellena el campo y marca la casilla. Al pulsar «Entrar» con credenciales válidas, lanzo una corrutina con rememberCoroutineScope() que guarda el email si la casilla está marcada y lo borra si no, porque DataStore es asíncrono y sus escrituras son funciones suspend. Solo se guarda el email, nunca la contraseña. Comprobé que tras cerrar la app por completo el email sigue relleno. Frente a SharedPreferences, DataStore es asíncrono, transaccional y expone los datos como Flow, lo que evita bloquear el hilo principal; SharedPreferences es más sencillo pero síncrono.

[Insertar captura de Login con el email recordado]

![Captura reto 7: Login con el email recordado](Capturas/reto7.png)

### Reto 8: \[ViewModel compartido para el email\]

En lugar de pasar el email por la ruta, he creado un UserViewModel con un MutableStateFlow privado y un StateFlow público de solo lectura, con los métodos login(email) y logout(). Se instancia una sola vez en AppNavGraph con viewModel() y se comparte entre los destinos: Login y Registro lo escriben antes de navegar y Home lo lee con collectAsState(). La ruta de Home pasa a ser simplemente home, con lo que desaparecen Uri.encode y los problemas con caracteres como @ o +. Al cerrar sesión se llama a logout() y se vacía el estado. Las pantallas no cambian de firma, siguen recibiendo valores y callbacks. Ventajas: un único origen de verdad, sobrevive a rotaciones y admite datos complejos. Desventajas: el estado es menos explícito (Home depende de que otra pantalla lo haya rellenado), es más difícil probar un destino aislado y se pierde si el proceso muere, salvo que se persista, por ejemplo con el Reto 7. No he usado Hilt para no añadir configuración innecesaria.

[Insertar captura de Home mostrando el email desde el ViewModel]

![Captura reto 8: Home mostrando el email desde el ViewModel](Capturas/reto8.png)

## 9. Conclusiones

La actividad permite pasar de una app con una pantalla a una estructura de navegación real. Lo más valioso ha sido entender que en Compose la navegación se gestiona en un único lugar, que el estado (menú, diálogo, errores) se controla con variables observables y que el back stack hay que diseñarlo (`popUpTo`) para que el comportamiento del botón atrás sea el esperado. \[Añade una valoración personal: qué te ha costado más y qué mejorarías.\]

## 10. Autoevaluación

- [ ] El proyecto compila y arranca en el emulador.
- [ ] Navegación Login ⇄ Registro y a Home con el email.
- [ ] Home con TopAppBar, menú overflow y AlertDialog.
- [ ] «Cerrar sesión» vuelve a Login sin poder retroceder.
- [ ] Validaciones de Registro funcionando.
- [ ] Textos en `strings.xml`.
- [ ] Probado en vertical, horizontal y con TalkBack.
- [ ] `HomeActivity` y su entrada del Manifest eliminadas.
- [ ] Capturas e informe completos.

## 11. Webgrafía

- Navigation in Compose: https://developer.android.com/develop/ui/compose/navigation
- Scaffold, TopAppBar, DropdownMenu y AlertDialog (Material 3): https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary
- Guía de la actividad: https://actionperformed.github.io/guias\_html/PMDM/PMDM%20-%20ACT%202.2.html
