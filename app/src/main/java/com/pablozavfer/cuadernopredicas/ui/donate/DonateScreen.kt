@file:OptIn(ExperimentalMaterial3Api::class)

package com.pablozavfer.cuadernopredicas.ui.donate

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.pablozavfer.cuadernopredicas.config.DonationConfig
import kotlinx.coroutines.launch

/** Texto acordado para el apartado de donaciones. */
private const val DONATION_TEXT = "Esta aplicación es gratuita y todas sus funciones están disponibles para todos. " +
    "Si deseas apoyar su desarrollo y mantenimiento, puedes realizar una aportación voluntaria. " +
    "Gracias por ayudar a mantener este proyecto."

/**
 * Apartado de aportación voluntaria al desarrollador.
 * Solo abre un enlace externo en el navegador cuando el usuario toca "Donar":
 * no hay ventanas emergentes, no se procesan pagos dentro de la app y donar no desbloquea nada.
 */
@Composable
fun DonateScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val url = DonationConfig.url

    Scaffold(
        topBar = { TopAppBar(title = { Text("Donar") }) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(56.dp),
            )
            Text("Apoya este proyecto", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text(
                DONATION_TEXT,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 560.dp),
            )

            if (url != null) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).addCategory(Intent.CATEGORY_BROWSABLE)
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            scope.launch { snackbar.showSnackbar("No se encontró un navegador para abrir el enlace.") }
                        }
                    },
                    modifier = Modifier
                        .widthIn(max = 400.dp)
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                ) {
                    Text("Donar", style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    "Se abrirá en tu navegador, fuera de la aplicación.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            } else {
                Text(
                    "Por ahora no hay un enlace de aportación disponible. ¡Gracias por tu interés!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.widthIn(max = 560.dp),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Fact("Tu aportación es totalmente voluntaria y va dirigida al desarrollador de la aplicación.")
                    Fact("Donar no desbloquea funciones, contenido, insignias ni ventajas: todo ya está disponible para ti.")
                    Fact(
                        "El pago se realiza fuera de la aplicación, en el sitio del proveedor. " +
                            "Esta app no procesa pagos ni solicita datos de tarjetas.",
                    )
                }
            }
        }
    }
}

@Composable
private fun Fact(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("•", color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodyMedium)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
