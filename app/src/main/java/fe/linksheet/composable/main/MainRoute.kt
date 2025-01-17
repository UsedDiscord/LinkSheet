package fe.linksheet.composable.main

import android.app.Activity
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import fe.linksheet.R
import fe.linksheet.composable.util.ColoredIcon
import fe.linksheet.extension.compose.currentActivity
import fe.linksheet.extension.compose.observeAsState
import fe.linksheet.module.viewmodel.MainViewModel
import fe.linksheet.settingsRoute
import fe.linksheet.ui.HkGroteskFontFamily
import fe.linksheet.util.AndroidVersion
import fe.linksheet.util.Results
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainRoute(
    navController: NavHostController,
    viewModel: MainViewModel = koinViewModel()
) {
    val activity = LocalContext.currentActivity()
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current

    var defaultBrowserEnabled by remember { mutableStateOf(Results.loading()) }
    var sheetOpen by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        delay(200)
        defaultBrowserEnabled = Results.result(viewModel.checkDefaultBrowser())
    }

    val lifecycleState = LocalLifecycleOwner.current.lifecycle
        .observeAsState(ignoreFirst = Lifecycle.Event.ON_RESUME)

    LaunchedEffect(lifecycleState.first) {
        if (lifecycleState.first == Lifecycle.Event.ON_RESUME) {
            defaultBrowserEnabled = Results.loading()
            defaultBrowserEnabled = Results.result(viewModel.checkDefaultBrowser())

            sheetOpen = null
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        TopAppBar(title = {}, modifier = Modifier.padding(horizontal = 8.dp), navigationIcon = {
            IconButton(onClick = { navController.navigate(settingsRoute) }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(id = R.string.settings)
                )
            }
        })
    }) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 5.dp)
        ) {
            item(key = "title") {
                Row(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraLarge)
                        .padding(horizontal = 12.dp)
                        .padding(top = 12.dp, bottom = 3.dp)
                ) {
                    Text(
                        modifier = Modifier,
                        text = stringResource(R.string.app_name),
                        fontFamily = HkGroteskFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 30.sp,
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))
            }

            item(key = "open_default_browser") {
                OpenDefaultBrowserCard(
                    activity = activity,
                    defaultBrowserEnabled = defaultBrowserEnabled,
                    defaultBrowserChanged = { defaultBrowserEnabled = it },
                    viewModel = viewModel
                )
            }

            // sheetOpen is used to avoid the card flickering since clipboardManager.hasText() returns null once the activity looses focus
            if (clipboardManager.hasText() || sheetOpen != null) {
                val item = clipboardManager.getText()?.text

                if ((item != null && Patterns.WEB_URL.matcher(item)
                        .matches()) || sheetOpen != null
                ) {
                    item(key = "open_copied_link") {
                        OpenCopiedLink(
                            uriHandler = uriHandler,
                            item = item ?: sheetOpen!!,
                            sheetOpen = {
                                sheetOpen = item
                            }
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun OpenDefaultBrowserCard(
    activity: Activity,
    defaultBrowserEnabled: Results<Unit>,
    defaultBrowserChanged: (Results<Unit>) -> Unit,
    viewModel: MainViewModel
) {
    val browserLauncherAndroidQPlus = if (AndroidVersion.AT_LEAST_API_29_Q) {
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {
                defaultBrowserChanged(
                    if (it.resultCode == Activity.RESULT_OK) Results.success()
                    else Results.error()
                )
            }
        )
    } else null

    val shouldUsePrimaryColor = defaultBrowserEnabled.isSuccess || defaultBrowserEnabled.isLoading
    Card(
        colors = CardDefaults.cardColors(containerColor = if (shouldUsePrimaryColor) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.error),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clickable {
                if (defaultBrowserEnabled.isLoading) {
                    return@clickable
                }

                if (AndroidVersion.AT_LEAST_API_29_Q && !defaultBrowserEnabled.isSuccess) {
                    val intent = viewModel.getRequestRoleBrowserIntent()
                    browserLauncherAndroidQPlus!!.launch(intent)
                } else {
                    viewModel.openDefaultBrowserSettings(activity)
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            val color =
                if (shouldUsePrimaryColor) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onError

            if (defaultBrowserEnabled.isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = color)
                }
            } else {
                Spacer(modifier = Modifier.width(10.dp))
                ColoredIcon(
                    icon = if (defaultBrowserEnabled.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                    descriptionId = if (defaultBrowserEnabled.isSuccess) R.string.checkmark else R.string.error,
                    color = color
                )

                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = stringResource(id = if (defaultBrowserEnabled.isSuccess) R.string.browser_status else R.string.set_as_browser),
                        fontFamily = HkGroteskFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = color
                    )
                    Text(
                        text = stringResource(id = if (defaultBrowserEnabled.isSuccess) R.string.set_as_browser_done else R.string.set_as_browser_explainer),
                        color = if (defaultBrowserEnabled.isSuccess) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun OpenCopiedLink(uriHandler: UriHandler, item: String, sheetOpen: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clickable {
                sheetOpen()
                uriHandler.openUri(item)
            },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.width(10.dp))

            ColoredIcon(icon = Icons.Default.ContentPaste, descriptionId = R.string.paste)

            Column(modifier = Modifier.padding(15.dp)) {
                Text(
                    text = stringResource(id = R.string.open_copied_link),
                    fontFamily = HkGroteskFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                )
                Text(text = item)
            }
        }
    }
}