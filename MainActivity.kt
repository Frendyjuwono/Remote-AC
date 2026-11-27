package id.frendy.remoteac

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {
    private lateinit var web: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.addJavascriptInterface(Bridge(this), "Android")
        setContentView(web)
        web.loadUrl("file:///android_asset/index.html")
    }

    override fun onResume() {
        super.onResume()
        if (::web.isInitialized) web.evaluateJavascript("window.onNative&&window.onNative()", null)
    }
}

/** Jembatan: tampilan HTML (JavaScript) memanggil fungsi-fungsi ini lewat window.Android */
class Bridge(private val act: Activity) {
    private val p = Prefs(act)

    @JavascriptInterface
    fun hasIr(): Boolean = PanasonicIr.hasIr(act)

    @JavascriptInterface
    fun send(power: Boolean, temp: Int, fan: Int, swing: Int, variant: Int): Boolean {
        val c = p.cfg
        c.put("temp", temp).put("fan", fan).put("swing", swing).put("variant", variant)
        p.cfg = c
        return PanasonicIr.send(act, PanasonicIr.State(power, temp, fan, swing, variant))
    }

    @JavascriptInterface
    fun sync(json: String) {
        val j = try { JSONObject(json) } catch (e: Exception) { return }
        val c = p.cfg
        c.put("sched", j.optJSONArray("sched") ?: JSONArray())
        c.put("offAt", j.optLong("offAt", 0L))
        if (j.has("swing")) c.put("swing", j.optInt("swing"))
        if (j.has("variant")) c.put("variant", j.optInt("variant"))
        if (j.has("temp")) c.put("temp", j.optInt("temp"))
        if (j.has("fan")) c.put("fan", j.optInt("fan"))
        p.cfg = c
        Scheduler.apply(act)
    }

    @JavascriptInterface
    fun takeEvents(): String = p.takeEvents()

    @JavascriptInterface
    fun exactOk(): Boolean =
        Build.VERSION.SDK_INT < 31 ||
            (act.getSystemService(AlarmManager::class.java)).canScheduleExactAlarms()

    @JavascriptInterface
    fun askExact() {
        if (Build.VERSION.SDK_INT >= 31) act.runOnUiThread {
            act.startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + act.packageName))
            )
        }
    }
}
