/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.game.Card
import com.chanicpanic.chanicpanicmobile.game.Game
import com.chanicpanic.chanicpanicmobile.game.Player

/**
 * Blessed
 * During your Draw Phase: If you control fewer than 2 Hearts, draw 1 card.
 *
 * Special
 * Passive
 * Resolvable
 */
class BlessedAbility(gameScreen: GameScreen, player: Player) : PassiveAbility(gameScreen, player), Resolvable {

    override val nameID
        get() = R.string.ability_name_blessed

    override val descriptionID
        get() = R.string.ability_description_blessed

    override val isBase = false

    override val priority
        get() = PRIORITY

    override val isResolvable: Boolean
        get() {
            return Game.getInstance().phase == Game.PHASE_DRAW
                    && player.board.rowSize(Card.Suit.HEARTS) < 2
                    && Game.getInstance().round > 1
                    && !player.isHandFull
        }

    override fun resolve(): Boolean {
        if (isResolvable) {
            logActivation()
            player.draw(1, Game.Log.TAG_ABILITY)
            return true
        }
        return false
    }

    companion object {
        const val PRIORITY = TraderAbility.PRIORITY + 1
        private const val serialVersionUID = 1L
    }
}