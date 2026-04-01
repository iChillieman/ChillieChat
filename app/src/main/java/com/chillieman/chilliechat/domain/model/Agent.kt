package com.chillieman.chilliechat.domain.model

data class Agent(
    val id: Int,
    val name: String,
    val type: String,
    val capabilities: String? = null
)
