package com.example.voicecareagent.data

import android.content.Context
import com.example.voicecareagent.model.FriendContact

object ContactsRepository {

    // Safe Test Contacts whitelist — shields real device contacts from accidental calls during testing
    val defaultContacts: List<FriendContact> = listOf(
        FriendContact(
            id = "test_alex",
            name = "Alex (Test Friend)",
            phoneNumber = "+1 (555) 014-9922",
            relation = "Sandbox Friend",
            avatarColorHex = 0xFF0A84FF
        ),
        FriendContact(
            id = "test_self",
            name = "My Personal Test Phone",
            phoneNumber = "+1 (555) 019-2831",
            relation = "Authorized Test Line",
            avatarColorHex = 0xFF30D158
        ),
        FriendContact(
            id = "test_tutor",
            name = "Math Tutor (Test Line)",
            phoneNumber = "+1 (555) 017-4820",
            relation = "Sandbox Tutor",
            avatarColorHex = 0xFFFF9500
        ),
        FriendContact(
            id = "test_sarah",
            name = "Sarah (Colleague)",
            phoneNumber = "+1 (555) 018-7711",
            relation = "Sandbox Colleague",
            avatarColorHex = 0xFFAF52DE
        ),
        FriendContact(
            id = "test_landlord",
            name = "Marcus (Landlord)",
            phoneNumber = "+1 (555) 012-4401",
            relation = "Sandbox Landlord",
            avatarColorHex = 0xFFFF2D55
        )
    )

    val presetTopics: List<String> = listOf(
        "Check if free this Saturday around 7 PM for dinner at an Italian restaurant",
        "Coordinate tutoring timings and schedule for math classes this week",
        "Wish Happy Birthday and catch up on weekend plans",
        "Follow up on the sent resume and documents to check if received",
        "Ask landlord about the schedule for air conditioning maintenance"
    )

    /**
     * In Safe Test Mode, real phonebook contacts are strictly shielded to prevent
     * any accidental calls or disturbance to actual contacts.
     * Returns only explicitly authorized test contacts.
     */
    fun loadSafeTestContacts(context: Context): List<FriendContact> {
        return defaultContacts
    }

    // Keep backwards compatibility method name but return safe test contacts
    fun loadDeviceContacts(context: Context, limit: Int = 1000): List<FriendContact> {
        return defaultContacts
    }

    /**
     * Query device contacts ONLY for the explicit user picker dialog.
     * These contacts are never automatically dialed or added to the active test list
     * until the user manually checks and confirms them.
     */
    fun loadDeviceDirectoryForPicker(context: Context): List<FriendContact> {
        val contactList = mutableListOf<FriendContact>()
        val seenKeys = mutableSetOf<String>()
        try {
            val cursor = context.contentResolver.query(
                android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    android.provider.ContactsContract.CommonDataKinds.Phone._ID,
                    android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE NOCASE ASC"
            )

            cursor?.use {
                val idIndex = it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone._ID)
                val nameIndex = it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)

                val palette = listOf(
                    0xFF0A84FF, 0xFF30D158, 0xFFFF9500, 0xFFAF52DE,
                    0xFFFF2D55, 0xFF5856D6, 0xFF64D2FF, 0xFFFF375F
                )
                var count = 0

                while (it.moveToNext() && count < 800) {
                    val id = if (idIndex >= 0) it.getString(idIndex) else count.toString()
                    val name = if (nameIndex >= 0) it.getString(nameIndex) else "Contact"
                    val number = if (numberIndex >= 0) it.getString(numberIndex) else ""
                    val cleanPhone = number.replace("[^0-9+]".toRegex(), "")
                    val dedupeKey = "${name.trim().lowercase()}_$cleanPhone"

                    if (name.isNotBlank() && cleanPhone.isNotBlank() && !seenKeys.contains(dedupeKey)) {
                        seenKeys.add(dedupeKey)
                        val colorIdx = (name.hashCode() and 0x7FFFFFFF) % palette.size
                        contactList.add(
                            FriendContact(
                                id = "phone_$id",
                                name = name.trim(),
                                phoneNumber = number.trim(),
                                relation = "Phonebook Contact",
                                avatarColorHex = palette[colorIdx]
                            )
                        )
                        count++
                    }
                }
            }
        } catch (_: Exception) {}

        return contactList
    }
}
