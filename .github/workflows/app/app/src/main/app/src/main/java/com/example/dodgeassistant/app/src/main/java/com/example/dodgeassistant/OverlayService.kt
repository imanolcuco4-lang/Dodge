package com.example.dodgeassistant

import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import android.os.Handler
import android.os.Looper

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubble: TextView? = null
    private val handler = Handler(Looper.getMainLooper())
    private var seconds = 0
    private var running = false

    private val timer = object : Runnable {
        override fun run() {
            if (running) {
                seconds++
                bubble?.text = "⏱ ${seconds}s"
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        val channelId = "dodge_assistant"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Dodge Assistant",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Dodge Assistant")
            .setContentText("Asistente de práctica activo")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()

        startForeground(1, notification)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        bubble = TextView(this).apply {
            text = "▶ Iniciar"
            textSize = 14f
            setPadding(24, 18, 24, 18)
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xDD222222.toInt())

            setOnClickListener {
                running = !running
                if (running) {
                    seconds = 0
                    text = "⏱ 0s"
                    handler.post(timer)
                } else {
                    handler.removeCallbacks(timer)
                    text = "▶ ${seconds}s"
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }

        bubble?.setOnTouchListener(object : android.view.View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var moved = false

            override fun onTouch(v: android.view.View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        moved = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (kotlin.math.abs(dx) > 8 || kotlin.math.abs(dy) > 8) {
                            moved = true
                        }
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager.updateViewLayout(bubble, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!moved) v.performClick()
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(bubble, params)
    }

    override fun onDestroy() {
        running = false
        handler.removeCallbacks(timer)
        bubble?.let { windowManager.removeView(it) }
        bubble = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
