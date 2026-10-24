package agency.lacosta.venceya.ui.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import agency.lacosta.venceya.R
import agency.lacosta.venceya.domain.Kind
import agency.lacosta.venceya.domain.Plan
import agency.lacosta.venceya.domain.Repeat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemScreen(
    onDone: () -> Unit,
    viewModel: ItemViewModel = viewModel(factory = ItemViewModel.Factory),
) {
    val f by viewModel.form.collectAsStateWithLifecycle()
    var pickingDate by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (f.isNew) R.string.item_new else R.string.item_edit)) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (!f.isNew) {
                        IconButton(onClick = { viewModel.delete(onDone) }) {
                            Icon(Icons.Outlined.Delete, stringResource(R.string.action_delete))
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (!f.loaded) return@Scaffold
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (f.limitReached) {
                Text(
                    stringResource(R.string.item_limit, Plan.FREE_LIMIT),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            OutlinedTextField(
                value = f.name,
                onValueChange = viewModel::setName,
                label = { Text(stringResource(R.string.item_name)) },
                placeholder = { Text(stringResource(R.string.item_name_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )

            Label(stringResource(R.string.item_kind))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Kind.entries.forEach { k ->
                    FilterChip(selected = f.kind == k, onClick = { viewModel.setKind(k) }, label = { Text(stringResource(k.labelRes)) })
                }
            }

            Label(stringResource(R.string.item_due))
            OutlinedButton(onClick = { pickingDate = true }) {
                Icon(Icons.Outlined.DateRange, null)
                Text(
                    f.due.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            Label(stringResource(R.string.item_repeat))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Repeat.entries.forEach { r ->
                    FilterChip(selected = f.repeat == r, onClick = { viewModel.setRepeat(r) }, label = { Text(stringResource(r.labelRes)) })
                }
            }

            Label(stringResource(R.string.item_remind))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ItemViewModel.REMIND_OPTIONS.forEach { d ->
                    FilterChip(
                        selected = f.remindDaysBefore == d,
                        onClick = { viewModel.setRemind(d) },
                        label = {
                            Text(if (d == 0) stringResource(R.string.remind_same_day) else pluralStringResource(R.plurals.remind_days, d, d))
                        },
                    )
                }
            }

            OutlinedTextField(
                value = f.amount,
                onValueChange = viewModel::setAmount,
                label = { Text(stringResource(R.string.item_amount)) },
                placeholder = { Text(stringResource(R.string.item_amount_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = f.note,
                onValueChange = viewModel::setNote,
                label = { Text(stringResource(R.string.item_note)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = { viewModel.save(onDone) },
                enabled = f.canSave,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text(stringResource(R.string.action_save)) }
            Spacer(Modifier.height(8.dp).navigationBarsPadding())
        }
    }

    if (pickingDate) {
        // El selector trabaja en milisegundos UTC; el vencimiento es un día sin hora.
        val state = rememberDatePickerState(
            initialSelectedDateMillis = f.due.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { pickingDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { viewModel.setDue(it.toLocalDate()) }
                    pickingDate = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = { TextButton(onClick = { pickingDate = false }) { Text(stringResource(R.string.action_cancel)) } },
        ) { DatePicker(state) }
    }
}

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@Composable
private fun Label(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
