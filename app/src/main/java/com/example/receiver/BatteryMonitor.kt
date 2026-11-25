package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class BatteryInfo(
    val percentage: Int = 100,
    val isCharging: Boolean = false
)

private fun Intent.toBatteryInfo(): BatteryInfo {
    val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    val status = getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
    val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
    return BatteryInfo(percentage = percent, isCharging = isCharging)
}

fun getBatteryFlow(context: Context): Flow<BatteryInfo> = callbackFlow {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            intent?.let { trySend(it.toBatteryInfo()) }
        }
    }

    val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    context.registerReceiver(receiver, filter)?.let { stickyIntent ->
        trySend(stickyIntent.toBatteryInfo())
    }

    awaitClose {
        try {
            context.unregisterReceiver(receiver)
        } catch (_: Exception) {
        }
    }
}
