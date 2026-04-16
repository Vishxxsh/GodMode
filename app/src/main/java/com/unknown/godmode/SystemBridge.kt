package com.unknown.godmode

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Scanner

object SystemBridge {
    @JvmStatic
    fun main(args: Array<String>) {
        println("--- GODMODE SYSTEM BRIDGE ACTIVE ---")
        
        // This is the raw hardware listener. 
        // '-l' makes it human-readable (hex codes), '-t' adds timestamps.
        val process = Runtime.getRuntime().exec("getevent -lt")
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        
        while (true) {
            val line = reader.readLine() ?: break
            
            // This is the 'Discovery' part. It prints every hardware move to your laptop.
            if (line.contains("EV_KEY")) {
                println("SIGNAL_DETECTED: $line")
                
                // Check if this hex code matches our 'Saved Trigger'
                // We will read the trigger from a shared file in /data/local/tmp
                val config = File("/data/local/tmp/godmode.cfg")
                if (config.exists()) {
                    val lines = config.readLines()
                    if (lines.isNotEmpty()) {
                        val savedHex = lines[0] // e.g., "00d9"
                        val savedPkg = lines[1] // e.g., "com.brave.browser"
                        
                        if (line.contains(savedHex) && line.contains("DOWN")) {
                            println("MATCH FOUND! Launching $savedPkg")
                            // DIRECT HARDWARE EXECUTION: Bypasses Android's Assistant logic
                            Runtime.getRuntime().exec("am start -n $savedPkg")
                        }
                    }
                }
            }
        }
    }
}