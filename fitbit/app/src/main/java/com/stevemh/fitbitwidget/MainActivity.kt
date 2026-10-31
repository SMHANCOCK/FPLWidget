package com.stevemh.fitbitwidget

import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import android.graphics.Color
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.containsAll(HealthSync.permissions)) refreshNow()
        else Toast.makeText(this, "Health Connect access not granted", Toast.LENGTH_LONG).show()
    }

    private fun refreshNow() {
        lifecycleScope.launch {
            try {
                val status = HealthSync.refresh(this@MainActivity)
                Toast.makeText(this@MainActivity, status, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Sync failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("health", MODE_PRIVATE)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(42, 60, 42, 24)
            setBackgroundColor(Color.rgb(32,32,32))
        }
        fun note(text: String) {
            root.addView(TextView(this).apply {
                this.text = text
                textSize = 16f
                setTextColor(Color.WHITE)
                setPadding(0, 12, 0, 12)
            })
        }
        note("Fitbit Health Widget v1.1")
        note("Connect to Health Connect for automatic sleep duration and HRV updates. Fitbit Sleep Score and Readiness still require manual entry.")
        root.addView(Button(this).apply {
            text = "Connect Health Connect"
            setOnClickListener {
                if (HealthConnectClient.getSdkStatus(this@MainActivity) == HealthConnectClient.SDK_AVAILABLE)
                    permissionLauncher.launch(HealthSync.permissions)
                else Toast.makeText(this@MainActivity, "Health Connect unavailable", Toast.LENGTH_LONG).show()
            }
        })
        root.addView(Button(this).apply {
            text = "Sync now"
            setOnClickListener { refreshNow() }
        })
        val entries = mutableMapOf<String,EditText>()
        listOf("sleep" to "Fitbit Sleep Score (manual)", "readiness" to "Fitbit Readiness (manual)").forEach { (key,label) ->
            root.addView(EditText(this).apply {
                hint = label
                setHintTextColor(Color.GRAY)
                setTextColor(Color.WHITE)
                setSingleLine(true)
                setText(prefs.getString(key,""))
                entries[key] = this
            })
        }
        root.addView(Button(this).apply {
            text = "Save Fitbit scores"
            setOnClickListener {
                val edit = prefs.edit()
                entries.forEach { (key,field) -> edit.putString(key,field.text.toString().trim().ifEmpty { "—" }) }
                edit.apply()
                HealthWidget.refresh(this@MainActivity)
                Toast.makeText(this@MainActivity, "Scores saved", Toast.LENGTH_SHORT).show()
            }
        })
        note("Optional screen-reading test: Android Accessibility can read visible Google Health labels when you open the app. This is a diagnostic only; it does not collect scores yet. It reads only supported health app screens, keeps the result on this phone, and does not upload any screen content. Accessibility permission can expose sensitive on-screen information, so only enable it if you accept that risk.")
        root.addView(Button(this).apply {
            text = "Enable score diagnostic (Accessibility settings)"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })
        root.addView(Button(this).apply {
            text = "Check score diagnostic"
            setOnClickListener {
                val result = prefs.getString("score_diagnostic", "No supported Google Health screen detected yet")
                Toast.makeText(this@MainActivity, result, Toast.LENGTH_LONG).show()
            }
        })
        note("Background refresh is scheduled every two hours. Actual timing depends on Android battery management and Health Connect data availability.")
        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
        HealthSync.schedule(this)
    }
}