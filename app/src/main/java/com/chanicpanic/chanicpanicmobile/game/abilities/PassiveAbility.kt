/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import android.widget.Toast
import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.game.Game
import com.chanicpanic.chanicpanicmobile.game.Player

abstract class PassiveAbility(gameScreen: GameScreen, player: Player) : Ability(gameScreen, player) {

    final override val isActive = false

    override fun logActivation() {
        val activation = name + " " + gameScreen.getString(R.string.activated)
        Game.getInstance().log(activation, Game.Log.TAG_ABILITY)
        if (!gameScreen.isAutoplay) {
            Toast.makeText(gameScreen, activation, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}