package com.example.model

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.network.BackendApiClient
import com.example.network.PingResult

class JaringanKantorManager(private val context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("server_config_pref", Context.MODE_PRIVATE)

  val radiusMaksimalKantorMeter: Int = 100
  var namaWifiKantor: String by mutableStateOf("")

  private fun loadInitialServerUrl(): String {
    val saved = prefs.getString("key_server_url", null)
    if (saved.isNullOrBlank() || saved == "http://10.0.2.2:3000/" || saved == "http://10.0.2.2:3000") {
      return "http://192.168.0.10:3000/"
    }
    return saved
  }

  var serverXamppUrl by mutableStateOf(loadInitialServerUrl())
    private set

  var isDalamJaringanKantor by mutableStateOf(true)
    private set
  var jarakDariKantorMeter by mutableIntStateOf(15)
    private set
  var isTestingKoneksi by mutableStateOf(false)
    private set
  var lastPingResult by mutableStateOf<PingResult?>(null)
    private set

  init {
    prefs.edit().putString("key_server_url", serverXamppUrl).apply()
    BackendApiClient.setServerUrl(serverXamppUrl)
  }

  fun updateServerUrl(urlBaru: String) {
    serverXamppUrl = urlBaru.trim()
    prefs.edit().putString("key_server_url", serverXamppUrl).apply()
    BackendApiClient.setServerUrl(serverXamppUrl)
    lastPingResult = null
  }

  suspend fun tesKoneksiXampp(): PingResult {
    isTestingKoneksi = true
    return try {
      val result = BackendApiClient.pingServer()
      val ping = result.getOrElse { err ->
        PingResult(
          isSuccess = false,
          statusCode = 0,
          responseTimeMs = 0,
          message = err.localizedMessage ?: "Tidak dapat terhubung ke server"
        )
      }
      lastPingResult = ping
      ping
    } finally {
      isTestingKoneksi = false
    }
  }

  fun isTerhubungKoneksi(): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val activeNetwork = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
  }

  fun setMasukKantor() {
    isDalamJaringanKantor = true
    jarakDariKantorMeter = 15
  }

  fun setJauhDariKantor(jarakMeter: Int = 1200) {
    isDalamJaringanKantor = false
    jarakDariKantorMeter = jarakMeter
  }

  fun toggleModeSimulasi() {
    if (isDalamJaringanKantor) setJauhDariKantor(1200) else setMasukKantor()
  }
}
