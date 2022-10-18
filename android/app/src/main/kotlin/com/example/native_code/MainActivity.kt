package com.example.native_code

import android.content.BroadcastReceiver
import android.content.Context
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.os.BatteryManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.content.Intent
import android.content.IntentFilter
import android.content.ContextWrapper
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import io.flutter.plugin.common.EventChannel


class MainActivity : FlutterActivity() {
    private val CHANNEL = "battery"
    private val EVENT_CHANNEL = "battery_event"
    private lateinit var channel: MethodChannel
    private lateinit var eventChannel: EventChannel
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        channel =
            MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
        channel.setMethodCallHandler { call, result ->
            if (call.method == "getBatteryLevel") {
                val arguments = call.arguments as Map<String, String>
                val name = arguments["name"]
                val batteryLevel = getBatteryLevel()
                result.success("$name says: $batteryLevel")
            } else {
                result.notImplemented()
            }
        }

        eventChannel = EventChannel(flutterEngine.dartExecutor.binaryMessenger, EVENT_CHANNEL)
        eventChannel.setStreamHandler(MyStreamHandler(context))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Handler(Looper.getMainLooper()).postDelayed({
            val batteryLevel = getBatteryLevel()
            channel.invokeMethod("onBatteryChanged", batteryLevel)
        }, 0)
    }


    private fun getBatteryLevel(): Int {
        val batteryLevel: Int
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } else {
            val intent = ContextWrapper(applicationContext).registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            batteryLevel =
                intent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100 / intent.getIntExtra(
                    BatteryManager.EXTRA_SCALE,
                    -1
                )
        }

        return batteryLevel
    }

}


class MyStreamHandler(private val context: Context) : EventChannel.StreamHandler {
    private var receiver: BroadcastReceiver? = null

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        if (events == null) return
        receiver = initReceiver(events)
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onCancel(arguments: Any?) {
        context.unregisterReceiver(receiver)
        receiver = null
    }

    private fun initReceiver(events: EventChannel.EventSink): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val status = intent!!.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                when (status) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> events.success("charging")
                    BatteryManager.BATTERY_STATUS_FULL -> events.success("full")
                    BatteryManager.BATTERY_STATUS_DISCHARGING -> events.success("discharging")
                    BatteryManager.BATTERY_STATUS_NOT_CHARGING -> events.success("not charging")
                    BatteryManager.BATTERY_STATUS_UNKNOWN -> events.success("unknown")
                }
            }
        }
    }
}
