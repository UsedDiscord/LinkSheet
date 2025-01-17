package fe.linksheet.composable.settings.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import fe.linksheet.BuildConfig
import fe.linksheet.R
import fe.linksheet.composable.settings.SettingsScaffold
import fe.linksheet.composable.util.ColoredIcon
import fe.linksheet.composable.util.SettingsItemRow
import fe.linksheet.composable.util.SubtitleText
import fe.linksheet.creditsSettingsRoute
import fe.linksheet.donationLink
import fe.linksheet.lineSeparator
import fe.linksheet.linksheetGithub


@Composable
fun AboutSettingsRoute(
    navController: NavHostController,
    onBackPressed: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current

    SettingsScaffold(R.string.about, onBackPressed = onBackPressed) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxHeight(),
            contentPadding = PaddingValues(horizontal = 5.dp)
        ) {
            item(creditsSettingsRoute) {
                SettingsItemRow(
                    navController = navController,
                    navigateTo = creditsSettingsRoute,
                    headlineId = R.string.credits,
                    subtitleId = R.string.credits_explainer,
                    image = {
                        ColoredIcon(icon = Icons.Default.Link, descriptionId = R.string.credits)
                    }
                )
            }

            item("github") {
                SettingsItemRow(
                    headlineId = R.string.github,
                    subtitleId = R.string.github_explainer,
                    onClick = {
                        uriHandler.openUri(linksheetGithub)
                    },
                    image = {
                        ColoredIcon(icon = Icons.Default.Home, descriptionId = R.string.github)
                    }
                )
            }

            item("donate") {
                SettingsItemRow(
                    headlineId = R.string.donate,
                    subtitleId = R.string.donate_explainer,
                    onClick = {
                        uriHandler.openUri(donationLink)
                    },
                    image = {
                        ColoredIcon(
                            icon = Icons.Default.CurrencyBitcoin,
                            descriptionId = R.string.donate
                        )
                    }
                )
            }

            item("version") {
                SettingsItemRow(
                    headline = stringResource(id = R.string.version),
                    subtitle = BuildConfig.VERSION_NAME,
                    onClick = {
                        clipboardManager.setText(buildAnnotatedString {
                            append(BuildConfig.VERSION_NAME, lineSeparator, BuildConfig.VERSION_CODE.toString())
                        })
                    },
                    image = {
                        ColoredIcon(icon = Icons.Default.Info, descriptionId = R.string.version)
                    },
                    content = {
                        SubtitleText(subtitle = BuildConfig.VERSION_CODE.toString())
                    }
                )
            }
        }
    }
}
