package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.CameraConfig
import com.example.data.ObservationLog
import com.example.data.AiCompanion
import com.example.R
import androidx.compose.ui.res.painterResource
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.random.Random

// --- Localized Translation Mapping ---
val translations = mapOf(
    "MY" to mapOf(
        "app_title" to "Enjin Tingkah Laku NASADEV AI",
        "vision_tab" to "Pemerhatian AI",
        "family_tab" to "Keluarga AI",
        "logs_tab" to "Log & Audit",
        "settings_tab" to "Tetapan",
        "warning_title" to "AMARAN STATISTIK & PENAFIAN",
        "warning_body" to "Keputusan ini adalah pemerhatian statistik berdasarkan corak visual dan audio. Ia bukan diagnosis perubatan, bukan pengesan pembohongan, dan bukan penentu keadaan mental individu.",
        "start_analysis" to "MULA ANALISIS FRAME",
        "analyzing" to "Sedang Menganalisis...",
        "fps" to "FPS",
        "cpu" to "CPU",
        "ram" to "RAM",
        "storage" to "Stor",
        "emotion_obs" to "Pemerhatian Emosi",
        "behavior_obs" to "Pemerhatian Tingkah Laku",
        "animal_obs" to "Pemerhatian Haiwan",
        "active_cam" to "Kamera Aktif",
        "select_char" to "Pilih Watak Keluarga AI",
        "relationship" to "Hubungan",
        "mood" to "Mood Semasa",
        "chat_placeholder" to "Tulis mesej anda dalam dialek...",
        "send" to "Hantar",
        "wallet_balance" to "Baki E-Wallet",
        "crypto_price" to "Harga NASACOIN",
        "buy" to "Beli",
        "sell" to "Jual",
        "savings" to "Pelaburan & Tabung",
        "foodpanda" to "Foodpanda AI",
        "order_now" to "Pesan Sekarang",
        "movies" to "Pawagam AI",
        "video_call" to "Panggilan Video",
        "avatar_gen" to "Jana Avatar Keluarga",
        "gen_prompt" to "Masukkan penerangan imej...",
        "generate" to "Jana",
        "clear_logs" to "Kosongkan Log",
        "export_data" to "Eksport Audit Trail",
        "lang_toggle" to "Tukar Bahasa (English)",
        "theme_toggle" to "Tukar Mod Gelap/Cerah",
        "camera_sources" to "Senarai Sumber Kamera",
        "add_cam" to "Tambah Kamera",
        "no_logs" to "Tiada rekod pemerhatian lagi.",
        "simulation_mode" to "Mod Simulasi AI Pintar",
        "incoming_call" to "Panggilan Video Masuk..."
    ),
    "EN" to mapOf(
        "app_title" to "NASADEV AI Behavioral Engine",
        "vision_tab" to "AI Vision",
        "family_tab" to "AI Family",
        "logs_tab" to "Logs & Audit",
        "settings_tab" to "Settings",
        "warning_title" to "STATISTICAL WARNING & DISCLAIMER",
        "warning_body" to "This output is a statistical observation generated from visual patterns. It is not a diagnosis, not a lie detector, and not a determination of a person's mental state.",
        "start_analysis" to "START FRAME ANALYSIS",
        "analyzing" to "Analyzing...",
        "fps" to "FPS",
        "cpu" to "CPU",
        "ram" to "RAM",
        "storage" to "Storage",
        "emotion_obs" to "Emotion Observation",
        "behavior_obs" to "Behavioral Observation",
        "animal_obs" to "Animal Tracking",
        "active_cam" to "Active Camera",
        "select_char" to "Select AI Family Character",
        "relationship" to "Relation",
        "mood" to "Current Mood",
        "chat_placeholder" to "Type your message in dialect...",
        "send" to "Send",
        "wallet_balance" to "E-Wallet Balance",
        "crypto_price" to "NASACOIN Price",
        "buy" to "Buy",
        "sell" to "Sell",
        "savings" to "Savings & Investment",
        "foodpanda" to "Foodpanda AI",
        "order_now" to "Order Now",
        "movies" to "AI Movies",
        "video_call" to "Video Call",
        "avatar_gen" to "Generate Family Avatar",
        "gen_prompt" to "Enter image description...",
        "generate" to "Generate",
        "clear_logs" to "Clear Logs",
        "export_data" to "Export Audit Trail",
        "lang_toggle" to "Switch Language (Bahasa Melayu)",
        "theme_toggle" to "Switch Light/Dark Theme",
        "camera_sources" to "Camera Source Registry",
        "add_cam" to "Add Camera",
        "no_logs" to "No observation records yet.",
        "simulation_mode" to "Smart AI Simulation Mode",
        "incoming_call" to "Incoming Video Call..."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: NasadevViewModel) {
    val currentTab = remember { mutableStateOf(0) }
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val lang by viewModel.language.collectAsState()

    val localTranslations = translations[lang] ?: translations["MY"]!!

    val customColorScheme = if (isDarkMode) {
        darkColorScheme(
            primary = Color(0xFF10B981),
            secondary = Color(0xFF3B82F6),
            background = Color(0xFF0F172A),
            surface = Color(0xFF1E293B),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFFF1F5F9),
            onSurface = Color(0xFFF1F5F9)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF059669),
            secondary = Color(0xFF2563EB),
            background = Color(0xFFF8FAFC),
            surface = Color.White,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF0F172A),
            onSurface = Color(0xFF0F172A)
        )
    }

    MaterialTheme(
        colorScheme = customColorScheme
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = localTranslations["app_title"] ?: "NASADEV AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    actions = {
                        IconButton(onClick = { viewModel.toggleDarkMode() }) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Theme Toggle",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Image(
                            painter = painterResource(id = R.drawable.img_admin_avatar),
                            contentDescription = "Admin Avatar",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val items = listOf(
                        Triple(localTranslations["vision_tab"]!!, Icons.Default.Visibility, 0),
                        Triple(localTranslations["family_tab"]!!, Icons.Default.People, 1),
                        Triple(localTranslations["logs_tab"]!!, Icons.Default.History, 2),
                        Triple(localTranslations["settings_tab"]!!, Icons.Default.Settings, 3)
                    )
                    items.forEach { (label, icon, index) ->
                        NavigationBarItem(
                            selected = currentTab.value == index,
                            onClick = { currentTab.value = index },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_item_$index")
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                when (currentTab.value) {
                    0 -> VisionScreen(viewModel, localTranslations)
                    1 -> KeluargaScreen(viewModel, localTranslations)
                    2 -> LogsScreen(viewModel, localTranslations)
                    3 -> SettingsScreen(viewModel, localTranslations)
                }
            }
        }
    }
}

