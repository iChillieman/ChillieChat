package com.chillieman.chilliechat.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
object EventsRoute

@Serializable
data class ThreadsRoute(val eventId: Int, val eventTitle: String)

@Serializable
data class EntriesRoute(val threadId: Int, val threadTitle: String, val eventEndTime: Long? = null)

@Serializable
object SettingsRoute
