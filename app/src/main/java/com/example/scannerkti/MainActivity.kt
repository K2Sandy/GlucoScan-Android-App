package com.example.scannerkti

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts


// --- PALET WARNA ---
val WarnaBackground = Color.White
val WarnaUtama      = Color(0xFF529BFA)
val WarnaPucat      = Color(0xFFE3F2FD)
val WarnaTeks       = Color.Black
val WarnaTeksAbu    = Color.Gray
val WarnaMerah      = Color(0xFFFF5252)
val WarnaKuning     = Color(0xFFFFD740)
val WarnaHijau      = Color(0xFF69F0AE)

// --- KAMUS BAHASA ---
object AppText {
    fun get(isIndo: Boolean, key: String): String = if (isIndo) IND[key] ?: key else ENG[key] ?: key

    private val IND = mapOf(
        "desc_home" to "Cek kandungan pada Label Makanan",
        "tips_title" to "Info Sehat",
        "profile_title" to "Menu Profil",
        "guide_btn" to "Panduan Pengguna",
        "settings_btn" to "Pengaturan Scanner",
        "history_btn" to "Riwayat Hasil Scan",
        "history_title" to "Riwayat Scan",
        "lang_btn" to "Bahasa / Language",
        "back" to "Kembali",
        "guide_1" to "Arahkan kamera ke tabel Nilai Gizi. Aplikasi akan mendeteksi Gula dan Karbohidrat.",
        "guide_2" to "Pastikan cahaya cukup agar angka terbaca jelas.",
        "settings_title" to "Profil Kesehatan",
        "settings_desc" to "Masukkan data lab Anda. Kosongkan jika tidak ada.",
        "input_fasting" to "Gula Darah Puasa (mg/dL)",
        "input_random" to "Gula Darah Sewaktu (mg/dL)",
        "input_hba1c" to "HbA1c (%)",
        "btn_analyze" to "Simpan & Analisis Profil",
        "result_normal" to "Normal (Mode Baca)",
        "result_pre" to "Pre-Diabetes (Waspada)",
        "result_diab" to "Diabetes (Batas Ketat)",
        "limit_info" to "Batas Gula Harian:",
        "reasons" to "Alasan Deteksi:",
        "reading_mode" to "Mode Baca",
        "scan_res" to "Hasil Scan",
        "sugar" to "Gula",
        "carb" to "Karbo",
        "status_safe" to "AMAN",
        "status_warning" to "HATI-HATI",
        "status_danger" to "BAHAYA",
        "msg_normal" to "Hasil Normal. Mode baca aktif.",
        "choose_lang" to "Pilih Bahasa",
        "cancel" to "Batal",
        "read_all" to "Baca Semua Teks",
        "check_safety" to "Cek Keamanan",
        "view_history" to "Lihat Riwayat",
        "scan_again" to "Scan Ulang",
        "warning_voice" to "Peringatan! Kandungan melebihi batas profil anda.",
        "safe_voice" to "Aman. Kandungan masih dalam batas wajar.",
        "reading_voice" to "Membacakan teks yang terdeteksi",
        "re_analyze" to "Analisis Ulang",
        "no_history" to "Belum ada riwayat scan.",
        "click_to_view" to "Klik untuk melihat foto label",
        "photo_label" to "Foto Label",
        "select_mode" to "Pilih Item",
        "cancel_select" to "Batal Pilih",
        "total_selected" to "Total Gabungan",
        "combined_status" to "Status Akumulasi:"
    )

    private val ENG = mapOf(
        "desc_home" to "Check content in Food Label.",
        "tips_title" to "Health Info",
        "profile_title" to "Profile Menu",
        "guide_btn" to "User Guide",
        "settings_btn" to "Scanner Settings",
        "history_btn" to "Scan History",
        "history_title" to "Scan History",
        "lang_btn" to "Language / Bahasa",
        "back" to "Back",
        "guide_1" to "Point camera at Nutrition Facts. App will detect Sugar and Carbohydrates.",
        "guide_2" to "Ensure adequate lighting for clear reading.",
        "settings_title" to "Health Profile",
        "settings_desc" to "Enter lab data. Leave empty if unavailable.",
        "input_fasting" to "Fasting Glucose (mg/dL)",
        "input_random" to "Random Glucose (mg/dL)",
        "input_hba1c" to "HbA1c (%)",
        "btn_analyze" to "Save & Analyze Profile",
        "result_normal" to "Normal (Reading Mode)",
        "result_pre" to "Pre-Diabetes (Warning)",
        "result_diab" to "Diabetes (Strict Limit)",
        "limit_info" to "Daily Sugar Limit:",
        "reasons" to "Detection Reasons:",
        "reading_mode" to "Reading Mode",
        "scan_res" to "Scan Result",
        "sugar" to "Sugar",
        "carb" to "Carb",
        "status_safe" to "SAFE",
        "status_warning" to "CAUTION",
        "status_danger" to "DANGER",
        "msg_normal" to "Normal Result. Reading mode active.",
        "choose_lang" to "Select Language",
        "cancel" to "Cancel",
        "read_all" to "Read All Text",
        "check_safety" to "Check Safety",
        "view_history" to "View History",
        "scan_again" to "Scan Again",
        "warning_voice" to "Warning! Content exceeds your profile limit.",
        "safe_voice" to "Safe. Content is within reasonable limits.",
        "reading_voice" to "Reading detected text",
        "re_analyze" to "Analyze Again",
        "no_history" to "No scan history yet.",
        "click_to_view" to "Click to view label photo",
        "photo_label" to "Label Photo",
        "select_mode" to "Select Items",
        "cancel_select" to "Cancel",
        "total_selected" to "Combined Total",
        "combined_status" to "Accumulated Status:"
    )
}

