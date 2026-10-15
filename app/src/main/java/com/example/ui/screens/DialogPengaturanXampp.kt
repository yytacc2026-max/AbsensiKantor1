package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JaringanKantorManager
import com.example.network.PingResult
import com.example.ui.theme.*
import kotlinx.coroutines.launch

// Dialog pengaturan dan pengujian koneksi ke local server XAMPP (Apache + PHP)
@Composable
fun DialogPengaturanXampp(
  jaringanManager: JaringanKantorManager,
  onDismiss: () -> Unit
) {
  var urlInput by remember { mutableStateOf(jaringanManager.serverXamppUrl) }
  var pingResult by remember { mutableStateOf<PingResult?>(jaringanManager.lastPingResult) }
  var isLoading by remember { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()
  val scrollState = rememberScrollState()

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Dns,
          contentDescription = null,
          tint = TealPrimary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Koneksi Server Node.js",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = TextDark
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Aplikasi terhubung ke backend Node.js (Express) di komputer/laptop untuk sinkronisasi data presensi secara real-time.",
          fontSize = 12.sp,
          color = TextMuted,
          lineHeight = 16.sp
        )

        OutlinedTextField(
          value = urlInput,
          onValueChange = {
            urlInput = it
            pingResult = null
          },
          label = { Text("Base URL Server Node.js") },
          placeholder = { Text("http://192.168.0.10:3000/") },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = absensiTextFieldColors(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_xampp_url")
        )

        // Shortcut alamat umum
        Text(
          text = "Pilihan Cepat Alamat:",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextDark
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          AssistChip(
            onClick = {
              urlInput = "http://192.168.0.10:3000/"
              pingResult = null
            },
            label = { Text(":3000 (Node.js)", fontSize = 11.sp) }
          )
          AssistChip(
            onClick = {
              urlInput = "http://10.0.2.2:3000/"
              pingResult = null
            },
            label = { Text("Emulator", fontSize = 11.sp) }
          )
          AssistChip(
            onClick = {
              urlInput = "http://192.168.0.10:5000/"
              pingResult = null
            },
            label = { Text("Port 5000", fontSize = 11.sp) }
          )
        }

        // Tombol tes koneksi ping
        Button(
          onClick = {
            coroutineScope.launch {
              isLoading = true
              jaringanManager.updateServerUrl(urlInput)
              val res = jaringanManager.tesKoneksiXampp()
              pingResult = res
              isLoading = false
            }
          },
          enabled = !isLoading && urlInput.isNotBlank(),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_tes_ping_xampp")
        ) {
          if (isLoading) {
            CircularProgressIndicator(
              color = Color.White,
              modifier = Modifier.size(16.dp),
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Menghubungkan ke Node.js...", fontSize = 13.sp)
          } else {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Tes Koneksi ke Server Node.js", fontSize = 13.sp)
          }
        }

        // Tampilan status respon koneksi
        if (pingResult != null) {
          val res = pingResult!!
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (res.isSuccess) StatusGreenBg else StatusRedBg
            ),
            border = BorderStroke(
              1.dp,
              if (res.isSuccess) StatusGreen.copy(alpha = 0.4f) else StatusRed.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (res.isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                  contentDescription = null,
                  tint = if (res.isSuccess) StatusGreen else StatusRed,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (res.isSuccess) "Terhubung ke Server Node.js!" else "Koneksi Belum Berhasil",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (res.isSuccess) StatusGreen else StatusRed
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = res.message,
                fontSize = 11.sp,
                color = TextDark
              )
              if (res.isSuccess) {
                Text(
                  text = "Latency: ${res.responseTimeMs} ms • HTTP Status: ${res.statusCode}",
                  fontSize = 10.sp,
                  color = TextMuted
                )
              } else {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Tips: Pastikan server sudah dijalankan di laptop ('node server.js'), HP & Laptop satu Wi-Fi, dan periksa IP Laptop.",
                  fontSize = 10.sp,
                  color = TextMuted,
                  lineHeight = 14.sp
                )
              }
            }
          }
        }

        // Section Petunjuk Setup Node.js
        var showPanduanDetail by remember { mutableStateOf(false) }
        OutlinedCard(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.outlinedCardColors(containerColor = BgLight),
          border = BorderStroke(1.dp, BorderMuted),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Panduan Menjalankan Server Node.js",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
              )
              TextButton(
                onClick = { showPanduanDetail = !showPanduanDetail },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text(
                  text = if (showPanduanDetail) "Tutup" else "Lihat",
                  fontSize = 11.sp,
                  color = TealPrimary
                )
              }
            }

            if (showPanduanDetail) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "1. Buka folder 'nodejs_backend' di Laptop via Terminal/CMD.\n" +
                  "2. Ketik 'npm install' (hanya sekali untuk pasang Express).\n" +
                  "3. Jalankan server dengan perintah: 'node server.js'\n" +
                  "4. Tidak perlu install MySQL atau XAMPP! Database otomatis tersimpan persisten di 'database.json'.\n" +
                  "5. Masukkan URL: http://[IP_LAPTOP]:3000/ (contoh: http://192.168.0.10:3000/).\n" +
                  "6. Jumlah karyawan dimulai dari 0 dan otomatis bertambah setiap kali ada karyawan yang registrasi di aplikasi!",
                fontSize = 11.sp,
                color = TextDark,
                lineHeight = 16.sp
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          jaringanManager.updateServerUrl(urlInput)
          onDismiss()
        },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
      ) {
        Text("Simpan & Tutup")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Batal", color = TextMuted)
      }
    }
  )
}
