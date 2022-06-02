/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.gamescreen.HandFragment
import com.chanicpanic.chanicpanicmobile.game.*
import java.util.*

/**
 * Trader:
 * Discard 2 cards: Draw 1 card.
 *
 * Base
 * Active
 * Resolvable
 * Selection Necessary
 */
class TraderAbility(gameScreen: GameScreen, player: Player) : ActiveAbility(gameScreen, player), SelectionNecessary {

    private val selections = CardGroup(2)

    override val nameID
        get() = R.string.ability_name_trader

    override val descriptionID
        get() = R.string.ability_description_trader

    override val isBase = true

    override val maxUsages = NO_MAX_USAGES

    // come after special abilities
    override val priority = PRIORITY

    override fun shouldActivate(checkpoint: AI.Checkpoint): Boolean {
        var count = 0
        for (card in player.hand) {
            if (card.value <= MAX_TRADEABLE_VALUE && card.suit != Card.Suit.CLUBS) {
                if (++count == 2) {
                    return true
                }
            }
        }
        return false
    }

    override val isActivateable
        get() = super.isActivateable
                && Game.getInstance().phase >= Game.PHASE_POINT
                && player.hand.size > 1


    override fun activate() {
        if (isActivateable) {
            super.activate()
            if (!gameScreen.isAutoplay) {
                gameScreen.activateAbilitySelection(2, true, Game.MAX_VALUE, EnumSet.allOf(Card.Suit::class.java))
            }
        }
    }

    override fun resolve(): Boolean {
        if (activated) {
            logActivation()

            // discard 2 cards
            if (!isSelectionMade) {
                selections.addAll(gameScreen.selectedCards)
            }

            player.discard(selections[0])
            player.discard(selections[1])
            Game.getInstance().log("${player.name} ${gameScreen.getString(R.string.discarded)} $selections", Game.Log.TAG_ABILITY.or(Game.Log.TAG_DISCARD))

            clearSelection()
            gameScreen.clearSelections()

            // draw 1 card
            player.draw(1, Game.Log.TAG_ABILITY)


            if (!gameScreen.isAutoplay) {

                // show draw
                val handFragment = gameScreen.supportFragmentManager.findFragmentByTag(
                    GameScreen.HAND_FRAGMENT_TAG) as HandFragment

                handFragment.removeSelections()
                gameScreen.updateDiscardPile()

                handFragment.notifyDraw(1)
                handFragment.scrollToEnd()
            }

            // increment usages and deactivate
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
            // set state
            super.deactivate()
        }
    }

    override val isSelectionMade: Boolean
        get() {
            return !selections.isEmpty
        }

    override fun clearSelection() {
        selections.clear()
    }

    override fun makeSelection() {
        clearSelection()
        val hand = CardGroup()
        hand.addAll(player.hand.filter { Card.UTILITY_CARDS.contains(it.suit) })

        for (i in 0..1) {
            val trade = hand.minOrNull()
            hand.remove(trade!!)
            selections.add(trade)
        }
    }

    companion object {
        const val PRIORITY = 0
        private const val MAX_TRADEABLE_VALUE = 3
        private const val serialVersionUID = 1L
    }
}
