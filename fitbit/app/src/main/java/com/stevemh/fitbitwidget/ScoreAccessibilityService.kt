package com.stevemh.fitbitwidget

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Opt-in, local-only feasibility test. Does not store screen content, perform
 * gestures, access other apps, or infer scores from unrelated numbers.
 */
class ScoreAccessibilityService : AccessibilityService() {
    override fun onInterrupt() = Unit

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (!isHealthPackage(pkg)) return
        val root = rootInActiveWindow ?: return
        val text = mutableListOf<String>()
        collect(root, text, 0)
        val labels = text.map { it.lowercase() }
        val sleepFound = labels.any { it.contains("sleep score") || it.contains("sleep quality") }
        val readyFound = labels.any { it.contains("readiness") || it.contains("daily readiness") }
        val prefs = getSharedPreferences("health", MODE_PRIVATE)
        prefs.edit()
            .putString("score_diagnostic", "Google Health visible · Sleep label: ${if (sleepFound) "yes" else "no"} · Readiness label: ${if (readyFound) "yes" else "no"}")
            .putLong("score_diagnostic_time", System.currentTimeMillis())
            .apply()
    }

    private fun isHealthPackage(pkg: String): Boolean =
        pkg == "com.fitbit.FitbitMobile" || pkg == "com.google.android.apps.healthdata" ||
            pkg == "com.google.android.apps.health"

    private fun collect(node: AccessibilityNodeInfo, result: MutableList<String>, depth: Int) {
        if (depth > 25 || result.size > 300) return
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let { result.add(it) }
        node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let { result.add(it) }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { collect(it, result, depth + 1) }
        }
    }
}