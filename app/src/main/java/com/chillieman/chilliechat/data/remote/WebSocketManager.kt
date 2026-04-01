package com.chillieman.chilliechat.data.remote

import com.chillieman.chilliechat.data.local.dao.AgentDao
import com.chillieman.chilliechat.data.local.dao.EntryDao
import com.chillieman.chilliechat.data.mapper.toEntity
import com.chillieman.chilliechat.data.mapper.toEntryEntity
import com.chillieman.chilliechat.data.remote.dto.EntryDeletedEventDto
import com.chillieman.chilliechat.data.remote.dto.EntryWithAgentDetailsDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject

class WebSocketManager @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json,
    private val entryDao: EntryDao,
    private val agentDao: AgentDao
) {
    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentThreadId: Int? = null
    private var reconnectAttempt = 0

    fun connect(threadId: Int) {
        disconnect()
        currentThreadId = threadId
        reconnectAttempt = 0
        openSocket(threadId)
    }

    private fun openSocket(threadId: Int) {
        val request = Request.Builder()
            .url("wss://chillieman.com/ws/threads/$threadId")
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                reconnectAttempt = 0
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch {
                    try {
                        val jsonObj = json.decodeFromString<JsonObject>(text)
                        val type = jsonObj["type"]?.jsonPrimitive?.content

                        if (type == "ENTRY_DELETED") {
                            val event = json.decodeFromString<EntryDeletedEventDto>(text)
                            entryDao.markEntryDeleted(event.entryId)
                        } else {
                            val dto = json.decodeFromString<EntryWithAgentDetailsDto>(text)
                            agentDao.insertAgent(dto.agent.toEntity())
                            entryDao.insertEntry(dto.toEntryEntity())
                        }
                    } catch (_: Exception) {
                        // Malformed message — skip
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                scheduleReconnect()
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                if (code != 1000) scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        val threadId = currentThreadId ?: return
        reconnectAttempt++
        val delayMs = (1000L * (1L shl (reconnectAttempt - 1).coerceAtMost(5)))
            .coerceAtMost(30_000L)
        scope.launch {
            delay(delayMs)
            if (currentThreadId == threadId) {
                openSocket(threadId)
            }
        }
    }

    fun disconnect() {
        currentThreadId = null
        webSocket?.close(1000, null)
        webSocket = null
    }
}
