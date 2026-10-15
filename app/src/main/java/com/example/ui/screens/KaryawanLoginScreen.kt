package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DataKaryawan
import com.example.model.SessionManager
import com.example.network.XamppApiClient
import com.example.ui.theme.*
import kotlinx.coroutines.launch

// Layar khusus Login Karyawan via NIP / NIK
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KaryawanLoginScreen(
  onLoginSuccess: (DataKaryawan) -> Unit,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val sessionManager = remember { SessionManager(context) }
  val coroutineScope = rememberCoroutineScope()

  var nipInput by remember { mutableStateOf("") }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  val scrollState = rememberScrollState()

  Scaffold(
    containerColor = BgLight,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Login Karyawan",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("login_back_button")) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Kembali ke Beranda",
              tint = TextDark
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
        .padding(paddingValues)
        .imePadding(),
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 480.dp)
          .verticalScroll(scrollState)
          .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // Logo Fadiya Kencana
        Box(
          modifier = Modifier
            .size(86.dp)
            .clip(CircleShape)
            .background(Color.White),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(id = R.drawable.ic_fadiya_logo),
            contentDescription = "Logo Fadiya Kencana",
            modifier = Modifier
              .size(86.dp)
              .clip(CircleShape)
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
          text = "Login Karyawan",
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
          color = TextDark
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Masukkan NIK / NIP Anda yang sudah terdaftar",
          fontSize = 13.sp,
          color = TextMuted
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Pesan Peringatan / Error jika NIK tidak ditemukan
        if (errorMessage != null) {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StatusRedBg),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Peringatan",
                tint = StatusRed,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = errorMessage ?: "",
                fontSize = 13.sp,
                color = StatusRed,
                fontWeight = FontWeight.Medium
              )
            }
          }
          Spacer(modifier = Modifier.height(16.dp))
        }

        // Field Input NIK / NIP
        OutlinedTextField(
          value = nipInput,
          onValueChange = {
            nipInput = it
            errorMessage = null
          },
          label = { Text("NIK / NIP") },
          placeholder = { Text("Contoh: 2344664") },
          leadingIcon = {
            Icon(Icons.Default.Badge, contentDescription = null, tint = TealPrimary)
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = absensiTextFieldColors(),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Login / Masuk
        Button(
          onClick = {
            val cleanNip = nipInput.trim()
            if (cleanNip.isEmpty()) {
              errorMessage = "Silakan masukkan NIK / NIP Anda terlebih dahulu!"
              return@Button
            }

            coroutineScope.launch {
              isLoading = true
              val res = XamppApiClient.cekAtauLoginKaryawan(cleanNip)
              isLoading = false

              if (res.isSuccess) {
                val karyawanKetemu = res.getOrNull()!!
                sessionManager.simpanKaryawanTerdaftar(karyawanKetemu)
                sessionManager.simpanInputTerakhir(
                  karyawanKetemu.nip,
                  karyawanKetemu.nama,
                  karyawanKetemu.departemen
                )
                errorMessage = null
                onLoginSuccess(karyawanKetemu)
              } else {
                errorMessage = res.exceptionOrNull()?.message ?: "NIK / NIP '$cleanNip' tidak terdaftar. Silakan daftar karyawan baru terlebih dahulu."
              }
            }
          },
          enabled = !isLoading,
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("karyawan_login_submit_button")
        ) {
          if (isLoading) {
            CircularProgressIndicator(
              color = Color.White,
              modifier = Modifier.size(18.dp),
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Memproses Login...", fontSize = 15.sp, color = Color.White)
          } else {
            Text(
              text = "Login",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
          onClick = onBack,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
        ) {
          Text(text = "Batal", color = TextDark)
        }
      }
    }
  }
}
