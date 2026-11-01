package com.studyhub.sa.util

object PasswordValidator {

    fun isStrong(password: String): Boolean = problems(password).isEmpty()

    fun problems(password: String): List<String> {
        val list = mutableListOf<String>()
        if (password.length < 8) list.add("At least 8 characters")
        if (!password.any { it.isUpperCase() }) list.add("An uppercase letter")
        if (!password.any { it.isDigit() }) list.add("A number")
        if (!password.any { !it.isLetterOrDigit() }) list.add("A symbol")
        return list
    }
}