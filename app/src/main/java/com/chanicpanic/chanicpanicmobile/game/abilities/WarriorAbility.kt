/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.game.Player

/**
 * Warrior
 * The values of Spades you control are increased by 1.
 *
 * Special
 * Passive
 * Continuous
 */
class WarriorAbility(gameScreen: GameScreen, player: Player) : PassiveAbility(gameScreen, player), Continuous {

    override val nameID
        get() = R.string.ability_name_warrior

    override val descriptionID
        get() = R.string.ability_description_warrior

    override val isBase = false


    companion object {
        private const val serialVersionUID = 1L
    }
}