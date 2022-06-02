/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.game.Player

/**
 * Resourceful
 * The values of your Clubs are increased by 2.
 *
 * Special
 * Passive
 * Continuous
 */
class ResourcefulAbility(gameScreen: GameScreen, player: Player) : PassiveAbility(gameScreen, player), Continuous {

    override val nameID
        get() = R.string.ability_name_resourceful

    override val descriptionID
        get() = R.string.ability_description_resourceful

    override val isBase = false


    companion object {
        private const val serialVersionUID = 1L
    }
}