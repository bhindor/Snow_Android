package kr.hs.anu.snow.data.repository

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.concurrent.fixedRateTimer

object ChzzkLiveStatusRepository {

    private val client = OkHttpClient()
    private const val channelId = "24bfee5272d330b48ac0a1274e199a94"
    private const val apiUrl = "https://api.chzzk.naver.com/polling/v2/channels/$channelId/live-status"

    interface OnLiveStatusUpdate {
        fun onUpdate(isLive: Boolean, title: String, viewers: Int)
    }

    private var listener: OnLiveStatusUpdate? = null
    private var timer: Timer? = null

    fun startPolling(listener: OnLiveStatusUpdate, intervalMs: Long = 10000L) {
        this.listener = listener
        timer?.cancel()
        timer = fixedRateTimer("chzzk_timer", initialDelay = 0, period = intervalMs) {
            val request = Request.Builder().url(apiUrl).get().build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("CHZZK", "API 실패: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string() ?: return

                    try {
                        val json = JSONObject(body)

                        if (!json.has("content")) {
                            Log.e("CHZZK", "응답에 content 없음: $body")
                            return
                        }

                        val content = json.getJSONObject("content")
                        val isLive = content.optBoolean("open", false)
                        val title = content.optString("liveTitle", "제목 없음")
                        val viewers = content.optInt("concurrentUserCount", 0)

                        listener.onUpdate(isLive, title, viewers)

                    } catch (e: Exception) {
                        Log.e("CHZZK", "JSON 파싱 에러: ${e.message}")
                        Log.e("CHZZK", "응답 원문: $body")
                    }
                }

            })
        }
    }

    fun stopPolling() {
        timer?.cancel()
        timer = null
        listener = null
    }
}