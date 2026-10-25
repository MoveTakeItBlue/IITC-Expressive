package org.exarhteam.iitcprime.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.exarhteam.iitcprime.i18n.L
import org.exarhteam.iitcprime.platform.PlatformUtils
import org.exarhteam.iitcprime.theme.*

data class LicenseItem(val name: String, val url: String)

val DEPENDENCIES = listOf(
    LicenseItem("Compose Multiplatform", "https://www.jetbrains.com/lp/compose-multiplatform/"),
    LicenseItem("Kotlin", "https://kotlinlang.org/"),
    LicenseItem("IITC-CE", "https://github.com/IITC-CE/ingress-intel-total-conversion"),
    LicenseItem("Material 3 Expressive", "https://m3.material.io/"),
    LicenseItem("Ktor Client", "https://ktor.io/"),
    LicenseItem("Okio", "https://square.github.io/okio/"),
    LicenseItem("Multiplatform Settings", "https://github.com/russhwolf/multiplatform-settings"),
    LicenseItem("Material Design Icons", "https://github.com/google/material-design-icons")
)

/**
 * Material 3 Expressive Licenses Screen
 */
@Composable
fun LicensesView(
    onBack: () -> Unit
) {
    SettingsBase(
        title = L("licenses.title"),
        onBack = onBack,
        useScroll = false
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(DEPENDENCIES, key = { _, item -> item.name }) { _, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerDark)
                        .border(width = 1.dp, color = OutlineSubtle, shape = RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = AccentTeal),
                            onClick = { PlatformUtils.openUrl(item.url) }
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.name,
                            color = OnSurfaceWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.url,
                            color = AccentTeal,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = OnSurfaceVariantMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
