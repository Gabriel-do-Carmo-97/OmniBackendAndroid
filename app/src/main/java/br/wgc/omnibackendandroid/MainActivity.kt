package br.wgc.omnibackendandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.wgc.omnibackend.core.model.OmniUser
import br.wgc.omnibackend.core.repository.AuthRepository
import br.wgc.omnibackend.core.repository.FirestoreRepository
import br.wgc.omnibackend.core.repository.StorageRepository
import br.wgc.omnibackend.core.utils.DataResult
import br.wgc.omnibackend.testing.FakeAuthRepository
import br.wgc.omnibackend.testing.FakeFirestoreRepository
import br.wgc.omnibackend.testing.FakeStorageRepository
import br.wgc.omnibackendandroid.ui.theme.OmniBackendAndroidTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val fakeAuth: AuthRepository = FakeAuthRepository()
    private val fakeDb: FirestoreRepository = FakeFirestoreRepository()
    private val fakeStorage: StorageRepository = FakeStorageRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OmniBackendAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OmniBackendApp(
                        authRepo = fakeAuth,
                        dbRepo = fakeDb,
                        storageRepo = fakeStorage,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
fun OmniBackendApp(authRepo: AuthRepository, dbRepo: FirestoreRepository, storageRepo: StorageRepository, modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Showcase Interativo", "11 Bundles Empresariais")

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                )
            }
        }

        when (selectedTab) {
            0 -> InteractivePlayground(authRepo, dbRepo, storageRepo)
            1 -> BundlesCatalog()
        }
    }
}

@Composable
fun InteractivePlayground(authRepo: AuthRepository, dbRepo: FirestoreRepository, storageRepo: StorageRepository) {
    val scope = rememberCoroutineScope()
    var currentUser by remember { mutableStateOf<OmniUser?>(null) }
    var statusMessage by remember { mutableStateOf("Pronto para testar contratos.") }
    var noteTitle by remember { mutableStateOf("") }
    val savedNotes = remember { mutableStateListOf<String>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "⚡ Playground de Validação de Contratos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Demonstração das interfaces desacopladas do :core utilizando os Fakes do módulo :testing em tempo real.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔐 Autenticação (AuthRepository)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sessão Atual: ${currentUser?.email ?: "Desconectado"}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            scope.launch {
                                when (val res = authRepo.createUser("demo@wgc.com.br", "123456")) {
                                    is DataResult.Success -> {
                                        currentUser = res.data
                                        statusMessage = "Usuário demo criado com sucesso!"
                                    }
                                    is DataResult.Failure -> {
                                        when (val loginRes = authRepo.login("demo@wgc.com.br", "123456")) {
                                            is DataResult.Success -> {
                                                currentUser = loginRes.data
                                                statusMessage = "Login efetuado com sucesso!"
                                            }
                                            is DataResult.Failure -> {
                                                statusMessage = "Erro no login."
                                            }
                                        }
                                    }
                                }
                            }
                        }) {
                            Text("Login / Cadastro")
                        }

                        OutlinedButton(onClick = {
                            scope.launch {
                                authRepo.signOut()
                                currentUser = null
                                statusMessage = "Sessão encerrada com sucesso."
                            }
                        }) {
                            Text("Sign Out")
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📄 Banco de Dados (FirestoreRepository)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Título do Documento") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        if (noteTitle.isNotBlank()) {
                            val title = noteTitle
                            scope.launch {
                                val res = dbRepo.addDocument("notes", mapOf("title" to title))
                                if (res is DataResult.Success) {
                                    savedNotes.add(title)
                                    noteTitle = ""
                                    statusMessage = "Documento salvo com ID: ${res.data}"
                                }
                            }
                        }
                    }) {
                        Text("Persistir Documento")
                    }

                    if (savedNotes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Documentos salvos em memória:")
                        savedNotes.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📦 Armazenamento de Arquivos (StorageRepository)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        scope.launch {
                            val bytes = "Relatório Empresarial OmniBackend".toByteArray()
                            when (val res = storageRepo.uploadFileDirect("reports/doc1.txt", bytes)) {
                                is DataResult.Success -> {
                                    statusMessage = "Arquivo publicado! URI: ${res.data}"
                                }
                                is DataResult.Failure -> {
                                    statusMessage = "Falha no upload."
                                }
                            }
                        }
                    }) {
                        Text("Upload Simulado Direto")
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Status da Última Operação:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

data class BundleInfo(val name: String, val module: String, val description: String)

@Composable
fun BundlesCatalog() {
    val bundles = listOf(
        BundleInfo("Suíte All", ":bundle:all", "Todos os 8 provedores de backend do ecossistema."),
        BundleInfo("Híbrido de Alta Disponibilidade", ":bundle:hybrid", "Firebase + Supabase + Appwrite com failover em tempo real."),
        BundleInfo("Firebase + Supabase", ":bundle:firebase-supabase", "Google Cloud + PostgreSQL relacional Supabase com failover."),
        BundleInfo("Firebase + AWS Amplify", ":bundle:firebase-amplify", "Multi-Cloud direta entre GCP e AWS."),
        BundleInfo("Firebase + Back4App", ":bundle:firebase-back4app", "Contingência direta com infraestrutura Parse Platform."),
        BundleInfo("Supabase + Cloudflare", ":bundle:supabase-cloudflare", "PostgreSQL + Computação Edge Workers/D1/R2."),
        BundleInfo("Cloud Native", ":bundle:cloud-native", "Nuvens globais elásticas: Firebase, Supabase, Amplify, Cloudflare."),
        BundleInfo("Self-Hosted / LGPD", ":bundle:self-hosted", "Supabase, Appwrite, PocketBase e Back4App para redes privadas."),
        BundleInfo(
            "Enterprise Hybrid",
            ":bundle:enterprise-hybrid",
            "AWS Amplify, Cloudflare, Custom REST e Firebase para sistemas legados.",
        ),
        BundleInfo("Edge Serverless", ":bundle:edge-serverless", "Cloudflare, PocketBase e REST para ultra-baixa latência."),
        BundleInfo("BaaS Classic", ":bundle:baas-classic", "Firebase, Appwrite e Back4App para MVPs ultra-rápidos."),
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                text = "🏛️ Catálogo de 11 Bundles Corporativos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Selecione o bundle que melhor se adapta à infraestrutura da sua empresa.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        items(bundles) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Módulo: ${item.module}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(text = item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}
