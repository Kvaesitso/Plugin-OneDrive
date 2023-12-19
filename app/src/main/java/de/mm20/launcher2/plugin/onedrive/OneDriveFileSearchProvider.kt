package de.mm20.launcher2.plugin.onedrive

import android.content.Intent
import android.net.Uri
import com.microsoft.graph.models.DriveItem
import de.mm20.launcher2.plugin.config.SearchPluginConfig
import de.mm20.launcher2.sdk.PluginState
import de.mm20.launcher2.sdk.files.File
import de.mm20.launcher2.sdk.files.FileDimensions
import de.mm20.launcher2.sdk.files.FileMetadata
import de.mm20.launcher2.sdk.files.FileProvider
import kotlinx.coroutines.flow.firstOrNull
import org.koin.android.ext.android.inject

class OneDriveFileSearchProvider : FileProvider(
    SearchPluginConfig()
) {
    private val client: MicrosoftApiClient by inject()

    override suspend fun get(id: String): File? {
        return null
    }

    override suspend fun search(query: String): List<File> {
        val driveItems = client.queryOneDriveFiles(query) ?: return emptyList()
        return driveItems.mapNotNull {
            File(
                id = it.id ?: return@mapNotNull null,
                displayName = it.name ?: return@mapNotNull null,
                mimeType = if (it.folder != null) {
                    "inode/directory"
                } else {
                    it.file?.mimeType ?: "application/octet-stream"
                },
                isDirectory = it.folder != null,
                uri = Uri.parse(it.webUrl),
                size = it.size ?: 0,
                path = it.parentReference?.path ?: "",
                owner = it.shared?.owner?.user?.displayName,
                metadata = getMetadata(it),
            )
        }
    }

    private fun getMetadata(driveItem: DriveItem): FileMetadata {
        var dimensions: FileDimensions? = null
        if (driveItem.image?.width != null && driveItem.image?.height != null) {
            dimensions = FileDimensions(
                width = driveItem.image!!.width!!,
                height = driveItem.image!!.height!!,
            )
        } else if (driveItem.video?.width != null && driveItem.video?.height != null) {
            dimensions = FileDimensions(
                width = driveItem.video!!.width!!,
                height = driveItem.video!!.height!!,
            )
        }
        return FileMetadata(
            dimensions = dimensions,
            title = driveItem.audio?.title,
            artist = driveItem.audio?.artist,
            album = driveItem.audio?.album,
            duration = driveItem.video?.duration ?: driveItem.audio?.duration,
            year = driveItem.audio?.year,
        )
    }

    override suspend fun getPluginState(): PluginState {
        val context = context!!
        val account = client.currentAccount.firstOrNull()
        if (account is MicrosoftAccount) {
            return PluginState.Ready(
                context.getString(R.string.plugin_state_ready, account.displayName ?: account.username)
            )
        }
        return PluginState.SetupRequired(
            setupActivity = Intent(context, SettingsActivity::class.java),
            message = context.getString(R.string.plugin_state_login_required)
        )
    }
}