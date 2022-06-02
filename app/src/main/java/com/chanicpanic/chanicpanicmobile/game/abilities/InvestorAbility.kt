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
 * Investor
 * During your Standby Phase: Gain 2 points for each Diamond and Spade you control.
 *
 * Special
 * Passive
 * Resolvable
 */
class InvestorAbility(gameScreen: GameScreen, player: Player) : PassiveAbility(gameScreen, player), Resolvable {

    override val nameID
        get() = R.string.ability_name_investor

    override val descriptionID
        get() = R.string.ability_description_investor

    override val isBase = false

    override val priority
        get() = 1

    override val isResolvable: Boolean
        get() {
            return Game.getInstance().phase == Game.PHASE_STANDBY
                    && player.board.rowSize(Card.Suit.DIAMONDS) + player.board.rowSize(Card.Suit.SPADES) > 0
        }

    override fun resolve(): Boolean {
        if (isResolvable) {
            logActivation()
            val points = 2 * (player.board.rowSize(Card.Suit.DIAMONDS) + player.board.rowSize(Card.Suit.SPADES))
            Game.getInstance().changePoints(points)
            Game.getInstance().log("${player.name} ${gameScreen.getString(R.string.gained)} $points ${gameScreen.getString(R.string.ability_points)}", Game.Log.TAG_ABILITY)
            return true
        }
        return false
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}