package com.example

import com.example.util.validatePassword
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testPasswordValidation_PassingPasswords() {
    val passing = listOf("F@123456a", "Abc@1234", "Train@2026")
    for (pwd in passing) {
      val (valid, reason) = validatePassword(pwd)
      assertTrue("Expected '$pwd' to pass password validation, but failed with reason: $reason", valid)
    }
  }

  @Test
  fun testPasswordValidation_FailingPasswords() {
    val failing = listOf("password", "PASSWORD", "12345678", "Password", "Password1")
    for (pwd in failing) {
      val (valid, _) = validatePassword(pwd)
      assertFalse("Expected '$pwd' to fail password validation, but passed", valid)
    }
  }
}
