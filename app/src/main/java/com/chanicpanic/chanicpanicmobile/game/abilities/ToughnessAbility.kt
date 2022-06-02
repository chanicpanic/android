/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.game.Player

/**
 * Toughness
 * The values of Diamonds you control are increased by 1.
 *
 * Special
 * Passive
 * Continuous
 */
class ToughnessAbility(gameScreen: GameScreen, player: Player) : PassiveAbility(gameScreen, player), Continuous {

    override val nameID
        get() = R.string.ability_name_toughness

    override val descriptionID
        get() = R.string.ability_description_toughness

    override val isBase = false


    companion object {
        private const val serialVersionUID = 1L
    }
}