package org.towerporchfest.shared.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal sealed interface SnapshotResponse {
    /** The server's data matches the ETag we sent. */
    data object NotModified : SnapshotResponse
    data class Fresh(val snapshot: SnapshotDto, val etag: String?) : SnapshotResponse
}

internal class ApiException(message: String) : Exception(message)

internal fun createHttpClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
    expectSuccess = false
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(HttpTimeout) {
        connectTimeoutMillis = 10_000
        requestTimeoutMillis = 30_000
    }
}

internal class PorchFestApi(private val client: HttpClient, baseUrl: String) {
    private val snapshotUrl = baseUrl.trimEnd('/') + "/api/mobile/v1/snapshot"

    /**
     * Fetches the full festival snapshot. Pass the ETag from the last successful fetch to get
     * [SnapshotResponse.NotModified] instead of the body when nothing has changed. The ETag is
     * handled here by hand, not by an HTTP cache, because the database is the cache.
     */
    suspend fun fetchSnapshot(etag: String?): SnapshotResponse {
        val response = client.get(snapshotUrl) {
            header(HttpHeaders.Accept, "application/json")
            if (etag != null) header(HttpHeaders.IfNoneMatch, etag)
        }
        return when (response.status) {
            HttpStatusCode.NotModified -> SnapshotResponse.NotModified
            HttpStatusCode.OK -> SnapshotResponse.Fresh(response.body(), response.headers[HttpHeaders.ETag])
            else -> throw ApiException("Server returned ${response.status.value}")
        }
    }
}
