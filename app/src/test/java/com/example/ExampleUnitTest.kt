package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testMandatoryProfilePhotoField() {
    val userWithPhoto = com.example.data.model.UserEntity(
      badgeNumber = "POL-100",
      name = "Agente Silva",
      role = "AGENTE_CAMPO",
      unit = "Luanda Sul",
      photoUri = com.example.ui.components.ProfilePhotoUtils.PRESET_OFFICER_ONE
    )
    assertNotNull(userWithPhoto.photoUri)
    assertTrue(userWithPhoto.photoUri!!.isNotBlank())

    val userWithoutPhoto = com.example.data.model.UserEntity(
      badgeNumber = "PARTICULAR",
      name = "Conta Particular",
      role = "AGENTE_CAMPO",
      unit = "",
      photoUri = null
    )
    assertNull(userWithoutPhoto.photoUri)
  }

  @Test
  fun testPresetOfficerPhotosExist() {
    assertTrue(com.example.ui.components.ProfilePhotoUtils.PRESET_OFFICER_ONE.startsWith("drawable:"))
    assertTrue(com.example.ui.components.ProfilePhotoUtils.PRESET_OFFICER_TWO.startsWith("drawable:"))
  }
}
