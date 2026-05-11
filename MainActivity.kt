package com.webtopack

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.webkit.GeolocationPermissions
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var splashScreen: View
    private lateinit var progressBar: ProgressBar
    private lateinit var noInternetLayout: LinearLayout
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    private val FILE_CHOOSER_REQUEST_CODE = 1
    private val PERMISSION_REQUEST_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a root layout to hold splash screen, webview, progress bar, and no internet screen
        val rootLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            orientation = LinearLayout.VERTICAL
        }

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

        rootLayout.addView(splashLayout)
        splashScreen = splashLayout

        // Initialize WebView (hidden initially)
        webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            visibility = View.GONE // Hidden initially
        }
        rootLayout.addView(webView)

        // Initialize ProgressBar
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(4))
            visibility = View.GONE // Hidden initially
            progressDrawable.setColorFilter(Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN) // Example color
        }
        rootLayout.addView(progressBar)

        // Initialize No Internet Layout
        noInternetLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            visibility = View.GONE // Hidden initially
        }
        val noInternetText = TextView(this).apply {
            text = "لا يوجد اتصال بالإنترنت"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setTextColor(Color.GRAY)
            setPadding(0, 0, 0, dpToPx(20))
        }
        val retryButton = Button(this).apply {
            text = "إعادة المحاولة"
            setOnClickListener { reloadWebView() }
        }
        noInternetLayout.addView(noInternetText)
        noInternetLayout.addView(retryButton)
        rootLayout.addView(noInternetLayout)

        setContentView(rootLayout)

        // Initialize WebView after a delay to show splash screen
        splashScreen.postDelayed({ 
            setupWebView()
            splashScreen.visibility = View.GONE
            webView.visibility = View.VISIBLE
        }, 2000) // Show splash screen for 2 seconds
    }

    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK // Enable offline mode

        // Enable WebRTC and other features
        settings.mediaPlaybackRequiresUserGesture = false
        settings.setGeolocationEnabled(true)
        settings.setAppCacheEnabled(true)
        settings.databaseEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false // Load all links inside the WebView
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: android.webkit.WebResourceError?) {
                super.onReceivedError(view, request, error)
                if (!isNetworkAvailable(this@MainActivity)) {
                    showNoInternetScreen()
                } else {
                    // Handle other errors if needed
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                } else {
                    progressBar.visibility = View.VISIBLE
                }
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@MainActivity.filePathCallback = filePathCallback
                val intent = fileChooserParams?.createIntent()
                try {
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE)
                } catch (e: Exception) {
                    this@MainActivity.filePathCallback = null
                    return false
                }
                return true
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                // Request geolocation permission
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
                }
                callback?.invoke(origin, true, false) // Always grant for now, but handle user choice in onRequestPermissionsResult
            }
        }

        // Load the local index.html from assets/www or the provided URL
        // This part will be handled by the Python CLI based on user input
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

    private fun showNoInternetScreen() {
        webView.visibility = View.GONE
        progressBar.visibility = View.GONE
        noInternetLayout.visibility = View.VISIBLE
    }

    private fun hideNoInternetScreen() {
        noInternetLayout.visibility = View.GONE
        webView.visibility = View.VISIBLE
    }

    private fun reloadWebView() {
        hideNoInternetScreen()
        webView.reload()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (filePathCallback == null) return
            val result = if (data == null || resultCode != RESULT_OK) null else data.data
            var results: Array<Uri>? = null
            if (result != null) {
                results = arrayOf(result)
            }
            filePathCallback?.onReceiveValue(results)
            filePathCallback = null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Handle permission results here if needed, e.g., for geolocation
            // For now, we assume it's granted or handled by WebChromeClient
        }
    }
}
