package com.webtopack

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context
import android.util.TypedValue

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var splashScreen: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up Splash Screen
        val appName = getString(R.string.app_name)
        val splashLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE) // Default background
            gravity = android.view.Gravity.CENTER
        }

        // Add an ImageView for the icon (if available)
        val iconImageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(120), dpToPx(120)).apply {
                bottomMargin = dpToPx(20)
            }
            // Attempt to load the app icon dynamically
            try {
                val iconResId = applicationInfo.icon
                setImageResource(iconResId)
            } catch (e: Exception) {
                // Fallback or log error if icon not found
                e.printStackTrace()
            }
        }
        splashLayout.addView(iconImageView)

        // Add a TextView for the app name
        val appNameTextView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            text = appName
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            setTextColor(Color.BLACK) // Default text color
        }
        splashLayout.addView(appNameTextView)

        setContentView(splashLayout)
        splashScreen = splashLayout

        // Initialize WebView after a delay to show splash screen
        splashScreen.postDelayed({ 
            setupWebView()
            splashScreen.visibility = View.GONE
        }, 2000) // Show splash screen for 2 seconds
    }

    private fun setupWebView() {
        webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            visibility = View.VISIBLE
        }
        setContentView(webView)

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK // Enable offline mode

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false // Load all links inside the WebView
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: android.webkit.WebResourceError?) {
                super.onReceivedError(view, request, error)
                if (!isNetworkAvailable(this@MainActivity)) {
                    // Load a local offline page if no network
                    view?.loadUrl("file:///android_asset/www/offline.html")
                }
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                // Implement service worker like caching or custom offline handling here if needed
                return super.shouldInterceptRequest(view, request)
            }
        }

        // Load the local index.html from assets/www
        webView.loadUrl("file:///android_asset/www/index.html")
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}
