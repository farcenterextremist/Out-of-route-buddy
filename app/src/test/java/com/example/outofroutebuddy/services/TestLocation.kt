package com.example.outofroutebuddy.services

data class TestLocation(
    val latitude: Double,
    val longitude: Double,
    val speed: Float = 0f, // meters/second
    val accuracy: Float = 5f, // meters
    val hasSpeed: Boolean = true,
) 
