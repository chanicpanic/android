/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.game.Player

/**
 * Direct Hitter
 * If the defender controls 2 or more Diamonds, your bypass attack power is 75%.
 *
 * Special
 * Passive
 * Continuous
 */
class DirectHitterAbility(gameScreen: GameScreen, player: Player) : PassiveAbility(gameScreen, player), Continuous {

    override val nameID
        get() = R.string.ability_name_direct_hitter

    override val descriptionID
        get() = R.string.ability_description_direct_hitter

    override val isBase = false

    companion object {
        private const val serialVersionUID = 1L
    }
}