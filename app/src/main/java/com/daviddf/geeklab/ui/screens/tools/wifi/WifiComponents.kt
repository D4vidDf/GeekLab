package com.daviddf.geeklab.ui.screens.tools.wifi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daviddf.geeklab.ui.theme.GeekLabTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*

@Composable
fun InfoChip(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TechBadge(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ResultBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.extraLarge,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, color.copy(alpha = 0.5f))
    ) {
        BoxWithConstraints(contentAlignment = Alignment.Center) {
            val isSmall = maxWidth < 160.dp
            val verticalPadding = if (isSmall) 12.dp else 20.dp
            val horizontalPadding = if (isSmall) 8.dp else 16.dp
            
            Column(
                modifier = Modifier.padding(vertical = verticalPadding, horizontal = horizontalPadding), 
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label.uppercase(), 
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = if (isSmall) 9.sp else 11.sp
                    ), 
                    color = color,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value, 
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = if (isSmall) 20.sp else 28.sp
                    ),
                    fontWeight = FontWeight.ExtraBold, 
                    color = color,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Info Chip")
@Composable
fun InfoChipPreview() {
    GeekLabTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            InfoChip(Icons.Rounded.Speed, "Speed", "1200 Mbps")
        }
    }
}

@Preview(showBackground = true, name = "Tech Badge")
@Composable
fun TechBadgePreview() {
    GeekLabTheme {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TechBadge("Wi-Fi 6", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
            TechBadge("5 GHz", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Preview(showBackground = true, name = "Result Box")
@Composable
fun ResultBoxPreview() {
    GeekLabTheme {
        Row(modifier = Modifier.padding(16.dp).width(300.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ResultBox("Latency", "24ms", Color(0xFF4CAF50), Modifier.weight(1f))
            ResultBox("Jitter", "4ms", Color(0xFFFF9800), Modifier.weight(1f))
        }
    }
}