// ==========================================
// Helper TelemetryMeter Component
// ==========================================
@Composable
fun TelemetryMeter(label: String, value: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

// ==========================================
// 1. VISION TAB (AI OBSERVATION SCREEN)
// ==========================================
@Composable
fun VisionScreen(viewModel: NasadevViewModel, translations: Map<String, String>) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val activeCamera by viewModel.activeCamera.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val analysisResult by viewModel.analysisResult.collectAsState()

    val fps by viewModel.telemetryFps.collectAsState()
    val cpu by viewModel.telemetryCpu.collectAsState()
    val ram by viewModel.telemetryRam.collectAsState()
    val storage by viewModel.telemetryStorage.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    var showSkeleton by remember { mutableStateOf(true) }
    var showBoundingBoxes by remember { mutableStateOf(true) }
    var showHeatmap by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = translations["warning_title"] ?: "WARNING",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = translations["warning_body"] ?: "",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (hasCameraPermission && activeCamera?.type == "Internal") {
                        AndroidView(
                            factory = { ctx ->
                                PreviewView(ctx).apply {
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = androidx.camera.core.Preview.Builder().build().also {
                                        it.setSurfaceProvider(surfaceProvider)
                                    }
                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            imageCapture
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black)
                                .drawBehind {
                                    if (showHeatmap) {
                                        drawRect(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color.Red.copy(alpha = 0.25f),
                                                    Color.Yellow.copy(alpha = 0.15f),
                                                    Color.Transparent
                                                ),
                                                center = Offset(size.width * 0.5f, size.height * 0.4f),
                                                radius = size.width * 0.4f
                                            )
                                        )
                                        drawRect(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color.Blue.copy(alpha = 0.25f),
                                                    Color.Green.copy(alpha = 0.1f),
                                                    Color.Transparent
                                                ),
                                                center = Offset(size.width * 0.3f, size.height * 0.6f),
                                                radius = size.width * 0.3f
                                            )
                                        )
                                    }

                                    if (showBoundingBoxes) {
                                        drawRect(
                                            color = Color(0xFF10B981),
                                            topLeft = Offset(size.width * 0.2f, size.height * 0.15f),
                                            size = androidx.compose.ui.geometry.Size(
                                                size.width * 0.6f,
                                                size.height * 0.7f
                                            ),
                                            style = Stroke(width = 3f)
                                        )
                                    }

                                    if (showSkeleton) {
                                        val points = listOf(
                                            Offset(size.width * 0.5f, size.height * 0.25f),
                                            Offset(size.width * 0.5f, size.height * 0.45f),
                                            Offset(size.width * 0.35f, size.height * 0.48f),
                                            Offset(size.width * 0.65f, size.height * 0.48f),
                                            Offset(size.width * 0.32f, size.height * 0.65f),
                                            Offset(size.width * 0.68f, size.height * 0.65f),
                                            Offset(size.width * 0.4f, size.height * 0.8f),
                                            Offset(size.width * 0.6f, size.height * 0.8f)
                                        )

                                        points.forEach { pt ->
                                            drawCircle(color = Color(0xFF3B82F6), radius = 10f, center = pt)
                                        }

                                        drawLine(Color(0xFF3B82F6), points[0], points[1], strokeWidth = 4f)
                                        drawLine(Color(0xFF3B82F6), points[1], points[2], strokeWidth = 4f)
                                        drawLine(Color(0xFF3B82F6), points[1], points[3], strokeWidth = 4f)
                                        drawLine(Color(0xFF3B82F6), points[2], points[4], strokeWidth = 4f)
                                        drawLine(Color(0xFF3B82F6), points[3], points[5], strokeWidth = 4f)
                                        drawLine(Color(0xFF3B82F6), points[1], points[6], strokeWidth = 4f)
                                        drawLine(Color(0xFF3B82F6), points[1], points[7], strokeWidth = 4f)
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                        Text(
                                            translations["simulation_mode"] ?: "SIMULATION",
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        activeCamera?.name ?: "INTERNAL CAMERA",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .border(1.dp, Color.Yellow.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = "FACE MESH: ACTIVE (BLINKS: ${Random.nextInt(12, 18)}/min)",
                                        color = Color.Yellow,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "REC ⏺",
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        "RESOLVED: RTSP://UVC_WIFI_cam_V380",
                                        color = Color.Green,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    val infiniteTransition = rememberInfiniteTransition()
                    val lineOffsetY by infiniteTransition.animateFloat(
                        initialValue = 0.1f,
                        targetValue = 0.9f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2500, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.005f)
                            .align(Alignment.TopCenter)
                            .offset(y = 280.dp * lineOffsetY)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, Color(0xFF10B981), Color.Transparent)
                                )
                            )
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = { showSkeleton = !showSkeleton },
                        modifier = Modifier
                            .background(
                                if (showSkeleton) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else Color.Transparent,
                                CircleShape
                            )
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Accessibility,
                            contentDescription = "Skeleton Overlay",
                            tint = if (showSkeleton) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    IconButton(
                        onClick = { showBoundingBoxes = !showBoundingBoxes },
                        modifier = Modifier
                            .background(
                                if (showBoundingBoxes) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else Color.Transparent,
                                CircleShape
                            )
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.SelectAll,
                            contentDescription = "Bounding Boxes Overlay",
                            tint = if (showBoundingBoxes) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    IconButton(
                        onClick = { showHeatmap = !showHeatmap },
                        modifier = Modifier
                            .background(
                                if (showHeatmap) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else Color.Transparent,
                                CircleShape
                            )
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Grain,
                            contentDescription = "Heatmap Overlay",
                            tint = if (showHeatmap) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TelemetryMeter(translations["fps"]!!, "$fps", Icons.Default.Speed, MaterialTheme.colorScheme.primary)
                    TelemetryMeter(translations["cpu"]!!, "$cpu%", Icons.Default.Memory, Color.Red)
                    TelemetryMeter(translations["ram"]!!, String.format("%.1f GB", ram), Icons.Default.Flip, Color.Blue)
                    TelemetryMeter(translations["storage"]!!, String.format("%.1f GB", storage), Icons.Default.Storage, Color.Yellow)
                }
            }
        }

        item {
            Button(
                onClick = {
                    val mockBitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
                    viewModel.runVisionAnalysis(mockBitmap)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_analysis_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                enabled = !isAnalyzing
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(translations["analyzing"] ?: "Analyzing...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Camera, contentDescription = "Capture Camera Frame")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(translations["start_analysis"] ?: "ANALYZE FRAME", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI REAL-TIME ANOMALY OBSERVATION REPORT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (analysisResult != null) {
                        Text(
                            text = analysisResult ?: "",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Analytics,
                                contentDescription = "Report Empty",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Tiada data analisis aktif. Sila klik 'Mula Analisis Frame' untuk mencetuskan pengamatan sensor AI.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. KELUARGA AI (DIALECTS, CHAT & GAMES TAB)
// ==========================================
@Composable
fun KeluargaScreen(viewModel: NasadevViewModel, translations: Map<String, String>) {
    val companions by viewModel.companions.collectAsState()
    val selectedCompanion by viewModel.selectedCompanion.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isChatting by viewModel.isChatting.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) }

    // Auto Research & Plan AI Companion Creator states
    var showCompanionCreator by remember { mutableStateOf(false) }
    var companionIdeaInput by remember { mutableStateOf("") }
    val isGeneratingCompanion by viewModel.isGeneratingCompanion.collectAsState()
    var companionCreationStatus by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = translations["select_char"] ?: "Pilih Watak",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )

        // Collapsible AI Character Auto Builder Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCompanionCreator = !showCompanionCreator },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Auto Creator Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Auto-Jana & Rancang Watak AI (Gemini 3.5)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        imageVector = if (showCompanionCreator) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand Collapse",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                if (showCompanionCreator) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Masukkan idea watak impian anda (cth: 'Adik perempuan yang manja dan kelakar dari Kedah' atau 'Pakcik suka berseloroh dari Kelantan'). Gemini akan melakukan research budaya, merancang personaliti secara auto, dan mendaftarkannya ke sistem bersuara.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = companionIdeaInput,
                        onValueChange = { companionIdeaInput = it },
                        placeholder = { Text("cth: Kakak sulung tegas tapi baik hati dari Terengganu", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("auto_companion_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                        ),
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    if (isGeneratingCompanion) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            Text(
                                text = "Sedang menyelidik dialek & merancang profil watak...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                if (companionIdeaInput.isNotBlank()) {
                                    viewModel.autoCreateAiCharacter(companionIdeaInput) { msg ->
                                        companionCreationStatus = msg
                                        if (msg.contains("Berjaya")) {
                                            companionIdeaInput = ""
                                        }
                                    }
                                } else {
                                    companionCreationStatus = "Sila tulis idea watak terlebih dahulu."
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("auto_companion_build_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Rancang & Jana Watak Pintar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    if (companionCreationStatus.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = companionCreationStatus,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (companionCreationStatus.contains("Berjaya")) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            companions.forEach { companion ->
                val isSelected = selectedCompanion?.id == companion.id
                Column(
                    modifier = Modifier
                        .width(76.dp)
                        .clickable { viewModel.selectCompanion(companion) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                2.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = companion.name.take(2).uppercase(),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        companion.name,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                    Text(
                        "(${companion.role})",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedCompanion != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "WATAK: ${selectedCompanion!!.name} (${translations["relationship"]!!}: ${selectedCompanion!!.relationshipScore}%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "${translations["mood"]!!}: ${selectedCompanion!!.currentMood}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        selectedCompanion!!.backgroundStory,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 14.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val subTabs = listOf(
                    "Sembang Dialek" to 0,
                    "Pelaburan NASACOIN" to 1,
                    "Foodpanda AI" to 2,
                    "Pawagam AI" to 3,
                    "Panggilan Video" to 4,
                    "Avatar Generator" to 5
                )
                subTabs.forEach { (title, idx) ->
                    val active = activeSubTab == idx
                    Box(
                        modifier = Modifier
                            .background(
                                if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(20.dp)
                            )
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .clickable { activeSubTab = idx }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            title,
                            color = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (activeSubTab) {
                    0 -> ChatDialectView(viewModel, chatHistory, isChatting, translations)
                    1 -> CryptoInvestmentView(viewModel, translations)
                    2 -> FoodpandaSimView(viewModel, translations)
                    3 -> MoviesAppView(viewModel)
                    4 -> VideoCallSimView(selectedCompanion!!, translations)
                    5 -> AvatarGeneratorView(viewModel, translations)
                }
            }

        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.People,
                    contentDescription = "Empty profiles",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Sila pilih mana-mana watak Keluarga AI di atas untuk memulakan sembang dialek Kelantan, Kedah, Terengganu, bermain permainan, atau panggilan video.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ChatDialectView(
    viewModel: NasadevViewModel,
    chatHistory: List<ChatMessage>,
    isChatting: Boolean,
    translations: Map<String, String>
) {
    var textMessage by remember { mutableStateOf("") }
    val selectedCompanion by viewModel.selectedCompanion.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val reversedList = chatHistory.reversed()
                items(reversedList) { chat ->
                    val isUser = chat.sender == "User"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (!isUser) {
                            // Companion Avatar Circle with initials
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = chat.sender.take(2).uppercase(),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Card(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 0.dp,
                                bottomEnd = if (isUser) 0.dp else 16.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = if (!isUser) BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)) else null,
                            modifier = Modifier.widthIn(max = 240.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = chat.sender,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (isUser) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = chat.message,
                                    fontSize = 13.sp,
                                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        if (isUser) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Image(
                                painter = painterResource(id = R.drawable.img_admin_avatar),
                                contentDescription = "Admin Avatar",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Speaker icon for Voice Actor replay
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    viewModel.speakText(chat.message, selectedCompanion?.dialect ?: "Kelantan")
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Mainkan Suara",
                                    tint = if (isSpeaking) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (isChatting) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Abe Wan/Mek Nab sedang menaip...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textMessage,
                onValueChange = { textMessage = it },
                placeholder = { Text(translations["chat_placeholder"]!!, fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                maxLines = 2
            )

            FloatingActionButton(
                onClick = {
                    if (textMessage.isNotBlank()) {
                        viewModel.sendChatMessage(textMessage)
                        textMessage = ""
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("chat_send_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send Message", tint = Color.White)
            }
        }
    }
}

@Composable
fun CryptoInvestmentView(viewModel: NasadevViewModel, translations: Map<String, String>) {
    val savings by viewModel.savingsBalance.collectAsState()
    val holdings by viewModel.nasacoinHoldings.collectAsState()
    val coinPrice by viewModel.nasacoinPrice.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Simulated Account Balance HUD", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(translations["wallet_balance"]!!, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(String.format("RM %.2f", savings), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Holdings: NASACOIN", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(String.format("%.4f COIN", holdings), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(translations["crypto_price"]!!, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, contentDescription = "Crypto price trending", tint = Color.Green, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(String.format("RM %.2f", coinPrice), fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.Green)
                }
                Text("Fluctuating live in realtime", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.buyNasacoin(100.0) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("BUY RM100")
                    }

                    Button(
                        onClick = { viewModel.sellNasacoin(0.1) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SELL 0.1 COIN")
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp)) {
                Icon(Icons.Default.Info, contentDescription = "Tip details", tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "AI Tip: Selalu rujuk Abe Wan untuk petua simpanan. Jangan melabur melebihi kemampuan bajet bulanan anda!",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun FoodpandaSimView(viewModel: NasadevViewModel, translations: Map<String, String>) {
    val currentOrder by viewModel.foodpandaOrder.collectAsState()

    val foodList = listOf(
        com.example.ui.FoodItem("Nasi Lemak Ayam Berempah", 14.50, "Nasi lemak santan harum dengan ayam goreng rangup berempah Kelantan."),
        com.example.ui.FoodItem("Roti Canai Garing (2 Keping)", 5.00, "Roti canai tradisi garing di luar lembut di dalam bersama kuah dhal Kedah."),
        com.example.ui.FoodItem("Laksa Terengganu Asli", 11.00, "Laksa kuah lemak pekat putih Terengganu disajikan dengan cili potong asli.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("AI Food Delivery Foodpanda Simulator", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        if (currentOrder != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("PESANAN AKTIF", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        Text(String.format("RM %.2f", currentOrder!!.totalPrice), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(currentOrder!!.itemName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Status: ${currentOrder!!.status}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = currentOrder!!.progress,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        foodList.forEach { food ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(food.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(String.format("RM %.2f", food.price), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(food.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.orderFoodFromPanda(food.name, food.price) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(translations["order_now"]!!)
                    }
                }
            }
        }
    }
}

@Composable
fun MoviesAppView(viewModel: NasadevViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("AI Local Movies List Showcase", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        viewModel.movies.forEach { movie ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(70.dp, 100.dp)
                            .background(Color(movie.posterPlaceholderColor), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("CINEMA", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(movie.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(movie.rating, color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Text(movie.genre, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(movie.synopsis, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), lineHeight = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun VideoCallSimView(companion: AiCompanion, translations: Map<String, String>) {
    var isRinging by remember { mutableStateOf(false) }
    var inCall by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isRinging && !inCall) {
            Icon(
                Icons.Default.VideoCall,
                contentDescription = "Video Call Main Button",
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Sembang Video Bersama ${companion.name}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                "Saksikan simulasi pautan video di mana ${companion.name} bersedia berbual.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { isRinging = true },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("MULAKAN PANGGILAN")
            }
        } else if (isRinging) {
            Text(translations["incoming_call"] ?: "Incoming Call...", fontSize = 20.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    companion.name.take(2).uppercase(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Button(
                    onClick = {
                        isRinging = false
                        inCall = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Answer Video Call", tint = Color.White)
                }

                Button(
                    onClick = { isRinging = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = "Decline Video Call", tint = Color.White)
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Face, contentDescription = "Partner Avatar", modifier = Modifier.size(120.dp), tint = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "${companion.name} (${companion.dialect})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                "Voice-Over AI Dialect: CONNECTED",
                                color = Color.Green,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(80.dp, 120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .border(1.5.dp, Color.White, RoundedCornerShape(8.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_admin_avatar),
                            contentDescription = "You Avatar Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                        )
                        Text(
                            text = "Anda (You)",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(4.dp)
                        )
                    }

                    Button(
                        onClick = { inCall = false },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "Hang Up Video Call", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AvatarGeneratorView(viewModel: NasadevViewModel, translations: Map<String, String>) {
    val generatedImage by viewModel.generatedImage.collectAsState()
    val isGeneratingImage by viewModel.isGeneratingImage.collectAsState()
    val errorMsg by viewModel.imageGenError.collectAsState()

    var promptInput by remember { mutableStateOf("") }
    var selectedSize by remember { mutableStateOf("1K") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(translations["avatar_gen"] ?: "Jana Avatar Keluarga", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = promptInput,
            onValueChange = { promptInput = it },
            label = { Text(translations["gen_prompt"] ?: "Masukkan penerangan imej...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Column {
            Text("Image Size Choices (Veo Affordance):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("1K", "2K", "4K").forEach { size ->
                    val active = selectedSize == size
                    Button(
                        onClick = { selectedSize = size },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (active) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(size)
                    }
                }
            }
        }

        Button(
            onClick = {
                if (promptInput.isNotBlank()) {
                    viewModel.generateImage(promptInput, selectedSize)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isGeneratingImage && promptInput.isNotBlank()
        ) {
            if (isGeneratingImage) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Menjana Gambar...")
            } else {
                Icon(Icons.Default.Image, contentDescription = "Generate Avatar Button")
                Spacer(modifier = Modifier.width(8.dp))
                Text(translations["generate"] ?: "Jana")
            }
        }

        if (errorMsg != null) {
            Text(errorMsg!!, color = Color.Red, fontSize = 12.sp)
        }

        if (generatedImage != null) {
            Text("Selesai dijana! Avatar baharu anda:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Image(
                bitmap = generatedImage!!.asImageBitmap(),
                contentDescription = "Generated Avatar",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

// ==========================================
// 3. LOGS TAB (ROOM OBSERVATION HISTORY)
// ==========================================
@Composable
fun LogsScreen(viewModel: NasadevViewModel, translations: Map<String, String>) {
    val logs by viewModel.logs.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }
    var exportContent by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "AI VISION AUDIT TRAIL LOGS",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = {
                    val csv = StringBuilder("ID,Timestamp,Type,Label,Confidence\n")
                    val json = StringBuilder("[\n")
                    logs.forEachIndexed { idx, log ->
                        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                        csv.append("${log.id},\"$date\",\"${log.type}\",\"${log.label}\",${log.confidence}\n")
                        json.append("  {\n")
                        json.append("    \"id\": ${log.id},\n")
                        json.append("    \"timestamp\": \"$date\",\n")
                        json.append("    \"type\": \"${log.type}\",\n")
                        json.append("    \"label\": \"${log.label}\",\n")
                        json.append("    \"confidence\": ${log.confidence}\n")
                        json.append("  }${if (idx == logs.size - 1) "" else ","}\n")
                    }
                    json.append("]")
                    exportContent = "=== CSV EXPORT ===\n\n$csv\n\n=== JSON EXPORT ===\n\n$json"
                    showExportDialog = true
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Export Audit Data")
                }

                IconButton(onClick = { viewModel.clearLogHistory() }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Logs Sweep", tint = Color.Red)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (logs.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.FolderOpen,
                    contentDescription = "Empty log folder",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    translations["no_logs"] ?: "No logs.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(logs) { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val icon = when (log.type) {
                                        "Emotion" -> Icons.Default.Face
                                        "Behavior" -> Icons.Default.AccessibilityNew
                                        else -> Icons.Default.Pets
                                    }
                                    Icon(icon, contentDescription = log.type, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        log.label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Badge(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) {
                                    Text("${log.confidence}% CONFIDENT", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp)),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                log.rawNotes,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                lineHeight = 16.sp,
                                maxLines = 4
                            )
                        }
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Eksport Audit Trail Data") },
            text = {
                Box(modifier = Modifier.height(300.dp)) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(exportContent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("TUTUP")
                }
            }
        )
    }
}

// ==========================================
// 4. SETTINGS TAB (REGISTRY & DISCLAIMERS)
// ==========================================
@Composable
fun SettingsScreen(viewModel: NasadevViewModel, translations: Map<String, String>) {
    val language by viewModel.language.collectAsState()
    val cameras by viewModel.cameras.collectAsState()

    var showAddCamDialog by remember { mutableStateOf(false) }
    var newCamName by remember { mutableStateOf("") }
    var newCamType by remember { mutableStateOf("RTSP") }
    var newCamUrl by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("REGISTRY & SETTINGS", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
        }

        // Admin Profile Section displaying custom avatar image
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_admin_avatar),
                        contentDescription = "Admin Avatar",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Admin Utama (Nasadev Master)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Bahasa: Bahasa Melayu & English",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text("SISTEM AKTIF", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }

        // ThemeSwitcher Component
        item {
            ThemeSwitcher(viewModel = viewModel)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Bahasa Utama / Primary Language", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Active: ${if (language == "MY") "Bahasa Melayu" else "English"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }

                    Button(
                        onClick = { viewModel.setLanguage(if (language == "MY") "EN" else "MY") },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(translations["lang_toggle"] ?: "Toggle Language")
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(translations["camera_sources"] ?: "Kamera", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Button(
                    onClick = { showAddCamDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Camera Feed", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(translations["add_cam"] ?: "Tambah", fontSize = 12.sp)
                }
            }
        }

        items(cameras) { cam ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(cam.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("URL: ${cam.url}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    IconButton(onClick = { viewModel.deleteCamera(cam.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Camera Feed", tint = Color.Red)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("ENGINE LEGAL DISCLAIMER & CAPABILITIES", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Lie Detection: Strictly prohibited. Visual tracking of micro-expressions and body posture should only be used as general statistical indices.\n\n" +
                               "2. Sanity, Mental Diagnoses & Legal Determinations: Strictly prohibited. This engine must not and cannot replace standard psychiatric, cognitive or medical diagnoses.\n\n" +
                               "3. Accuracy: All confidence metrics are statistical estimations. Results are strictly approximations and should be evaluated alongside proper professional judgment.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    if (showAddCamDialog) {
        AlertDialog(
            onDismissRequest = { showAddCamDialog = false },
            title = { Text("Tambah Sumber Kamera") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newCamName,
                        onValueChange = { newCamName = it },
                        label = { Text("Nama Kamera (e.g. UAV Drone 1)") }
                    )
                    OutlinedTextField(
                        value = newCamUrl,
                        onValueChange = { newCamUrl = it },
                        label = { Text("RTSP / HTTP Address URL") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newCamName.isNotBlank() && newCamUrl.isNotBlank()) {
                        viewModel.addCustomCamera(newCamName, newCamType, newCamUrl)
                        newCamName = ""
                        newCamUrl = ""
                        showAddCamDialog = false
                    }
                }) {
                    Text("SIMPAN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCamDialog = false }) {
                    Text("BATAL")
                }
            }
        )
    }
}

@Composable
fun ThemeSwitcher(viewModel: NasadevViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Theme Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Aplikasi Tema / App Theme",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 'Elegant Dark' Option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isDarkMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                        )
                        .border(
                            width = 2.dp,
                            color = if (isDarkMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { if (!isDarkMode) viewModel.toggleDarkMode() }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = "Elegant Dark",
                            tint = if (isDarkMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Elegant Dark",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isDarkMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // 'Clean Light' Option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (!isDarkMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                        )
                        .border(
                            width = 2.dp,
                            color = if (!isDarkMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { if (isDarkMode) viewModel.toggleDarkMode() }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LightMode,
                            contentDescription = "Clean Light",
                            tint = if (!isDarkMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Clean Light",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (!isDarkMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
