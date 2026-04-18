package com.daviddf.geeklab

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import org.xmlpull.v1.XmlPullParser
import java.io.File

class ManifestProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String = "text/xml"

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val packageName = uri.lastPathSegment ?: return null
        val context = context ?: return null

        try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            val res = context.packageManager.getResourcesForApplication(appInfo)
            val parser = res.assets.openXmlResourceParser("AndroidManifest.xml")
            
            val cacheFile = File(context.cacheDir, "manifest_$packageName.xml")
            cacheFile.bufferedWriter().use { writer ->
                var indent = 0
                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            writer.write("  ".repeat(indent))
                            writer.write("<")
                            writer.write(parser.name)
                            for (i in 0 until parser.attributeCount) {
                                val attrName = parser.getAttributeName(i)
                                val attrValue = try {
                                    val resId = parser.getAttributeResourceValue(i, 0)
                                    if (resId != 0) {
                                        try {
                                            res.getResourceName(resId).let { "@${it.replace(":", "/")}" }
                                        } catch (_: Exception) {
                                            parser.getAttributeValue(i) ?: "@$resId"
                                        }
                                    } else {
                                        parser.getAttributeValue(i)
                                    }
                                } catch (_: Exception) {
                                    "resource_id_${parser.getAttributeNameResource(i)}"
                                }
                                writer.write("\n")
                                writer.write("  ".repeat(indent + 1))
                                writer.write(attrName)
                                writer.write("=\"")
                                writer.write(attrValue ?: "")
                                writer.write("\"")
                            }
                            writer.write(">\n")
                            indent++
                        }
                        XmlPullParser.END_TAG -> {
                            indent--
                            writer.write("  ".repeat(indent))
                            writer.write("</")
                            writer.write(parser.name)
                            writer.write(">\n")
                        }
                        XmlPullParser.TEXT -> {
                            val text = parser.text?.trim()
                            if (!text.isNullOrEmpty()) {
                                writer.write("  ".repeat(indent))
                                writer.write(text)
                                writer.write("\n")
                            }
                        }
                    }
                    eventType = parser.next()
                }
            }
            return ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
            
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
