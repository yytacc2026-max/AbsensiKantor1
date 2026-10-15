package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.model.JaringanKantorManager
import com.example.model.SessionManager
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminLoginScreen
import com.example.ui.screens.KaryawanDashboardScreen
import com.example.ui.screens.KaryawanLoginScreen
import com.example.ui.screens.KaryawanRegisterScreen
import com.example.ui.screens.RoleSelectionScreen

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val jaringanManager = JaringanKantorManager(applicationContext)
    val sessionManager = SessionManager(applicationContext)

    setContent {
      MaterialTheme(
        colorScheme = lightColorScheme(
          primary = Color(0xFF00695C),
          primaryContainer = Color(0xFFB2DFDB),
          surfaceVariant = Color(0xFFF1F8F6)
        )
      ) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          var isRegistered by remember { mutableStateOf(sessionManager.isSudahTerdaftar()) }
          var currentScreen by remember { mutableStateOf("role_selection") }

          val karyawanTerdaftar = remember(isRegistered) { sessionManager.getKaryawanTerdaftar() }

          when (currentScreen) {
            "role_selection" -> {
              RoleSelectionScreen(
                karyawanTerdaftar = karyawanTerdaftar,
                onOpenDashboard = { currentScreen = "karyawan_dashboard" },
                onRegisterNew = { currentScreen = "karyawan_register" },
                onSelectLogin = { currentScreen = "karyawan_login" },
                onSelectAdmin = { currentScreen = "admin_login" },
                jaringanManager = jaringanManager
              )
            }
            "karyawan_login" -> {
              KaryawanLoginScreen(
                onLoginSuccess = { karyawanFound ->
                  sessionManager.simpanKaryawanTerdaftar(karyawanFound)
                  isRegistered = true
                  currentScreen = "karyawan_dashboard"
                },
                onBack = { currentScreen = "role_selection" }
              )
            }
            "karyawan_register" -> {
              KaryawanRegisterScreen(
                onRegisterSuccess = { karyawanBaru ->
                  sessionManager.simpanKaryawanTerdaftar(karyawanBaru)
                  isRegistered = true
                  currentScreen = "karyawan_dashboard"
                },
                onBack = { currentScreen = "role_selection" },
                jaringanManager = jaringanManager
              )
            }
            "karyawan_dashboard" -> {
              if (karyawanTerdaftar != null) {
                KaryawanDashboardScreen(
                  karyawan = karyawanTerdaftar,
                  onLogout = {
                    sessionManager.hapusPendaftaran()
                    isRegistered = false
                    currentScreen = "role_selection"
                  },
                  onBack = { currentScreen = "role_selection" },
                  jaringanManager = jaringanManager
                )
              } else {
                currentScreen = "role_selection"
              }
            }
            "admin_login" -> {
              AdminLoginScreen(
                onLoginSuccess = { currentScreen = "admin_dashboard" },
                onBack = { currentScreen = "role_selection" }
              )
            }
            "admin_dashboard" -> {
              AdminDashboardScreen(
                onLogout = { currentScreen = "role_selection" }
              )
            }
            else -> {
              RoleSelectionScreen(
                karyawanTerdaftar = karyawanTerdaftar,
                onOpenDashboard = { currentScreen = "karyawan_dashboard" },
                onRegisterNew = { currentScreen = "karyawan_register" },
                onSelectLogin = { currentScreen = "karyawan_login" },
                onSelectAdmin = { currentScreen = "admin_login" },
                jaringanManager = jaringanManager
              )
            }
          }
        }
      }
    }
  }
}
