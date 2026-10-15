package com.example.model

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class SessionManager(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("absensi_session_pref", Context.MODE_PRIVATE)

  companion object {
    private const val KEY_IS_REGISTERED = "key_is_registered"
    private const val KEY_KARYAWAN_ID = "key_karyawan_id"
    private const val KEY_KARYAWAN_NIP = "key_karyawan_nip"
    private const val KEY_KARYAWAN_NAMA = "key_karyawan_nama"
    private const val KEY_KARYAWAN_JENIS_KELAMIN = "key_karyawan_jenis_kelamin"
    private const val KEY_KARYAWAN_TTL = "key_karyawan_ttl"
    private const val KEY_KARYAWAN_DEPARTEMEN = "key_karyawan_departemen"
    private const val KEY_DEVICE_TEST_ID = "key_device_test_id"

    private const val KEY_LAST_NIP = "key_last_nip"
    private const val KEY_LAST_NAMA = "key_last_nama"
    private const val KEY_LAST_DEPARTEMEN = "key_last_departemen"

    private const val KEY_ABSENSI_MASUK = "key_absensi_masuk"
    private const val KEY_ABSENSI_PULANG = "key_absensi_pulang"
    private const val KEY_ABSENSI_STATUS = "key_absensi_status"
    private const val KEY_ABSENSI_SUDAH_MASUK = "key_absensi_sudah_masuk"
    private const val KEY_ABSENSI_SUDAH_PULANG = "key_absensi_sudah_pulang"

    private const val KEY_RIWAYAT_JSON = "key_riwayat_json"
    private const val KEY_ADMIN_ABSENSI_JSON = "key_admin_absensi_json"
    private const val KEY_MASTER_KARYAWAN_JSON = "key_master_karyawan_json"

    private const val KEY_CFG_JAM_MASUK = "key_cfg_jam_masuk"
    private const val KEY_CFG_JAM_PULANG = "key_cfg_jam_pulang"
    private const val KEY_CFG_BATAS_TERLAMBAT = "key_cfg_batas_terlambat"
    private const val KEY_CFG_PENGUMUMAN = "key_cfg_pengumuman"
    private const val KEY_CFG_TOTAL_KARYAWAN = "key_cfg_total_karyawan"
    private const val KEY_CFG_TOTAL_HADIR = "key_cfg_total_hadir"
    private const val KEY_CFG_TOTAL_TERLAMBAT = "key_cfg_total_terlambat"
    private const val KEY_CFG_TOTAL_BELUM_ABSEN = "key_cfg_total_belum_absen"
  }

  // --- ID Perangkat ---
  fun getOrCreateDeviceTestId(): String {
    var id = prefs.getString(KEY_DEVICE_TEST_ID, null)
    if (id.isNullOrBlank()) {
      id = "DEV-" + UUID.randomUUID().toString().take(8).uppercase()
      prefs.edit().putString(KEY_DEVICE_TEST_ID, id).apply()
    }
    return id
  }

  // --- Registrasi / Karyawan ---
  fun isSudahTerdaftar(): Boolean {
    return prefs.getBoolean(KEY_IS_REGISTERED, false)
  }

  fun getKaryawanTerdaftar(): DataKaryawan? {
    if (!isSudahTerdaftar()) return null
    val id = prefs.getString(KEY_KARYAWAN_ID, "") ?: ""
    val nip = prefs.getString(KEY_KARYAWAN_NIP, "") ?: ""
    val nama = prefs.getString(KEY_KARYAWAN_NAMA, "") ?: ""
    val jk = prefs.getString(KEY_KARYAWAN_JENIS_KELAMIN, "-") ?: "-"
    val ttl = prefs.getString(KEY_KARYAWAN_TTL, "-") ?: "-"
    val dept = prefs.getString(KEY_KARYAWAN_DEPARTEMEN, "") ?: ""
    return DataKaryawan(
      id = id,
      nip = nip,
      nama = nama,
      jenisKelamin = jk,
      tempatTanggalLahir = ttl,
      departemen = dept,
      avatarInisial = if (nama.length >= 2) nama.take(2).uppercase() else "KA"
    )
  }

  fun simpanPendaftaran(karyawan: DataKaryawan) {
    prefs.edit()
      .putBoolean(KEY_IS_REGISTERED, true)
      .putString(KEY_KARYAWAN_ID, karyawan.id)
      .putString(KEY_KARYAWAN_NIP, karyawan.nip)
      .putString(KEY_KARYAWAN_NAMA, karyawan.nama)
      .putString(KEY_KARYAWAN_JENIS_KELAMIN, karyawan.jenisKelamin)
      .putString(KEY_KARYAWAN_TTL, karyawan.tempatTanggalLahir)
      .putString(KEY_KARYAWAN_DEPARTEMEN, karyawan.departemen)
      .apply()
  }

  fun simpanKaryawanTerdaftar(karyawan: DataKaryawan) {
    simpanPendaftaran(karyawan)
  }

  fun hapusPendaftaran() {
    prefs.edit()
      .putBoolean(KEY_IS_REGISTERED, false)
      .remove(KEY_KARYAWAN_ID)
      .remove(KEY_KARYAWAN_NIP)
      .remove(KEY_KARYAWAN_NAMA)
      .remove(KEY_KARYAWAN_JENIS_KELAMIN)
      .remove(KEY_KARYAWAN_TTL)
      .remove(KEY_KARYAWAN_DEPARTEMEN)
      .remove(KEY_ABSENSI_MASUK)
      .remove(KEY_ABSENSI_PULANG)
      .remove(KEY_ABSENSI_STATUS)
      .remove(KEY_ABSENSI_SUDAH_MASUK)
      .remove(KEY_ABSENSI_SUDAH_PULANG)
      .remove(KEY_RIWAYAT_JSON)
      .apply()
  }

  // --- Input Terakhir ---
  fun getInputTerakhir(): DataKaryawan? {
    val nip = prefs.getString(KEY_LAST_NIP, null) ?: return null
    val nama = prefs.getString(KEY_LAST_NAMA, "") ?: ""
    val dept = prefs.getString(KEY_LAST_DEPARTEMEN, "") ?: ""
    return DataKaryawan(
      id = "",
      nip = nip,
      nama = nama,
      jenisKelamin = "-",
      tempatTanggalLahir = "-",
      departemen = dept
    )
  }

  fun simpanInputTerakhir(nip: String, nama: String, departemen: String) {
    prefs.edit()
      .putString(KEY_LAST_NIP, nip)
      .putString(KEY_LAST_NAMA, nama)
      .putString(KEY_LAST_DEPARTEMEN, departemen)
      .apply()
  }

  // --- Absensi Hari Ini ---
  fun getAbsensiHariIni(): AbsensiHariIni {
    val jamMasuk = prefs.getString(KEY_ABSENSI_MASUK, null)
    val jamPulang = prefs.getString(KEY_ABSENSI_PULANG, null)
    val statusStr = prefs.getString(KEY_ABSENSI_STATUS, null)
    val status = statusStr?.let {
      try { StatusAbsensi.valueOf(it) } catch (_: Exception) { null }
    }
    val sudahMasuk = prefs.getBoolean(KEY_ABSENSI_SUDAH_MASUK, false)
    val sudahPulang = prefs.getBoolean(KEY_ABSENSI_SUDAH_PULANG, false)

    return AbsensiHariIni(
      jamMasuk = jamMasuk,
      jamPulang = jamPulang,
      status = status,
      sudahAbsenMasuk = sudahMasuk,
      sudahAbsenPulang = sudahPulang
    )
  }

  fun simpanAbsensiHariIni(absensi: AbsensiHariIni) {
    prefs.edit()
      .putString(KEY_ABSENSI_MASUK, absensi.jamMasuk)
      .putString(KEY_ABSENSI_PULANG, absensi.jamPulang)
      .putString(KEY_ABSENSI_STATUS, absensi.status?.name)
      .putBoolean(KEY_ABSENSI_SUDAH_MASUK, absensi.sudahAbsenMasuk)
      .putBoolean(KEY_ABSENSI_SUDAH_PULANG, absensi.sudahAbsenPulang)
      .apply()
  }

  // --- Riwayat Absensi ---
  fun getRiwayat(): List<RiwayatAbsensiItem> {
    val jsonStr = prefs.getString(KEY_RIWAYAT_JSON, null) ?: return emptyList()
    val result = mutableListOf<RiwayatAbsensiItem>()
    try {
      val jsonArray = JSONArray(jsonStr)
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val statusStr = obj.optString("status", StatusAbsensi.TEPAT_WAKTU.name)
        val status = try { StatusAbsensi.valueOf(statusStr) } catch (e: Exception) { StatusAbsensi.TEPAT_WAKTU }
        result.add(
          RiwayatAbsensiItem(
            id = obj.optString("id", UUID.randomUUID().toString()),
            hariTanggal = obj.optString("hariTanggal", ""),
            jamMasuk = obj.optString("jamMasuk", "-"),
            jamPulang = obj.optString("jamPulang", "-"),
            status = status,
            catatan = obj.optString("catatan", "")
          )
        )
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return result
  }

  fun simpanRiwayat(list: List<RiwayatAbsensiItem>) {
    try {
      val jsonArray = JSONArray()
      for (item in list) {
        val obj = JSONObject().apply {
          put("id", item.id)
          put("hariTanggal", item.hariTanggal)
          put("jamMasuk", item.jamMasuk)
          put("jamPulang", item.jamPulang)
          put("status", item.status.name)
          put("catatan", item.catatan)
        }
        jsonArray.put(obj)
      }
      prefs.edit().putString(KEY_RIWAYAT_JSON, jsonArray.toString()).apply()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  // --- Konfigurasi Admin ---
  fun getKonfigurasiAdmin(): KonfigurasiAdminDashboard {
    return KonfigurasiAdminDashboard(
      jamMasukKerja = prefs.getString(KEY_CFG_JAM_MASUK, "08:00") ?: "08:00",
      jamPulangKerja = prefs.getString(KEY_CFG_JAM_PULANG, "17:00") ?: "17:00",
      batasTerlambat = prefs.getString(KEY_CFG_BATAS_TERLAMBAT, "08:00") ?: "08:00",
      pengumuman = prefs.getString(KEY_CFG_PENGUMUMAN, "Pengumuman: Jam kerja resmi kantor dimulai pukul 08:00 WIB.")
        ?: "Pengumuman: Jam kerja resmi kantor dimulai pukul 08:00 WIB.",
      totalKaryawan = prefs.getInt(KEY_CFG_TOTAL_KARYAWAN, 0),
      totalHadir = prefs.getInt(KEY_CFG_TOTAL_HADIR, 0),
      totalTerlambat = prefs.getInt(KEY_CFG_TOTAL_TERLAMBAT, 0),
      totalBelumAbsen = prefs.getInt(KEY_CFG_TOTAL_BELUM_ABSEN, 0)
    )
  }

  fun simpanKonfigurasiAdmin(config: KonfigurasiAdminDashboard) {
    prefs.edit()
      .putString(KEY_CFG_JAM_MASUK, config.jamMasukKerja)
      .putString(KEY_CFG_JAM_PULANG, config.jamPulangKerja)
      .putString(KEY_CFG_BATAS_TERLAMBAT, config.batasTerlambat)
      .putString(KEY_CFG_PENGUMUMAN, config.pengumuman)
      .putInt(KEY_CFG_TOTAL_KARYAWAN, config.totalKaryawan)
      .putInt(KEY_CFG_TOTAL_HADIR, config.totalHadir)
      .putInt(KEY_CFG_TOTAL_TERLAMBAT, config.totalTerlambat)
      .putInt(KEY_CFG_TOTAL_BELUM_ABSEN, config.totalBelumAbsen)
      .apply()
  }

  // --- Admin Daftar Absensi ---
  fun getAdminDaftarAbsensi(): List<BarisAbsensiKaryawan> {
    val jsonStr = prefs.getString(KEY_ADMIN_ABSENSI_JSON, null) ?: return emptyList()
    val result = mutableListOf<BarisAbsensiKaryawan>()
    try {
      val jsonArray = JSONArray(jsonStr)
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val statusStr = obj.optString("status", StatusAbsensi.TEPAT_WAKTU.name)
        val status = try { StatusAbsensi.valueOf(statusStr) } catch (e: Exception) { StatusAbsensi.TEPAT_WAKTU }
        result.add(
          BarisAbsensiKaryawan(
            idKaryawan = obj.optString("idKaryawan", ""),
            nama = obj.optString("nama", ""),
            departemen = obj.optString("departemen", ""),
            jamMasuk = obj.optString("jamMasuk", "-"),
            jamPulang = obj.optString("jamPulang", "-"),
            status = status
          )
        )
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return result
  }

  fun simpanAdminDaftarAbsensi(list: List<BarisAbsensiKaryawan>) {
    try {
      val jsonArray = JSONArray()
      for (item in list) {
        val obj = JSONObject().apply {
          put("idKaryawan", item.idKaryawan)
          put("nama", item.nama)
          put("departemen", item.departemen)
          put("jamMasuk", item.jamMasuk)
          put("jamPulang", item.jamPulang)
          put("status", item.status.name)
        }
        jsonArray.put(obj)
      }
      prefs.edit().putString(KEY_ADMIN_ABSENSI_JSON, jsonArray.toString()).apply()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun tambahAtauUpdateAbsensiKaryawan(baris: BarisAbsensiKaryawan) {
    val list = getAdminDaftarAbsensi().toMutableList()
    val index = list.indexOfFirst { it.idKaryawan == baris.idKaryawan }
    if (index >= 0) {
      list[index] = baris
    } else {
      list.add(baris)
    }
    simpanAdminDaftarAbsensi(list)
  }

  fun resetAdminAbsensiHariIni() {
    prefs.edit().remove(KEY_ADMIN_ABSENSI_JSON).apply()
  }

  // --- Master Karyawan ---
  fun getMasterKaryawanList(): List<DataKaryawan> {
    val jsonStr = prefs.getString(KEY_MASTER_KARYAWAN_JSON, null) ?: return emptyList()
    val result = mutableListOf<DataKaryawan>()
    try {
      val jsonArray = JSONArray(jsonStr)
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val nama = obj.optString("nama", "")
        result.add(
          DataKaryawan(
            id = obj.optString("id", ""),
            nip = obj.optString("nip", ""),
            nama = nama,
            jenisKelamin = obj.optString("jenisKelamin", "-"),
            tempatTanggalLahir = obj.optString("tempatTanggalLahir", "-"),
            departemen = obj.optString("departemen", ""),
            avatarInisial = obj.optString("avatarInisial", if (nama.length >= 2) nama.take(2).uppercase() else "KA")
          )
        )
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return result
  }

  fun simpanMasterKaryawan(list: List<DataKaryawan>) {
    try {
      val jsonArray = JSONArray()
      for (item in list) {
        val obj = JSONObject().apply {
          put("id", item.id)
          put("nip", item.nip)
          put("nama", item.nama)
          put("jenisKelamin", item.jenisKelamin)
          put("tempatTanggalLahir", item.tempatTanggalLahir)
          put("departemen", item.departemen)
          put("avatarInisial", item.avatarInisial)
        }
        jsonArray.put(obj)
      }
      prefs.edit().putString(KEY_MASTER_KARYAWAN_JSON, jsonArray.toString()).apply()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
