package de.mm20.launcher2.plugin.onedrive

import androidx.datastore.core.Serializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.InputStream
import java.io.OutputStream

@Serializable
sealed interface Account

@Serializable
data class MicrosoftAccount(
    val displayName: String? = null,
    val username: String? = null,
    val photo: ByteArray? = null,
) : Account

@Serializable
data object NoAccount : Account

object AccountSerializer: Serializer<Account> {
    override val defaultValue: Account = NoAccount

    override suspend fun readFrom(input: InputStream): Account {
        return Json.decodeFromStream(input)
    }

    override suspend fun writeTo(t: Account, output: OutputStream) {
        Json.encodeToStream(Account.serializer(), t, output)
    }
}