package com.cowabonga.boriattinobluetoothapp.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cowabonga.boriattinobluetoothapp.R
import java.text.SimpleDateFormat
import java.util.*

class GpsModeFragment : Fragment() {

    private lateinit var chronoText: TextView
    private lateinit var chronoMillis: TextView
    private lateinit var startStopButton: Button
    private lateinit var lapButton: Button
    private lateinit var lapLog: TextView

    private var isRunning = false
    private var startTime = 0L
    private var handler = Handler(Looper.getMainLooper())
    private var lapTimes = mutableListOf<Pair<Long, Long>>()
    private var lastLapStart = 0L

    private val updateRunnable = object : Runnable {
        override fun run() {
            val now = System.currentTimeMillis()
            val elapsed = now - startTime

            val seconds = (elapsed / 1000) % 60
            val minutes = (elapsed / 1000) / 60
            val millis = (elapsed % 1000) / 100

            chronoText.text = String.format("%02d:%02d", minutes, seconds)
            chronoMillis.text = ".$millis"
            handler.postDelayed(this, 100)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gps_mode, container, false)

        chronoText = view.findViewById(R.id.chronoText)
        chronoMillis = view.findViewById(R.id.chronoMillis)
        startStopButton = view.findViewById(R.id.startStopButton)
        lapButton = view.findViewById(R.id.lapButton)
        lapLog = view.findViewById(R.id.lapLog)

        startStopButton.setOnClickListener {
            if (isRunning) {
                stopChrono()
            } else {
                startChrono()
            }
        }

        lapButton.setOnClickListener {
            recordLap()
        }

        return view
    }

    private fun startChrono() {
        startTime = System.currentTimeMillis()
        lastLapStart = startTime
        isRunning = true
        handler.post(updateRunnable)
        startStopButton.text = "STOP"
        lapTimes.clear()
        lapLog.text = ""
    }

    private fun stopChrono() {
        isRunning = false
        handler.removeCallbacks(updateRunnable)
        startStopButton.text = "START"
    }

    private fun recordLap() {
        if (!isRunning) return
        val now = System.currentTimeMillis()
        lapTimes.add(Pair(lastLapStart, now))
        lastLapStart = now
        updateLapLog()
    }

    private fun updateLapLog() {
        val formatter = SimpleDateFormat("HH:mm:ss.S", Locale.getDefault())
        val builder = StringBuilder()
        lapTimes.forEachIndexed { index, (start, end) ->
            val lapTime = end - start
            builder.append("Lap ${index + 1}  ${formatter.format(Date(start))} - ${formatter.format(Date(end))}\n")
        }
        lapLog.text = builder.toString()
    }
}