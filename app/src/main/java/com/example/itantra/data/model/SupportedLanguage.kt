package com.example.itantra.data.model

enum class SupportedLanguage(val isoCode: String, val displayName: String, val nativeLabel: String) {
    HINDI("hi", "Hindi", "हिंदी"),
    GUJARATI("gu", "Gujarati", "ગુજરાતી"),
    MARATHI("mr", "Marathi", "मराठी"),
    KANNADA("kn", "Kannada", "ಕನ್ನಡ"),
    MALAYALAM("ml", "Malayalam", "മലയാളം"),
    TAMIL("ta", "Tamil", "தமிழ்"),
    TELUGU("te", "Telugu", "తెలుగు"),
    ODIA("or", "Odia", "ଓଡ଼ିଆ"),
    BENGALI("bn", "Bengali", "বাংলা"),
    ENGLISH("en", "English", "English");

    companion object {
        fun fromIsoCode(code: String): SupportedLanguage? {
            return entries.find { it.isoCode == code }
        }
    }
}
