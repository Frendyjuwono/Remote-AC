package id.frendy.remoteac

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import org.json.JSONArray
import java.util.Calendar

/** Pasang alarm Android untuk setiap jadwal aktif + timer "matikan dalam X jam". */
object Scheduler {
    const val OFF_TIMER_ID = 0L

    fun apply(ctx: Context) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val p = Prefs(ctx)
        val c = p.cfg
        for (id in p.ids()) am.cancel(pi(ctx, id))

        val ids = mutableListOf<Long>()
        val arr = c.optJSONArray("sched") ?: JSONArray()
        for (i in 0 until arr.length()) {
            val s = arr.getJSONObject(i)
            if (!s.optBoolean("en", true)) continue
            val days = s.optJSONArray("days") ?: continue
            val ds = (0 until days.length()).map { days.getInt(it) }
            val at = next(s.optString("t", "00:00"), ds) ?: continue
            val id = s.optLong("id", -1L)
            if (id <= 0L) continue
            set(am, at, pi(ctx, id))
            ids.add(id)
        }
        val off = c.optLong("offAt", 0L)
        if (off > System.currentTimeMillis()) {
            set(am, off, pi(ctx, OFF_TIMER_ID))
            ids.add(OFF_TIMER_ID)
        }
        p.setIds(ids)
    }

    /** days: 0 = Minggu ... 6 = Sabtu */
    private fun next(t: String, days: List<Int>): Long? {
        val parts = t.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val now = System.currentTimeMillis()
        for (d in 0..7) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, d)
            c.set(Calendar.HOUR_OF_DAY, h)
            c.set(Calendar.MINUTE, m)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            if (c.timeInMillis > now + 1000 && days.contains(c.get(Calendar.DAY_OF_WEEK) - 1)) {
                return c.timeInMillis
            }
        }
        return null
    }

    private fun pi(ctx: Context, id: Long): PendingIntent {
        val i = Intent(ctx, AlarmReceiver::class.java)
            .setAction("id.frendy.remoteac.ALARM.$id")
            .putExtra("id", id)
        return PendingIntent.getBroadcast(
            ctx, (id xor (id ushr 32)).toInt(), i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @SuppressLint("ScheduleExactAlarm", "MissingPermission")
    private fun set(am: AlarmManager, at: Long, pi: PendingIntent) {
        if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }
}
