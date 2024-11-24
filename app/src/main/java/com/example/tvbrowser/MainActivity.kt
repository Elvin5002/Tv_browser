package com.example.tvbrowser

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewTreeObserver
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.text.toFloat

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var cursorView: ImageView // İmleç için ImageView
    private var cursorX = 0f // İmlecin X koordinatı
    private var cursorY = 0f // İmlecin Y koordinatı
    private val cursorStep = 20f // Her hareket adımının mesafesi (piksel cinsinden)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        cursorView = findViewById(R.id.cursorView)

        // WebView ayarları
        val webSettings: WebSettings = webView.settings
        webView.isClickable = true
        webView.isFocusable = true
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true

        // WebView'de sayfa yükleme
        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://www.google.com")

        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                Toast.makeText(this@MainActivity, "Page Load Error", Toast.LENGTH_SHORT).show()
            }
        }

        cursorX = webView.width / 2f
        cursorY = webView.height / 2f
        webView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                updateCursorPosition()
                return true
            }
        })


    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    cursorX -= cursorStep
                    updateCursorPosition()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    cursorX += cursorStep
                    updateCursorPosition()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    cursorY -= cursorStep
                    updateCursorPosition()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    cursorY += cursorStep
                    updateCursorPosition()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_CENTER -> {
                    simulateClick()
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun updateCursorPosition() {
        cursorX = cursorX.coerceIn(0f, webView.width.toFloat())
        cursorY = cursorY.coerceIn(0f, webView.height.toFloat())

        cursorView.translationX = cursorX
        cursorView.translationY = cursorY

        if (cursorY <= 0) {
            smoothScroll(-dpToPx(170)) // Yukarı kaydır
        } else if (cursorY >= webView.height - cursorView.height) {
            smoothScroll(dpToPx(170)) // Aşağı kaydır
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun smoothScroll(dy: Int) {
        ObjectAnimator.ofInt(webView, "scrollY", webView.scrollY + dy).apply {
            duration = 100 // Kaydırma süresi (ms)
            start()
        }
    }

    private fun simulateClick() {
        // Simulate a click on the WebView element at the cursor position
        val cursorXPosition = cursorX.toInt()
        val cursorYPosition = cursorY.toInt()

        Log.d("Cursor", "Simulate click at: ($cursorXPosition, $cursorYPosition)")

        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis() + 100 // Add a small delay for up event

        val downEvent = MotionEvent.obtain(
            downTime,
            downTime,
            MotionEvent.ACTION_DOWN,
            cursorXPosition.toFloat(),
            cursorYPosition.toFloat(),
            0
        )
        webView.dispatchTouchEvent(downEvent)
        downEvent.recycle()

        val upEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_UP,
            cursorXPosition.toFloat(),
            cursorYPosition.toFloat(),
            0
        )
        webView.dispatchTouchEvent(upEvent)
        upEvent.recycle()

        // Alternatively, consider using webView.performClick() if appropriate
    }

}

