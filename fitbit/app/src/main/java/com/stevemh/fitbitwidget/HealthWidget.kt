package com.stevemh.fitbitwidget
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
class HealthWidget : AppWidgetProvider() {
 override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) { ids.forEach { update(context, manager, it) } }
 companion object {
  fun refresh(context: Context) { val m=AppWidgetManager.getInstance(context); m.getAppWidgetIds(ComponentName(context,HealthWidget::class.java)).forEach { update(context,m,it) } }
  private fun update(context: Context,m: AppWidgetManager,id:Int) {
   val prefs=context.getSharedPreferences("health",Context.MODE_PRIVATE)
   val rv=RemoteViews(context.packageName,R.layout.widget)
   listOf(R.id.value_sleep to "sleep",R.id.value_readiness to "readiness",R.id.value_duration to "duration",R.id.value_hrv to "hrv").forEach { (view,key) -> rv.setTextViewText(view,prefs.getString(key,"—")) }
   val pi=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
   rv.setOnClickPendingIntent(R.id.root,pi)
   m.updateAppWidget(id,rv)
  }
 }
}