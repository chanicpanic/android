/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.game.Game
import com.chanicpanic.chanicpanicmobile.game.Player

/**
 * Wealth
 * During your Standby Phase: Gain 3 points.
 *
 * Special
 * Passive
 * Resolvable
 */
class WealthAbility(gameScreen: GameScreen, player: Player) : PassiveAbility(gameScreen, player), Resolvable {

    override val nameID
        get() = R.string.ability_name_wealth

    override val descriptionID
        get() = R.string.ability_description_wealth

    override val isBase = false

    override val priority
        get() = 1

    override val isResolvable: Boolean
        get() {
            return Game.getInstance().phase == Game.PHASE_STANDBY
        }

    override fun resolve(): Boolean {
        if (isResolvable) {
            logActivation()
            Game.getInstance().changePoints(3)
            Game.getInstance().log("${player.name} ${gameScreen.getString(R.string.gained)} 3 ${gameScreen.getString(R.string.ability_points)}", Game.Log.TAG_ABILITY)
            return true
        }
        return false
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}