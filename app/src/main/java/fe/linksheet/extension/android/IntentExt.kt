package fe.linksheet.extension.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.gson.JsonObject
import fe.clearurlskt.ClearURLLoader
import fe.clearurlskt.clearUrl
import fe.fastforwardkt.getRuleRedirect
import fe.linksheet.BuildConfig
import fe.linksheet.module.log.LoggerFactory
import fe.linksheet.module.log.UrlProcessor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


fun Intent.newIntent(uri: Uri?, dropExtras: Boolean = false) = Intent(this).apply {
    action = Intent.ACTION_VIEW
    data = uri
    flags = this@newIntent.flags and Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS.inv()
    `package` = null
    component = null

    if (dropExtras) {
        replaceExtras(null)
    }
}

fun Intent.getUri(): String? {
    var uriData = dataString
    if (uriData == null) {
        uriData = getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
    }

    if (uriData == null) {
        uriData = getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
    }

    return uriData
}

object IntentExt : KoinComponent {
    private val loggerFactory by inject<LoggerFactory>()
    private val logger = loggerFactory.createLogger(IntentExt::class)

    private val clearUrlProviders by lazy {
        ClearURLLoader.loadBuiltInClearURLProviders()
    }

    fun Intent.getUri(
        clearUrl: Boolean = false,
        fastForward: Boolean = false,
    ): Uri? {
        var uriData = dataString
        if (uriData == null) {
            uriData = getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
        }

        if (uriData == null) {
            uriData = getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
        }

        if (uriData != null) {
            val uri = Uri.parse(uriData)
            if (uri.host != null && uri.scheme != null) {
                var url = uri.toString()

                logger.debug("GetUri: Pre modification=%s", url, UrlProcessor)
                if (fastForward) {
                    getRuleRedirect(url)?.let { url = it }
                }

                logger.debug("GetUri: Post FastForward=%s", url, UrlProcessor)
                if (clearUrl) {
                    url = clearUrl(url, clearUrlProviders)
                }

                logger.debug("GetUri: Post ClearURL=%s", url, UrlProcessor)
                return Uri.parse(url)
            }
        }

        return null
    }
}


fun Intent.buildSendTo(uri: Uri?): Intent {
    return Intent.createChooser(this.apply {
        this.action = Intent.ACTION_SEND
        this.type = "text/plain"
        this.putExtra(Intent.EXTRA_TEXT, uri?.toString())
    }, null)
}