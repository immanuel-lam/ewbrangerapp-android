package org.yac.llamarangers.service.map

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.MapTileIndex
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages offline map tiles from MBTiles files or tile directory trees.
 * Ports iOS OfflineTileManager + LocalTileOverlay using osmdroid tile providers.
 */
@Singleton
class OfflineTileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "OfflineTileManager"
        private const val MBTILES_FILENAME = "PortStewart.mbtiles"
        private const val TILES_DIRECTORY = "MapTiles"
    }

    sealed class TileStatus {
        data object Checking : TileStatus()
        data class Available(val version: String, val coverage: String) : TileStatus()
        data class Downloading(val progress: Double) : TileStatus()
        data object Unavailable : TileStatus()
    }

    private val _tileStatus = MutableStateFlow<TileStatus>(TileStatus.Checking)
    val tileStatus: StateFlow<TileStatus> = _tileStatus.asStateFlow()

    init {
        checkTileAvailability()
    }

    private fun checkTileAvailability() {
        // Check for MBTiles in app assets/files
        val mbtilesFile = File(context.filesDir, MBTILES_FILENAME)
        if (mbtilesFile.exists()) {
            _tileStatus.value = TileStatus.Available(
                version = "bundled",
                coverage = "Port Stewart +/-30km, zoom 10-18"
            )
            return
        }

        // Check for tile directory tree
        val tilesDir = File(context.filesDir, TILES_DIRECTORY)
        if (tilesDir.exists() && tilesDir.isDirectory) {
            _tileStatus.value = TileStatus.Available(
                version = "local",
                coverage = "Port Stewart +/-30km, zoom 10-18"
            )
            return
        }

        _tileStatus.value = TileStatus.Unavailable
    }

    /**
     * Returns an osmdroid tile source that reads from MBTiles (SQLite) if available,
     * or null if no offline tiles are present.
     */
    fun createTileSource(): ITileSource? {
        val mbtilesFile = File(context.filesDir, MBTILES_FILENAME)
        if (mbtilesFile.exists()) {
            return MBTileSource(mbtilesFile)
        }

        val tilesDir = File(context.filesDir, TILES_DIRECTORY)
        if (tilesDir.exists() && tilesDir.isDirectory) {
            return DirectoryTileSource(tilesDir)
        }

        return null
    }

    /**
     * osmdroid tile source backed by an MBTiles (SQLite) file.
     */
    private class MBTileSource(
        private val mbtilesFile: File
    ) : XYTileSource(
        "OfflineMBTiles",
        10, 18, 256, ".png",
        arrayOf()
    ) {
        private var db: SQLiteDatabase? = null

        init {
            try {
                db = SQLiteDatabase.openDatabase(
                    mbtilesFile.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READONLY
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open MBTiles database", e)
            }
        }

        fun getTileBytes(z: Int, x: Int, y: Int): ByteArray? {
            val database = db ?: return null
            // MBTiles uses TMS y (flipped): tms_y = (1 << z) - 1 - y
            val tmsY = (1 shl z) - 1 - y
            val cursor = database.rawQuery(
                "SELECT tile_data FROM tiles WHERE zoom_level=? AND tile_column=? AND tile_row=?",
                arrayOf(z.toString(), x.toString(), tmsY.toString())
            )
            return cursor.use {
                if (it.moveToFirst()) it.getBlob(0) else null
            }
        }

        fun close() {
            db?.close()
            db = null
        }
    }

    /**
     * osmdroid tile source backed by a {z}/{x}/{y}.png directory tree.
     */
    private class DirectoryTileSource(
        private val tilesDir: File
    ) : XYTileSource(
        "OfflineDirectory",
        10, 18, 256, ".png",
        arrayOf()
    ) {
        fun getTileFile(z: Int, x: Int, y: Int): File {
            return File(tilesDir, "$z/$x/$y.png")
        }
    }
}
