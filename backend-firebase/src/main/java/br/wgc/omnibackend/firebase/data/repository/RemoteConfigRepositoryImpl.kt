package br.wgc.omnibackend.firebase.data.repository

import br.wgc.omnibackend.firebase.domain.repository.RemoteConfigRepository
import br.wgc.omnibackend.firebase.utils.AppError
import br.wgc.omnibackend.firebase.utils.DataResult
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class RemoteConfigRepositoryImpl @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) : RemoteConfigRepository {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        val configSettings = remoteConfigSettings {
            // Em modo de debug, um intervalo baixo é útil para testes.
            // Em produção, o valor padrão (12 horas) ou um valor alto (ex: 3600s = 1 hora)
            // é recomendado para evitar throttling (limitação de requisições).
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    override suspend fun fetchAndActivate(): DataResult<Boolean> {
        return runCatching {
            val success = remoteConfig.fetchAndActivate().await()
            DataResult.Success(success)
        }.getOrElse { exception ->
            DataResult.Failure(
                error = getRemoteConfigError(exception)
            )
        }
    }

    override fun observeKeyUpdates(key: String): Flow<DataResult<Unit>> = callbackFlow {
        val listener = object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                if (configUpdate.updatedKeys.contains(key)) {
                    repositoryScope.launch {
                        runCatching {
                            remoteConfig.activate().await()
                            trySend(DataResult.Success(Unit))
                        }.getOrElse {
                            trySend(DataResult.Failure(AppError.RemoteConfig.Unknown))
                        }
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                trySend(DataResult.Failure(getRemoteConfigError(error)))
            }
        }
        val registration = remoteConfig.addOnConfigUpdateListener(listener)
        awaitClose { registration.remove() }
    }

    override fun detectedUpdateFlagInLive(
        key: String,
        updateFlagResult: suspend () -> Unit,
        errorResult: suspend (AppError) -> Unit
    ) {
        val configUpdateListener = object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                if (configUpdate.updatedKeys.contains(key)) {
                    repositoryScope.launch {
                        val activated = remoteConfig.activate().await()
                        if (activated) updateFlagResult()
                        else errorResult(AppError.RemoteConfig.Unknown)
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                repositoryScope.launch {
                    errorResult(getRemoteConfigError(error))
                }
            }
        }
        remoteConfig.addOnConfigUpdateListener(configUpdateListener)
    }

    override fun getString(key: String): DataResult<String> {
        return runCatching {
            val value = remoteConfig.getString(key)
            DataResult.Success(value)
        }.getOrElse { exception ->
            DataResult.Failure(AppError.Generic.Unknown(exception))
        }
    }

    override fun getBoolean(key: String): DataResult<Boolean> {
        return runCatching {
            val value = remoteConfig.getBoolean(key)
            DataResult.Success(value)
        }.getOrElse { exception ->
            DataResult.Failure(AppError.Generic.Unknown(exception))
        }
    }

    override fun getLong(key: String): DataResult<Long> {
        return runCatching {
            val value = remoteConfig.getLong(key)
            DataResult.Success(value)
        }.getOrElse { exception ->
            DataResult.Failure(AppError.Generic.Unknown(exception))
        }
    }

    override fun getDouble(key: String): DataResult<Double> {
        return runCatching {
            val value = remoteConfig.getDouble(key)
            DataResult.Success(value)
        }.getOrElse { exception ->
            DataResult.Failure(AppError.Generic.Unknown(exception))
        }
    }

    private fun getRemoteConfigError(
        exception: Throwable
    ): AppError = if (exception is FirebaseRemoteConfigException) {
        when (exception.code) {
            FirebaseRemoteConfigException.Code.CONFIG_UPDATE_STREAM_ERROR -> AppError.RemoteConfig.StreamError
            FirebaseRemoteConfigException.Code.CONFIG_UPDATE_MESSAGE_INVALID -> AppError.RemoteConfig.MessageInvalid
            FirebaseRemoteConfigException.Code.CONFIG_UPDATE_NOT_FETCHED -> AppError.RemoteConfig.FetchFailure
            FirebaseRemoteConfigException.Code.CONFIG_UPDATE_UNAVAILABLE -> AppError.RemoteConfig.UpdateUnavailable
            else -> AppError.RemoteConfig.Unknown
        }
    } else {
        AppError.Generic.Unknown(exception)
    }
}
