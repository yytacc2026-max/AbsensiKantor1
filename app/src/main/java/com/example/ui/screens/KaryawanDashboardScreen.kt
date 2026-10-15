package com.example.ui.screens

import android.graphics.Bitmap
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DataKaryawan
import com.example.model.JaringanKantorManager
import com.example.network.XamppApiClient
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KaryawanDashboardScreen(
  karyawan: DataKaryawan,
  onLogout: () -> Unit,
  onBack: () -> Unit,
  jaringanManager: JaringanKantorManager? = null
) {
  val coroutineScope = rememberCoroutineScope()
  val scrollState = rememberScrollState()

  var isMasuk by remember { mutableStateOf(false) }
  var isSelesai by remember { mutableStateOf(false) }
  var isProsesAbsen by remember { mutableStateOf(false) }
  var statusPesan by remember { mutableStateOf<String?>(null) }
  var isError by remember { mutableStateOf(false) }
  var showXamppDialog by remember { mutableStateOf(false) }

  // Launcher Kamera Swafoto (Selfie Check-in)
  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap: Bitmap? ->
    if (bitmap != null) {
      val outputStream = ByteArrayOutputStream()
      bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
      val base64Foto = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

      val tipeAbsen = if (isMasuk) "keluar" else "masuk"

      coroutineScope.launch {
        isProsesAbsen = true
        val res = XamppApiClient.kirimAbsen(
          idKaryawan = karyawan.id,
          nip = karyawan.nip,
          nama = karyawan.nama,
          departemen = karyawan.departemen,
          tipe = tipeAbsen,
          fotoSelfie = base64Foto
        )
        isProsesAbsen = false

        if (res.isSuccess) {
          if (!isMasuk) {
            isMasuk = true
            statusPesan = "Berhasil mencatat jam MASUK beserta Foto Swafoto!"
          } else {
            isSelesai = true
            statusPesan = "Berhasil mencatat jam KELUAR beserta Foto Swafoto!"
          }
          isError = false
        } else {
          val errMsg = res.exceptionOrNull()?.message ?: "Gagal mencatat jam ${tipeAbsen.uppercase()}"
          statusPesan = if (errMsg.contains("10.0.2.2")) {
            "Gagal terhubung ke server. Klik tombol [Server] di atas dan ubah IP ke IP Laptop Anda (192.168.0.10)."
          } else {
            errMsg
          }
          isError = true

          if (errMsg.contains("sudah melakukan Absen Masuk", ignoreCase = true)) {
            isMasuk = true
          } else if (errMsg.contains("sudah melakukan Absen Keluar", ignoreCase = true)) {
            isMasuk = true
            isSelesai = true
          }
        }
      }
    } else {
      statusPesan = "Pengambilan foto swafoto dibatalkan. Presensi memerlukan foto swafoto."
      isError = true
    }
  }

  if (showXamppDialog && jaringanManager != null) {
    DialogPengaturanXampp(
      jaringanManager = jaringanManager,
      onDismiss = { showXamppDialog = false }
    )
  }

  Scaffold(
    containerColor = BgLight,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Dashboard Karyawan",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("karyawan_dashboard_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Kembali",
              tint = TextDark
            )
          }
        },
        actions = {
          IconButton(
            onClick = onLogout,
            modifier = Modifier.testTag("karyawan_dashboard_logout_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Logout,
              contentDescription = "Keluar Akun",
              tint = StatusRed
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = BgLight)
      )
    }
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
          .widthIn(max = 500.dp)
          .verticalScroll(scrollState)
          .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Kartu Profil Karyawan
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = CardWhite),
          border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.3f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("karyawan_profile_card")
        ) {
          Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(TealContainer),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = karyawan.avatarInisial.ifBlank { karyawan.nama.take(2).uppercase() },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TealDark
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = karyawan.nama,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "NIP: ${karyawan.nip}\n${karyawan.departemen}",
                fontSize = 13.sp,
                color = TealDark,
                fontWeight = FontWeight.Medium
              )
              if (karyawan.jenisKelamin.isNotBlank() && karyawan.jenisKelamin != "-") {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Jenis Kelamin: ${if (karyawan.jenisKelamin == "P") "Perempuan" else if (karyawan.jenisKelamin == "L") "Laki-laki" else karyawan.jenisKelamin}",
                  fontSize = 11.sp,
                  color = TextMuted
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Jaringan Kantor
        if (jaringanManager != null) {
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
              .testTag("karyawan_network_card")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
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
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = if (jaringanManager.isDalamJaringanKantor) "Terhubung Jaringan Kantor" else "Di Luar Jaringan Kantor",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (jaringanManager.isDalamJaringanKantor) TealDark else StatusRed
                  )
                  Text(
                    text = "Jarak: ${jaringanManager.jarakDariKantorMeter}m ${if (jaringanManager.isDalamJaringanKantor) "• Siap Absen" else "• Absensi Terkunci"}",
                    fontSize = 11.sp,
                    color = TextDark
                  )
                }
              }

              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                  onClick = { showXamppDialog = true },
                  shape = RoundedCornerShape(10.dp),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
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
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
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

          Spacer(modifier = Modifier.height(16.dp))
        }

        // Pesan Feedback
        if (statusPesan != null) {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isError) StatusRedBg else TealContainer
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("karyawan_status_card")
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (isError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isError) StatusRed else TealDark,
                modifier = Modifier.size(22.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = statusPesan ?: "",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isError) StatusRed else TealDark
              )
            }
          }
          Spacer(modifier = Modifier.height(16.dp))
        }

        // Kartu Jadwal Jam Kerja Resmi (POV Karyawan)
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = TealContainer.copy(alpha = 0.45f)),
          border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.25f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = TealDark,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text("Jam Masuk", fontSize = 11.sp, color = TextMuted)
                Text("08.00 WIB", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TealDark)
              }
            }

            VerticalDivider(
              modifier = Modifier
                .height(28.dp)
                .width(1.dp),
              color = TealPrimary.copy(alpha = 0.3f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = StatusRed,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text("Jam Pulang", fontSize = 11.sp, color = TextMuted)
                Text("17.00 WIB", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tombol Presensi
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = CardWhite),
          border = BorderStroke(1.dp, BorderMuted),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Presensi Harian",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (isMasuk) "Anda sudah mencatat jam masuk hari ini" else "Tekan tombol di bawah untuk mencatat kehadiran",
              fontSize = 12.sp,
              color = TextMuted,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
              onClick = {
                if (jaringanManager != null && !jaringanManager.isDalamJaringanKantor) {
                  statusPesan = "Presensi Ditolak: Anda berada di luar jaringan kantor!"
                  isError = true
                  return@Button
                }
                // Buka Kamera HP untuk Mengambil Foto Swafoto (Selfie)
                cameraLauncher.launch(null)
              },
              enabled = !isProsesAbsen && !isSelesai,
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isSelesai) Color.Gray else if (isMasuk) StatusRed else TealPrimary
              ),
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("karyawan_absen_button")
            ) {
              if (isProsesAbsen) {
                CircularProgressIndicator(
                  color = Color.White,
                  modifier = Modifier.size(20.dp),
                  strokeWidth = 2.dp
                )
              } else {
                Icon(
                  imageVector = if (isSelesai) Icons.Default.CheckCircle else if (isMasuk) Icons.AutoMirrored.Filled.Logout else Icons.Default.CheckCircle,
                  contentDescription = null,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isSelesai) "Absensi Hari Ini Selesai" else if (isMasuk) "Catat Jam Keluar" else "Catat Jam Masuk",
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Keluar / Hapus Sesi HP ini
        OutlinedButton(
          onClick = onLogout,
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("karyawan_logout_account_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            tint = StatusRed,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Keluar & Lepas Akun dari HP Ini",
            fontSize = 14.sp,
            color = StatusRed,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }
}
