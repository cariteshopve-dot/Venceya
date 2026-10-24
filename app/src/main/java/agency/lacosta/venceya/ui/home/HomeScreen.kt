package agency.lacosta.venceya.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import agency.lacosta.venceya.R
import agency.lacosta.venceya.domain.Countdown
import agency.lacosta.venceya.domain.Item
import agency.lacosta.venceya.domain.Summary
import agency.lacosta.venceya.ui.item.labelRes
import agency.lacosta.venceya.ui.theme.Semaforo
import agency.lacosta.venceya.ui.theme.Signal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HomeScreen(
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.navigationBarsPadding(),
            ) { Icon(Icons.Outlined.Add, stringResource(R.string.action_add)) }
        },
    ) { _ ->
        val s = state
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Header(s?.today ?: LocalDate.now(), onSettings)
            when {
                s == null -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                s.items.isEmpty() -> Empty()
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item { SummaryRow(s.summary) }
                    items(s.items, key = { it.id }) { ItemRow(it, s.today) { onOpen(it.id) } }
                }
            }
        }
    }
}

@Composable
private fun Header(today: LocalDate, onSettings: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
            Text(
                today.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onSettings) {
            Icon(Icons.Outlined.Settings, stringResource(R.string.settings_title))
        }
    }
}

@Composable
private fun SummaryRow(summary: Summary) {
    val c = Semaforo.colors
    Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryTile(summary.thisWeek, stringResource(R.string.summary_week), c.red, Modifier.weight(1f))
        SummaryTile(summary.thisMonth, stringResource(R.string.summary_month), c.amber, Modifier.weight(1f))
        SummaryTile(summary.later, stringResource(R.string.summary_later), c.green, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryTile(count: Int, label: String, signal: Signal, modifier: Modifier) {
    Column(modifier.background(signal.soft, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 10.dp)) {
        Text("$count", color = signal.text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, color = signal.text, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun Empty() {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CountdownRing(fraction = 0.35f, color = Semaforo.colors.green.stroke, size = 72) {}
        Text(stringResource(R.string.home_empty_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
        Text(
            stringResource(R.string.home_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun ItemRow(item: Item, today: LocalDate, onClick: () -> Unit) {
    val left = item.daysLeft(today)
    val signal = Semaforo.colors.of(item.urgency(today))
    val short = Countdown.short(left)
    Row(
        Modifier
            .fillMaxWidth()
            .background(Semaforo.colors.row, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CountdownRing(Countdown.fraction(left, item.repeat), signal.stroke, size = 48) {
            Text(shortText(short), color = signal.stroke, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
            val sub = buildList {
                add(whenText(left, item.nextDue(today)))
                if (item.amount.isNotBlank()) add(item.amount)
                else add(stringResource(item.kind.labelRes))
            }.joinToString(" · ")
            Text(sub, style = MaterialTheme.typography.bodySmall, color = if (left < 0) signal.stroke else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Anillo que se llena a medida que se acerca la fecha. */
@Composable
private fun CountdownRing(fraction: Float, color: Color, size: Int, content: @Composable () -> Unit) {
    val track = Semaforo.colors.track
    Box(Modifier.size(size.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = this.size.minDimension * 0.12f
            val stroke = Stroke(width = w, cap = StrokeCap.Round)
            val inset = w / 2
            val arcSize = androidx.compose.ui.geometry.Size(this.size.width - w, this.size.height - w)
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(track, 0f, 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(w))
            drawArc(color, -90f, 360f * fraction, useCenter = false, topLeft = topLeft, size = arcSize, style = stroke)
        }
        content()
    }
}

@Composable
private fun shortText(s: Countdown.Short): String = when (s.span) {
    Countdown.Span.DAYS -> stringResource(R.string.short_days, s.value)
    Countdown.Span.MONTHS -> stringResource(R.string.short_months, s.value)
    Countdown.Span.YEARS -> stringResource(R.string.short_years, s.value)
}

@Composable
private fun whenText(left: Long, due: LocalDate): String = when {
    left < 0 -> pluralStringResource(R.plurals.days_overdue, (-left).toInt(), (-left).toInt())
    left == 0L -> stringResource(R.string.due_today)
    left == 1L -> stringResource(R.string.due_tomorrow)
    else -> due.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
}
