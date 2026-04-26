package com.daviddf.geeklab.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.theme.GeekLabTheme
import androidx.compose.ui.res.stringResource

@Composable
fun DetailItem(
    icon: ImageVector, 
    label: String, 
    value: String,
    multiline: Boolean = false,
) {
    val labelStyle = MaterialTheme.typography.bodyMedium
    val valueStyle = MaterialTheme.typography.bodyMedium
    val iconColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val valueColor = MaterialTheme.colorScheme.onSurface

    Layout(
        content = {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                modifier = Modifier.size(16.dp), 
                tint = iconColor
            )
            Text(
                text = label, 
                style = labelStyle, 
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value, 
                style = valueStyle, 
                fontWeight = FontWeight.Bold,
                color = valueColor,
                textAlign = TextAlign.End
            )
        }
    ) { measurables, constraints ->
        val iconSpacing = 12.dp.roundToPx()
        val horizontalGap = 16.dp.roundToPx()
        val verticalGap = 4.dp.roundToPx()
        
        val iconPlaceable = measurables[0].measure(constraints.copy(minWidth = 0, minHeight = 0))
        val availableWidth = (constraints.maxWidth - iconPlaceable.width - iconSpacing).coerceAtLeast(0)
        
        // Measure label and value to see if they fit on one line
        val labelPlaceable = measurables[1].measure(constraints.copy(minWidth = 0, maxWidth = availableWidth))
        val valuePlaceable = measurables[2].measure(constraints.copy(minWidth = 0, maxWidth = availableWidth))
        
        val fitsInOneLine = !multiline && (labelPlaceable.width + valuePlaceable.width + horizontalGap <= availableWidth)
        
        if (fitsInOneLine) {
            val height = maxOf(iconPlaceable.height, labelPlaceable.height, valuePlaceable.height)
            layout(constraints.maxWidth, height) {
                iconPlaceable.placeRelative(0, (height - iconPlaceable.height) / 2)
                labelPlaceable.placeRelative(iconPlaceable.width + iconSpacing, (height - labelPlaceable.height) / 2)
                valuePlaceable.placeRelative(constraints.maxWidth - valuePlaceable.width, (height - valuePlaceable.height) / 2)
            }
        } else {
            val height = labelPlaceable.height + verticalGap + valuePlaceable.height
            layout(constraints.maxWidth, height) {
                // In multiline, center icon vertically with the first line (label)
                iconPlaceable.placeRelative(0, (labelPlaceable.height - iconPlaceable.height).coerceAtLeast(0) / 2)
                labelPlaceable.placeRelative(iconPlaceable.width + iconSpacing, 0)
                valuePlaceable.placeRelative(constraints.maxWidth - valuePlaceable.width, labelPlaceable.height + verticalGap)
            }
        }
    }
}

@Preview(showBackground = true, name = "Auto Responsive")
@Composable
fun DetailItemAutoPreview() {
    GeekLabTheme {
        Card(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            )
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailItem(Icons.Rounded.Numbers, stringResource(R.string.wifi_ip_address), "192.168.1.1")
                DetailItem(Icons.Rounded.Security, stringResource(R.string.wifi_security), "WPA2/WPA3 (Personal) with long enterprise configuration")
                DetailItem(Icons.Rounded.Tag, stringResource(R.string.package_name), "com.daviddf.geeklab.technical.inspector.widgets")
            }
        }
    }
}

@Preview(showBackground = true, name = "Technical Sheet")
@Composable
fun DetailItemSheetPreview() {
    GeekLabTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.technical_recon),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                
                DetailItem(Icons.Rounded.Numbers, stringResource(R.string.hardware_id), "SCAN-NDEF-77291-B")
                DetailItem(Icons.Rounded.Security, stringResource(R.string.encryption), "AES-256-GCM (Hardware Backed)")
                DetailItem(Icons.Rounded.Tag, stringResource(R.string.namespace), "com.daviddf.geeklab.system.services.internal")
            }
        }
    }
}