// --- DATA MODEL RIWAYAT ---
data class ScanHistoryItem(
    val time: String, // Format: HH:mm
    val sugar: Int,
    val carb: Int,
    val statusLevel: Int, // 0=Hijau, 1=Kuning, 2=Merah
    val imagePath: String // Lokasi file foto
)

// --- LOGIKA ANALISIS KESEHATAN ---
data class HealthAnalysisResult(val status: String, val limit: Int, val reasons: List<String>)

fun analyzeHealth(isIndo: Boolean, fastingStr: String, randomStr: String, hba1cStr: String): HealthAnalysisResult {
    val fasting = fastingStr.toIntOrNull()
    val random = randomStr.toIntOrNull()
    val hba1c = hba1cStr.replace(",", ".").toDoubleOrNull()
    val reasons = mutableListOf<String>()
    var isDiabetes = false
    var isPre = false

    if (fasting != null && fasting >= 126) { isDiabetes = true; if (isIndo) reasons.add("Gula Puasa anda $fasting mg/dL. Angka ini melebihi batas diabetes (126 mg/dL).") else reasons.add("Your Fasting Glucose is $fasting mg/dL. This exceeds the diabetes limit (126 mg/dL).") }
    if (random != null && random >= 200) { isDiabetes = true; if (isIndo) reasons.add("Gula Sewaktu anda $random mg/dL. Angka ini melebihi batas diabetes (200 mg/dL).") else reasons.add("Your Random Glucose is $random mg/dL. This exceeds the diabetes limit (200 mg/dL).") }
    if (hba1c != null && hba1c >= 6.5) { isDiabetes = true; if (isIndo) reasons.add("HbA1c anda $hba1c%. Angka ini melebihi batas diabetes (6.5%).") else reasons.add("Your HbA1c is $hba1c%. This exceeds the diabetes limit (6.5%).") }
    if (isDiabetes) return HealthAnalysisResult("Diabetes", 25, reasons)

    if (fasting != null && fasting in 100..125) { isPre = true; if (isIndo) reasons.add("Gula Puasa anda $fasting mg/dL. Masuk kategori waspada/pre-diabetes (100-125 mg/dL).") else reasons.add("Your Fasting Glucose is $fasting mg/dL. Falls into warning/pre-diabetes range (100-125 mg/dL).") }
    if (random != null && random in 140..199) { isPre = true; if (isIndo) reasons.add("Gula Sewaktu anda $random mg/dL. Masuk kategori waspada/pre-diabetes (140-199 mg/dL).") else reasons.add("Your Random Glucose is $random mg/dL. Falls into warning/pre-diabetes range (140-199 mg/dL).") }
    if (hba1c != null && hba1c >= 5.7 && hba1c < 6.5) { isPre = true; if (isIndo) reasons.add("HbA1c anda $hba1c%. Masuk kategori waspada/pre-diabetes (5.7% - 6.4%).") else reasons.add("Your HbA1c is $hba1c%. Falls into warning/pre-diabetes range (5.7% - 6.4%).") }
    if (isPre) return HealthAnalysisResult("Pre-Diabetes", 50, reasons)

    return HealthAnalysisResult("Normal", -1, listOf(if(isIndo) "Semua indikator dalam batas normal." else "All indicators are within normal range."))
}

// --- MANAGER PENYIMPANAN ---
class PreferenceManager(context: Context) {
    private val prefs = context.getSharedPreferences("settings_app", Context.MODE_PRIVATE)

    fun saveProfileData(fasting: String, random: String, hba1c: String, status: String, limit: Int, reasons: String) {
        prefs.edit().putString("p_fasting", fasting).putString("p_random", random).putString("p_hba1c", hba1c).putString("p_status", status).putInt("p_limit", limit).putString("p_reasons", reasons).putBoolean("p_has_data", true).apply()
    }

    // SIMPAN RIWAYAT (DENGAN SISTEM LIMIT 10 FOTO)
    @SuppressLint("NewApi")
    fun saveScanResult(sugar: Int, carb: Int, level: Int, imagePath: String) {
        val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        // Format: "HH:mm|Gula|Karbo|Level|PathFoto"
        val newItem = "$currentTime|$sugar|$carb|$level|$imagePath"
        val existingHistory = prefs.getString("scan_history", "") ?: ""

        var historyList = if (existingHistory.isEmpty()) mutableListOf() else existingHistory.split("#").toMutableList()

        // --- ALGORITMA FIFO (Limit 10) ---
        if (historyList.size >= 10) {
            // 1. Ambil item paling tua (paling bawah di list)
            val oldItem = historyList.last() // Item terakhir di list string
            val parts = oldItem.split("|")
            if (parts.size >= 5) {
                val oldPhotoPath = parts[4]
                // 2. Hapus file fotonya dari HP
                try { File(oldPhotoPath).delete() } catch (e: Exception) { e.printStackTrace() }
            }
            // 3. Hapus data teksnya dari list
            historyList.removeLast()
        }

        // 4. Masukkan data baru ke paling depan (indeks 0)
        historyList.add(0, newItem)

        // 5. Simpan kembali
        val newHistoryString = historyList.joinToString("#")
        prefs.edit().putString("scan_history", newHistoryString).apply()
    }

