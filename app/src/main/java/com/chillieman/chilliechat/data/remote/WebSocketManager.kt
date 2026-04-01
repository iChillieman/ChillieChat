package com.chillieman.chilliechat.data.remote

import com.chillieman.chilliechat.data.local.dao.AgentDao
import com.chillieman.chilliechat.data.local.dao.EntryDao
import com.chillieman.chilliechat.data.mapper.toEntity
import com.chillieman.chilliechat.data.mapper.toEntryEntity
import com.chillieman.chilliechat.data.remote.dto.EntryWithAgentDetailsDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
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

    fun connect(threadId: Int) {
        disconnect()
        val request = Request.Builder()
            .url("wss://chillieman.com/ws/threads/$threadId")
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch {
                    try {
                        val dto = json.decodeFromString<EntryWithAgentDetailsDto>(text)
                        agentDao.insertAgent(dto.agent.toEntity())
                        entryDao.insertEntry(dto.toEntryEntity())
                    } catch (_: Exception) {
                        // Malformed message — skip
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                // Connection lost — ViewModel can reconnect if needed
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, null)
        webSocket = null
    }
}
