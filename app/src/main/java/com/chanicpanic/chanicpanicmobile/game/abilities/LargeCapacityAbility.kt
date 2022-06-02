/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.game.Game
import com.chanicpanic.chanicpanicmobile.game.Player

/**
 * Large Capacity
 * Your full hand is 12 cards. During your Draw Phase: Draw 1 card for every 4 cards in your hand.
 *
 * Special
 * Passive
 * Resolvable
 * Continuous
 */
class LargeCapacityAbility(gameScreen: GameScreen, player: Player) : PassiveAbility(gameScreen, player), Resolvable, Continuous {

    override val nameID
        get() = R.string.ability_name_large_capacity

    override val descriptionID
        get() = R.string.ability_description_large_capacity

    override val isBase = false

    override val priority
        get() = PRIORITY

    override val isResolvable: Boolean
        get() {
            return Game.getInstance().phase == Game.PHASE_DRAW
                    && Game.getInstance().round > 1
                    && player.hand.size >= 4
                    && !player.isHandFull
        }

    override fun resolve(): Boolean {
        if (isResolvable) {
            logActivation()
            player.draw(player.hand.size / 4, Game.Log.TAG_ABILITY)
            return true
        }
        return false
    }

    companion object {
        const val PRIORITY = BlessedAbility.PRIORITY + 1
        const val FULL = 12
        private const val serialVersionUID = 1L
    }
}