    fun getScanHistory(): List<ScanHistoryItem> {
        val rawData = prefs.getString("scan_history", "") ?: ""
        if (rawData.isEmpty()) return emptyList()

        return rawData.split("#").mapNotNull { itemStr ->
            val parts = itemStr.split("|")
            if (parts.size >= 5) {
                ScanHistoryItem(
                    time = parts[0],
                    sugar = parts[1].toIntOrNull() ?: 0,
                    carb = parts[2].toIntOrNull() ?: 0,
                    statusLevel = parts[3].toIntOrNull() ?: 0,
                    imagePath = parts[4]
                )
            } else null
        }
    }

    fun hasData() = prefs.getBoolean("p_has_data", false)
    fun getFasting() = prefs.getString("p_fasting", "") ?: ""
    fun getRandom() = prefs.getString("p_random", "") ?: ""
    fun getHba1c() = prefs.getString("p_hba1c", "") ?: ""
    fun getStatus() = prefs.getString("p_status", "Normal") ?: "Normal"
    fun getLimit() = prefs.getInt("p_limit", -1)
    fun getReasons() = prefs.getString("p_reasons", "") ?: ""
    fun saveIsIndonesian(isIndo: Boolean) = prefs.edit().putBoolean("key_is_indo", isIndo).apply()
    fun getIsIndonesian() = prefs.getBoolean("key_is_indo", true)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // ---------------------------

        setContent {
            MaterialTheme {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }
    var currentScreen by remember { mutableStateOf("home") }

    var isIndo by remember { mutableStateOf(prefs.getIsIndonesian()) }
    var sugarLimit by remember { mutableIntStateOf(prefs.getLimit()) }

    // --- VARIABEL BARU UTK DETEKSI MODE PILIH ---
    var isHistorySelectionMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
       
            if (currentScreen != "camera" ) {
                Surface(shadowElevation = 8.dp, color = Color.White) {
                    CenterAlignedTopAppBar(
                        title = { Text("Label Scanner", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = WarnaUtama, modifier = Modifier.padding(top = 12.dp)) },
                        actions = {
                            if (currentScreen != "home") {
                                IconButton(onClick = { currentScreen = "home" }, modifier = Modifier.padding(top = 12.dp)) {
                                    Icon(Icons.Default.Home, contentDescription = "Home", tint = WarnaUtama)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )
                }
            }
        },
    )
    { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(WarnaBackground).padding(top = innerPadding.calculateTopPadding())) {

            AnimatedContent(
                // ... transisi code ...
                targetState = currentScreen,
                label = "Screen Transition",
                transitionSpec = {
                    if (targetState == "camera") EnterTransition.None togetherWith ExitTransition.None
                    else (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.92f, animationSpec = tween(400))).togetherWith(fadeOut(animationSpec = tween(400)))
                },
                modifier = Modifier.fillMaxSize()
            ) { targetState ->
                when (targetState) {
                    "home" -> HomeScreen(isIndo)
                    "tips" -> TipsScreen(isIndo)
                    "profile" -> ProfileMenuScreen(
                        isIndo = isIndo,
                        onNavigateToGuide = { currentScreen = "guide" },
                        onNavigateToSettings = { if (prefs.hasData()) currentScreen = "settings_result" else currentScreen = "settings_form" },
                        onNavigateToHistory = { currentScreen = "history" },
                        onLanguageChanged = { newLang -> isIndo = newLang; prefs.saveIsIndonesian(newLang) }
                    )
                    "guide" -> GuideScreen(isIndo = isIndo, onBack = { currentScreen = "profile" })

                    // --- UPDATE PEMANGGILAN HISTORY SCREEN ---
                    "history" -> HistoryScreen(
                        isIndo = isIndo,
                        prefs = prefs,
                        onBack = {
                            currentScreen = "profile"
                            isHistorySelectionMode = false // Reset saat keluar
                        },
                        // Kirim status mode pilih ke MainAppScreen
                        onSelectionModeChanged = { isSelecting ->
                            isHistorySelectionMode = isSelecting
                        }
                    )

                    "settings_form" -> SettingsScreen(
                        isIndo = isIndo, prefs = prefs,
                        onSave = { limit -> sugarLimit = limit; currentScreen = "settings_result" },
                        onBack = { currentScreen = "profile" }
                    )
                    "settings_result" -> AnalysisResultScreen(
                        isIndo = isIndo, prefs = prefs,
                        onAnalyzeAgain = { currentScreen = "settings_form" },
                        onBack = { currentScreen = "profile" }
                    )
                    "camera" -> ScannerScreen(
                        isIndo = isIndo, prefs = prefs, sugarLimit = sugarLimit,
                        onBack = { currentScreen = "home" },
                        onViewHistory = { currentScreen = "history" }
                    )
                }
            }

            // --- LOGIKA NAVBAR (HILANG JIKA MODE PILIH AKTIF) ---
            if (currentScreen != "camera" && !isHistorySelectionMode) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp).padding(bottom = 45.dp).align(Alignment.BottomCenter)) {
                    Image(painter = painterResource(id = R.drawable.navbarbg_app), contentDescription = null, contentScale = ContentScale.FillWidth, modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth())
                    Row(modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { currentScreen = "tips" }, modifier = Modifier.size(60.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val warna = if(currentScreen == "tips") Color.White else Color.White.copy(alpha = 0.6f); Icon(Icons.Default.Star, contentDescription = null, tint = warna); Text("Tips", fontSize = 11.sp, color = warna, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(80.dp))
                        IconButton(onClick = { currentScreen = "profile" }, modifier = Modifier.size(60.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val isProfileActive = currentScreen == "profile" || currentScreen == "guide" || currentScreen == "history" || currentScreen.startsWith("settings"); val warna = if(isProfileActive) Color.White else Color.White.copy(alpha = 0.6f); Icon(Icons.Default.Person, contentDescription = null, tint = warna); Text(if(isIndo) "Profil" else "Profile", fontSize = 11.sp, color = warna, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                FloatingActionButton(onClick = { currentScreen = "camera" }, containerColor = WarnaUtama, contentColor = Color.White, shape = CircleShape, modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-80).dp).size(80.dp)) {
                    Icon(Icons.Default.Search, contentDescription = "Scan", modifier = Modifier.size(40.dp))
                }
            }
        }
    }
}

// --- HOME SCREEN ---
@Composable
fun HomeScreen(isIndo: Boolean) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp).offset(y = (-60).dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Image(painter = painterResource(id = R.drawable.logo_app), contentDescription = null, modifier = Modifier.size(180.dp).clip(RoundedCornerShape(20.dp)), contentScale = ContentScale.Fit)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Label Scanner", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = WarnaTeks)
        AnimatedContent(targetState = isIndo, label = "text_anim") { indo -> Text(AppText.get(indo, "desc_home"), color = WarnaTeksAbu, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp)) }
    }
}

