package com.example.dodgeassistant

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
        }

        val title = TextView(this).apply {
            text = "Dodge Assistant"
            textSize = 26f
        }

        val info = TextView(this).apply {
            text = "Asistente de práctica flotante"
            textSize = 16f
        }

        val button = Button(this).apply {
            text = "Activar asistente"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                } else {
                    startService(
                        Intent(this@MainActivity, OverlayService::class.java)
                    )
                    info.text = "Asistente activado"
                }
            }
        }

        layout.addView(title)
        layout.addView(info)
        layout.addView(button)

        setContentView(layout)
    }
}
