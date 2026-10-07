package br.wgc.omnibackend.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Representa os possíveis estados de conectividade de rede do dispositivo.
 */
sealed interface NetworkStatus {
    /** A rede está ativa e validada para tráfego com a internet. */
    data object Available : NetworkStatus

    /** A conexão de rede foi perdida ou desconectada. */
    data object Lost : NetworkStatus

    /** Nenhuma rede adequada para acesso à internet está disponível. */
    data object Unavailable : NetworkStatus
}

/**
 * Utilitário reativo para monitorar o status de conectividade à internet em tempo real.
 *
 * Utiliza o [ConnectivityManager] do sistema Android registrando um [ConnectivityManager.NetworkCallback]
 * e emitindo as alterações de forma reativa via [Flow].
 *
 * Permite que orquestradores como o `:bundle:hybrid` e o `OfflineFirstRepository`
 * ativem failover preemptivo e enfileiramento offline instantâneo ao detectar queda de sinal.
 *
 * @param context Contexto da aplicação Android.
 */
class NetworkConnectivityMonitor(private val context: Context) {
    private val connectivityManager: ConnectivityManager? by lazy {
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    }

    /** Emite o status detalhado de conectividade ([NetworkStatus.Available], [NetworkStatus.Lost], [NetworkStatus.Unavailable]). */
    val status: Flow<NetworkStatus> = callbackFlow {
        val cm = connectivityManager
        if (cm == null) {
            trySend(NetworkStatus.Unavailable)
            close()
            return@callbackFlow
        }

        val activeNetwork = cm.activeNetwork
        val initialCapabilities = cm.getNetworkCapabilities(activeNetwork)
        val initialConnected = initialCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
            initialCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        trySend(if (initialConnected) NetworkStatus.Available else NetworkStatus.Unavailable)

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(NetworkStatus.Available)
            }

            override fun onLost(network: Network) {
                trySend(NetworkStatus.Lost)
            }

            override fun onUnavailable() {
                trySend(NetworkStatus.Unavailable)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm.registerNetworkCallback(request, callback)

        awaitClose {
            runCatching { cm.unregisterNetworkCallback(callback) }
        }
    }.distinctUntilChanged()

    /** Emite `true` se houver conexão com a internet disponível e validada, ou `false` caso contrário. */
    val isConnected: Flow<Boolean> = callbackFlow {
        val cm = connectivityManager
        if (cm == null) {
            trySend(false)
            close()
            return@callbackFlow
        }

        // Emite estado inicial
        val activeNetwork = cm.activeNetwork
        val initialCapabilities = cm.getNetworkCapabilities(activeNetwork)
        val initialConnected = initialCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
            initialCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        trySend(initialConnected)

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }

            override fun onUnavailable() {
                trySend(false)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm.registerNetworkCallback(request, callback)

        awaitClose {
            runCatching { cm.unregisterNetworkCallback(callback) }
        }
    }.distinctUntilChanged()
}