// --- TIPS SCREEN ---
@Composable
fun TipsScreen(isIndo: Boolean) {
    val infoListIndo = listOf("5 Nama Lain Gula" to "Waspada nama samaran: Maltodextrin, Dextrose, Fructose, Corn Syrup, dan Barley Malt.", "Batas Gula Harian" to "Kemenkes menyarankan batas konsumsi gula harian maksimal 50 gram (4 sendok makan).", "Bahaya Minuman Kemasan" to "Satu botol minuman teh kemasan bisa mengandung hingga 20-30 gram gula.")
    val infoListEng = listOf("5 Other Names for Sugar" to "Watch out for: Maltodextrin, Dextrose, Fructose, Corn Syrup, and Barley Malt.", "Daily Sugar Limit" to "Health Ministry suggests a max daily intake of 50 grams (4 tablespoons).", "Packaged Drink Danger" to "A single bottle of sweet tea can contain up to 20-30 grams of sugar.")
    val currentList = if(isIndo) infoListIndo else infoListEng
    Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Star, contentDescription = null, tint = WarnaUtama, modifier = Modifier.size(80.dp))
        AnimatedContent(targetState = isIndo, label = "tips_title") { indo -> Text(AppText.get(indo, "tips_title"), color = WarnaTeks, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.height(20.dp))
        currentList.forEach { (judul, isi) ->
            Card(colors = CardDefaults.cardColors(containerColor = WarnaPucat), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(judul, fontWeight = FontWeight.Bold, color = WarnaTeks, fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(isi, textAlign = TextAlign.Center, color = WarnaTeksAbu, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        Spacer(modifier = Modifier.height(130.dp))
    }
}

// --- PROFILE MENU SCREEN ---
@Composable
fun ProfileMenuScreen(isIndo: Boolean, onNavigateToGuide: () -> Unit, onNavigateToSettings: () -> Unit, onNavigateToHistory: () -> Unit, onLanguageChanged: (Boolean) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(AppText.get(isIndo, "choose_lang"), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = WarnaUtama)
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { onLanguageChanged(true); showDialog = false }.background(if (isIndo) WarnaPucat else Color.Transparent).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = isIndo, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = WarnaUtama)); Spacer(modifier = Modifier.width(12.dp)); Text("Bahasa Indonesia", fontSize = 16.sp, fontWeight = if(isIndo) FontWeight.Bold else FontWeight.Normal)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { onLanguageChanged(false); showDialog = false }.background(if (!isIndo) WarnaPucat else Color.Transparent).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = !isIndo, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = WarnaUtama)); Spacer(modifier = Modifier.width(12.dp)); Text("English", fontSize = 16.sp, fontWeight = if(!isIndo) FontWeight.Bold else FontWeight.Normal)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(onClick = { showDialog = false }, border = BorderStroke(1.dp, WarnaUtama), shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth()) { Text(AppText.get(isIndo, "cancel"), color = WarnaUtama, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp).offset(y = (-10).dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(20.dp))
        Icon(Icons.Default.Person, contentDescription = null, tint = WarnaUtama, modifier = Modifier.size(80.dp))
        AnimatedContent(targetState = isIndo, label = "profile_title") { indo -> Text(AppText.get(indo, "profile_title"), color = WarnaTeks, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.height(40.dp))
        AnimatedContent(targetState = isIndo, label = "menu_buttons") { indo ->
            Column {
                MenuButton(icon = Icons.Default.Info, title = AppText.get(indo, "guide_btn"), onClick = onNavigateToGuide)
                Spacer(modifier = Modifier.height(16.dp))
                MenuButton(icon = Icons.Default.Settings, title = AppText.get(indo, "settings_btn"), onClick = onNavigateToSettings)
                Spacer(modifier = Modifier.height(16.dp))
                MenuButton(icon = Icons.Default.DateRange, title = AppText.get(indo, "history_btn"), onClick = onNavigateToHistory)
                Spacer(modifier = Modifier.height(16.dp))
                MenuButton(icon = Icons.Default.Face, title = AppText.get(indo, "lang_btn"), onClick = { showDialog = true })
            }
        }
    }
}

