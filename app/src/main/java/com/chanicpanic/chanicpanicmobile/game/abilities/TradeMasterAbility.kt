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
 * Trade Master
 * Once per turn: Discard 1 card: Draw 2 cards; Discard 1 card.
 *
 * Special
 * Active
 * Resolvable
 * Selection Necessary
 */
class TradeMasterAbility(gameScreen: GameScreen, player: Player) : ActiveAbility(gameScreen, player), SelectionNecessary {

    private var selection: Card? = null

    private var stage = 0

    override val nameID
        get() = R.string.ability_name_trade_master

    override val descriptionID
        get() = R.string.ability_description_trade_master

    override val isBase = false

    override val priority
        get() = PRIORITY

    override val maxUsages = 1

    override val isActivateable: Boolean
        get() {
            return super.isActivateable
                    && Game.getInstance().phase >= Game.PHASE_POINT
                    && !player.hand.isEmpty
                    && !player.isHandFull
        }

    override fun shouldActivate(checkpoint: AI.Checkpoint): Boolean {
        // maybe differentiate this from Trader: consider the fact that 2 cards will be drawn
        var count = 0
        for (card in player.hand) {
            if (card.value <= MAX_TRADEABLE_VALUE && Card.UTILITY_CARDS.contains(card.suit)) {
                if (++count == 2) {
                    return true
                }
            }
        }
        return false
    }

    override fun activate() {
        if (isActivateable) {
            super.activate()

            if (!gameScreen.isAutoplay) {
                stage = 0
                gameScreen.activateAbilitySelection(1, true, Card.MAX_VALUE, EnumSet.allOf(Card.Suit::class.java))
            }
        }
    }

    override fun resolve(): Boolean {
        if (activated) {
            logActivation()

            stage = 1

            // discard 1 card
            if (!isSelectionMade) {
                selection = gameScreen.selectedCards[0]
            }

            player.discard(selection!!)
            Game.getInstance().log("${player.name} ${gameScreen.getString(R.string.discarded)} $selection", Game.Log.TAG_ABILITY.or(Game.Log.TAG_DISCARD))

            // draw 1 card
            player.draw(2, Game.Log.TAG_ABILITY)


            if (!gameScreen.isAutoplay) {

                // show draw
                val handFragment = gameScreen.supportFragmentManager.findFragmentByTag(
                    GameScreen.HAND_FRAGMENT_TAG) as HandFragment

                handFragment.removeSelections()
                gameScreen.updateDiscardPile()

                handFragment.notifyDraw(2)
                handFragment.scrollToEnd()

                gameScreen.lockForAbility(false, "Select a card to discard") {
                    selection = gameScreen.selectedCards[0]
                    player.discard(selection!!)

                    Game.getInstance().log("${player.name} ${gameScreen.getString(R.string.discarded)} $selection", Game.Log.TAG_ABILITY.or(Game.Log.TAG_DISCARD))

                    handFragment.removeSelections()
                    gameScreen.updateDiscardPile()

                    clearSelection()

                    gameScreen.lockForAbility(true, "", null)
                    gameScreen.deactivateAbilitySelection()

                    stage = 0
                }

                gameScreen.activateAbilitySelection(1, true, Card.MAX_VALUE, EnumSet.allOf(Card.Suit::class.java))
            } else {
                makeSelection()
                player.discard(selection!!)

                Game.getInstance().log("${player.name} ${gameScreen.getString(R.string.discarded)} $selection", Game.Log.TAG_ABILITY.or(Game.Log.TAG_DISCARD))
                clearSelection()
                return super.resolve()
            }

            // always return false because resolution is not complete
            super.resolve()
        }
        return false
    }

    override fun deactivate() {
        if (activated) {
            if (!gameScreen.isAutoplay) {
                clearSelection()
                if (stage == 0) {
                    gameScreen.deactivateAbilitySelection()
                }
            }
            super.deactivate()
        }
    }

    override fun makeSelection() {
        clearSelection()
        val hand = CardGroup()
        hand.addAll(player.hand.filter { Card.UTILITY_CARDS.contains(it.suit) })

        val trade = hand.minOrNull()
        hand.remove(trade!!)
        selection = trade
    }

    override val isSelectionMade
        get() = selection != null

    override fun clearSelection() {
        selection = null
    }

    companion object {
        const val PRIORITY = TraderAbility.PRIORITY + 1
        private const val MAX_TRADEABLE_VALUE = 4
        private const val serialVersionUID = 1L
    }
}