package id.frendy.remoteac

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.json.JSONArray
import org.json.JSONObject

/** Dipanggil saat jadwal / timer tiba — kirim IR walau aplikasi tertutup. */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val id = intent.getLongExtra("id", -1L)
        val p = Prefs(ctx)
        val c = p.cfg
        val variant = c.optInt("variant", 0)
        val swing = c.optInt("swing", 0)
        var temp = c.optInt("temp", 24)
        var fan = c.optInt("fan", 0)
        var on = false
        var act: String? = null

        if (id == Scheduler.OFF_TIMER_ID) {
            act = "off"
            c.put("offAt", 0L)
        } else {
            val arr = c.optJSONArray("sched") ?: JSONArray()
            for (i in 0 until arr.length()) {
                val s = arr.getJSONObject(i)
                if (s.optLong("id") == id && s.optBoolean("en", true)) {
                    act = s.optString("a", "off")
                    if (act == "on") {
                        on = true
                        temp = s.optInt("temp", temp)
                        fan = s.optInt("fan", fan)
                    }
                }
            }
        }

        if (act != null) {
            PanasonicIr.send(ctx, PanasonicIr.State(on, temp, fan, swing, variant))
            c.put("temp", temp).put("fan", fan)
            p.cfg = c
            p.addEvent(
                JSONObject().put("ts", System.currentTimeMillis())
                    .put("a", act).put("temp", temp).put("fan", fan)
            )
        }
        Scheduler.apply(ctx)
    }
}

/** Pasang ulang jadwal setelah HP restart. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) Scheduler.apply(ctx)
    }
}
