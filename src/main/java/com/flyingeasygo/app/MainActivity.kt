package com.flyingeasygo.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val WEBSITE_URL = "https://flyingeasygo.com"

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorLayout: LinearLayout
    private lateinit var retryButton: MaterialButton
    private lateinit var cacheManager: CacheManager

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.loadingProgressBar)
        errorLayout = findViewById(R.id.errorLayout)
        retryButton = findViewById(R.id.retryButton)
        
        cacheManager = CacheManager(applicationContext)

        // Configure WebView
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            loadsImagesAutomatically = true
        }

        // Set up WebView clients
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress < 100) {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                } else {
                    progressBar.visibility = View.GONE
                }
            }
        }

        webView.webViewClient = WebCacheInterceptor(cacheManager).apply {
            onPageFinished { _, _ ->
                progressBar.visibility = View.GONE
                errorLayout.visibility = View.GONE
            }
            onReceivedError { _, _, _, _ ->
                showError()
            }
        }

        // Set up retry button
        retryButton.setOnClickListener {
            loadWebsite()
        }

        // Initial website load
        loadWebsite()
    }

    private fun loadWebsite() {
        errorLayout.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        webView.loadUrl(WEBSITE_URL)
    }

    private fun showError() {
        webView.visibility = View.GONE
        progressBar.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
        
        Snackbar.make(
            findViewById(android.R.id.content),
            getString(R.string.error_loading),
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}