package id.frendy.remoteac

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Simpan setelan terakhir, jadwal, dan event yang terjadi saat aplikasi tertutup. */
class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("ac", Context.MODE_PRIVATE)

    var cfg: JSONObject
        get() = try { JSONObject(sp.getString("cfg", "{}") ?: "{}") } catch (e: Exception) { JSONObject() }
        set(v) { sp.edit().putString("cfg", v.toString()).commit() }

    fun addEvent(o: JSONObject) {
        val a = try { JSONArray(sp.getString("ev", "[]")) } catch (e: Exception) { JSONArray() }
        a.put(o)
        sp.edit().putString("ev", a.toString()).commit()
    }

    fun takeEvents(): String {
        val s = sp.getString("ev", "[]") ?: "[]"
        sp.edit().putString("ev", "[]").commit()
        return s
    }

    fun ids(): List<Long> {
        val a = try { JSONArray(sp.getString("ids", "[]")) } catch (e: Exception) { JSONArray() }
        return (0 until a.length()).map { a.getLong(it) }
    }

    fun setIds(ids: List<Long>) {
        sp.edit().putString("ids", JSONArray(ids).toString()).commit()
    }
}
