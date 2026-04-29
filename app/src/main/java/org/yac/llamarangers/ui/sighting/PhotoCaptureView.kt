package org.yac.llamarangers.ui.sighting

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

/**
 * Photo capture component with camera integration and thumbnail display.
 * Ports iOS PhotoCaptureView using ActivityResultContracts.TakePicture.
 */
@Composable
fun PhotoCaptureView(
    photoFilenames: List<String>,
    onPhotoAdded: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Prepare a temp file for the camera to write to
    val photoDir = remember {
        File(context.filesDir, "Photos").also { it.mkdirs() }
    }
    val tempFilename = remember { "photo_${UUID.randomUUID()}.jpg" }
    val tempFile = remember { File(photoDir, tempFilename) }
    val tempUri: Uri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempFile.exists()) {
            // Rename to a permanent unique name
            val permanentName = "photo_${UUID.randomUUID()}.jpg"
            val permanentFile = File(photoDir, permanentName)
            tempFile.renameTo(permanentFile)
            onPhotoAdded(permanentName)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(tempUri)
    }

    fun launchCamera() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) cameraLauncher.launch(tempUri)
        else permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Column(modifier = modifier) {
        Text(
            text = "Photos (${photoFilenames.size}/3)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            // Thumbnails
            photoFilenames.forEach { filename ->
                PhotoThumbnail(filename = filename)
                Spacer(modifier = Modifier.width(12.dp))
            }
            // Camera button
            if (photoFilenames.size < 3) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { launchCamera() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Take photo",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add Photo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Thumbnail for a saved photo file.
 * Ports iOS PhotoThumbnail.
 */
@Composable
fun PhotoThumbnail(
    filename: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photoDir = File(context.filesDir, "Photos")
    val file = File(photoDir, filename)

    if (file.exists()) {
        val bitmap = remember(filename) {
            BitmapFactory.decodeFile(file.absolutePath)
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Photo",
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
        } else {
            PhotoPlaceholder(modifier)
        }
    } else {
        PhotoPlaceholder(modifier)
    }
}

@Composable
private fun PhotoPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(80.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Photo,
            contentDescription = "Photo placeholder",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
