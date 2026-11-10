package org.towerporchfest.shared

import app.cash.sqldelight.db.SqlDriver
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CoroutineDispatcher
import org.towerporchfest.shared.data.EventRepository
import org.towerporchfest.shared.data.SyncEngine
import org.towerporchfest.shared.data.local.PorchFestDatabase
import org.towerporchfest.shared.data.remote.PorchFestApi
import org.towerporchfest.shared.data.remote.createHttpClient

/** A fresh in-memory database with the schema created. */
internal expect fun createTestDriver(): SqlDriver

/**
 * A fake backend serving [body] with [etag]. It honours If-None-Match like the real endpoint,
 * and records what each request sent. Change [body], [etag] or [failing] between syncs.
 */
internal class FakeServer(var body: String = Fixtures.SNAPSHOT, var etag: String = "\"v1\"") {
    var failing = false
    val sentEtags = mutableListOf<String?>()

    val engine = MockEngine { request ->
        val sent = request.headers[HttpHeaders.IfNoneMatch]
        sentEtags += sent
        when {
            failing -> respond("boom", HttpStatusCode.InternalServerError)
            sent == etag -> respond("", HttpStatusCode.NotModified, headersOf(HttpHeaders.ETag, etag))
            else -> respond(
                body,
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType to listOf("application/json"), HttpHeaders.ETag to listOf(etag)),
            )
        }
    }
}

/** The real data layer wired to a fake server and an in-memory database. */
internal class TestGraph(dispatcher: CoroutineDispatcher, val server: FakeServer = FakeServer()) {
    var now = 1_000L
    val database = PorchFestDatabase(createTestDriver())
    val repository = EventRepository(database, dispatcher)
    val syncEngine = SyncEngine(
        api = PorchFestApi(createHttpClient(server.engine), "http://test"),
        database = database,
        dispatcher = dispatcher,
        nowEpochMillis = { now },
    )
}
