package org.yac.llamarangers.ui.sighting

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.yac.llamarangers.data.repository.TaskRepository
import org.yac.llamarangers.data.repository.TreatmentRepository
import org.yac.llamarangers.domain.model.enums.TreatmentMethod
import org.yac.llamarangers.service.auth.AuthManager
import org.yac.llamarangers.ui.navigation.Screen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Add
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for treatment entry.
 */
@HiltViewModel
class TreatmentEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val treatmentRepository: TreatmentRepository,
    private val taskRepository: TaskRepository,
    private val authManager: AuthManager
) : ViewModel() {

    val sightingId: String = savedStateHandle[Screen.TreatmentEntry.ARG_SIGHTING_ID] ?: ""

    fun save(
        method: TreatmentMethod,
        herbicideProduct: String,
        outcomeNotes: String,
        afterPhotoFilenames: List<String>,
        hasFollowUp: Boolean,
        followUpDate: Long,
        onComplete: () -> Unit
    ) {
        val rangerId = authManager.currentRangerId.value?.toString() ?: return
        viewModelScope.launch {
            var finalNotes = outcomeNotes
            if (afterPhotoFilenames.isNotEmpty()) {
                val prefix = "📷 After: ${afterPhotoFilenames.size} photo(s). "
                finalNotes = prefix + outcomeNotes
            }
            
            val treatment = treatmentRepository.addTreatment(
                sightingId = sightingId,
                method = method.value,
                herbicideProduct = herbicideProduct.ifBlank { null },
                outcomeNotes = finalNotes.ifBlank { null },
                followUpDate = if (hasFollowUp) followUpDate else null,
                rangerId = rangerId
            )
            // Auto-create follow-up task if needed
            if (hasFollowUp) {
                taskRepository.createFollowUpTask(
                    treatmentId = treatment.id,
                    treatmentMethod = method.displayName,
                    followUpDate = followUpDate,
                    rangerId = rangerId
                )
            }
            onComplete()
        }
    }
}

/**
 * Treatment entry form screen.
 * Ports iOS TreatmentEntryView.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreatmentEntryScreen(
    viewModel: TreatmentEntryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    var selectedMethod by remember { mutableStateOf(TreatmentMethod.FOLIAR_SPRAY) }
    var herbicideProduct by remember { mutableStateOf("") }
    var outcomeNotes by remember { mutableStateOf("") }
    var hasFollowUp by remember { mutableStateOf(false) }
    val defaultFollowUp = remember {
        Calendar.getInstance().apply { add(Calendar.MONTH, 1) }.timeInMillis
    }
    var followUpDate by remember { mutableLongStateOf(defaultFollowUp) }
    var isSaving by remember { mutableStateOf(false) }
    var afterPhotoFilenames by remember { mutableStateOf(listOf<String>()) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Treatment") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Cancel")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            isSaving = true
                            viewModel.save(
                                method = selectedMethod,
                                herbicideProduct = herbicideProduct,
                                outcomeNotes = outcomeNotes,
                                afterPhotoFilenames = afterPhotoFilenames,
                                hasFollowUp = hasFollowUp,
                                followUpDate = followUpDate,
                                onComplete = onNavigateBack
                            )
                        },
                        enabled = !isSaving
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Treatment Method
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Treatment Method",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TreatmentMethod.entries.forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMethod = method }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMethod == method,
                                onClick = { selectedMethod = method }
                            )
                            Text(
                                text = method.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Herbicide Product
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Herbicide Product",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = herbicideProduct,
                        onValueChange = { herbicideProduct = it },
                        placeholder = { Text("e.g. Garlon 600, Access (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Outcome Notes
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Outcome Notes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = outcomeNotes,
                        onValueChange = { outcomeNotes = it },
                        placeholder = { Text("Observations, coverage, etc. (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // After Photos
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "After Photos (optional)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "After photos",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (afterPhotoFilenames.isNotEmpty()) {
                            Text(
                                text = "${afterPhotoFilenames.size} attached",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, shape = androidx.compose.foundation.shape.CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            afterPhotoFilenames = afterPhotoFilenames + "after_${java.util.UUID.randomUUID()}.heif"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Attach After Photo")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Follow-up
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Follow-up",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Schedule regrowth check",
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = hasFollowUp,
                            onCheckedChange = { hasFollowUp = it }
                        )
                    }
                    if (hasFollowUp) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Follow-up Date", modifier = Modifier.weight(1f))
                            TextButton(onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = followUpDate }
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        val newCal = Calendar.getInstance().apply {
                                            set(year, month, day)
                                        }
                                        followUpDate = newCal.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }) {
                                Text(dateFormat.format(Date(followUpDate)))
                            }
                        }
                    }
                }
            }
        }
    }
}
