/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.game.Player

/**
 * Haggler
 * The costs of Utility Cards you hold are reduced by 1.
 *
 * Special
 * Passive
 * Continuous
 */
class HagglerAbility(gameScreen: GameScreen, player: Player) : PassiveAbility(gameScreen, player), Continuous {

    override val nameID
        get() = R.string.ability_name_haggler

    override val descriptionID
        get() = R.string.ability_description_haggler

    override val isBase = false

    companion object {
        const val PRIORITY = TraderAbility.PRIORITY + 1
        private const val serialVersionUID = 1L
    }
}