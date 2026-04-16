package com.unknown.godmode

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object SystemBridge {
    @JvmStatic
    fun main(args: Array<String>) {
        println("--- GODMODE SYSTEM BRIDGE ACTIVE ---")
        
        val process = Runtime.getRuntime().exec("getevent -lt")
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        
        while (true) {
            val line = reader.readLine() ?: break
            
            if (line.contains("EV_KEY")) {
                val config = File("/data/local/tmp/godmode.cfg")
                if (config.exists()) {
                    val configLines = config.readLines()
                    if (configLines.size >= 2) {
                        val savedTrigger = configLines[0].trim() 
                        val savedPkg = configLines[1].trim()     
                        
                        if (line.contains(savedTrigger) && line.contains("DOWN")) {
                            println("MATCH! Launching $savedPkg via Monkey...")
                            // 'monkey' is the magic bullet. It finds the app's main entrance and opens it.
                            Runtime.getRuntime().exec("monkey -p $savedPkg 1")
                        }
                    }
                }
            }
        }
    }
}