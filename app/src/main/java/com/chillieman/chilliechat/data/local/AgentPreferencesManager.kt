package com.chillieman.chilliechat.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "agent_preferences")

data class AgentPreferences(
    val agentId: Int? = null,
    val agentName: String? = null,
    val agentSecret: String? = null,
    val agentType: String? = null,
    val alwaysShowReportedMessages: Boolean = false
)

@Singleton
class AgentPreferencesManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    val agentPreferences: Flow<AgentPreferences> = dataStore.data.map { prefs ->
        AgentPreferences(
            agentId = prefs[KEY_AGENT_ID],
            agentName = prefs[KEY_AGENT_NAME],
            agentSecret = prefs[KEY_AGENT_SECRET],
            agentType = prefs[KEY_AGENT_TYPE],
            alwaysShowReportedMessages = prefs[KEY_ALWAYS_SHOW_REPORTED] ?: false
        )
    }

    suspend fun saveAgent(id: Int, name: String, type: String, secret: String? = null) {
        dataStore.edit { prefs ->
            prefs[KEY_AGENT_ID] = id
            prefs[KEY_AGENT_NAME] = name
            prefs[KEY_AGENT_TYPE] = type
            if (secret != null) {
                prefs[KEY_AGENT_SECRET] = secret
            } else {
                prefs.remove(KEY_AGENT_SECRET)
            }
        }
    }

    suspend fun clearAgent() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_AGENT_ID)
            prefs.remove(KEY_AGENT_NAME)
            prefs.remove(KEY_AGENT_SECRET)
            prefs.remove(KEY_AGENT_TYPE)
        }
    }

    suspend fun setAlwaysShowReportedMessages(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_ALWAYS_SHOW_REPORTED] = enabled
        }
    }

    companion object {
        private val KEY_AGENT_ID = intPreferencesKey("agent_id")
        private val KEY_AGENT_NAME = stringPreferencesKey("agent_name")
        private val KEY_AGENT_SECRET = stringPreferencesKey("agent_secret")
        private val KEY_AGENT_TYPE = stringPreferencesKey("agent_type")
        private val KEY_ALWAYS_SHOW_REPORTED = booleanPreferencesKey("always_show_reported_messages")
    }
}
