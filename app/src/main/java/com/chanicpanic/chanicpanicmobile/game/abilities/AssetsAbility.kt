/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.game.Game
import com.chanicpanic.chanicpanicmobile.game.Player

/**
 * Assets
 * During your Standby Phase: Gain 1 point for every 2 cards in your hand.
 *
 * Special
 * Passive
 * Resolvable
 */
class AssetsAbility(gameScreen: GameScreen, player: Player) : PassiveAbility(gameScreen, player), Resolvable {

    override val nameID
        get() = R.string.ability_name_assets

    override val descriptionID
        get() = R.string.ability_description_assets

    override val isBase = false

    override val priority
        get() = 1

    override val isResolvable: Boolean
        get() {
            return Game.getInstance().phase == Game.PHASE_STANDBY
                    && player.hand.size > 1
        }

    override fun resolve(): Boolean {
        if (isResolvable) {
            logActivation()
            val points = player.hand.size / 2
            Game.getInstance().changePoints(points)
            Game.getInstance().log("${player.name} ${gameScreen.getString(R.string.gained)} $points ${if (points == 1) "point" else "points"}", Game.Log.TAG_ABILITY)
            return true
        }
        return false
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}