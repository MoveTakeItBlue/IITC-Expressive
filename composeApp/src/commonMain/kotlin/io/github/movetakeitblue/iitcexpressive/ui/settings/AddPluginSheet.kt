package io.github.movetakeitblue.iitcexpressive.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.movetakeitblue.iitcexpressive.i18n.L

/**
 * Material 3 Expressive Add Plugin BottomSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPluginSheet(
    initialUrl: String? = null,
    onLoadUrl: (String) -> Unit,
    onChooseFile: () -> Unit,
    onDismiss: () -> Unit
) {
    var urlText by remember { mutableStateOf(initialUrl ?: "") }
    val colorScheme = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = L("add_plugin.title"),
                color = colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // URL input field
            OutlinedTextField(
                value = urlText,
                onValueChange = { urlText = it },
                placeholder = {
                    Text(
                        text = L("add_plugin.url_hint"),
                        color = colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Link, contentDescription = null, tint = colorScheme.primary)
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colorScheme.surfaceContainer),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface,
                    cursorColor = colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Load from URL Button
            FilledTonalButton(
                onClick = {
                    if (urlText.isNotBlank()) {
                        onLoadUrl(urlText.trim())
                    }
                },
                enabled = urlText.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = colorScheme.primaryContainer,
                    contentColor = colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(percent = 50)
            ) {
                Text(
                    text = L("add_plugin.load_url"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Divider row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = colorScheme.outlineVariant)
                Text(
                    text = L("add_plugin.or"),
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = colorScheme.outlineVariant)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Choose file button
            FilledTonalButton(
                onClick = onChooseFile,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = colorScheme.surfaceContainerHigh,
                    contentColor = colorScheme.onSurface
                ),
                shape = RoundedCornerShape(percent = 50)
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = L("add_plugin.choose_file"),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
