package com.example.miprimerproyecto

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.miprimerproyecto.ui.theme.MiPrimerProyectoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MiApp", "onCreate ejecutado")
        enableEdgeToEdge()
        setContent {
            MiPrimerProyectoTheme {
                Scaffold( modifier = Modifier.fillMaxSize() ) { innerPadding ->
                    Greeting(
                        name = "Andrea",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    var contador by remember { mutableStateOf(0) }
    var fondoColor by remember { mutableStateOf(Color.White) }
    val context = LocalContext.current

    // Se ejecuta una sola vez, al entrar en composición (equivalente a "onCreate" para este composable)
    LaunchedEffect(Unit) {
        Toast.makeText(context, "¡Bienvenido, $name!", Toast.LENGTH_SHORT).show()
        Log.d("MiApp", "LaunchedEffect ejecutado al arrancar")
    }

    Column(
        modifier = modifier.background(fondoColor)
    ) {
        Text(
            text = "¡Hola desde mi primera app!",
            color = Color.Blue
        )
        Text(
            text = "Hola $name, bienvenido a Android"
        )

        Text(
            text = "Pulsado $contador veces",
            color = if (contador % 2 == 0) Color.Red else Color.Blue
        )

        Button(onClick = { contador++ }) {
            Text("Púlsame")
        }

        Button(onClick = { contador = 0 }) {
            Text("Reiniciar")
        }

//        Button(onClick = {
//            val resultado = 10 / contador
//            Log.d("MiApp", "Resultado: $resultado")
//        }) {
//            Text("Provocar crash")
//        }

        Button(onClick = {
            fondoColor = if (fondoColor == Color.White) Color(0xFFB3E5FC) else Color.White
        }) {
            Text("Cambiar fondo")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MiPrimerProyectoTheme {
        Greeting("Android")
    }
}

@Preview(showBackground = true, name = "Saludo personalizado")
@Composable
fun GreetingPreviewCustom(){
    MiPrimerProyectoTheme() {
        Greeting("Andrea")
    }
}

@Preview(showBackground = true, name = "Con padding y fondo")
@Composable
fun GreetingPreviewPadding() {
    MiPrimerProyectoTheme {
        Greeting(
            name = "Android",
            modifier = Modifier
                .padding(16.dp)
                .background(Color.LightGray)
        )
    }
}

@Preview(showBackground = true, name = "Column con separación")
@Composable
fun ColumnSpacingPreview() {
    MiPrimerProyectoTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Primera línea")
            Text("Segunda línea")
        }
    }
}