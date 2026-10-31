package com.stevemh.fitbitwidget

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

object HealthSync {
    val permissions = setOf(
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class)
    )

    fun schedule(context: Context) {
        val job = PeriodicWorkRequestBuilder<SyncWorker>(2, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "health-connect-refresh", ExistingPeriodicWorkPolicy.KEEP, job
        )
    }

    suspend fun refresh(context: Context): String {
        if (HealthConnectClient.getSdkStatus(context) != HealthConnectClient.SDK_AVAILABLE) {
            return "Health Connect is unavailable"
        }
        val client = HealthConnectClient.getOrCreate(context)
        val granted = client.permissionController.getGrantedPermissions()
        if (!granted.containsAll(permissions)) return "Health Connect permissions required"
        val prefs = context.getSharedPreferences("health", Context.MODE_PRIVATE)
        val now = Instant.now()
        val start = now.minus(48, ChronoUnit.HOURS)
        val sleeps = client.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, now),
                ascendingOrder = false,
                pageSize = 100
            )
        ).records
        val latestSleep = sleeps.maxByOrNull { it.endTime }
        val hrvRecords = client.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateVariabilityRmssdRecord::class,
                timeRangeFilter = TimeRangeFilter.between(now.minus(36, ChronoUnit.HOURS), now),
                ascendingOrder = false,
                pageSize = 100
            )
        ).records
        val latestHrv = hrvRecords.maxByOrNull { it.time }
        val edit = prefs.edit()
        if (latestSleep != null) {
            val mins = java.time.Duration.between(latestSleep.startTime, latestSleep.endTime).toMinutes()
            edit.putString("duration", "${mins / 60} h ${mins % 60} m")
        }
        if (latestHrv != null) edit.putString("hrv", "${latestHrv.heartRateVariabilityMillis.toInt()} ms")
        edit.putString("last_sync", java.time.ZonedDateTime.now().toLocalDateTime().toString().replace('T',' '))
        edit.apply()
        HealthWidget.refresh(context)
        return "Updated: ${if(latestSleep != null) "sleep" else "no sleep data"}, ${if(latestHrv != null) "HRV" else "no HRV data"}"
    }
}

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            HealthSync.refresh(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}