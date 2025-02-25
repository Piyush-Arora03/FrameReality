package com.example.framereality

data class PropertyModel(
    val id: String = "",
    val uid: String = "",
    val purpose: String = "",
    val category: String = "",
    val subcategory: String = "",
    val title: String = "",
    val description: String = "",
    val email: String = "",
    val phoneCode: String = "",
    val phoneNumber: String = "",
    val country: String = "",
    val city: String = "",
    val address: String = "",
    val status: String = "",
    val areaSizeUnit: String = "",
    val floors: Long = 0,
    val bedrooms: Long = 0,
    val bathrooms: Long = 0,
    val price: Double = 0.0,
    val timestamp: Long = 0L,
    val latitude: String = "",
    val longitude: String = "",
    val imageUrls: List<String> = emptyList() // List of image URLs (from "Images" child in DB)
)
