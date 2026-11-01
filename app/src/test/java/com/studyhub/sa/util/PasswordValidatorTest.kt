package com.studyhub.sa.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordValidatorTest {

    @Test
    fun rejectsPasswordsShorterThanEightCharacters() {
        assertFalse(PasswordValidator.isStrong("Ab1!"))
    }

    @Test
    fun rejectsPasswordWithoutUppercaseLetter() {
        assertFalse(PasswordValidator.isStrong("lowercase1!"))
    }

    @Test
    fun rejectsPasswordWithoutNumber() {
        assertFalse(PasswordValidator.isStrong("Uppercase!!"))
    }

    @Test
    fun rejectsPasswordWithoutSymbol() {
        assertFalse(PasswordValidator.isStrong("Uppercase12"))
    }

    @Test
    fun acceptsPasswordMeetingEveryRule() {
        assertTrue(PasswordValidator.isStrong("Strong1!Pass"))
    }
}