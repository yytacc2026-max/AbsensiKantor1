package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.network.XamppApiClient
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class TabAdmin(val label: String, val icon: ImageVector) {
  KEHADIRAN("Kehadiran", Icons.Default.Assessment),
  MASTER_KARYAWAN("Master Karyawan", Icons.Default.People),
  PENGAJUAN_IZIN("Pengajuan Izin", Icons.Default.AssignmentTurnedIn)
}

enum class FilterStatusAbsensi(val label: String) {
  SEMUA("Semua"),
  HADIR("Hadir"),
  TERLAMBAT("Terlambat")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
  onLogout: () -> Unit
) {
  val context = LocalContext.current
  val sessionManager = remember { SessionManager(context) }
  val jaringanManager = remember { JaringanKantorManager(context) }
  val coroutineScope = rememberCoroutineScope()

  var selectedTab by remember { mutableStateOf(TabAdmin.KEHADIRAN) }
  var filterStatus by remember { mutableStateOf(FilterStatusAbsensi.SEMUA) }

  var configAdmin by remember { mutableStateOf(sessionManager.getKonfigurasiAdmin()) }
  var isEditModeOpen by remember { mutableStateOf(false) }
  var isTambahKaryawanOpen by remember { mutableStateOf(false) }
  var showXamppConfigDialog by remember { mutableStateOf(false) }

  var isSyncing by remember { mutableStateOf(false) }
  var isConnectedToServer by remember { mutableStateOf(false) }
  var pesanStatusAdmin by remember { mutableStateOf<String?>(null) }

  var daftarAbsensi by remember {
    mutableStateOf(sessionManager.getAdminDaftarAbsensi())
  }

  var masterKaryawanList by remember {
    mutableStateOf<List<DataKaryawan>>(sessionManager.getMasterKaryawanList())
  }
  var pengajuanIzinList by remember { mutableStateOf<List<ItemPengajuanIzinAdmin>>(emptyList()) }

  // State Dialog Edit Dashboard
  var inputJamMasuk by remember { mutableStateOf(configAdmin.jamMasukKerja) }
  var inputBatasTerlambat by remember { mutableStateOf(configAdmin.batasTerlambat) }
  var inputJamPulang by remember { mutableStateOf(configAdmin.jamPulangKerja) }
  var inputPengumuman by remember { mutableStateOf(configAdmin.pengumuman) }

  // State Dialog Tambah Karyawan Admin
  var nipBaru by remember { mutableStateOf("") }
  var namaBaru by remember { mutableStateOf("") }
  var jkBaru by remember { mutableStateOf("Laki-laki") }
  var expandedJkAdmin by remember { mutableStateOf(false) }
  var ttlBaru by remember { mutableStateOf("") }
  var divisiBaru by remember { mutableStateOf("Produksi Pabrik") }
  var expandedDivisiAdmin by remember { mutableStateOf(false) }

  var serverUrlInput by remember { mutableStateOf(XamppApiClient.baseUrl) }

  fun muatDataDariServer() {
    coroutineScope.launch {
      isSyncing = true
      val dashResult = XamppApiClient.getDashboardAdminLengkap()
      if (dashResult.isSuccess) {
        val dataLengkap = dashResult.getOrNull()
        if (dataLengkap != null) {
          configAdmin = dataLengkap.config
          daftarAbsensi = dataLengkap.daftarAbsensi
          sessionManager.simpanKonfigurasiAdmin(dataLengkap.config)
          sessionManager.simpanAdminDaftarAbsensi(dataLengkap.daftarAbsensi)
          isConnectedToServer = true
        }
      } else {
        isConnectedToServer = false
      }

      val masterResult = XamppApiClient.getDaftarMasterKaryawan()
      if (masterResult.isSuccess) {
        val listMaster = masterResult.getOrNull().orEmpty()
        masterKaryawanList = listMaster
        sessionManager.simpanMasterKaryawan(listMaster)
      }

      val izinResult = XamppApiClient.getDaftarPengajuanIzin()
      if (izinResult.isSuccess) {
        pengajuanIzinList = izinResult.getOrNull().orEmpty()
      }

      isSyncing = false
    }
  }

  LaunchedEffect(Unit) {
    muatDataDariServer()
  }

  val totalHadir = remember(daftarAbsensi) {
    daftarAbsensi.count { it.status == StatusAbsensi.TEPAT_WAKTU || it.status == StatusAbsensi.TERLAMBAT }
  }
  val totalTerlambat = remember(daftarAbsensi) {
    daftarAbsensi.count { it.status == StatusAbsensi.TERLAMBAT }
  }

  val totalKaryawanTampil = remember(masterKaryawanList) {
    masterKaryawanList.size
  }

  val totalBelumAbsen = remember(totalKaryawanTampil, totalHadir) {
    maxOf(0, totalKaryawanTampil - totalHadir)
  }

  val filteredAbsensi = remember(daftarAbsensi, filterStatus) {
    when (filterStatus) {
      FilterStatusAbsensi.SEMUA -> daftarAbsensi
      FilterStatusAbsensi.HADIR -> daftarAbsensi.filter { it.status == StatusAbsensi.TEPAT_WAKTU || it.status == StatusAbsensi.TERLAMBAT }
      FilterStatusAbsensi.TERLAMBAT -> daftarAbsensi.filter { it.status == StatusAbsensi.TERLAMBAT }
    }
  }

  Scaffold(
    containerColor = BgLight,
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Dashboard Administrator",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = TextDark
            )
            Text(
              text = if (isConnectedToServer) "🟢 Terhubung ke Server (Sinkronisasi Semua Device)" else "🟠 Mode Offline / Belum Konek Server",
              fontSize = 11.sp,
              color = if (isConnectedToServer) StatusGreen else StatusAmber,
              fontWeight = FontWeight.SemiBold
            )
          }
        },
        actions = {
          IconButton(
            onClick = { showXamppConfigDialog = true },
            modifier = Modifier.testTag("admin_xampp_server_button")
          ) {
            Icon(
              imageVector = Icons.Default.Dns,
              contentDescription = "Pengaturan Server",
              tint = TealPrimary
            )
          }
          IconButton(
            onClick = { muatDataDariServer() },
            modifier = Modifier.testTag("admin_sync_button")
          ) {
            if (isSyncing) {
              CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = TealPrimary
              )
            } else {
              Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "Sinkronkan Data Server",
                tint = TealPrimary
              )
            }
          }
          IconButton(onClick = onLogout, modifier = Modifier.testTag("admin_logout_button")) {
            Icon(
              imageVector = Icons.Default.Logout,
              contentDescription = "Keluar",
              tint = StatusRed
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = BgLight)
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = CardWhite,
        tonalElevation = 0.dp,
        modifier = Modifier.testTag("admin_bottom_navigation")
      ) {
        TabAdmin.values().forEach { tab ->
          val isSelected = selectedTab == tab
          NavigationBarItem(
            selected = isSelected,
            onClick = { selectedTab = tab },
            icon = {
              Icon(
                imageVector = tab.icon,
                contentDescription = tab.label
              )
            },
            label = {
              Text(
                text = tab.label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = TealPrimary,
              selectedTextColor = TealPrimary,
              indicatorColor = TealContainer,
              unselectedIconColor = TextMuted,
              unselectedTextColor = TextMuted
            )
          )
        }
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentAlignment = Alignment.TopCenter
    ) {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .widthIn(max = 680.dp)
          .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        item(key = "top_spacer") {
          Spacer(modifier = Modifier.height(4.dp))
        }

        if (pesanStatusAdmin != null) {
          item(key = "admin_status_banner") {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = StatusGreenBg),
              border = BorderStroke(1.dp, StatusGreen.copy(alpha = 0.35f)),
              elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_status_banner")
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = StatusGreen,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Pembaruan Tersinkronisasi",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusGreen
                  )
                  Text(
                    text = pesanStatusAdmin ?: "",
                    fontSize = 12.sp,
                    color = TextDark
                  )
                }
                IconButton(onClick = { pesanStatusAdmin = null }, modifier = Modifier.size(28.dp)) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Tutup",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }
          }
        }

        item(key = "admin_server_status_card") {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isConnectedToServer) TealContainer.copy(alpha = 0.45f) else StatusAmberBg
            ),
            border = BorderStroke(
              1.dp,
              if (isConnectedToServer) TealPrimary.copy(alpha = 0.3f) else StatusAmber.copy(alpha = 0.4f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth().testTag("admin_server_card")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isConnectedToServer) TealPrimary else StatusAmber),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = if (isConnectedToServer) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = if (isConnectedToServer) "Sinkronisasi Server Aktif" else "Server Belum Terkoneksi",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isConnectedToServer) TealDark else StatusAmber
                  )
                  Text(
                    text = XamppApiClient.baseUrl,
                    fontSize = 11.sp,
                    color = TextMuted
                  )
                }
              }

              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalButton(
                  onClick = { showXamppConfigDialog = true },
                  shape = RoundedCornerShape(10.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isConnectedToServer) TealPrimary.copy(alpha = 0.15f) else StatusAmber.copy(alpha = 0.2f),
                    contentColor = if (isConnectedToServer) TealDark else StatusAmber
                  )
                ) {
                  Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("IP Server", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                  onClick = { muatDataDariServer() },
                  shape = RoundedCornerShape(10.dp),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = TealPrimary,
                    contentColor = Color.White
                  )
                ) {
                  Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Refresh", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }

        when (selectedTab) {
          TabAdmin.KEHADIRAN -> {
            item(key = "admin_metrics") {
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  AdminMetricCard(
                    judul = "Total Karyawan",
                    nilai = "$totalKaryawanTampil",
                    icon = Icons.Default.Groups,
                    warna = TealPrimary,
                    warnaBg = TealContainer,
                    modifier = Modifier.weight(1f)
                  )
                  AdminMetricCard(
                    judul = "Hadir",
                    nilai = "$totalHadir",
                    icon = Icons.Default.CheckCircle,
                    warna = StatusGreen,
                    warnaBg = StatusGreenBg,
                    modifier = Modifier.weight(1f)
                  )
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  AdminMetricCard(
                    judul = "Terlambat",
                    nilai = "$totalTerlambat",
                    icon = Icons.Default.Warning,
                    warna = StatusAmber,
                    warnaBg = StatusAmberBg,
                    modifier = Modifier.weight(1f)
                  )
                  AdminMetricCard(
                    judul = "Belum Absen",
                    nilai = "$totalBelumAbsen",
                    icon = Icons.Default.AccessTime,
                    warna = StatusRed,
                    warnaBg = StatusRedBg,
                    modifier = Modifier.weight(1f)
                  )
                }
              }
            }

            item(key = "admin_config_card") {
              Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, BorderMuted),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("admin_config_card")
              ) {
                Column(modifier = Modifier.padding(18.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "Pengaturan Jam Kerja Kantor",
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold,
                      color = TextDark
                    )
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TealContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                      Text(
                        text = "Semua Device",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealDark
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(10.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Column {
                      Text(text = "Jam Masuk", fontSize = 11.sp, color = TextMuted)
                      Text(text = configAdmin.jamMasukKerja, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                    Column {
                      Text(text = "Batas Terlambat", fontSize = 11.sp, color = TextMuted)
                      Text(text = configAdmin.batasTerlambat, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusAmber)
                    }
                    Column {
                      Text(text = "Jam Pulang", fontSize = 11.sp, color = TextMuted)
                      Text(text = configAdmin.jamPulangKerja, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                    Column {
                      Text(text = "Total Karyawan", fontSize = 11.sp, color = TextMuted)
                      Text(text = "$totalKaryawanTampil Org", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TealDark)
                    }
                  }

                  Spacer(modifier = Modifier.height(12.dp))
                  HorizontalDivider(color = BorderMuted)
                  Spacer(modifier = Modifier.height(10.dp))

                  Text(text = "Pesan Pengumuman Kantor:", fontSize = 11.sp, color = TextMuted)
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = configAdmin.pengumuman,
                    fontSize = 13.sp,
                    color = TextDark,
                    lineHeight = 18.sp
                  )
                }
              }
            }

            item(key = "admin_edit_button") {
              Button(
                onClick = {
                  inputJamMasuk = configAdmin.jamMasukKerja
                  inputBatasTerlambat = configAdmin.batasTerlambat
                  inputJamPulang = configAdmin.jamPulangKerja
                  inputPengumuman = configAdmin.pengumuman
                  isEditModeOpen = true
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(50.dp)
                  .testTag("button_edit_dashboard")
              ) {
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "Edit Dashboard",
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Edit Dashboard (Ubah Pengaturan Semua Device)",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }

            item(key = "admin_table_header") {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Daftar Absensi Karyawan Hari Ini",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                  )
                  Text(
                    text = "${daftarAbsensi.size} Data Masuk",
                    fontSize = 12.sp,
                    color = if (daftarAbsensi.isEmpty()) StatusAmber else TealPrimary,
                    fontWeight = FontWeight.SemiBold
                  )
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  FilterStatusAbsensi.values().forEach { filter ->
                    val isFilterSelected = filterStatus == filter
                    FilterChip(
                      selected = isFilterSelected,
                      onClick = { filterStatus = filter },
                      label = {
                        val count = when (filter) {
                          FilterStatusAbsensi.SEMUA -> daftarAbsensi.size
                          FilterStatusAbsensi.HADIR -> totalHadir
                          FilterStatusAbsensi.TERLAMBAT -> totalTerlambat
                        }
                        Text("${filter.label} ($count)", fontSize = 11.sp)
                      },
                      colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TealContainer,
                        selectedLabelColor = TealDark
                      )
                    )
                  }
                }
              }
            }

            if (filteredAbsensi.isEmpty()) {
              item(key = "admin_empty_table") {
                Card(
                  shape = RoundedCornerShape(16.dp),
                  colors = CardDefaults.cardColors(containerColor = CardWhite),
                  border = BorderStroke(1.dp, BorderMuted),
                  elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_empty_table_card")
                ) {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 32.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    Box(
                      modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(TealContainer.copy(alpha = 0.5f)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(26.dp)
                      )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                      text = if (daftarAbsensi.isEmpty()) "Belum Ada Data Absensi Masuk (0 Data)" else "Tidak Ada Data untuk Filter Ini",
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold,
                      color = TextDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = "Seluruh data absensi dimulai dari 0 agar pencatatan akurat. Data kehadiran akan otomatis masuk saat karyawan melakukan Absen Masuk di jaringan kantor.",
                      fontSize = 12.sp,
                      color = TextMuted,
                      textAlign = TextAlign.Center,
                      lineHeight = 16.sp
                    )
                  }
                }
              }
            } else {
              items(items = filteredAbsensi, key = { it.idKaryawan }) { row ->
                BarisTabelAbsensiCard(row = row)
              }
            }
          }

          TabAdmin.MASTER_KARYAWAN -> {
            item(key = "master_karyawan_header") {
              Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, BorderMuted),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(18.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text(
                        text = "Master Karyawan Kantor",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                      )
                      Text(
                        text = "${masterKaryawanList.size} Karyawan Terdaftar di Database",
                        fontSize = 12.sp,
                        color = TextMuted
                      )
                    }
                    Button(
                      onClick = {
                        nipBaru = ""
                        namaBaru = ""
                        jkBaru = "Laki-laki"
                        ttlBaru = ""
                        divisiBaru = "Produksi Pabrik"
                        isTambahKaryawanOpen = true
                      },
                      shape = RoundedCornerShape(12.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                      Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("Tambah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                  }

                  Spacer(modifier = Modifier.height(10.dp))
                  Text(
                    text = "Daftar karyawan resmi ini tersimpan di server database dan dipakai oleh setiap smartphone karyawan untuk registrasi akun dan absensi.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 16.sp
                  )
                }
              }
            }

            if (masterKaryawanList.isEmpty()) {
              item(key = "empty_master_karyawan") {
                Card(
                  shape = RoundedCornerShape(16.dp),
                  colors = CardDefaults.cardColors(containerColor = CardWhite),
                  border = BorderStroke(1.dp, BorderMuted),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    Icon(
                      imageVector = Icons.Default.PeopleOutline,
                      contentDescription = null,
                      tint = TextMuted,
                      modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                      text = "Belum Ada Karyawan Terdaftar",
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold,
                      color = TextDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = "Jumlah karyawan saat ini 0. Setiap kali karyawan mendaftarkan diri lewat aplikasi di smartphone mereka, data dan jumlah karyawan di sini akan otomatis bertambah.",
                      fontSize = 12.sp,
                      color = TextMuted,
                      textAlign = TextAlign.Center,
                      lineHeight = 16.sp
                    )
                  }
                }
              }
            } else {
              items(items = masterKaryawanList, key = { it.id }) { karyawan ->
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = CardWhite),
                  border = BorderStroke(1.dp, BorderMuted),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                      Box(
                        modifier = Modifier
                          .size(40.dp)
                          .clip(CircleShape)
                          .background(TealContainer),
                        contentAlignment = Alignment.Center
                      ) {
                        Text(
                          text = karyawan.avatarInisial,
                          fontSize = 14.sp,
                          fontWeight = FontWeight.Bold,
                          color = TealDark
                        )
                      }
                      Spacer(modifier = Modifier.width(12.dp))
                      Column {
                        Text(
                          text = karyawan.nama,
                          fontSize = 14.sp,
                          fontWeight = FontWeight.Bold,
                          color = TextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                          text = "NIP: ${karyawan.nip} • ${karyawan.departemen}",
                          fontSize = 12.sp,
                          color = TealDark,
                          fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                          text = "JK: ${karyawan.jenisKelamin} | TTL: ${karyawan.tempatTanggalLahir}",
                          fontSize = 11.sp,
                          color = TextMuted
                        )
                      }
                    }
                  }
                }
              }
            }
          }

          TabAdmin.PENGAJUAN_IZIN -> {
            item(key = "pengajuan_izin_header") {
              Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, BorderMuted),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(18.dp)) {
                  Text(
                    text = "Daftar Pengajuan Izin / Cuti Karyawan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Admin dapat meninjau dan menyetujui permohonan izin atau sakit dari karyawan di jaringan lokal.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 16.sp
                  )
                }
              }
            }

            if (pengajuanIzinList.isEmpty()) {
              item(key = "empty_izin") {
                Card(
                  shape = RoundedCornerShape(16.dp),
                  colors = CardDefaults.cardColors(containerColor = CardWhite),
                  border = BorderStroke(1.dp, BorderMuted),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    Icon(
                      imageVector = Icons.Default.AssignmentTurnedIn,
                      contentDescription = null,
                      tint = TextMuted,
                      modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                      text = "Belum Ada Pengajuan Izin",
                      fontSize = 15.sp,
                      fontWeight = FontWeight.Bold,
                      color = TextDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = "Tidak ada permohonan izin atau sakit yang diajukan hari ini.",
                      fontSize = 12.sp,
                      color = TextMuted,
                      textAlign = TextAlign.Center
                    )
                  }
                }
              }
            } else {
              items(items = pengajuanIzinList, key = { it.id }) { izin ->
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = CardWhite),
                  border = BorderStroke(1.dp, BorderMuted),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(text = izin.nama, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = StatusAmberBg
                      ) {
                        Text(
                          text = izin.statusPersetujuan,
                          fontSize = 10.sp,
                          fontWeight = FontWeight.Bold,
                          color = StatusAmber,
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                      }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Jenis: ${izin.jenisIzin}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TealDark)
                    Text(text = "Alasan: ${izin.alasan}", fontSize = 12.sp, color = TextMuted)
                  }
                }
              }
            }
          }
        }

        item(key = "bottom_spacer") {
          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    }

    // DIALOG EDIT DASHBOARD
    if (isEditModeOpen) {
      AlertDialog(
        onDismissRequest = { isEditModeOpen = false },
        title = { Text("Edit Pengaturan Sistem Kantor", fontWeight = FontWeight.Bold) },
        text = {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = inputJamMasuk,
              onValueChange = { inputJamMasuk = it },
              label = { Text("Jam Masuk (Contoh: 08:00)") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp)
            )
            OutlinedTextField(
              value = inputBatasTerlambat,
              onValueChange = { inputBatasTerlambat = it },
              label = { Text("Batas Terlambat (Contoh: 08:15)") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp)
            )
            OutlinedTextField(
              value = inputJamPulang,
              onValueChange = { inputJamPulang = it },
              label = { Text("Jam Pulang (Contoh: 17:00)") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp)
            )
            OutlinedTextField(
              value = inputPengumuman,
              onValueChange = { inputPengumuman = it },
              label = { Text("Broadcast Pengumuman Kantor") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp),
              maxLines = 3
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              val configBaru = configAdmin.copy(
                jamMasukKerja = inputJamMasuk,
                batasTerlambat = inputBatasTerlambat,
                jamPulangKerja = inputJamPulang,
                pengumuman = inputPengumuman
              )
              configAdmin = configBaru
              sessionManager.simpanKonfigurasiAdmin(configBaru)

              coroutineScope.launch {
                XamppApiClient.updateKonfigurasiAdmin(configBaru)
              }

              isEditModeOpen = false
              pesanStatusAdmin = "Pengaturan jam kerja dan pengumuman berhasil diperbarui!"
            },
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
          ) {
            Text("Simpan Perubahan")
          }
        },
        dismissButton = {
          TextButton(onClick = { isEditModeOpen = false }) {
            Text("Batal")
          }
        }
      )
    }

    // DIALOG TAMBAH KARYAWAN MASTER
    if (isTambahKaryawanOpen) {
      val opsiJk = listOf("Laki-laki", "Perempuan")
      val opsiDivisi = listOf(
        "Produksi Pabrik",
        "IT & Sistem Information",
        "Human Resources (HR)",
        "Keuangan & Akuntansi",
        "Operasional & Logistik",
        "Quality Control (QC)"
      )

      AlertDialog(
        onDismissRequest = { isTambahKaryawanOpen = false },
        title = { Text("Tambah Karyawan Master", fontWeight = FontWeight.Bold) },
        text = {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = nipBaru,
              onValueChange = { nipBaru = it },
              label = { Text("Nomor Induk Pegawai (NIP)") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp)
            )
            OutlinedTextField(
              value = namaBaru,
              onValueChange = { namaBaru = it },
              label = { Text("Nama Lengkap Karyawan") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp)
            )

            ExposedDropdownMenuBox(
              expanded = expandedJkAdmin,
              onExpandedChange = { expandedJkAdmin = !expandedJkAdmin },
              modifier = Modifier.fillMaxWidth()
            ) {
              OutlinedTextField(
                value = jkBaru,
                onValueChange = {},
                readOnly = true,
                label = { Text("Jenis Kelamin") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedJkAdmin) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
              )
              ExposedDropdownMenu(
                expanded = expandedJkAdmin,
                onDismissRequest = { expandedJkAdmin = false }
              ) {
                opsiJk.forEach { item ->
                  DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                      jkBaru = item
                      expandedJkAdmin = false
                    }
                  )
                }
              }
            }

            OutlinedTextField(
              value = ttlBaru,
              onValueChange = { ttlBaru = it },
              label = { Text("Tempat, Tanggal Lahir") },
              placeholder = { Text("Contoh: Jakarta, 15 Jan 1998") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp)
            )

            ExposedDropdownMenuBox(
              expanded = expandedDivisiAdmin,
              onExpandedChange = { expandedDivisiAdmin = !expandedDivisiAdmin },
              modifier = Modifier.fillMaxWidth()
            ) {
              OutlinedTextField(
                value = divisiBaru,
                onValueChange = {},
                readOnly = true,
                label = { Text("Departemen / Divisi") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDivisiAdmin) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
              )
              ExposedDropdownMenu(
                expanded = expandedDivisiAdmin,
                onDismissRequest = { expandedDivisiAdmin = false }
              ) {
                opsiDivisi.forEach { item ->
                  DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                      divisiBaru = item
                      expandedDivisiAdmin = false
                    }
                  )
                }
              }
            }
          }
        },
        confirmButton = {
          Button(
            onClick = {
              if (nipBaru.isNotBlank() && namaBaru.isNotBlank() && ttlBaru.isNotBlank()) {
                val karyawanBaru = DataKaryawan(
                  id = "K-${System.currentTimeMillis()}",
                  nip = nipBaru.trim(),
                  nama = namaBaru.trim(),
                  jenisKelamin = jkBaru,
                  tempatTanggalLahir = ttlBaru.trim(),
                  departemen = divisiBaru,
                  avatarInisial = namaBaru.take(2).uppercase()
                )
                val updatedList = masterKaryawanList + karyawanBaru
                masterKaryawanList = updatedList
                sessionManager.simpanMasterKaryawan(updatedList)

                coroutineScope.launch {
                  XamppApiClient.tambahKaryawanMaster(karyawanBaru)
                }

                isTambahKaryawanOpen = false
                pesanStatusAdmin = "Karyawan ${karyawanBaru.nama} berhasil ditambahkan!"
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
          ) {
            Text("Simpan Karyawan")
          }
        },
        dismissButton = {
          TextButton(onClick = { isTambahKaryawanOpen = false }) {
            Text("Batal")
          }
        }
      )
    }

    // DIALOG PENGATURAN URL SERVER
    if (showXamppConfigDialog) {
      AlertDialog(
        onDismissRequest = { showXamppConfigDialog = false },
        title = { Text("Pengaturan Alamat Server Lokal", fontWeight = FontWeight.Bold) },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text = "Masukkan URL server Node.js lokal di jaringan kantor:",
              fontSize = 12.sp,
              color = TextMuted
            )
            OutlinedTextField(
              value = serverUrlInput,
              onValueChange = { serverUrlInput = it },
              label = { Text("URL Server Node.js") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp),
              singleLine = true
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              XamppApiClient.baseUrl = serverUrlInput.trim()
              showXamppConfigDialog = false
              muatDataDariServer()
              pesanStatusAdmin = "Alamat server diperbarui ke ${XamppApiClient.baseUrl}"
            },
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
          ) {
            Text("Simpan & Hubungkan")
          }
        },
        dismissButton = {
          TextButton(onClick = { showXamppConfigDialog = false }) {
            Text("Tutup")
          }
        }
      )
    }
  }
}

@Composable
fun AdminMetricCard(
  judul: String,
  nilai: String,
  icon: ImageVector,
  warna: Color,
  warnaBg: Color,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = CardWhite),
    border = BorderStroke(1.dp, BorderMuted),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = modifier
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text(text = judul, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = nilai, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
      }
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(warnaBg),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = warna,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
fun BarisTabelAbsensiCard(row: BarisAbsensiKaryawan) {
  val isTepatWaktu = row.status == StatusAbsensi.TEPAT_WAKTU
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = CardWhite),
    border = BorderStroke(1.dp, BorderMuted),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (isTepatWaktu) StatusGreenBg else StatusAmberBg),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isTepatWaktu) Icons.Default.Check else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isTepatWaktu) StatusGreen else StatusAmber,
            modifier = Modifier.size(18.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(text = row.nama, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
          Text(text = "${row.departemen} • Masuk: ${row.jamMasuk}", fontSize = 11.sp, color = TextMuted)
        }
      }
      Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isTepatWaktu) StatusGreenBg else StatusAmberBg
      ) {
        Text(
          text = row.status.label,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = if (isTepatWaktu) StatusGreen else StatusAmber,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }
    }
  }
}