@Composable
fun MenuButton(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = WarnaPucat), modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = WarnaUtama, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WarnaTeks)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = WarnaTeksAbu)
        }
    }
}

// --- HISTORY SCREEN (FINAL FIX: PANEL AMAN & LAYOUT RAPI) ---
@Composable
fun HistoryScreen(
    isIndo: Boolean,
    prefs: PreferenceManager,
    onBack: () -> Unit,
    onSelectionModeChanged: (Boolean) -> Unit
) {
    val historyItems = remember { prefs.getScanHistory() }
    val sugarLimit = remember { prefs.getLimit() }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedIndices by remember { mutableStateOf(setOf<Int>()) }

    // Lapor perubahan mode ke Main Screen
    LaunchedEffect(isSelectionMode) {
        onSelectionModeChanged(isSelectionMode)
    }

    // Hitung Total (Akan 0 jika tidak ada yang dipilih)
    val totalSugar = remember(selectedIndices) { selectedIndices.sumOf { historyItems[it].sugar } }
    val totalCarb = remember(selectedIndices) { selectedIndices.sumOf { historyItems[it].carb } }

    // Logika Warna Gabungan
    val combinedLevel = remember(totalSugar, sugarLimit) {
        var level = 0
        if (selectedIndices.isEmpty()) return@remember -1 // -1 artinya Belum Pilih (Abu-abu)

        if (sugarLimit != -1) { if (totalSugar > sugarLimit) level = 2 else if (totalSugar >= (sugarLimit * 0.8)) level = 1 }
        else { if (totalSugar > 50) level = 2 else if (totalSugar > 30) level = 1 }
        level
    }

    // Dialog Foto
    if (selectedImageBitmap != null) {
        Dialog(onDismissRequest = { selectedImageBitmap = null }) {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column {
                    Box(modifier = Modifier.height(300.dp).fillMaxWidth().background(Color.Black)) {
                        Image(bitmap = selectedImageBitmap!!.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    }
                    Button(onClick = { selectedImageBitmap = null }, modifier = Modifier.padding(16.dp).fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = WarnaUtama)) { Text("Tutup / Close") }
                }
            }
        }
    }

    BackHandler {
        if (isSelectionMode) { isSelectionMode = false; selectedIndices = emptySet() }
        else onBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(top = 20.dp)) { // Padding samping & atas saja
            // HEADER
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable {
                    if(isSelectionMode) { isSelectionMode = false; selectedIndices = emptySet() } else onBack()
                }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = WarnaTeks)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(AppText.get(isIndo, "back"), fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = {
                    isSelectionMode = !isSelectionMode
                    if (!isSelectionMode) selectedIndices = emptySet()
                }) {
                    Text(
                        if (isSelectionMode) AppText.get(isIndo, "cancel_select") else AppText.get(isIndo, "select_mode"),
                        fontWeight = FontWeight.Bold, color = WarnaUtama
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = WarnaUtama, modifier = Modifier.size(70.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(AppText.get(isIndo, "history_title"), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = WarnaTeks)
            }
            Spacer(modifier = Modifier.height(20.dp))

            if (historyItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) { Text(AppText.get(isIndo, "no_history"), color = WarnaTeksAbu) }
            } else {
                // List diberi padding bawah besar supaya tidak ketutup Panel Total
                LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = if(isSelectionMode) 280.dp else 170.dp)) {
                    items(historyItems.size) { index ->
                        val item = historyItems[index]
                        val isSelected = selectedIndices.contains(index)
                        val baseColor = when(item.statusLevel) { 2 -> WarnaMerah; 1 -> Color(0xFFF57F17); else -> Color(0xFF2E7D32) }
                        val cardBorder = if (isSelectionMode && isSelected) BorderStroke(3.dp, WarnaUtama) else null
                        val containerColor = if (isSelectionMode && isSelected) WarnaPucat else baseColor.copy(alpha = 0.15f)

                        Card(
                            colors = CardDefaults.cardColors(containerColor = containerColor),
                            shape = RoundedCornerShape(16.dp),
                            border = cardBorder,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                .clickable {
                                    if (isSelectionMode) {
                                        val newSet = selectedIndices.toMutableSet()
                                        if (newSet.contains(index)) newSet.remove(index) else newSet.add(index)
                                        selectedIndices = newSet
                                    } else {
                                        val imgFile = File(item.imagePath)
                                        if (imgFile.exists()) {
                                            val bmp = BitmapFactory.decodeFile(imgFile.absolutePath)
                                            val matrix = Matrix().apply { postRotate(90f) }
                                            selectedImageBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                                        }
                                    }
                                }
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (isSelectionMode) {
                                    Checkbox(checked = isSelected, onCheckedChange = null, colors = CheckboxDefaults.colors(checkedColor = WarnaUtama))
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Card(colors = CardDefaults.cardColors(containerColor = baseColor), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(60.dp)) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(item.time, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("${AppText.get(isIndo, "sugar")}: ${item.sugar} g", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WarnaTeks)
                                    Text("${AppText.get(isIndo, "carb")}: ${item.carb} g", fontSize = 14.sp, color = WarnaTeksAbu)
                                    if (!isSelectionMode) Text(AppText.get(isIndo, "click_to_view"), fontSize = 10.sp, color = WarnaTeksAbu)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- PANEL TOTAL GABUNGAN (SELALU MUNCUL SAAT MODE PILIH) ---
        if (isSelectionMode) {
            // Tentukan Warna & Teks (Abu-abu jika kosong)
            val statusColor = when(combinedLevel) {
                2 -> WarnaMerah
                1 -> Color(0xFFF57F17)
                0 -> Color(0xFF2E7D32)
                else -> Color.Gray // Warna Netral
            }

            val statusText = if (selectedIndices.isEmpty()) {
                if(isIndo) "Pilih item di atas..." else "Select items above..."
            } else if (sugarLimit == -1) {
                when(combinedLevel) { 2 -> "TIDAK DIREKOMENDASIKAN"; 1 -> "KURANG DIREKOMENDASIKAN"; else -> "AMAN" }
            } else {
                when(combinedLevel) { 2 -> "BAHAYA"; 1 -> "WASPADA"; else -> "AMAN" }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    // FIX: Biar gak ketutup Navbar Android
                    .navigationBarsPadding()
                     // Tambah jarak dikit biar manis
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(AppText.get(isIndo, "total_selected"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WarnaTeks)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total Gula", color = WarnaTeksAbu, fontSize = 14.sp)
                            Text("$totalSugar g", color = if(selectedIndices.isEmpty()) Color.LightGray else statusColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Karbo", color = WarnaTeksAbu, fontSize = 14.sp)
                            Text("$totalCarb g", color = if(selectedIndices.isEmpty()) Color.LightGray else Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = statusColor), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (selectedIndices.isEmpty()) statusText else "${AppText.get(isIndo, "combined_status")} $statusText",
                            color = Color.White, fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

// Pastikan import ini ada di paling atas file


// --- SCANNER SCREEN (FINAL: IZIN OTOMATIS + MODE UMUM + FOTO) ---
@Composable
fun ScannerScreen(
    isIndo: Boolean,
    prefs: PreferenceManager,
    sugarLimit: Int,
    onBack: () -> Unit,
    onViewHistory: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isPreview = LocalInspectionMode.current

    // --- 1. LOGIKA IZIN KAMERA (PENTING! JANGAN DIHAPUS) ---
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
    // --------------------------------------------------------

    // Tampilan jika izin ditolak (Biar user gak bingung layar hitam)
    if (!hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Izin Kamera Diperlukan", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }, colors = ButtonDefaults.buttonColors(containerColor = WarnaUtama)) {
                    Text("Izinkan Kamera")
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onBack) { Text("Kembali", color = Color.Gray) }
            }
        }
        return // Stop di sini, jangan lanjut ke kode kamera
    }

    // --- 2. KODE KAMERA UTAMA ---
    var sugarAmount by remember { mutableStateOf(0) }
    var carbAmount by remember { mutableStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var fullText by remember { mutableStateOf("") }
    var alreadySaved by remember { mutableStateOf(false) }

    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = remember { Executors.newSingleThreadExecutor() }

    // LOGIKA LEVEL WARNA (MODE UMUM vs PERSONAL)
    val statusLevel = remember(sugarAmount, sugarLimit) {
        var level = 0
        if (sugarLimit != -1) {
            if (sugarAmount > sugarLimit) level = 2
            else if (sugarAmount >= (sugarLimit * 0.8)) level = 1
        } else {
            // Logika Mode Umum
            if (sugarAmount > 50) level = 2
            else if (sugarAmount > 30) level = 1
        }
        level
    }

    // LOGIKA AUTO-SAVE FOTO
    LaunchedEffect(isPaused) {
        if (isPaused && !alreadySaved) {
            val photoFile = File(context.filesDir, "scan_${System.currentTimeMillis()}.jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        prefs.saveScanResult(sugarAmount, carbAmount, statusLevel, photoFile.absolutePath)
                        alreadySaved = true
                    }
                }
                override fun onError(exc: ImageCaptureException) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        prefs.saveScanResult(sugarAmount, carbAmount, statusLevel, "")
                        alreadySaved = true
                    }
                }
            })
        }
    }

    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    if (!isPreview) {
        LaunchedEffect(Unit) {
            tts = TextToSpeech(context) { status -> if (status == TextToSpeech.SUCCESS) tts?.language = if(isIndo) Locale("id", "ID") else Locale.US }
        }
    }
    fun speak(text: String) { tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null) }

    if (!isPaused && !isPreview) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val pv = PreviewView(ctx)
                val providerFuture = ProcessCameraProvider.getInstance(ctx)
                providerFuture.addListener({
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build().also { it.setSurfaceProvider(pv.surfaceProvider) }
                    val analysis = ImageAnalysis.Builder().build()
                    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                    analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { proxy ->
                        val img = proxy.image
                        if (img != null && !isPaused) {
                            val inputImage = InputImage.fromMediaImage(img, proxy.imageInfo.rotationDegrees)
                            recognizer.process(inputImage).addOnSuccessListener { visionText ->
                                val txt = visionText.text.lowercase()
                                Regex("""(?:gula|sugar)\s*[:]*\s*(\d+)""").find(txt)?.let { sugarAmount = it.groupValues[1].toInt() }
                                Regex("""(?:karbohidrat|carbohydrate|carb|total carb)\s*(?:total)?\s*[:]*\s*(\d+)""").find(txt)?.let { carbAmount = it.groupValues[1].toInt() }

                                if (sugarAmount > 0 || carbAmount > 0) {
                                    fullText = visionText.text
                                    isPaused = true
                                }
                            }.addOnCompleteListener { proxy.close() }
                        } else proxy.close()
                    }
                    try {
                        provider.unbindAll()
                        provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis, imageCapture)
                    } catch (e: Exception) { e.printStackTrace() }

                }, ContextCompat.getMainExecutor(ctx))
                pv
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isPaused) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val s = 280.dp.toPx()
                drawRect(Color.White, topLeft = Offset((size.width-s)/2,(size.height-s)/2), size = Size(s,s), style = Stroke(3.dp.toPx()))
            }
            Text(if(sugarLimit == -1) "Mode Baca (Umum)" else "Mode Deteksi (Batas Gula: ${sugarLimit}g)", color = Color.White, modifier = Modifier.align(Alignment.Center).offset(y = (-180).dp), fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = onBack, modifier = Modifier.padding(16.dp).align(Alignment.TopStart).background(Color.Black.copy(0.5f), CircleShape)) {
            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
        }

        if (isPaused) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.8f)))
            Card(modifier = Modifier.align(Alignment.Center).padding(24.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(AppText.get(isIndo, "scan_res"), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatItem(AppText.get(isIndo, "sugar"), "$sugarAmount", "g", WarnaUtama)
                        StatItem(AppText.get(isIndo, "carb"), "$carbAmount", "g", Color(0xFFFFA726))
                    }
                    Spacer(modifier = Modifier.height(24.dp)); Divider(); Spacer(modifier = Modifier.height(16.dp))

                    val statusColor = when(statusLevel) { 2 -> WarnaMerah; 1 -> Color(0xFFF57F17); else -> Color(0xFF2E7D32) }

                    // TEKS STATUS (Mode Umum vs Personal)
                    val statusText = if (sugarLimit == -1) {
                        when(statusLevel) { 2 -> if(isIndo) "TIDAK DIREKOMENDASIKAN" else "NOT RECOMMENDED"; 1 -> if(isIndo) "KURANG DIREKOMENDASIKAN" else "LESS RECOMMENDED"; else -> if(isIndo) "AMAN (Konsumsi Wajar)" else "SAFE (Reasonable)" }
                    } else {
                        when(statusLevel) { 2 -> if(isIndo) "BAHAYA (Melebihi Batas)" else "DANGER (Exceeds Limit)"; 1 -> if(isIndo) "WASPADA (Hampir Batas)" else "WARNING (Near Limit)"; else -> if(isIndo) "AMAN (Dalam Batas)" else "SAFE (Within Limit)" }
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha=0.1f)), border = BorderStroke(1.dp, statusColor)) {
                        Text(statusText, color = statusColor, fontWeight = FontWeight.Black, fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(12.dp).fillMaxWidth())
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // TOMBOL 1
                    Button(onClick = { speak("${AppText.get(isIndo, "reading_voice")}: $fullText") }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = WarnaUtama)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null); Text(AppText.get(isIndo, "read_all"), modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // TOMBOL 2 (Cek Keamanan + Voice Mode Umum)
                    OutlinedButton(onClick = {
                        val msg = if (sugarLimit == -1) {
                            when(statusLevel) {
                                2 -> if(isIndo) "Produk ini tidak direkomendasikan karena kandungan gula sangat tinggi." else "Not recommended due to very high sugar."
                                1 -> if(isIndo) "Produk ini kurang direkomendasikan. Batasi konsumsi." else "Less recommended. Limit consumption."
                                else -> if(isIndo) "Aman. Konsumsi dalam batas wajar." else "Safe. Reasonable consumption."
                            }
                        } else {
                            when(statusLevel) {
                                2 -> if(isIndo) "Bahaya. Gula tinggi untuk kondisi anda." else "Danger. High sugar for your condition."
                                1 -> if(isIndo) "Waspada. Gula mendekati batas." else "Warning. Sugar near limit."
                                else -> if(isIndo) "Aman." else "Safe."
                            }
                        }
                        speak(msg)
                    }, modifier = Modifier.fillMaxWidth().height(50.dp), border = BorderStroke(2.dp, WarnaUtama)) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = WarnaUtama); Text(AppText.get(isIndo, "check_safety"), color = WarnaUtama, modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // TOMBOL 3
                    Button(onClick = onViewHistory, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = WarnaUtama)) {
                        Icon(Icons.Default.DateRange, contentDescription = null); Text(AppText.get(isIndo, "view_history"), modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { isPaused = false; sugarAmount=0; carbAmount=0; alreadySaved=false }) { Text(AppText.get(isIndo, "scan_again"), color = Color.Gray) }
                }
            }
        }
    }
}

