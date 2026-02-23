package com.kisanseva.ai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ActionItem(
    title: String,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    subtitle: String? = null,
    icon: ImageVector,
    color: Color,
    size: Float = 1.0f,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = (8 * size).dp),
        shape = RoundedCornerShape((20 * size).dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke((1 * size).dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .padding((16 * size).dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size((52 * size).dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size((28 * size).dp)
                )
            }

            Spacer(modifier = Modifier.width((16 * size).dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = titleStyle,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionItemPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Size: 0.8f")
            ActionItem(
                title = "Crop Recommendation",
                titleStyle = MaterialTheme.typography.bodySmall,
                icon = Icons.Default.Eco,
                color = Color(0xFF4CAF50),
                size = 0.8f,
                onClick = {}
            )
            
            Spacer(modifier = Modifier.size(16.dp))
            
            Text("Size: 1.0f (Default)")
            ActionItem(
                title = "Crop Recommendation",
                subtitle = "Find the best crop for your soil",
                icon = Icons.Default.Eco,
                color = Color(0xFF4CAF50),
                size = 1.0f,
                onClick = {}
            )
            
            Spacer(modifier = Modifier.size(16.dp))
            
            Text("Size: 1.2f")
            ActionItem(
                title = "Crop Recommendation",
                subtitle = "Find the best crop for your soil",
                icon = Icons.Default.Eco,
                color = Color(0xFF4CAF50),
                size = 1.2f,
                onClick = {}
            )
        }
    }
}
