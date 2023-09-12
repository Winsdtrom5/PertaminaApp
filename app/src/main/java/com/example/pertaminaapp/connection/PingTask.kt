package com.example.pertaminaapp.connection

import android.os.AsyncTask
import java.io.IOException
import java.net.InetAddress

class PingTask(private val listener: PingListener) : AsyncTask<Void, Void, String>() {

    override fun doInBackground(vararg params: Void?): String {
        try {
            val ipAddress = "sql.freedb.tech" // Replace with the IP address or hostname you want to ping
            val pingCommand = "ping -c 1 -W 1 $ipAddress" // Linux ping command for 1 packet with a timeout of 1 second
            val runtime = Runtime.getRuntime()
            val process = runtime.exec(pingCommand)
            val inputStream = process.inputStream
            val bufferedReader = inputStream.bufferedReader()
            val output = StringBuilder()
            var line: String?

            while (bufferedReader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                // Parsing the output to extract ping time (ms)
                val pingTime = parsePingTime(output.toString())
                return "$pingTime ms"
            } else {
                return "Error"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return "Error"
        } catch (e: InterruptedException) {
            e.printStackTrace()
            return "Error"
        }
    }

    override fun onPostExecute(result: String) {
        listener.onPingResult(result)
    }

    private fun parsePingTime(pingOutput: String): Long {
        // Parse ping output to extract ping time in milliseconds
        val lines = pingOutput.split("\n")
        val timeLine = lines.lastOrNull { it.contains("time=") }
        val timeStr = timeLine?.substringAfter("time=")?.substringBefore(" ") ?: ""
        return try {
            timeStr.toLong()
        } catch (e: NumberFormatException) {
            -1
        }
    }

    interface PingListener {
        fun onPingResult(result: String)
    }
}
