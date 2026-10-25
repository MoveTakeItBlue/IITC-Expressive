package org.exarhteam.iitcprime.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.exarhteam.iitcprime.i18n.L
import org.exarhteam.iitcprime.platform.PlatformUtils
import org.exarhteam.iitcprime.theme.Accent
import org.exarhteam.iitcprime.theme.SurfaceContainer

data class LicenseItem(val name: String, val url: String)

val DEPENDENCIES = listOf(
    LicenseItem("Compose Multiplatform", "https://www.jetbrains.com/lp/compose-multiplatform/"),
    LicenseItem("Kotlin", "https://kotlinlang.org/"),
    LicenseItem("IITC-CE", "https://github.com/IITC-CE/ingress-intel-total-conversion"),
    LicenseItem("Material 3", "https://m3.material.io/"),
    LicenseItem("Ktor Client", "https://ktor.io/"),
    LicenseItem("Okio", "https://square.github.io/okio/"),
    LicenseItem("Multiplatform Settings", "https://github.com/russhwolf/multiplatform-settings"),
    LicenseItem("Material Design Icons", "https://github.com/google/material-design-icons")
)

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
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(DEPENDENCIES) { index, item ->
                val isFirst = index == 0
                val isLast = index == DEPENDENCIES.size - 1

                val cornerShape = when {
                    isFirst && isLast -> RoundedCornerShape(12.dp)
                    isFirst -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    isLast -> RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                    else -> RoundedCornerShape(0.dp)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cornerShape)
                        .background(SurfaceContainer)
                        .clickable { PlatformUtils.openUrl(item.url) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = item.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.url,
                            color = Accent,
                            fontSize = 13.sp
                        )
                    }

                    if (!isLast) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
