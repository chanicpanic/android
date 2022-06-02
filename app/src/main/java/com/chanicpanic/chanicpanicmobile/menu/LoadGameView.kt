/*
 * Copyright (c) chanicpanic 2022
 */
package com.chanicpanic.chanicpanicmobile.menu

import com.chanicpanic.chanicpanicmobile.game.Game.Companion.loadFromDeserialization
import com.chanicpanic.chanicpanicmobile.game.Game.Companion.getInstance
import androidx.navigation.Navigation.findNavController
import android.widget.LinearLayout
import com.chanicpanic.chanicpanicmobile.R
import android.widget.TextView
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.game.Game
import android.widget.Toast
import android.content.DialogInterface
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AlertDialog
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*

/**
 * This View displays info about a saved game
 */
class LoadGameView : LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    /**
     * @param file the file to display info about
     */
    fun set(file: File, onDelete: DialogInterface.OnClickListener) {
        try {
            val properties = loadProperties(file)
            val time = buildTimeString(properties)
            findViewById<TextView>(R.id.txtProperties).text = StringBuilder()
                .append(time).append("\nPlayers: ").append(properties.getProperty("Players", "0"))
                .append("\nRound: ").append(properties.getProperty("Round", "0"))
            findViewById<View>(R.id.btnLoad).setOnClickListener {
                if (System.currentTimeMillis() - click > 1000) {
                    click = System.currentTimeMillis()
                    try {
                        val saveFile = File(file, "save")
                        GameScreen.setSaveFile(saveFile)
                        val fileInputStream1 = FileInputStream(saveFile)
                        val objectInputStream = ObjectInputStream(fileInputStream1)
                        loadFromDeserialization((objectInputStream.readObject() as Game))
                        objectInputStream.close()
                        fileInputStream1.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Error loading game", Toast.LENGTH_SHORT).show()
                    }
                    if (getInstance().playerCount == 0) {
                        Toast.makeText(context, "Invalid Game State", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    findNavController(this).navigate(R.id.action_loadGameFragment_to_gameScreen)
                }
            }
            findViewById<View>(R.id.btnDelete).setOnClickListener {
                AlertDialog.Builder(
                    context
                ).setTitle("Do you want to delete this game?")
                    .setPositiveButton("Yes", onDelete)
                    .setNegativeButton("No") { _: DialogInterface?, _: Int -> }
                    .setOnDismissListener {
                        (this@LoadGameView.context as Activity).window.decorView.systemUiVisibility =
                            SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                                    SYSTEM_UI_FLAG_FULLSCREEN or
                                    SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                    SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    }
                    .show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading game", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun buildTimeString(properties: Properties): String {
        val lastSave = Calendar.getInstance()
        lastSave.timeInMillis = properties.getProperty("Time", "0").toLong()
        val minute = lastSave[Calendar.MINUTE]
        return (lastSave.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
            ?.plus(" ") ?: "") +
                lastSave[Calendar.DAY_OF_MONTH] +
                ", " +
                lastSave[Calendar.YEAR] +
                " " +
                lastSave[Calendar.HOUR_OF_DAY] +
                ":" +
                if (minute < 10) "0$minute" else minute
    }

    private fun loadProperties(file: File): Properties {
        val fileInputStream = FileInputStream(File(file, "properties"))
        val properties = Properties()
        properties.load(fileInputStream)
        fileInputStream.close()
        return properties
    }

    companion object {
        private var click: Long = 0
    }
}