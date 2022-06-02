/*
 * Copyright (c) chanicpanic 2022
 */
package com.chanicpanic.chanicpanicmobile.menu

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.chanicpanic.chanicpanicmobile.menu.PlayerSettingsFragment.Companion.getNextTeamColorIndex
import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.game.Game
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import android.view.View.OnFocusChangeListener
import android.widget.*
import com.chanicpanic.chanicpanicmobile.game.Player
import android.widget.SeekBar.OnSeekBarChangeListener
import java.util.*

/**
 * This View holds widgets to change player settings
 */
class PlayerSettingsView : LinearLayout {
    private lateinit var name: EditText
    private var index = 0
    private lateinit var btnColor: Button
    private var colorIndex = 0

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    /**
     * sets the position of this view, also the player's starting turn
     *
     * @param index the index of the view within the recycler
     */
    fun setPosition(index: Int, onColorChange: Function1<Int, Unit>) {
        // initialize widgets
        this.index = index
        findViewById<TextView>(R.id.txtTurn).text = (index + 1).toString()
        colorIndex = Game.getInstance().getTeamColorIndex(index % Game.getInstance().teams)
        findViewById<View>(R.id.btnTeamColor).setBackgroundColor(
            resources.getColor(
                GameScreen.TEAM_COLORS[colorIndex]
            )
        )
        name = findViewById(R.id.etxtPlayer)
        val swtMode = findViewById<SeekBar>(R.id.seekMode)
        name.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            if (hasFocus) {
                name.selectAll()
            } else {
                Game.getInstance().getPlayer(index).name = name.text.toString()
            }
        }
        name.setText(Game.getInstance().getPlayer(index).name)
        name.setOnClickListener { name.selectAll() }
        val playerMode = Game.getInstance().getPlayer(index).mode
        if (playerMode.contains(Player.Mode.HUMAN)) {
            if (playerMode.contains(Player.Mode.CPU)) {
                swtMode.progress = 1
            } else {
                swtMode.progress = 0
            }
        } else if (playerMode.contains(Player.Mode.CPU)) {
            swtMode.progress = 2
        }
        swtMode.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                when (progress) {
                    0 -> Game.getInstance().getPlayer(index).mode = EnumSet.of(Player.Mode.HUMAN)
                    1 -> Game.getInstance().getPlayer(index).mode =
                        EnumSet.allOf(Player.Mode::class.java)
                    2 -> Game.getInstance().getPlayer(index).mode = EnumSet.of(Player.Mode.CPU)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        btnColor = findViewById(R.id.btnTeamColor)
        btnColor.setOnClickListener {
            colorIndex = getNextTeamColorIndex(colorIndex)
            btnColor.setBackgroundColor(resources.getColor(GameScreen.TEAM_COLORS[colorIndex]))
            Game.getInstance().setTeamColorIndex(index % Game.getInstance().teams, colorIndex)
            onColorChange(index)
        }
    }

    /**
     * checks this View for invalid input and updates data for the Game
     *
     * @return whether there is an issue
     */
    fun update(): Boolean {
        if (name.text.isEmpty()) {
            return false
        }
        Game.getInstance().getPlayer(index).name = name.text.toString()
        return true
    }
}