package com.example.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DeviceSmsItem(
    val address: String,
    val body: String,
    val date: Long,
    val dateFormatted: String,
    val protocolType: String = "SMS" // "SMS", "RCS/MMS", "MMS"
)

object SmsReaderUtil {

    fun readRecentInboxSms(context: Context, limit: Int = 30): List<DeviceSmsItem> =
        readRecentInboxMessages(context, limit)

    fun readRecentInboxMessages(context: Context, limit: Int = 30): List<DeviceSmsItem> {
        val messages = mutableListOf<DeviceSmsItem>()
        val seenSignatures = HashSet<String>()
        val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

        // 1. Primary Query: Telephony.Sms.Inbox (or Telephony.Sms.CONTENT_URI for wider coverage)
        try {
            val smsUri: Uri = Telephony.Sms.CONTENT_URI
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE
            )

            val cursor = context.contentResolver.query(
                smsUri,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use {
                val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)

                var count = 0
                while (it.moveToNext() && count < limit) {
                    val address = if (addressIdx >= 0) it.getString(addressIdx) ?: "Unknown" else "Unknown"
                    val body = if (bodyIdx >= 0) it.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx >= 0) it.getLong(dateIdx) else System.currentTimeMillis()

                    if (body.isNotBlank()) {
                        val sig = "${address}_${body.take(30)}_${date / 5000}"
                        if (!seenSignatures.contains(sig)) {
                            seenSignatures.add(sig)
                            messages.add(
                                DeviceSmsItem(
                                    address = address,
                                    body = body,
                                    date = date,
                                    dateFormatted = dateFormat.format(Date(date)),
                                    protocolType = "SMS"
                                )
                            )
                            count++
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Query MMS / RCS / Rich messages parts where RCS messages routed via system telephony or fallback provider are saved
        try {
            readMmsAndRcsMessages(context, dateFormat, seenSignatures, messages, limit)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Query generic unified MmsSms conversation provider if available
        if (messages.size < limit) {
            try {
                readUnifiedMmsSms(context, dateFormat, seenSignatures, messages, limit)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Return sorted by date descending
        return messages.sortedByDescending { it.date }.take(limit)
    }

    private fun readMmsAndRcsMessages(
        context: Context,
        dateFormat: SimpleDateFormat,
        seenSignatures: HashSet<String>,
        messages: MutableList<DeviceSmsItem>,
        limit: Int
    ) {
        val mmsUri = Uri.parse("content://mms")
        val projection = arrayOf("_id", "date", "sub", "m_type")
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(mmsUri, projection, null, null, "date DESC")
            cursor?.use {
                val idIdx = it.getColumnIndex("_id")
                val dateIdx = it.getColumnIndex("date")
                val subIdx = it.getColumnIndex("sub")

                var count = 0
                while (it.moveToNext() && count < 15) {
                    val id = if (idIdx >= 0) it.getString(idIdx) else null ?: continue
                    val rawDate = if (dateIdx >= 0) it.getLong(dateIdx) else 0L
                    // MMS timestamps are in seconds; convert to ms
                    val date = if (rawDate < 1000000000000L) rawDate * 1000 else rawDate
                    val subject = if (subIdx >= 0) it.getString(subIdx) ?: "" else ""

                    val textBody = getMmsTextPart(context, id)
                    val fullBody = if (textBody.isNotBlank()) textBody else subject
                    val address = getMmsAddress(context, id)

                    if (fullBody.isNotBlank()) {
                        val sig = "${address}_${fullBody.take(30)}"
                        if (!seenSignatures.contains(sig)) {
                            seenSignatures.add(sig)
                            messages.add(
                                DeviceSmsItem(
                                    address = address.ifEmpty { "RCS/MMS" },
                                    body = fullBody,
                                    date = if (date > 0) date else System.currentTimeMillis(),
                                    dateFormatted = dateFormat.format(Date(if (date > 0) date else System.currentTimeMillis())),
                                    protocolType = "RCS/MMS"
                                )
                            )
                            count++
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun getMmsTextPart(context: Context, mmsId: String): String {
        val partUri = Uri.parse("content://mms/part")
        var cursor: Cursor? = null
        val sb = StringBuilder()
        try {
            cursor = context.contentResolver.query(
                partUri,
                arrayOf("_id", "mid", "ct", "text"),
                "mid=?",
                arrayOf(mmsId),
                null
            )
            cursor?.use {
                val ctIdx = it.getColumnIndex("ct")
                val textIdx = it.getColumnIndex("text")
                while (it.moveToNext()) {
                    val ct = if (ctIdx >= 0) it.getString(ctIdx) ?: "" else ""
                    if (ct.equals("text/plain", ignoreCase = true) || ct.equals("text/html", ignoreCase = true)) {
                        val text = if (textIdx >= 0) it.getString(textIdx) ?: "" else ""
                        if (text.isNotBlank()) {
                            if (sb.isNotEmpty()) sb.append(" ")
                            sb.append(text)
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
        return sb.toString()
    }

    private fun getMmsAddress(context: Context, mmsId: String): String {
        val addrUri = Uri.parse("content://mms/$mmsId/addr")
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                addrUri,
                arrayOf("address", "type"),
                "type=137", // 137 represents FROM in MMS
                null,
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val addressIdx = it.getColumnIndex("address")
                    if (addressIdx >= 0) {
                        val addr = it.getString(addressIdx)
                        if (!addr.isNullOrBlank() && !addr.contains("insert-address-token")) {
                            return addr
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
        return "Unknown"
    }

    private fun readUnifiedMmsSms(
        context: Context,
        dateFormat: SimpleDateFormat,
        seenSignatures: HashSet<String>,
        messages: MutableList<DeviceSmsItem>,
        limit: Int
    ) {
        val canonicalUri = Uri.parse("content://mms-sms/conversations?simple=true")
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                canonicalUri,
                arrayOf("_id", "date", "snippet"),
                null,
                null,
                "date DESC"
            )
            cursor?.use {
                val snippetIdx = it.getColumnIndex("snippet")
                val dateIdx = it.getColumnIndex("date")
                var count = 0
                while (it.moveToNext() && messages.size < limit && count < 10) {
                    val snippet = if (snippetIdx >= 0) it.getString(snippetIdx) ?: "" else ""
                    val date = if (dateIdx >= 0) it.getLong(dateIdx) else System.currentTimeMillis()
                    if (snippet.isNotBlank()) {
                        val sig = "unified_${snippet.take(30)}"
                        if (!seenSignatures.contains(sig)) {
                            seenSignatures.add(sig)
                            messages.add(
                                DeviceSmsItem(
                                    address = "Messages/Chat",
                                    body = snippet,
                                    date = date,
                                    dateFormatted = dateFormat.format(Date(date)),
                                    protocolType = "Chat/RCS"
                                )
                            )
                            count++
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
    }
}
