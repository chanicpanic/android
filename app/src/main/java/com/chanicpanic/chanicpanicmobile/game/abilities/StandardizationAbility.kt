/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.game.Player

/**
 * Standardization
 * All Clubs you play allow you to draw 2 cards.
 *
 * Special
 * Passive
 * Continuous
 */
class StandardizationAbility(gameScreen: GameScreen, player: Player) : PassiveAbility(gameScreen, player), Continuous {

    override val nameID
        get() = R.string.ability_name_standardization

    override val descriptionID
        get() = R.string.ability_description_standardization

    override val isBase = false


    companion object {
        private const val serialVersionUID = 1L
    }
}