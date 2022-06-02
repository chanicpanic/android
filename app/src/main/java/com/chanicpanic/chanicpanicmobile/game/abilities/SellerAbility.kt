/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.gamescreen.HandFragment
import com.chanicpanic.chanicpanicmobile.game.*
import kotlin.math.min

/**
 * Seller
 * Once per turn: Discard up to 3 Utility Cards: Gain points equal to half of the total value and draw cards equal to half of the number of cards discarded.
 *
 * Special
 * Active
 * Resolvable
 * Selection Necessary
 */
class SellerAbility(gameScreen: GameScreen, player: Player) : ActiveAbility(gameScreen, player), SelectionNecessary {

    private var selections = CardGroup(3)

    override val nameID
        get() = R.string.ability_name_seller

    override val descriptionID
        get() = R.string.ability_description_seller

    override val isBase = false

    override val priority
        get() = PRIORITY

    override val maxUsages = 1

    override val isActivateable: Boolean
        get() = super.isActivateable
                && Game.getInstance().phase == Game.PHASE_POINT
                && player.hand.has(Card.UTILITY_CARDS)


    override fun shouldActivate(checkpoint: AI.Checkpoint): Boolean {
        clearSelection()

        // optimize for gaining points
        if (checkpoint == AI.Checkpoint.PRE_PLAY) {
            val utilities = CardGroup()
            utilities.addAll(player.playableCards.filter { Card.UTILITY_CARDS.contains(it.suit) })

            val cost = utilities.costSum()
            if (cost > Game.getInstance().points) {
                // get all possible selection combos
                val combos = AI.combosOf(utilities.toArrayList(), 3)

                var best = Integer.MAX_VALUE

                for (combo in combos) {
                    val cardsLeft = CardGroup()
                    cardsLeft.addAll(utilities.filter { !combo.contains(it) })

                    val leftSum = cardsLeft.costSum()
                    val comboSum = combo.valueSum()

                    // if the leftovers cost more than the points, see if the combo can fix that
                    // select the combo that creates the least waste
                    if (leftSum > Game.getInstance().points && leftSum + comboSum / 2 >= Game.getInstance().points) {
                        val rating = leftSum + comboSum / 2 - Game.getInstance().points
                        best = min(best, rating)
                        selections = combo
                    }
                }

                if (isSelectionMade) {
                    return true
                }
            }
        }

        // activate as Trader
        val hand = CardGroup()
        hand.addAll(player.hand.filter { Card.UTILITY_CARDS.contains(it.suit) })

        if (hand.size >= 2) {
            for (i in 0..1) {
                val trade = hand.minOrNull()
                hand.remove(trade!!)
                selections.add(trade)
            }
        }

        if (selections.valueSum() > MAX_TRADEABLE_VALUE) {
            selections.clear()
        }

        return isSelectionMade
    }


    override fun activate() {
        if (isActivateable) {
            super.activate()

            if (!gameScreen.isAutoplay) {
                gameScreen.activateAbilitySelection(3, false, Game.MAX_VALUE, Card.UTILITY_CARDS)
            }
        }
    }

    override fun resolve(): Boolean {
        if (activated) {
            logActivation()


            if (!isSelectionMade) {
                selections.addAll(gameScreen.selectedCards)
            }


            for (card in selections) {
                player.discard(card)
            }
            Game.getInstance().log("${player.name} ${gameScreen.getString(R.string.discarded)} $selections", Game.Log.TAG_ABILITY.or(Game.Log.TAG_DISCARD))

            val points = selections.valueSum() / 2
            Game.getInstance().changePoints(points)
            Game.getInstance().log("${player.name} ${gameScreen.getString(R.string.gained)} $points ${if (points == 1) "point" else "points"}", Game.Log.TAG_ABILITY)

            val cards = selections.size / 2
            if (cards > 0) {
                player.draw(cards, Game.Log.TAG_ABILITY)
            }

            if (!gameScreen.isAutoplay) {
                (gameScreen.supportFragmentManager.findFragmentByTag(GameScreen.HAND_FRAGMENT_TAG) as HandFragment).removeSelections()
                gameScreen.updateDiscardPile()
                gameScreen.updateInfo()

                if (cards > 0) {
                    // show draw
                    val handFragment = gameScreen.supportFragmentManager.findFragmentByTag(
                        GameScreen.HAND_FRAGMENT_TAG) as HandFragment
                    handFragment.notifyDraw(cards)
                    handFragment.scrollToEnd()
                }
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
            super.deactivate()
        }
    }

    override fun makeSelection() {

    }

    override val isSelectionMade: Boolean
        get() {
            return !selections.isEmpty
        }

    override fun clearSelection() {
        selections.clear()
    }

    companion object {
        const val PRIORITY = HopefulAbility.PRIORITY + 1
        private const val MAX_TRADEABLE_VALUE = 7
        private const val serialVersionUID = 1L
    }
}