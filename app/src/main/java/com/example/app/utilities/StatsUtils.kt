package com.example.app.utilities

import android.app.Activity
import android.app.ActivityManager
import android.os.Debug
import android.os.Handler
import android.widget.TextView

class StatsUtils(private val activity: Activity) {

    private lateinit var memoryTextView: TextView
    private lateinit var cpuTextView: TextView

    private val handler = Handler()
    private val updateStatsRunnable = object : Runnable {
        override fun run() {
            updateStats()
            handler.postDelayed(this, 1000)
        }
    }

    fun setup(memoryTextViewId: Int, cpuTextViewId: Int) {
        memoryTextView = activity.findViewById(memoryTextViewId)
        cpuTextView = activity.findViewById(cpuTextViewId)
    }

    fun start() {
        handler.postDelayed(updateStatsRunnable, 1000)
    }

    fun stop() {
        handler.removeCallbacks(updateStatsRunnable)
    }

    private fun updateStats() {
        val activityManager = activity.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager
        val myPid = android.os.Process.myPid()
        val memoryInfoArray = activityManager.getProcessMemoryInfo(intArrayOf(myPid))
        val memoryInfo = memoryInfoArray.firstOrNull()
        val usedMemory = memoryInfo?.totalPss?.div(1024)
        val cpuUsage = Debug.threadCpuTimeNanos() / 1_000_000_000.0

        memoryTextView.text = "Memory Usage: ${usedMemory ?: "--"} MB"
        cpuTextView.text = "CPU Usage: ${String.format("%.2f", cpuUsage)} s"
    }
}
