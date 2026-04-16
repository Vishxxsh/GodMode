package com.unknown.godmode

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object SystemBridge {
    @JvmStatic
    fun main(args: Array<String>) {
        println("--- GODMODE SYSTEM BRIDGE ACTIVE ---")
        
        // We use -lt to get the human-readable names like KEY_ASSISTANT
        val process = Runtime.getRuntime().exec("getevent -lt")
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        
        while (true) {
            val line = reader.readLine() ?: break
            
            if (line.contains("EV_KEY")) {
                println("SIGNAL_DETECTED: $line")
                
                val config = File("/data/local/tmp/godmode.cfg")
                if (config.exists()) {
                    val configLines = config.readLines()
                    if (configLines.size >= 2) {
                        val savedTrigger = configLines[0].trim() // e.g., "KEY_ASSISTANT"
                        val savedPkg = configLines[1].trim()     // e.g., "com.brave.browser"
                        
                        // If the line has our word AND it's a DOWN press
                        if (line.contains(savedTrigger) && line.contains("DOWN")) {
                            println("MATCH! Launching $savedPkg")
                            // Launch the app as a fresh task
                            Runtime.getRuntime().exec("am start -n $savedPkg")
                        }
                    }
                }
            }
        }
    }
}