package com.flyingeasygo.app

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class CacheManager(private val context: Context) {
    private val TAG = "CacheManager"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://flyingeasygo.com"
    
    // List of resources to cache for faster loading
    private val resourcesToCache = listOf(
        "/assets/css/style.css",
        "/assets/js/main.js",
        "/assets/images/logo.png",
        "/assets/js/jquery.min.js",
        "/assets/js/bootstrap.min.js",
        "/assets/css/bootstrap.min.css"
    )

    suspend fun cacheResources() = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "web_cache")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        resourcesToCache.forEach { resource ->
            try {
                val request = Request.Builder()
                    .url(baseUrl + resource)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body
                    if (body != null) {
                        val fileName = resource.substring(resource.lastIndexOf("/") + 1)
                        val file = File(cacheDir, fileName)
                        file.outputStream().use { fileOut ->
                            body.byteStream().use { bodyStream ->
                                bodyStream.copyTo(fileOut)
                            }
                        }
                        Log.d(TAG, "Successfully cached: $fileName")
                    }
                } else {
                    Log.e(TAG, "Failed to cache $resource: ${response.code}")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error caching $resource", e)
            }
        }
    }

    fun getCachedFile(url: String): File? {
        val fileName = url.substring(url.lastIndexOf("/") + 1)
        val file = File(context.cacheDir.absolutePath + "/web_cache/" + fileName)
        return if (file.exists()) {
            Log.d(TAG, "Serving cached file: $fileName")
            file
        } else {
            Log.d(TAG, "Cache miss for: $fileName")
            null
        }
    }

    fun clearCache() {
        val cacheDir = File(context.cacheDir, "web_cache")
        if (cacheDir.exists()) {
            cacheDir.deleteRecursively()
            Log.d(TAG, "Cache cleared")
        }
    }
}