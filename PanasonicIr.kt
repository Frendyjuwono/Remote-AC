package id.frendy.remoteac

import android.content.Context
import android.hardware.ConsumerIrManager

/**
 * Encoder kode IR AC Panasonic (protokol 216-bit / 27 byte, 2 bagian).
 * Timing & tata letak byte mengikuti library open-source IRremoteESP8266 (ir_Panasonic).
 */
object PanasonicIr {
    const val FREQ = 36700
    private const val HDR_MARK = 3456
    private const val HDR_SPACE = 1728
    private const val BIT_MARK = 432
    private const val ONE_SPACE = 1296
    private const val ZERO_SPACE = 432
    private const val GAP = 10000

    const val MODE_AUTO = 0
    const val MODE_DRY = 2
    const val MODE_COOL = 3
    const val MODE_FAN = 6

    /** fan: 0 = auto, 1..5. swing: 0 = auto, 1..5 (atas ke bawah). variant: 0 = A, 1 = B. */
    data class State(
        val power: Boolean,
        val temp: Int,
        val fan: Int,
        val swing: Int,
        val variant: Int,
        val mode: Int = MODE_COOL
    )

    fun bytes(s: State): IntArray {
        val b = intArrayOf(
            0x02, 0x20, 0xE0, 0x04, 0x00, 0x00, 0x00, 0x06,
            0x02, 0x20, 0xE0, 0x04, 0x00, 0x00, 0x00, 0x80, 0x00, 0x00,
            0x00, 0x0E, 0xE0, 0x00, 0x00, 0x81, 0x00, 0x00, 0x00
        )
        b[13] = (s.mode shl 4) or (if (s.variant == 1) 0x08 else 0) or (if (s.power) 1 else 0)
        b[14] = s.temp.coerceIn(16, 30) shl 1
        val fan = if (s.fan in 1..5) s.fan + 2 else 0x0A
        val swing = if (s.swing in 1..5) s.swing else 0x0F
        b[16] = (fan shl 4) or swing
        if (s.variant == 1) b[23] = 0x89
        var sum = 0
        for (i in 8 until 26) sum += b[i]
        b[26] = sum and 0xFF
        return b
    }

    fun pattern(s: State): IntArray {
        val b = bytes(s)
        val out = ArrayList<Int>(460)
        fun section(from: Int, to: Int) {
            out.add(HDR_MARK); out.add(HDR_SPACE)
            for (i in from until to) {
                var v = b[i]
                repeat(8) {               // LSB dulu
                    out.add(BIT_MARK)
                    out.add(if ((v and 1) == 1) ONE_SPACE else ZERO_SPACE)
                    v = v shr 1
                }
            }
            out.add(BIT_MARK); out.add(GAP)
        }
        section(0, 8)
        section(8, 27)
        return out.toIntArray()
    }

    fun hasIr(ctx: Context): Boolean {
        val ir = ctx.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
        return ir?.hasIrEmitter() == true
    }

    fun send(ctx: Context, s: State): Boolean {
        val ir = ctx.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager ?: return false
        if (!ir.hasIrEmitter()) return false
        return try {
            ir.transmit(FREQ, pattern(s)); true
        } catch (e: Exception) {
            false
        }
    }
}
