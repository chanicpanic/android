/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.game.AI
import com.chanicpanic.chanicpanicmobile.game.Card
import com.chanicpanic.chanicpanicmobile.game.Game
import com.chanicpanic.chanicpanicmobile.game.Player

/**
 * Options
 * Skip your Draw Phase: Gain 6 points during your Standby Phase.
 *
 * Special
 * Active
 * Explicitly Checked
 */
class OptionsAbility(gameScreen: GameScreen, player: Player) : ActiveAbility(gameScreen, player), ExplicitlyChecked {

    override val nameID
        get() = R.string.ability_name_options

    override val descriptionID
        get() = R.string.ability_description_options

    override val isBase = false

    override val priority
        get() = 1

    override val maxUsages = 1

    override val isActivateable: Boolean
        get() {
            return false
        }

    override fun shouldActivate(checkpoint: AI.Checkpoint): Boolean {
        val utilities = player.playableCards
        utilities.removeAll(Card.Suit.CLUBS)
        return utilities.costSum() > Game.STARTING_POINTS
    }

    override fun activate() {
        if (isActivateable) {
            super.activate()
        }
    }

    override fun resolve(): Boolean {
        logActivation()
        Game.getInstance().phase = Game.PHASE_STANDBY
        Game.getInstance().log("Standby Phase", Game.Log.TAG_PHASE)
        Game.getInstance().changePoints(6)
        Game.getInstance().log("${player.name} ${gameScreen.getString(R.string.gained)} 6 ${gameScreen.getString(R.string.ability_points)}", Game.Log.TAG_ABILITY)
        return super.resolve()
    }

    override fun deactivate() {
        if (activated) {
            super.deactivate()
        }
    }


    companion object {
        private const val serialVersionUID = 1L
    }
}