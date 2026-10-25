package org.exarhteam.iitcprime.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.exarhteam.iitcprime.theme.Accent

@Composable
fun AboutView(
    onBack: () -> Unit
) {
    SettingsBase(
        title = L("about.title"),
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = L("app.name.release"),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Accent)
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "v0.42.5",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = L("about.description"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
