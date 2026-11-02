package com.campusconnect.app.core.util

object Validators {

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(value: String): Boolean = emailRegex.matches(value.trim())

    fun passwordError(value: String): String? = when {
        value.length < 8 -> "Password must be at least 8 characters"
        value.none { it.isDigit() } -> "Password must include a number"
        value.none { it.isLetter() } -> "Password must include a letter"
        else -> null
    }

    fun nameError(value: String): String? = when {
        value.trim().length < 3 -> "Enter your full name"
        value.trim().split(" ").size < 2 -> "Enter both your first and last name"
        else -> null
    }

    fun emailError(value: String): String? = when {
        value.isBlank() -> "Email is required"
        !isValidEmail(value) -> "Enter a valid email address"
        else -> null
    }

    fun phoneError(value: String): String? {
        if (value.isBlank()) return null
        val digits = value.filter { it.isDigit() }
        return if (digits.length in 9..13) null else "Enter a valid phone number"
    }

    fun profileSetupError(
        department: String,
        course: String,
        year: Int,
        bio: String,
    ): String? = when {
        department.isBlank() -> "Select your department"
        course.trim().length < 3 -> "Enter your course"
        year !in 1..6 -> "Select your year of study"
        bio.length > 280 -> "Bio must be 280 characters or fewer"
        else -> null
    }

    fun postError(content: String): String? = when {
        content.isBlank() -> "Write something before posting"
        content.length > 2000 -> "Posts are limited to 2000 characters"
        else -> null
    }

    fun listingError(title: String, description: String, price: String): String? = when {
        title.trim().length < 4 -> "Title must be at least 4 characters"
        description.trim().length < 10 -> "Add a short description"
        price.toIntOrNull() == null || price.toInt() <= 0 -> "Enter a valid price in KES"
        else -> null
    }

    fun formatKes(amount: Int): String = "KES " + "%,d".format(amount)
}
