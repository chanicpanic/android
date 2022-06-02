/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.game.Player

/**
 * Seer
 * The top card of the deck is visible to you.
 *
 * Special
 * Passive
 * Continuous
 */
class SeerAbility(gameScreen: GameScreen, player: Player) : PassiveAbility(gameScreen, player), Continuous {

    override val nameID
        get() = R.string.ability_name_seer

    override val descriptionID
        get() = R.string.ability_description_seer

    override val isBase = false


    companion object {
        private const val serialVersionUID = 1L
    }
}