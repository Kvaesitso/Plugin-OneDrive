package de.mm20.launcher2.plugin.onedrive

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.microsoft.graph.models.DriveItem
import de.mm20.launcher2.plugin.config.QueryPluginConfig
import de.mm20.launcher2.plugin.config.StorageStrategy
import de.mm20.launcher2.sdk.PluginState
import de.mm20.launcher2.sdk.base.GetParams
import de.mm20.launcher2.sdk.base.RefreshParams
import de.mm20.launcher2.sdk.base.SearchParams
import de.mm20.launcher2.sdk.files.File
import de.mm20.launcher2.sdk.files.FileDimensions
import de.mm20.launcher2.sdk.files.FileMetadata
import de.mm20.launcher2.sdk.files.FileProvider
import kotlinx.coroutines.flow.firstOrNull
import org.koin.android.ext.android.inject

class OneDriveFileSearchProvider : FileProvider(
    QueryPluginConfig(
        storageStrategy = StorageStrategy.StoreCopy,
    )
) {
    private val client: MicrosoftApiClient by inject()

    override suspend fun search(query: String, params: SearchParams): List<File> {
        Log.d("MM20", "Searching OneDrive for $query, $params")
        if (!params.allowNetwork) return emptyList()
        val driveItems = client.queryOneDriveFiles(query) ?: return emptyList()
        Log.d("MM20", driveItems.toString())
        return driveItems.mapNotNull {
            it.toFile()
        }
    }

    override suspend fun refresh(item: File, params: RefreshParams): File? {
        return super.refresh(item, params)
    }

    private fun DriveItem.toFile(): File? {
        return File(
            id = id ?: return null,
            displayName = name ?: return null,
            mimeType = if (folder != null) {
                "inode/directory"
            } else {
                file?.mimeType ?: "application/octet-stream"
            },
            isDirectory = folder != null,
            uri = Uri.parse(webUrl),
            size = size ?: 0,
            path = parentReference?.path ?: "",
            owner = shared?.owner?.user?.displayName,
            metadata = getMetadata(this),
        )
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
                context.getString(
                    R.string.plugin_state_ready,
                    account.displayName ?: account.username
                )
            )
        }
        return PluginState.SetupRequired(
            setupActivity = Intent(context, SettingsActivity::class.java),
            message = context.getString(R.string.plugin_state_login_required)
        )
    }
}