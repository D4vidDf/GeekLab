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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoChip(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DetailItem(
    icon: ImageVector, 
    label: String, 
    value: String,
    multiline: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = if (multiline) Alignment.Top else Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon, 
            contentDescription = null, 
            modifier = Modifier.size(16.dp).padding(top = if (multiline) 2.dp else 0.dp), 
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        
        if (multiline) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label, 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value, 
                    style = MaterialTheme.typography.bodySmall, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        } else {
            Text(
                text = label, 
                style = MaterialTheme.typography.bodyMedium, 
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = value, 
                style = MaterialTheme.typography.bodyMedium, 
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
        }
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