// --- ANALYSIS RESULT SCREEN (HALAMAN HASIL PROFIL) ---
@Composable
fun AnalysisResultScreen(isIndo: Boolean, prefs: PreferenceManager, onAnalyzeAgain: () -> Unit, onBack: () -> Unit) {
    val resultStatus = prefs.getStatus()
    val resultLimit = prefs.getLimit()
    val resultReasons = prefs.getReasons()
    BackHandler { onBack() }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onBack() }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = WarnaTeks); Spacer(modifier = Modifier.width(8.dp)); Text(AppText.get(isIndo, "back"), fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        val cardColor = when(resultStatus) { "Diabetes" -> WarnaMerah.copy(alpha = 0.1f); "Pre-Diabetes" -> WarnaKuning.copy(alpha = 0.2f); else -> WarnaHijau.copy(alpha = 0.2f) }
        val titleColor = when(resultStatus) { "Diabetes" -> Color.Red; "Pre-Diabetes" -> Color(0xFFF57F17); else -> Color(0xFF2E7D32) }
        val displayStatus = when(resultStatus) { "Diabetes" -> AppText.get(isIndo, "result_diab"); "Pre-Diabetes" -> AppText.get(isIndo, "result_pre"); else -> AppText.get(isIndo, "result_normal") }

        Card(colors = CardDefaults.cardColors(containerColor = cardColor), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, titleColor.copy(alpha = 0.5f)), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(displayStatus, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = titleColor)
                Spacer(modifier = Modifier.height(12.dp)); Divider(color = titleColor.copy(alpha = 0.3f)); Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = titleColor, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text(AppText.get(isIndo, "limit_info"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WarnaTeks)
                }
                Text(text = if(resultLimit == -1) AppText.get(isIndo, "reading_mode") else "$resultLimit gram / day", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = WarnaTeks, modifier = Modifier.padding(start = 28.dp, top=4.dp))
                if (resultStatus != "Normal" && resultReasons.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp)); Text(AppText.get(isIndo, "reasons"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WarnaTeks); Text(resultReasons, fontSize = 13.sp, color = WarnaTeks, modifier = Modifier.padding(top = 4.dp))
                }
                if (resultStatus == "Normal") { Spacer(modifier = Modifier.height(8.dp)); Text(AppText.get(isIndo, "msg_normal"), fontSize = 13.sp, color = WarnaTeksAbu, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(onClick = onAnalyzeAgain, modifier = Modifier.fillMaxWidth().height(50.dp), border = BorderStroke(1.dp, WarnaUtama), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = WarnaUtama); Spacer(modifier = Modifier.width(8.dp)); Text(AppText.get(isIndo, "re_analyze"), fontWeight = FontWeight.Bold, color = WarnaUtama)
        }
    }
}

