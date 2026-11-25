package com.example.conectividad

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectivityApp() {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Red", "Bluetooth", "SMS")

    Scaffold(
        topBar = { TopAppBar(title = { Text("Conectividad") }) }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTab) {
                0 -> NetworkScreen()      // ← antes: Text("Fase 1: Red (pendiente)")
                1 -> BluetoothScreen()   // ← antes: Text("Fase 2: Bluetooth (pendiente)")
                2 -> SmsScreen()        // ← antes: Text("Fase 3: SMS (pendiente)")
            }
        }
    }
}