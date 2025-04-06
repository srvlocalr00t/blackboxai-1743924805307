package com.flyingeasygo.app

import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.FileInputStream
import java.io.IOException

class WebCacheInterceptor(private val cacheManager: CacheManager) : WebViewClient() {
    private val TAG = "WebCacheInterceptor"

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        val url = request.url.toString()
        
        // Check if the requested resource is potentially cached
        if (url.contains("/assets/")) {
            val cachedFile = cacheManager.getCachedFile(url)
            if (cachedFile != null) {
                val mimeType = when {
                    url.endsWith(".css") -> "text/css"
                    url.endsWith(".js") -> "application/javascript"
                    url.endsWith(".png") -> "image/png"
                    url.endsWith(".jpg", ignoreCase = true) -> "image/jpeg"
                    url.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                    url.endsWith(".gif") -> "image/gif"
                    url.endsWith(".svg") -> "image/svg+xml"
                    url.endsWith(".woff") -> "application/font-woff"
                    url.endsWith(".woff2") -> "application/font-woff2"
                    url.endsWith(".ttf") -> "application/x-font-ttf"
                    url.endsWith(".eot") -> "application/vnd.ms-fontobject"
                    else -> "text/plain"
                }
                
                return try {
                    val inputStream = FileInputStream(cachedFile)
                    Log.d(TAG, "Serving cached resource: $url")
                    WebResourceResponse(
                        mimeType,
                        "UTF-8",
                        inputStream
                    ).apply {
                        responseHeaders = mapOf(
                            "Access-Control-Allow-Origin" to "*",
                            "Cache-Control" to "public, max-age=31536000"
                        )
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Error serving cached file for $url", e)
                    null
                }
            }
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        Log.d(TAG, "Page finished loading: $url")
    }

    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        Log.e(TAG, "WebView error: $errorCode - $description for URL: $failingUrl")
    }
}