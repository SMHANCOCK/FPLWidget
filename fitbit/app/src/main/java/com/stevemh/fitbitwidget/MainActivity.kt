package com.stevemh.fitbitwidget
import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.widget.*
class MainActivity : Activity() {
 override fun onCreate(savedInstanceState:Bundle?) { super.onCreate(savedInstanceState)
  val prefs=getSharedPreferences("health",MODE_PRIVATE)
  val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(42,60,42,24); setBackgroundColor(Color.rgb(32,32,32)) }
  val heading=TextView(this).apply { text="Fitbit Health Widget"; textSize=26f; setTextColor(Color.WHITE) }; root.addView(heading)
  val note=TextView(this).apply { text="Enter your latest Fitbit values. This version does not sync automatically. Add the widget from your home screen widget picker."; textSize=15f; setTextColor(Color.LTGRAY); setPadding(0,16,0,20) }; root.addView(note)
  val entries=mutableMapOf<String,EditText>()
  listOf(Triple("sleep","Sleep score (0–100)","85"),Triple("readiness","Readiness (0–100)","67"),Triple("duration","Total sleep (e.g. 8 h 50 m)","8 h 50 m"),Triple("hrv","Today's HRV (e.g. 27 ms)","27 ms")).forEach { (key,label,example) ->
   val field=EditText(this).apply { hint=label; setHintTextColor(Color.GRAY); setTextColor(Color.WHITE); setSingleLine(true); setText(prefs.getString(key,"")); setPadding(8,20,8,20) }; root.addView(field); entries[key]=field
  }
  val save=Button(this).apply { text="Save and update widget"; setOnClickListener { val edit=prefs.edit(); entries.forEach { (key,field) -> edit.putString(key,field.text.toString().trim().ifEmpty { "—" }) }; edit.apply(); HealthWidget.refresh(this@MainActivity); Toast.makeText(this@MainActivity,"Widget updated",Toast.LENGTH_SHORT).show() } }; root.addView(save)
  val scroll=ScrollView(this); scroll.addView(root); setContentView(scroll)
 }
}