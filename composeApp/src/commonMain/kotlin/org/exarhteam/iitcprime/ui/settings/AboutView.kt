package org.exarhteam.iitcprime.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.exarhteam.iitcprime.i18n.L

/**
 * Authentic Material 3 Expressive About Screen
 */
@Composable
fun AboutView(
    onBack: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    SettingsBase(
        title = L("about.title"),
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(colorScheme.surfaceContainer)
                .padding(vertical = 28.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Icon Circle
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    tint = colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = L("app.name.release"),
                color = colorScheme.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Version Pill Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(colorScheme.secondaryContainer)
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "v0.42.5 (Expressive)",
                    color = colorScheme.onSecondaryContainer,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = L("about.description"),
                color = colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        SettingsSection(title = L("about.section.links"))

        LinkSettingsItem(
            title = L("about.iitc_website.title"),
            description = L("about.iitc_website.url"),
            url = "https://iitc.app",
            isFirst = true
        )
        LinkSettingsItem(
            title = L("about.iitc_github.title"),
            description = L("about.iitc_github.url"),
            url = "https://github.com/IITC-CE/ingress-intel-total-conversion"
        )
        LinkSettingsItem(
            title = L("about.prime_github.title"),
            description = L("about.prime_github.url"),
            url = "https://github.com/IITC-CE/IITC-Prime",
            isLast = true
        )
    }
}
