package com.daviddf.geeklab.ui.apps

import android.app.Application
import android.app.usage.StorageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.TrafficStats
import android.os.Build
import android.os.Process
import android.os.storage.StorageManager
import android.text.format.Formatter
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import java.text.SimpleDateFormat
import java.util.Date

data class SignatureDetail(
    val subject: String? = null,
    val issuer: String? = null,
    val issueDate: String? = null,
    val expiryDate: String? = null,
    val type: String? = null,
    val version: String? = null,
    val serialNumber: String? = null,
    val md5: String? = null,
    val sha1: String? = null,
    val sha256: String? = null,
    val sha384: String? = null,
    val sha512: String? = null,
    val algorithm: String? = null,
    val oid: String? = null,
    val signature: String? = null,
    val publicKeyAlgorithm: String? = null,
    val publicKeyFormat: String? = null,
    val exponent: String? = null,
    val modulus: String? = null,
    val extensions: Map<String, String> = emptyMap()
)

data class AppDetailState(
    val packageInfo: PackageInfo? = null,
    val label: String = "",
    val icon: Bitmap? = null,
    val version: String = "",
    val size: String = "",
    val installTime: String = "",
    val origin: String = "",
    val services: List<ServiceInfo> = emptyList(),
    val receivers: List<ActivityInfo> = emptyList(),
    val sharedLibraries: List<String> = emptyList(),
    val signatures: List<SignatureDetail> = emptyList(),
    val isVerified: Boolean = false,
    val overlayTarget: String? = null,
    // New Info Tab data
    val sourceDir: String = "",
    val dataDir: String = "",
    val deviceProtectedDataDir: String? = null,
    val nativeLibraryDir: String? = null,
    val txBytes: String = "",
    val rxBytes: String = "",
    val appSize: String = "",
    val dataSize: String = "",
    val cacheSize: String = "",
    val totalSize: String = "",
    val targetSdk: Int = 0,
    val minSdk: Int = 0,
    val flags: List<String> = emptyList(),
    val updateTime: String = "",
    val uid: Int = 0,
    val sharedUserId: String? = null,
    val primaryCpuAbi: String? = null,
    val hiddenApiPolicy: String? = null,
    val selinux: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class AppDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val packageManager = application.packageManager
    private val context get() = getApplication<Application>().applicationContext

    private val _uiState = MutableStateFlow(AppDetailState())
    val uiState: StateFlow<AppDetailState> = _uiState.asStateFlow()

    fun loadAppDetails(packageName: String, notAvailable: String, sideloadedText: String, datePattern: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        try {
            val flags = PackageManager.GET_ACTIVITIES or 
                        PackageManager.GET_SERVICES or 
                        PackageManager.GET_RECEIVERS or
                        PackageManager.GET_PERMISSIONS or
                        PackageManager.GET_PROVIDERS or
                        PackageManager.GET_SHARED_LIBRARY_FILES or
                        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES)

            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, flags)
            }

            val appInfo = packageInfo.applicationInfo ?: throw Exception("Application info not available")
            val label = appInfo.loadLabel(packageManager).toString()
            val icon = appInfo.loadIcon(packageManager).let { drawable ->
                val bitmap = createBitmap(
                    drawable.intrinsicWidth.coerceAtLeast(1),
                    drawable.intrinsicHeight.coerceAtLeast(1)
                )
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }

            val version = packageInfo.versionName ?: notAvailable
            
            val file = File(appInfo.sourceDir)
            val size = if (file.exists()) Formatter.formatShortFileSize(context, file.length()) else notAvailable

            val locale = context.resources.configuration.locales[0]
            val sdf = SimpleDateFormat(datePattern, locale)
            val installTime = sdf.format(Date(packageInfo.firstInstallTime))
            val updateTime = sdf.format(Date(packageInfo.lastUpdateTime))

            // Data usage
            val uid = appInfo.uid
            val rx = TrafficStats.getUidRxBytes(uid)
            val tx = TrafficStats.getUidTxBytes(uid)
            val rxFormatted = if (rx != TrafficStats.UNSUPPORTED.toLong()) Formatter.formatShortFileSize(context, rx) else notAvailable
            val txFormatted = if (tx != TrafficStats.UNSUPPORTED.toLong()) Formatter.formatShortFileSize(context, tx) else notAvailable

            // Storage info
            var appSizeVal: Long
            var dataSizeVal = 0L
            var cacheSizeVal = 0L
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                    val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
                    val uuid = storageManager.getUuidForPath(File(appInfo.sourceDir))
                    val stats = storageStatsManager.queryStatsForPackage(uuid, packageName, Process.myUserHandle())
                    appSizeVal = stats.appBytes
                    dataSizeVal = stats.dataBytes
                    cacheSizeVal = stats.cacheBytes
                } catch (_: SecurityException) {
                    // Fallback if permission is not granted
                    appSizeVal = File(appInfo.sourceDir).length()
                } catch (_: Exception) {
                    appSizeVal = File(appInfo.sourceDir).length()
                }
            } else {
                appSizeVal = File(appInfo.sourceDir).length()
            }
            
            val appSizeStr = Formatter.formatShortFileSize(context, appSizeVal)
            val dataSizeStr = if (dataSizeVal > 0) Formatter.formatShortFileSize(context, dataSizeVal) else notAvailable
            val cacheSizeStr = if (cacheSizeVal > 0) Formatter.formatShortFileSize(context, cacheSizeVal) else notAvailable
            val totalSizeStr = Formatter.formatShortFileSize(context, appSizeVal + dataSizeVal + cacheSizeVal)

            // Flags
            val flagsList = mutableListOf<String>()
            val appFlags = appInfo.flags
            if ((appFlags and ApplicationInfo.FLAG_SYSTEM) != 0) flagsList.add("SYSTEM")
            if ((appFlags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) flagsList.add("DEBUGGABLE")
            if ((appFlags and ApplicationInfo.FLAG_HAS_CODE) != 0) flagsList.add("HAS_CODE")
            if ((appFlags and ApplicationInfo.FLAG_PERSISTENT) != 0) flagsList.add("PERSISTENT")
            if ((appFlags and ApplicationInfo.FLAG_FACTORY_TEST) != 0) flagsList.add("FACTORY_TEST")
            if ((appFlags and ApplicationInfo.FLAG_ALLOW_BACKUP) != 0) flagsList.add("ALLOW_BACKUP")
            if ((appFlags and ApplicationInfo.FLAG_KILL_AFTER_RESTORE) != 0) flagsList.add("KILL_AFTER_RESTORE")
            if ((appFlags and ApplicationInfo.FLAG_RESTORE_ANY_VERSION) != 0) flagsList.add("RESTORE_ANY_VERSION")
            if ((appFlags and ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) flagsList.add("EXTERNAL_STORAGE")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // FLAG_INSTANT is @hide, but its value is 1 << 23
                if ((appFlags and (1 shl 23)) != 0) flagsList.add("INSTANT")
            }

            val selinux = try {
                val file = File("/proc/self/attr/current")
                if (file.exists()) file.readText().trim() else notAvailable
            } catch (_: Exception) {
                notAvailable
            }

            val hiddenApiPolicy = try {
                val field = appInfo.javaClass.getField("mHiddenApiPolicy")
                field.get(appInfo)?.toString()
            } catch (_: Exception) {
                null
            }

            val signatures = mutableListOf<SignatureDetail>()
            var isVerified = false

            val localeSignature = context.resources.configuration.locales[0]
            val sdfSignature = SimpleDateFormat(datePattern, localeSignature)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.let { signingInfo ->
                    isVerified = true
                    val signers = if (signingInfo.hasMultipleSigners()) {
                        signingInfo.apkContentsSigners
                    } else {
                        signingInfo.signingCertificateHistory
                    }
                    signers.forEach { sig ->
                        parseSignature(sig.toByteArray(), sdfSignature)?.let { signatures.add(it) }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures?.let { sigs ->
                    if (sigs.isNotEmpty()) isVerified = true
                    sigs.forEach { sig ->
                        parseSignature(sig.toByteArray(), sdfSignature)?.let { signatures.add(it) }
                    }
                }
            }

            val overlayTarget = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val isResourceOverlayField = appInfo.javaClass.getField("isResourceOverlay")
                    val isResourceOverlay = isResourceOverlayField.get(appInfo) as Boolean
                    if (isResourceOverlay) {
                        val overlayTargetField = appInfo.javaClass.getField("overlayTarget")
                        overlayTargetField.get(appInfo) as String
                    } else null
                } catch (_: Exception) {
                    null
                }
            } else null

            val origin = try {
                val installerPackage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    packageManager.getInstallSourceInfo(packageName).installingPackageName
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getInstallerPackageName(packageName)
                }

                installerPackage?.let { pkg ->
                    try {
                        packageManager.getApplicationInfo(pkg, 0).loadLabel(packageManager).toString()
                    } catch (_: Exception) {
                        pkg
                    }
                } ?: sideloadedText
            } catch (_: Exception) {
                notAvailable
            }

            _uiState.value = AppDetailState(
                packageInfo = packageInfo,
                label = label,
                icon = icon,
                version = version,
                size = size,
                installTime = installTime,
                origin = origin,
                services = packageInfo.services?.toList() ?: emptyList(),
                receivers = packageInfo.receivers?.toList() ?: emptyList(),
                sharedLibraries = appInfo.sharedLibraryFiles?.toList() ?: emptyList(),
                signatures = signatures,
                isVerified = isVerified,
                overlayTarget = overlayTarget,
                sourceDir = appInfo.sourceDir,
                dataDir = appInfo.dataDir,
                deviceProtectedDataDir = appInfo.deviceProtectedDataDir,
                nativeLibraryDir = appInfo.nativeLibraryDir,
                txBytes = txFormatted,
                rxBytes = rxFormatted,
                appSize = appSizeStr,
                dataSize = dataSizeStr,
                cacheSize = cacheSizeStr,
                totalSize = totalSizeStr,
                targetSdk = appInfo.targetSdkVersion,
                minSdk = appInfo.minSdkVersion,
                flags = flagsList,
                updateTime = updateTime,
                uid = uid,
                sharedUserId = packageInfo.sharedUserId,
                primaryCpuAbi = try {
                    val field = appInfo.javaClass.getField("primaryCpuAbi")
                    field.get(appInfo) as? String
                } catch (_: Exception) { null },
                selinux = selinux,
                hiddenApiPolicy = hiddenApiPolicy,
                isLoading = false
            )
        } catch (e: Exception) {
            _uiState.value = AppDetailState(isLoading = false, error = e.message)
        }
    }

    fun launchApp(packageName: String) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
            }
        } catch (_: Exception) {
            // Handle error
        }
    }

    fun shareApk(packageName: String, label: String, version: String) {
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val apkFile = File(appInfo.sourceDir)
            
            // Create a temporary file with the desired name in the cache directory
            val sanitizedLabel = label.replace(Regex("[^a-zA-Z0-9]"), "_")
            val sanitizedVersion = version.replace(Regex("[^a-zA-Z0-9.]"), "_")
            val newFileName = "${sanitizedLabel}_${sanitizedVersion}.apk"
            
            val cacheDir = File(context.cacheDir, "shared_apks")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            
            val tempFile = File(cacheDir, newFileName)
            apkFile.inputStream().use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                tempFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, label)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (_: Exception) {
            // Handle error
        }
    }

    fun launchActivity(packageName: String, activityName: String) {
        try {
            val intent = Intent()
            intent.component = ComponentName(packageName, activityName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {
            // Handle error
        }
    }

    private fun parseSignature(signatureBytes: ByteArray, sdf: SimpleDateFormat): SignatureDetail? {
        return try {
            val cf = CertificateFactory.getInstance("X.509")
            val cert = cf.generateCertificate(signatureBytes.inputStream()) as X509Certificate
            
            val publicKey = cert.publicKey
            val rsaPublicKey = publicKey as? RSAPublicKey

            val extensions = mutableMapOf<String, String>()
            cert.nonCriticalExtensionOIDs?.forEach { oid ->
                val extValue = cert.getExtensionValue(oid)
                extensions[oid] = extValue?.joinToString("") { "%02X".format(it) } ?: ""
            }

            SignatureDetail(
                subject = cert.subjectDN.name,
                issuer = cert.issuerDN.name,
                issueDate = sdf.format(cert.notBefore),
                expiryDate = sdf.format(cert.notAfter),
                type = cert.type,
                version = cert.version.toString(),
                serialNumber = cert.serialNumber.toString(16).uppercase(),
                md5 = getDigest(signatureBytes, "MD5"),
                sha1 = getDigest(signatureBytes, "SHA-1"),
                sha256 = getDigest(signatureBytes, "SHA-256"),
                sha384 = getDigest(signatureBytes, "SHA-384"),
                sha512 = getDigest(signatureBytes, "SHA-512"),
                algorithm = cert.sigAlgName,
                oid = cert.sigAlgOID,
                signature = cert.signature.joinToString("") { "%02X".format(it) },
                publicKeyAlgorithm = publicKey.algorithm,
                publicKeyFormat = publicKey.format,
                exponent = rsaPublicKey?.publicExponent?.toString(),
                modulus = rsaPublicKey?.modulus?.toString(16)?.uppercase(),
                extensions = extensions
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun getDigest(bytes: ByteArray, algorithm: String): String {
        return try {
            val md = MessageDigest.getInstance(algorithm)
            val digest = md.digest(bytes)
            digest.joinToString(":") { "%02X".format(it) }
        } catch (_: Exception) {
            ""
        }
    }
}
