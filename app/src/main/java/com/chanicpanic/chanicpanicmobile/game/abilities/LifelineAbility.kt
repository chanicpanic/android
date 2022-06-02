/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.game.Player

/**
 * Lifeline
 * While you only control 1 Heart, you cannot be bypassed.
 *
 * Special
 * Passive
 * Continuous
 */
class LifelineAbility(gameScreen: GameScreen, player: Player) : PassiveAbility(gameScreen, player), Continuous {

    override val nameID
        get() = R.string.ability_name_lifeline

    override val descriptionID
        get() = R.string.ability_description_lifeline

    override val isBase = false


    companion object {
        const val PRIORITY = TraderAbility.PRIORITY + 1
        private const val serialVersionUID = 1L
    }
}