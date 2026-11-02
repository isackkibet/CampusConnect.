package com.campusconnect.app

import com.campusconnect.app.core.util.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    @Test
    fun emailValidationAcceptsCommonAddresses() {
        assertTrue(Validators.isValidEmail("student@example.com"))
        assertTrue(Validators.isValidEmail("mary.achieng+school@nnp.ac.ke"))
        assertFalse(Validators.isValidEmail("not-an-email"))
        assertFalse(Validators.isValidEmail("user@domain"))
    }

    @Test
    fun passwordRequiresLengthLetterAndDigit() {
        assertEquals("Password must be at least 8 characters", Validators.passwordError("abc1"))
        assertEquals("Password must include a number", Validators.passwordError("password"))
        assertEquals("Password must include a letter", Validators.passwordError("12345678"))
        assertNull(Validators.passwordError("campus2026"))
    }

    @Test
    fun fullNameRequiresTwoParts() {
        assertEquals("Enter your full name", Validators.nameError("Al"))
        assertEquals("Enter both your first and last name", Validators.nameError("Amina"))
        assertNull(Validators.nameError("Amina Otieno"))
    }

    @Test
    fun profileSetupEnforcesCourseAndYear() {
        assertNull(Validators.profileSetupError("Computing", "Information Technology", 2, "Hello"))
        assertEquals("Select your department", Validators.profileSetupError("", "IT", 2, ""))
        assertEquals("Enter your course", Validators.profileSetupError("Computing", "IT", 2, ""))
        assertEquals("Select your year of study", Validators.profileSetupError("Computing", "Information Technology", 7, ""))
    }

    @Test
    fun listingValidationRejectsBadPrices() {
        assertNull(Validators.listingError("Desk lamp", "Working lamp with shade", "1200"))
        assertEquals("Title must be at least 4 characters", Validators.listingError("Des", "Working lamp", "1200"))
        assertEquals("Add a short description", Validators.listingError("Desk lamp", "Short", "1200"))
        assertEquals("Enter a valid price in KES", Validators.listingError("Desk lamp", "Working lamp", "0"))
    }

    @Test
    fun formatsKenyanShillings() {
        assertEquals("KES 1,250", Validators.formatKes(1250))
        assertEquals("KES 0", Validators.formatKes(0))
    }
}
