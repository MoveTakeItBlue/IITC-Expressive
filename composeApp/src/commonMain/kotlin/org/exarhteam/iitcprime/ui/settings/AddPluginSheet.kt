package org.exarhteam.iitcprime.ui.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.exarhteam.iitcprime.i18n.L
import org.exarhteam.iitcprime.theme.*

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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceContainerDark,
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
                color = OnSurfaceWhite,
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
                        color = OnSurfaceVariantMuted
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Link, contentDescription = null, tint = AccentTeal)
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerHighDark),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentTeal,
                    unfocusedBorderColor = OutlineSubtle,
                    focusedTextColor = OnSurfaceWhite,
                    unfocusedTextColor = OnSurfaceWhite,
                    cursorColor = AccentTeal
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
                    containerColor = AccentContainerTeal,
                    contentColor = AccentTeal
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
                HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineSubtle)
                Text(
                    text = L("add_plugin.or"),
                    color = OnSurfaceVariantMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineSubtle)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Choose file button
            OutlinedButton(
                onClick = onChooseFile,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = OnSurfaceWhite
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle),
                shape = RoundedCornerShape(percent = 50)
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = AccentTeal,
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
