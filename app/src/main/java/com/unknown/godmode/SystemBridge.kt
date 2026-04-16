package com.unknown.godmode

import java.io.*
import java.util.*

object SystemBridge {
    private val activeKeys = mutableSetOf<String>()
    private val lastPressTime = mutableMapOf<String, Long>()
    private val tapCount = mutableMapOf<String, Int>()

    @JvmStatic
    fun main(args: Array<String>) {
        val process = Runtime.getRuntime().exec("getevent -lt")
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        
        while (true) {
            val line = reader.readLine() ?: break
            File("/data/local/tmp/live_signal.txt").writeText(line) // For App Live Capture

            if (line.contains("EV_KEY")) {
                val parts = line.split(Regex("\\s+"))
                if (parts.size < 4) continue
                
                val keyName = parts[parts.size - 2] // e.g., KEY_ASSISTANT
                val action = parts[parts.size - 1] // DOWN or UP
                val now = System.currentTimeMillis()

                if (action == "DOWN") {
                    activeKeys.add(keyName)
                    val durationSinceLast = now - (lastPressTime[keyName] ?: 0)
                    
                    // Detect Double Tap
                    if (durationSinceLast < 400) {
                        tapCount[keyName] = (tapCount[keyName] ?: 0) + 1
                    } else {
                        tapCount[keyName] = 1
                    }
                    lastPressTime[keyName] = now
                    
                    checkTriggers("SINGLE", keyName)
                    if (tapCount[keyName] == 2) checkTriggers("DOUBLE", keyName)
                    if (activeKeys.size > 1) checkTriggers("COMBO", activeKeys.joinToString("+"))
                    
                } else if (action == "UP") {
                    val pressDuration = now - (lastPressTime[keyName] ?: 0)
                    if (pressDuration > 800) checkTriggers("LONG", keyName)
                    activeKeys.remove(keyName)
                }
            }
        }
    }

    private fun checkTriggers(type: String, pattern: String) {
        val folder = File("/data/local/tmp/remaps/")
        if (!folder.exists()) return
        
        folder.listFiles()?.forEach { file ->
            val config = file.readLines()
            // Config Format: TriggerName | Type (SINGLE/DOUBLE/LONG/COMBO) | ActionType | Value
            if (config.size >= 4 && config[0] == pattern && config[1] == type) {
                execute(config[2], config[3])
            }
        }
    }

    private fun execute(actionType: String, value: String) {
        when (actionType) {
            "APP" -> Runtime.getRuntime().exec("monkey -p $value 1")
            "URL" -> Runtime.getRuntime().exec("am start -a android.intent.action.VIEW -d $value")
            "TEXT" -> Runtime.getRuntime().exec("input text \"$value\"")
        }
    }
}