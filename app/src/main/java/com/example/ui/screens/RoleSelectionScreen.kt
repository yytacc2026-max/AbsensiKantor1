package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DataKaryawan
import com.example.model.JaringanKantorManager
import com.example.ui.theme.*

// Layar utama pemilihan akses (Opening Screen)
@Composable
fun RoleSelectionScreen(
  karyawanTerdaftar: DataKaryawan?,
  onOpenDashboard: () -> Unit,
  onRegisterNew: () -> Unit,
  onSelectLogin: () -> Unit,
  onSelectAdmin: () -> Unit,
  jaringanManager: JaringanKantorManager? = null
) {
  val scrollState = rememberScrollState()
  var showXamppDialog by remember { mutableStateOf(false) }

  if (showXamppDialog && jaringanManager != null) {
    DialogPengaturanXampp(
      jaringanManager = jaringanManager,
      onDismiss = { showXamppDialog = false }
    )
  }

  Scaffold(
    containerColor = BgLight
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentAlignment = Alignment.TopCenter
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 520.dp)
          .verticalScroll(scrollState)
          .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Header & Branding Aplikasi
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
        ) {
          // Logo Aplikasi Fadiya Kencana
          Box(
            modifier = Modifier
              .size(90.dp)
              .clip(CircleShape)
              .background(Color.White),
            contentAlignment = Alignment.Center
          ) {
            Image(
              painter = painterResource(id = R.drawable.ic_fadiya_logo),
              contentDescription = "Logo Fadiya Kencana",
              modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "Fadiya Kencana",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.testTag("app_title")
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Sistem Presensi Karyawan & Outsourcing",
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Label Info Server Lokal
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(TealContainer)
              .padding(horizontal = 14.dp, vertical = 6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Computer,
              contentDescription = null,
              tint = TealDark,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Penyimpanan: Komputer Lokal Kantor",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = TealDark
            )
          }

          if (jaringanManager != null) {
            Spacer(modifier = Modifier.height(10.dp))
            // Status Validasi Jaringan Kantor
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (jaringanManager.isDalamJaringanKantor) TealContainer.copy(alpha = 0.5f) else StatusRedBg
              ),
              border = BorderStroke(
                1.dp,
                if (jaringanManager.isDalamJaringanKantor) TealPrimary.copy(alpha = 0.35f) else StatusRed.copy(alpha = 0.4f)
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("role_network_card")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(
                    imageVector = if (jaringanManager.isDalamJaringanKantor) Icons.Default.Wifi else Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = if (jaringanManager.isDalamJaringanKantor) TealDark else StatusRed,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = if (jaringanManager.isDalamJaringanKantor) "Terhubung Jaringan Kantor" else "Di Luar Jaringan Kantor",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (jaringanManager.isDalamJaringanKantor) TealDark else StatusRed
                    )
                    Text(
                      text = if (jaringanManager.isDalamJaringanKantor) "Jarak: ${jaringanManager.jarakDariKantorMeter}m • Siap Absensi" else "Jarak: ${jaringanManager.jarakDariKantorMeter}m • Aplikasi Terkunci",
                      fontSize = 11.sp,
                      color = TextDark
                    )
                  }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  OutlinedButton(
                    onClick = { showXamppDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("role_btn_xampp_server")
                  ) {
                    Icon(
                      imageVector = Icons.Default.Dns,
                      contentDescription = null,
                      modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Server",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                  OutlinedButton(
                    onClick = { jaringanManager.toggleModeSimulasi() },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("role_toggle_network_button")
                  ) {
                    Text(
                      text = if (jaringanManager.isDalamJaringanKantor) "Uji: Jauh" else "Uji: Di Kantor",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (jaringanManager.isDalamJaringanKantor) TealDark else StatusRed
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bagian Aksi Pengguna (Register -> Login -> Admin)
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Text(
            text = "Pilih Akses Pengguna:",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark
          )

          if (karyawanTerdaftar != null) {
            // JIKA HP SUDAH TERIKAT DENGAN 1 KARYAWAN
            KaryawanMainCard(
              testTag = "open_registered_dashboard_card",
              title = "Buka Dashboard Karyawan",
              subtitle = "Akun Aktif: ${karyawanTerdaftar.nama} (NIP: ${karyawanTerdaftar.nip})",
              icon = Icons.Default.Person,
              badgeColor = TealPrimary,
              onClick = onOpenDashboard
            )
          } else {
            // 1. TOMBOL REGISTRASI KARYAWAN
            KaryawanMainCard(
              testTag = "role_karyawan_register_card",
              title = "Daftar Karyawan Baru",
              subtitle = "Registrasi NIP baru untuk mendaftar sebagai karyawan",
              icon = Icons.Default.Badge,
              badgeColor = TealPrimary,
              onClick = onRegisterNew
            )

            // 2. TOMBOL LOGIN KARYAWAN
            KaryawanMainCard(
              testTag = "role_karyawan_login_card",
              title = "Login Karyawan",
              subtitle = "Masuk dengan NIK / NIP Anda yang sudah terdaftar",
              icon = Icons.Default.Person,
              badgeColor = TealDark,
              onClick = onSelectLogin
            )
          }

          // 3. TOMBOL MASUK SEBAGAI ADMINISTRATOR
          KaryawanMainCard(
            testTag = "role_admin_card",
            title = "Masuk sebagai Administrator",
            subtitle = "Akses Web Admin & Manajemen HRIS Kantor",
            icon = Icons.Default.Lock,
            badgeColor = Color(0xFFE65100),
            onClick = onSelectAdmin
          )
        }

        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }
}

@Composable
fun KaryawanMainCard(
  testTag: String,
  title: String,
  subtitle: String,
  icon: ImageVector,
  badgeColor: Color,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag(testTag)
      .clickable { onClick() },
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = CardWhite),
    border = BorderStroke(1.dp, BorderMuted),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(50.dp)
          .clip(CircleShape)
          .background(badgeColor.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = badgeColor,
          modifier = Modifier.size(26.dp)
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = TextDark
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          fontSize = 12.sp,
          color = TextMuted,
          lineHeight = 16.sp
        )
      }
    }
  }
}
