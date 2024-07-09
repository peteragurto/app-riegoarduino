package com.peteragurto.riegoapp.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RiegoScreen(
    viewModel: RiegoViewModel
) {
    var editingIpAddress by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    // Estados
    val ipAddress by viewModel.ipAddress.collectAsState()
    val sensorValue by viewModel.sensorValue.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Derivar isRelayOn del sensorValue
    val relayState by viewModel.relayState.collectAsState()

    if (errorMessage != null) {
        Snackbar(
            action = {
                Button(onClick = { viewModel.clearErrorMessage() }) {
                    Text("OK")
                }
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text(errorMessage ?: "Error")
        }
    }

    Scaffold { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    "Control de riego - Palomino S.A.C.",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = if (isEditing) editingIpAddress else (ipAddress ?: ""),
                        onValueChange = { if (isEditing) editingIpAddress = it },
                        label = { Text("Dirección IP") },
                        enabled = isEditing
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = if (connectionState) Color.Green else Color.Red,
                                shape = CircleShape
                            )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(
                        onClick = {
                            if (isEditing) {
                                viewModel.saveIpAddress(editingIpAddress)
                                isEditing = false
                            } else {
                                editingIpAddress = ipAddress ?: ""
                                isEditing = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEditing) Color.Gray else Color.Magenta
                        )
                    ) {
                        Text(if (isEditing) "Guardar IP" else "Modificar IP")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isEditing) Icons.Outlined.Check else Icons.Outlined.Create,
                            contentDescription = null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("Valor del sensor de humedad: $sensorValue")
                Spacer(modifier = Modifier.height(20.dp))
                Row {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        onClick = { viewModel.updateRelayState(true) },
                        enabled = !relayState
                    ) {
                        Text("Apagar Riego")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                        onClick = { viewModel.updateRelayState(false) },
                        enabled = relayState
                    ) {
                        Text("Encender Riego")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RiegoScreenPreview() {

}