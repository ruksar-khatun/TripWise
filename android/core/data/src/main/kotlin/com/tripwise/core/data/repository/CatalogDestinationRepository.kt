package com.tripwise.core.data.repository

import android.content.Context
import android.util.Log
import com.tripwise.core.common.coroutines.Dispatcher
import com.tripwise.core.common.coroutines.TripWiseDispatcher
import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.AppErrorException
import com.tripwise.core.common.result.Outcome
import com.tripwise.core.data.mapper.toModel
import com.tripwise.core.domain.repository.DestinationRepository
import com.tripwise.core.model.Destination
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.network.datasource.CatalogNetworkDataSource
import com.tripwise.core.network.dto.DestinationDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Curated catalog from the backend. The catalog is small, so it's fetched whole and searched on-device:
 *  - every subscriber gets the on-disk copy (if any) immediately; one background refresh per session updates it
 *  - offline with a cached copy: keep showing it
 *  - no cached copy yet (first launch, or no server reachable): the catalog bundled in the APK
 *    (assets/catalog/destinations.json, a copy of backend/catalog/destinations.json), so the app works standalone
 */
@Singleton
internal class CatalogDestinationRepository @Inject constructor(
    private val network: CatalogNetworkDataSource,
    @ApplicationContext private val context: Context,
    @Dispatcher(TripWiseDispatcher.IO) private val io: CoroutineDispatcher,
    @com.tripwise.core.common.coroutines.ApplicationScope private val scope: kotlinx.coroutines.CoroutineScope,
) : DestinationRepository {

    private sealed interface State {
        data object Idle : State
        data object Loading : State
        data class Ready(val items: List<Destination>) : State
        data class Failed(val error: AppError) : State
    }

    private val state = MutableStateFlow<State>(State.Idle)
    private val loadMutex = Mutex()
    private val cacheFile = File(context.filesDir, "catalog/destinations.json")
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(DestinationDto.serializer())
    @Volatile private var refreshedThisSession = false
    private var refreshJob: kotlinx.coroutines.Job? = null

    /** Emits the local copy (cache or bundled) right away; the network refresh never blocks it. */
    private fun catalog(): Flow<List<Destination>> = flow {
        ensureLocal()
        refreshInBackground()
        emitAll(
            state.filter { it is State.Ready || it is State.Failed }.map {
                when (it) {
                    is State.Ready -> it.items
                    is State.Failed -> throw AppErrorException(it.error)
                    else -> error("unreachable")
                }
            },
        )
    }

    private suspend fun ensureLocal() = loadMutex.withLock {
        if (state.value is State.Ready) return@withLock
        (readCache() ?: readBundled())?.let { state.value = State.Ready(it) }
    }

    /** At most one refresh at a time and one success per app session; screens never wait for it. */
    private fun refreshInBackground() {
        synchronized(this) { startRefreshLocked() }
    }

    private fun startRefreshLocked() {
        if (refreshedThisSession || refreshJob?.isActive == true) return
        if (state.value !is State.Ready) state.value = State.Loading // e.g. retry after a failure
        refreshJob = scope.launch {
            when (val result = network.fetchAll()) {
                is Outcome.Success -> {
                    refreshedThisSession = true
                    state.value = State.Ready(result.value.map { it.toModel() })
                    writeCache(result.value)
                }
                is Outcome.Failure -> if (state.value !is State.Ready) state.value = State.Failed(result.error)
            }
        }
    }

    private suspend fun readCache(): List<Destination>? = withContext(io) {
        runCatching { cacheFile.takeIf { it.exists() }?.readText()?.let { json.decodeFromString(serializer, it) } }
            .onFailure { Log.w(TAG, "Catalog cache unreadable; ignoring", it) }
            .getOrNull()?.map { it.toModel() }
    }

    @kotlinx.serialization.Serializable private data class BundledCatalog(val destinations: List<DestinationDto>)

    /** The curated catalog shipped inside the APK, in the API's order (popularity, then id). */
    private suspend fun readBundled(): List<Destination>? = withContext(io) {
        runCatching {
            context.assets.open(BUNDLED_ASSET).bufferedReader().use { json.decodeFromString(BundledCatalog.serializer(), it.readText()) }
                .destinations.sortedWith(compareByDescending<DestinationDto> { it.popularity }.thenBy { it.id })
        }.onFailure { Log.w(TAG, "Bundled catalog unreadable", it) }.getOrNull()?.map { it.toModel() }
    }

    private suspend fun writeCache(items: List<DestinationDto>) = withContext(io) {
        runCatching {
            cacheFile.parentFile?.mkdirs()
            val tmp = File(cacheFile.path + ".tmp")
            tmp.writeText(json.encodeToString(serializer, items))
            tmp.renameTo(cacheFile)
        }.onFailure { Log.w(TAG, "Couldn't write catalog cache", it) }
    }

    override fun observeDestinations(query: String, interest: TravelInterest?): Flow<List<Destination>> {
        val q = query.trim()
        return catalog().map { all ->
            all.filter { d ->
                (q.isEmpty() || d.name.contains(q, ignoreCase = true) || d.country.contains(q, ignoreCase = true)) &&
                    (interest == null || interest in d.interests)
            }
        }
    }

    override fun observePopular(limit: Int): Flow<List<Destination>> =
        catalog().map { all -> all.sortedByDescending { it.ratingCount ?: 0 }.take(limit) }

    override fun observeFeatured(): Flow<List<Destination>> = catalog().map { all -> all.filter { it.featured } }

    override fun observeDestination(id: String): Flow<Destination?> = catalog().map { all -> all.firstOrNull { it.id == id } }

    private companion object {
        const val BUNDLED_ASSET = "catalog/destinations.json"
        const val TAG = "Catalog"
    }
}
