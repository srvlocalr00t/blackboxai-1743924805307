package com.flyingeasygo.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {
    private val TAG = "SplashActivity"
    private val SPLASH_DELAY = 2000L
    
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        progressBar = findViewById(R.id.progressBar)
        loadingText = findViewById(R.id.loadingText)

        val cacheManager = CacheManager(applicationContext)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(Dispatchers.IO) {
                    cacheManager.cacheResources()
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    startMainActivity()
                }, SPLASH_DELAY)
            } catch (e: Exception) {
                Log.e(TAG, "Error during resource caching", e)
                showError()
            }
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun showError() {
        progressBar.visibility = View.GONE
        loadingText.text = getString(R.string.cache_error)
        Snackbar.make(
            findViewById(android.R.id.content),
            getString(R.string.cache_error),
            Snackbar.LENGTH_INDEFINITE
        ).setAction("RETRY") {
            recreate()
        }.show()
    }
}