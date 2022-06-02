/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.gamescreen.HandFragment
import com.chanicpanic.chanicpanicmobile.game.AI
import com.chanicpanic.chanicpanicmobile.game.Card
import com.chanicpanic.chanicpanicmobile.game.Game
import com.chanicpanic.chanicpanicmobile.game.Player
import java.util.*

/**
 * Ally:
 * Once per turn: Give a card from your hand to your next teammate.
 *
 * Base
 * Active
 * Resolvable
 * Selection Necessary
 */
class AllyAbility(gameScreen: GameScreen, player: Player) : ActiveAbility(gameScreen, player), SelectionNecessary {

    private var selection: Card? = null

    override val nameID
        get() = R.string.ability_name_ally

    override val descriptionID
        get() = R.string.ability_description_ally

    override val isBase = true

    override val maxUsages = 1

    override val priority
        get() = PRIORITY

    override val isActivateable: Boolean
        get() {
            val teammates = Game.getInstance().teammatesOf(player.turn)
            return super.isActivateable
                    && Game.getInstance().phase >= Game.PHASE_POINT
                    && teammates.isNotEmpty()
                    && !player.hand.isEmpty
                    && !teammates[0].isHandFull
        }

    override fun shouldActivate(checkpoint: AI.Checkpoint) = isActivateable && checkpoint == AI.Checkpoint.END_TURN

    override fun activate() {
        if (isActivateable) {
            super.activate()
            clearSelection()
            if (!gameScreen.isAutoplay) {
                gameScreen.activateAbilitySelection(1, true, Game.MAX_VALUE, EnumSet.allOf(Card.Suit::class.java))
            }
        }
    }

    override fun resolve(): Boolean {
        if (activated) {
            logActivation()

            val nextTeammate = Game.getInstance().teammatesOf(player.turn)[0]

            if (!isSelectionMade) {
                selection = gameScreen.selectedCards[0]
            }

            player.hand.remove(selection!!)

            nextTeammate.hand.add(selection!!)

            clearSelection()

            gameScreen.clearSelections()

            Game.getInstance().log("${player.name} ${gameScreen.getString(R.string.ally_log)} to ${nextTeammate.name}", Game.Log.TAG_ABILITY)

            if (!gameScreen.isAutoplay) {
                (gameScreen.supportFragmentManager.findFragmentByTag(GameScreen.HAND_FRAGMENT_TAG) as HandFragment).removeSelections()
            }

            return super.resolve()
        }
        return false
    }

    override fun deactivate() {
        if (activated) {
            if (!gameScreen.isAutoplay) {
                clearSelection()
                gameScreen.deactivateAbilitySelection()
            }
        }
        super.deactivate()
    }

    override fun makeSelection() {
        selection = player.hand.maxOrNull()
    }

    override val isSelectionMade
        get() = selection != null

    override fun clearSelection() {
        selection = null
    }

    companion object {
        const val PRIORITY = -1
        private const val serialVersionUID = 1L
    }
}