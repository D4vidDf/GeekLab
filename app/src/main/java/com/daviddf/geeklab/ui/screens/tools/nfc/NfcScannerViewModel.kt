package com.daviddf.geeklab.ui.screens.tools.nfc

import android.app.Activity
import android.app.Application
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.nio.charset.Charset
import java.util.Arrays
import kotlin.experimental.and

data class NfcScannerUiState(
    val isNfcSupported: Boolean = false,
    val isNfcEnabled: Boolean = false,
    val isScanning: Boolean = false,
    val lastScannedTag: NfcTagInfo? = null
)

data class NfcTagInfo(
    val id: String,
    val techList: List<String>,
    val ndefInfo: NdefInfo? = null
)

data class NdefInfo(
    val type: String,
    val maxSize: Int,
    val isWritable: Boolean,
    val canMakeReadOnly: Boolean,
    val records: List<NdefRecordData>
)

data class NdefRecordData(
    val tnf: Short,
    val type: String,
    val payload: String
)

class NfcScannerViewModel(application: Application) : AndroidViewModel(application) {
    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(application)
    
    private val _uiState = MutableStateFlow(NfcScannerUiState(
        isNfcSupported = nfcAdapter != null,
        isNfcEnabled = nfcAdapter?.isEnabled == true
    ))
    val uiState: StateFlow<NfcScannerUiState> = _uiState.asStateFlow()

    fun startScanning(activity: Activity) {
        val adapter = nfcAdapter ?: return
        if (!adapter.isEnabled) {
            _uiState.update { it.copy(isNfcEnabled = false) }
            return
        }

        _uiState.update { it.copy(isScanning = true, isNfcEnabled = true) }
        
        adapter.enableReaderMode(
            activity,
            { tag -> handleTagDiscovered(tag) },
            NfcAdapter.FLAG_READER_NFC_A or 
            NfcAdapter.FLAG_READER_NFC_B or 
            NfcAdapter.FLAG_READER_NFC_F or 
            NfcAdapter.FLAG_READER_NFC_V or 
            NfcAdapter.FLAG_READER_NFC_BARCODE,
            null
        )
    }

    fun stopScanning(activity: Activity) {
        nfcAdapter?.disableReaderMode(activity)
        _uiState.update { it.copy(isScanning = false) }
    }

    private fun handleTagDiscovered(tag: Tag) {
        val id = tag.id.joinToString(":") { "%02X".format(it) }
        val techList = tag.techList.map { it.substringAfterLast('.') }
        
        val ndef = Ndef.get(tag)
        val ndefInfo = ndef?.let {
            it.connect()
            val records = it.cachedNdefMessage?.records?.map { record ->
                NdefRecordData(
                    tnf = record.tnf,
                    type = String(record.type),
                    payload = parseNdefRecordPayload(record)
                )
            } ?: emptyList()
            
            NdefInfo(
                type = it.type,
                maxSize = it.maxSize,
                isWritable = it.isWritable,
                canMakeReadOnly = it.canMakeReadOnly(),
                records = records
            ).also { ndef.close() }
        }

        _uiState.update { 
            it.copy(
                lastScannedTag = NfcTagInfo(id, techList, ndefInfo),
                isScanning = false
            )
        }
    }

    private fun parseNdefRecordPayload(record: NdefRecord): String {
        return when (record.tnf) {
            NdefRecord.TNF_WELL_KNOWN -> {
                when {
                    Arrays.equals(record.type, NdefRecord.RTD_TEXT) -> parseTextRecord(record)
                    Arrays.equals(record.type, NdefRecord.RTD_URI) -> parseUriRecord(record)
                    else -> String(record.payload)
                }
            }
            NdefRecord.TNF_MIME_MEDIA -> String(record.payload)
            NdefRecord.TNF_ABSOLUTE_URI -> String(record.payload)
            else -> String(record.payload)
        }
    }

    private fun parseTextRecord(record: NdefRecord): String {
        val payload = record.payload
        val statusByte = payload[0]
        val isUtf16 = (statusByte.toInt() and 0x80) != 0
        val languageCodeLength = statusByte.toInt() and 0x3F
        val charset = if (isUtf16) Charsets.UTF_16 else Charsets.UTF_8
        return String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, charset)
    }

    private fun parseUriRecord(record: NdefRecord): String {
        val payload = record.payload
        if (payload.isEmpty()) return ""
        val prefix = URI_PREFIX_MAP[payload[0].toInt()] ?: ""
        return prefix + String(payload, 1, payload.size - 1, Charsets.UTF_8)
    }

    companion object {
        private val URI_PREFIX_MAP = mapOf(
            0x00 to "",
            0x01 to "http://www.",
            0x02 to "https://www.",
            0x03 to "http://",
            0x04 to "https://",
            0x05 to "tel:",
            0x06 to "mailto:",
            0x07 to "ftp://anonymous:anonymous@",
            0x08 to "ftp://ftp.",
            0x09 to "ftps://",
            0x0A to "sftp://",
            0x0B to "smb://",
            0x0C to "nfs://",
            0x0D to "ftp://",
            0x0E to "dav://",
            0x0F to "news:",
            0x10 to "telnet://",
            0x11 to "imap:",
            0x12 to "rtsp://",
            0x13 to "urn:",
            0x14 to "pop:",
            0x15 to "sip:",
            0x16 to "sips:",
            0x17 to "tftp:",
            0x18 to "btspp://",
            0x19 to "btl2cap://",
            0x1A to "btgoep://",
            0x1B to "tcpobex://",
            0x1C to "irdaobex://",
            0x1D to "file://",
            0x1E to "urn:epc:id:",
            0x1F to "urn:epc:tag:",
            0x20 to "urn:epc:pat:",
            0x21 to "urn:epc:raw:",
            0x22 to "urn:otp:",
            0x23 to "urn:nfc:"
        )
    }
}