// --- KOMPONEN INPUT & STAT ITEM (TETAP) ---
@Composable
fun StatItem(label: String, value: String, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 14.sp, color = WarnaTeksAbu)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = color); Text(unit, fontSize = 14.sp, color = color, modifier = Modifier.padding(bottom = 6.dp, start=4.dp))
        }
    }
}

@Composable
fun InputCard(label: String, value: String, onValChange: (String) -> Unit, isDecimal: Boolean = false) {
    Column {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = WarnaTeksAbu)
        Spacer(modifier = Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = { if (it.length <= 5) onValChange(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, color = WarnaTeks),
            modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)).padding(14.dp)
        )
    }
}
// --- SETTINGS SCREEN FORMULIR (SUPAYA TIDAK HILANG) ---
@Composable
fun SettingsScreen(isIndo: Boolean, prefs: PreferenceManager, onSave: (Int) -> Unit, onBack: () -> Unit) {
    var fasting by remember { mutableStateOf(prefs.getFasting()) }
    var random by remember { mutableStateOf(prefs.getRandom()) }
    var hba1c by remember { mutableStateOf(prefs.getHba1c()) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).imePadding().verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onBack() }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = WarnaTeks); Spacer(modifier = Modifier.width(8.dp)); Text(AppText.get(isIndo, "back"), fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = WarnaUtama, modifier = Modifier.size(70.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(AppText.get(isIndo, "settings_title"), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = WarnaTeks)
        }
        Spacer(modifier = Modifier.height(20.dp))
        InputCard(label = AppText.get(isIndo, "input_fasting"), value = fasting, onValChange = { fasting = it })
        Spacer(modifier = Modifier.height(12.dp))
        InputCard(label = AppText.get(isIndo, "input_random"), value = random, onValChange = { random = it })
        Spacer(modifier = Modifier.height(12.dp))
        InputCard(label = AppText.get(isIndo, "input_hba1c"), value = hba1c, onValChange = { hba1c = it }, isDecimal = true)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val analysis = analyzeHealth(isIndo, fasting, random, hba1c)
                prefs.saveProfileData(fasting, random, hba1c, analysis.status, analysis.limit, analysis.reasons.joinToString("\n• ", prefix = "• "))
                onSave(analysis.limit)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WarnaUtama),
            shape = RoundedCornerShape(12.dp)
        ) { Text(AppText.get(isIndo, "btn_analyze"), fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.height(300.dp))
    }
}
// --- GUIDE SCREEN (SUPAYA TIDAK HILANG) ---
@Composable
fun GuideScreen(isIndo: Boolean, onBack: () -> Unit) {
    BackHandler { onBack() }
    Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onBack() }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = WarnaTeks); Spacer(modifier = Modifier.width(8.dp)); Text(AppText.get(isIndo, "back"), fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Icon(Icons.Default.Info, contentDescription = null, tint = WarnaUtama, modifier = Modifier.size(70.dp))
        Spacer(modifier = Modifier.height(8.dp))
        AnimatedContent(targetState = isIndo) { indo -> Text(AppText.get(indo, "guide_btn"), color = WarnaTeks, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.height(20.dp))
        Card(colors = CardDefaults.cardColors(containerColor = WarnaPucat), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) { Text(AppText.get(isIndo, "guide_1"), modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center, color = WarnaTeksAbu) }
        Card(colors = CardDefaults.cardColors(containerColor = WarnaPucat), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) { Text(AppText.get(isIndo, "guide_2"), modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center, color = WarnaTeksAbu) }
        Spacer(modifier = Modifier.height(130.dp))
    }
}

fun Modifier.clip(shape: androidx.compose.ui.graphics.Shape) = this.then(Modifier.graphicsLayer(shape = shape, clip = true))