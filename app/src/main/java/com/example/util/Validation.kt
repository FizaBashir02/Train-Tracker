package com.example.util

fun validatePassword(password: String): Pair<Boolean, String?> {
    if (password.length < 8) {
        return Pair(false, "Password must be at least 8 characters long")
    }
    if (!password.any { it in 'A'..'Z' }) {
        return Pair(false, "Password must contain at least one uppercase letter")
    }
    if (!password.any { it in 'a'..'z' }) {
        return Pair(false, "Password must contain at least one lowercase letter")
    }
    if (!password.any { it in '0'..'9' }) {
        return Pair(false, "Password must contain at least one number")
    }
    val specialChars = "!@#$%^&*()_+-=[]{};':\"\\|,.<>/?~`"
    if (!password.any { !it.isLetterOrDigit() || it in specialChars }) {
        return Pair(false, "Password must contain at least one special character")
    }
    return Pair(true, null)
}

