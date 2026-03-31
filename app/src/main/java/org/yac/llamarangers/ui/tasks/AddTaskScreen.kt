package org.yac.llamarangers.ui.tasks

import android.app.DatePickerDialog
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.yac.llamarangers.data.repository.TaskRepository
import org.yac.llamarangers.domain.model.enums.TaskPriority
import org.yac.llamarangers.service.auth.AuthManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for adding a new task.
 */
@HiltViewModel
class AddTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val authManager: AuthManager
) : ViewModel() {

    fun save(
        title: String,
        notes: String,
        priority: TaskPriority,
        hasDueDate: Boolean,
        dueDate: Long,
        onComplete: () -> Unit,
        onError: () -> Unit = {}
    ) {
        val rangerId = authManager.currentRangerId.value?.toString()
        if (rangerId == null) {
            onError()
            return
        }
        viewModelScope.launch {
            try {
                taskRepository.createTask(
                    title = title.trim(),
                    notes = notes.ifBlank { null },
                    priority = priority.value,
                    dueDate = if (hasDueDate) dueDate else null,
                    rangerId = rangerId
                )
                onComplete()
            } catch (_: Exception) {
                onError()
            }
        }
    }
}

/**
 * Add task form screen.
 * Ports iOS AddTaskView.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    viewModel: AddTaskViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var hasDueDate by remember { mutableStateOf(false) }
    val defaultDue = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.timeInMillis
    }
    var dueDate by remember { mutableLongStateOf(defaultDue) }
    var isSaving by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Task") },
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
                                title = title,
                                notes = notes,
                                priority = priority,
                                hasDueDate = hasDueDate,
                                dueDate = dueDate,
                                onComplete = onNavigateBack,
                                onError = { isSaving = false }
                            )
                        },
                        enabled = title.trim().isNotEmpty() && !isSaving
                    ) {
                        Text("Add")
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
            // Task info
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Task",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 5
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Priority
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        TaskPriority.entries.forEachIndexed { index, p ->
                            SegmentedButton(
                                selected = priority == p,
                                onClick = { priority = p },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = TaskPriority.entries.size
                                )
                            ) {
                                Text(p.displayName)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Due Date
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Due Date",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Set due date", modifier = Modifier.weight(1f))
                        Switch(
                            checked = hasDueDate,
                            onCheckedChange = { hasDueDate = it }
                        )
                    }
                    if (hasDueDate) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Due", modifier = Modifier.weight(1f))
                            TextButton(onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = dueDate }
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        val newCal = Calendar.getInstance().apply {
                                            set(year, month, day)
                                        }
                                        dueDate = newCal.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }) {
                                Text(dateFormat.format(Date(dueDate)))
                            }
                        }
                    }
                }
            }
        }
    }
}
