package com.aam.health

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class MainActivity : android.app.Activity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true

        webView.addJavascriptInterface(AndroidBridge(), "Android")

        webView.loadUrl("file:///android_asset/index.html")

        setContentView(webView)
    }

    inner class AndroidBridge {

        @JavascriptInterface
        fun saveTextFile(
            name: String,
            mime: String,
            base64: String
        ) {
            try {

                val bytes = Base64.decode(
                    base64,
                    Base64.DEFAULT
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    val values = ContentValues().apply {
                        put(
                            MediaStore.Downloads.DISPLAY_NAME,
                            name
                        )

                        put(
                            MediaStore.Downloads.MIME_TYPE,
                            mime
                        )

                        put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            Environment.DIRECTORY_DOWNLOADS +
                                    "/AAM Health Management"
                        )

                        put(
                            MediaStore.Downloads.IS_PENDING,
                            1
                        )
                    }

                    val uri = contentResolver.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        values
                    )

                    if (uri == null) {
                        toast("File save nahi hua")
                        return
                    }

                    contentResolver.openOutputStream(uri)?.use {
                        it.write(bytes)
                    }

                    values.clear()

                    values.put(
                        MediaStore.Downloads.IS_PENDING,
                        0
                    )

                    contentResolver.update(
                        uri,
                        values,
                        null,
                        null
                    )

                    toast(
                        "Saved: Downloads/AAM Health Management"
                    )

                } else {

                    val dir = java.io.File(
                        Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS
                        ),
                        "AAM Health Management"
                    )

                    if (!dir.exists()) {
                        dir.mkdirs()
                    }

                    java.io.File(
                        dir,
                        name
                    ).outputStream().use {
                        it.write(bytes)
                    }

                    toast(
                        "Saved: Downloads/AAM Health Management"
                    )
                }

            } catch (e: Exception) {

                toast("File save failed")
            }
        }
    }

    private fun toast(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onBackPressed() {

        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
