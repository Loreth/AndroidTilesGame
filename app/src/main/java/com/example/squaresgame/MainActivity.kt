package com.example.squaresgame

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.squaresgame.GameGridActivity.Companion.CLEARED_BOXES_KEY
import com.example.squaresgame.GameGridActivity.Companion.CLEARED_BOXES_REQUEST_CODE
import com.example.squaresgame.GameGridActivity.Companion.CLEARED_BOXES_RESULT_OK
import kotlinx.android.synthetic.main.activity_main.*
import android.hardware.SensorManager
import android.widget.Toast


class MainActivity : AppCompatActivity(), SensorEventListener {
    val MIN_PROXIMITY_IN_CM = 5.0f
    lateinit var sensorManager: SensorManager
    lateinit var proximitySensor: Sensor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startGameButton.setOnClickListener {
            startActivityForResult(Intent(this@MainActivity, GameGridActivity::class.java), CLEARED_BOXES_REQUEST_CODE)
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
                if (event.values[0] < MIN_PROXIMITY_IN_CM) {
                    startActivityForResult(
                        Intent(this@MainActivity, GameGridActivity::class.java),
                        CLEARED_BOXES_REQUEST_CODE
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CLEARED_BOXES_REQUEST_CODE && resultCode == CLEARED_BOXES_RESULT_OK && data != null) {
            val result = data.getIntExtra(CLEARED_BOXES_KEY, 0)
            resultTextView.text = result.toString()
            if (topScoreTextView.text.toString().toInt() < result) {
                topScoreTextView.text = result.toString()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
