package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DiyDevice
import com.example.data.DiyDeviceType
import com.example.data.JarvisRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Jarvis AI", appName)
  }

  @Test
  fun `repository initializes with default protocols and devices`() = runTest {
    val repo = JarvisRepository()
    assertTrue(repo.diyDevices.value.isNotEmpty())
    assertTrue(repo.protocols.value.isNotEmpty())
    assertTrue(repo.pinouts.value.isNotEmpty())

    val cleanSlate = repo.protocols.value.find { it.id == "clean_slate" }
    assertNotNull(cleanSlate)
    assertEquals("Protocol Clean Slate", cleanSlate?.title)
  }

  @Test
  fun `repository can register custom DIY hardware node`() = runTest {
    val repo = JarvisRepository()
    val initialCount = repo.diyDevices.value.size
    val customDevice = DiyDevice(
      id = "test_esp32",
      name = "Test ESP32 Motor",
      deviceType = DiyDeviceType.ESP32,
      ipAddressOrUrl = "http://192.168.1.200/motor"
    )
    repo.addDiyDevice(customDevice)
    assertEquals(initialCount + 1, repo.diyDevices.value.size)
    assertNotNull(repo.diyDevices.value.find { it.id == "test_esp32" })
  }
}
