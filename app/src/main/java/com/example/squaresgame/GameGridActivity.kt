package com.example.squaresgame

import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import com.example.squaresgame.model.Box
import kotlinx.android.synthetic.main.activity_game_grid.*
import android.widget.GridView
import android.widget.Toast
import androidx.core.view.children
import com.squareup.seismic.ShakeDetector
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.random.Random


class GameGridActivity : AppCompatActivity(), ShakeDetector.Listener {
    val grid = Array(GRID_WIDTH * GRID_HEIGHT) { Box(false) }
    lateinit var gameGrid: GridView
    var filledBoxesCount = AtomicInteger()
    var clearedBoxesCounter = 0
    var suspendFilling = false
    var shakeUsed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_grid)

        gameGrid = gameGridView
        val gameAdapter = ArrayAdapter<Box>(this, R.layout.box_item, grid)
        gameGrid.adapter = gameAdapter
        gameGrid.setOnItemClickListener { parent, view, position, id ->
            if (grid[position].filled) {
                onBoxClicked(position)
            }
        }

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val shakeDetector = ShakeDetector(this)
        shakeDetector.start(sensorManager)

        startGame()
    }

    private fun onBoxClicked(position: Int) {
        filledBoxesCount.decrementAndGet()
        grid[position].filled = false
        gameGrid.getChildAt(position).setBackgroundColor(ContextCompat.getColor(this, R.color.colorEmptyBox))
        clearedBoxesCounter++
        clearedBoxesCounterTextView.text = clearedBoxesCounter.toString()
    }

    private fun startGame(): Thread {
        var gameFinished = false

        return thread(start = true) {
            var timeReduction = 0L
            while (!gameFinished) {
                while (suspendFilling) {
                    sleep(200)
                }

                var foundEmptyBox = false
                while (!foundEmptyBox) {
                    val index = Random.nextInt(0, grid.size)
                    if (!grid[index].filled) {
                        foundEmptyBox = true;
                        grid[index].filled = true
                        filledBoxesCount.getAndIncrement()
                        runOnUiThread {
                            gameGrid.getChildAt(index)
                                .setBackgroundColor(ContextCompat.getColor(this, R.color.colorFilledBox))
                        }
                    }
                }
                if (filledBoxesCount.get() == grid.size) {
                    gameFinished = true
                    val intent = Intent()
                    intent.putExtra(CLEARED_BOXES_KEY, clearedBoxesCounter)
                    setResult(CLEARED_BOXES_RESULT_OK, intent)
                    finish()
                    return@thread
                }

                if (clearedBoxesCounter % 9 == 0) {
                    timeReduction += 13
                }
                sleep(460 - timeReduction)
            }
        }
    }

    override fun hearShake() {
        if (shakeUsed) return

        suspendFilling = true
        sleep(100)
        grid.forEach { it.filled = false }
        gameGrid.children.forEach { it.setBackgroundColor(ContextCompat.getColor(this, R.color.colorEmptyBox)) }
        filledBoxesCount.getAndSet(0)
        shakeUsed = true
        suspendFilling = false
    }

    companion object {
        const val GRID_WIDTH = 4
        const val GRID_HEIGHT = 7
        const val CLEARED_BOXES_REQUEST_CODE = 666
        const val CLEARED_BOXES_RESULT_OK = 4321
        const val CLEARED_BOXES_KEY = "clearedBoxes"
    }
}
