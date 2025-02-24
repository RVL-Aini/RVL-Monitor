package com.example.sitemonitor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private val sites = listOf("https://google.com", "https://example.com")
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval: Long = 60000 // 60 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel()
        startMonitoring()
    }

    private fun startMonitoring() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                checkSites()
                handler.postDelayed(this, checkInterval)
            }
        }, checkInterval)
    }

    private fun checkSites() {
        for (site in sites) {
            thread {
                try {
                    val url = URL(site)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.connect()

                    if (connection.responseCode != 200) {
                        sendNotification(getString(R.string.site_unavailable), site)
                    }
                } catch (e: Exception) {
                    sendNotification(getString(R.string.connection_error), site)
                }
            }
        }
    }

    private fun sendNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, "monitor_channel")
            .setSmallIcon(R.drawable.ic_web_monitor)
            .setContentTitle(title)
            .setContentText(message)
