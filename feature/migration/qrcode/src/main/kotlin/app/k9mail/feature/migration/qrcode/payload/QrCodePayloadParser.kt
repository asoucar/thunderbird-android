package app.k9mail.feature.migration.qrcode.payload

import com.squareup.moshi.JsonDataException
import java.io.IOException
import net.thunderbird.core.logging.legacy.Log

internal class QrCodePayloadParser(
    private val qrCodePayloadAdapter: QrCodePayloadAdapter,
) {
    /**
     * Parses the QR code payload as JSON and reads it into [QrCodeData].
     *
     * @return [QrCodeData] if the JSON was parsed successfully and has the correct structure, `null` otherwise.
     */
    fun parse(payload: String): QrCodeData? {
        return try {
            qrCodePayloadAdapter.fromJson(payload)
        } catch (e: JsonDataException) {
            Log.d(e, "Failed to parse JSON")
            null
        } catch (e: IOException) {
            Log.d(e, "Unexpected IOException")
            null
        }
    }
}
