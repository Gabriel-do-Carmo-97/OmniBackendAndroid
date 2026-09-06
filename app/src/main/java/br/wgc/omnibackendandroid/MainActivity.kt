package br.wgc.omnibackendandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.wgc.omnibackend.firebase.OmniFirebase
import br.wgc.omnibackendandroid.ui.theme.OmniBackendAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicialização rápida da Facade OmniFirebase
        OmniFirebase.initialize(
            context = this,
            enableAppCheck = false,
            isDebug = true
        )

        setContent {
            OmniBackendAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OmniBackendHomeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

data class ProviderItem(
    val name: String,
    val module: String,
    val features: String
)

@Composable
fun OmniBackendHomeScreen(modifier: Modifier = Modifier) {
    val providers = listOf(
        ProviderItem("Google Firebase", ":backend-firebase", "Auth • Firestore • RTDB • Storage • Telemetry • AppCheck • Vertex AI"),
        ProviderItem("Supabase", ":backend-supabase", "GoTrue Auth • PostgREST • Storage • Realtime"),
        ProviderItem("Appwrite", ":backend-appwrite", "Account • Databases • Storage • Realtime WebSockets"),
        ProviderItem("Back4App", ":backend-back4app", "ParseUser • ParseObject • ParseFile • LiveQuery"),
        ProviderItem("PocketBase", ":backend-pocketbase", "RecordAuth • Collections • Files • SSE"),
        ProviderItem("Cloudflare", ":backend-cloudflare", "Workers Auth • D1 Database • R2 Storage"),
        ProviderItem("AWS Amplify", ":backend-amplify", "Cognito Auth • DynamoDB / AppSync • S3 Storage"),
        ProviderItem("Custom REST API", ":backend-rest", "JWT Auth • Rest Endpoints • RFC 7807")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "OmniBackend Android",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Framework Empresarial Multi-Provedor BaaS",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(providers) { provider ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = provider.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Módulo: ${provider.module}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                        Text(
                            text = provider.features,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
