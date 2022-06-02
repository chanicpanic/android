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
 * Manipulator
 * Once per turn: Pay 3 points: Draw 1 card; Place a card from your hand on the top of the deck.
 *
 * Special
 * Active
 * Resolvable
 * Selection Necessary
 */
class ManipulatorAbility(gameScreen: GameScreen, player: Player) : ActiveAbility(gameScreen, player), SelectionNecessary {

    private var selection: Card? = null

    override val nameID
        get() = R.string.ability_name_manipulator

    override val descriptionID
        get() = R.string.ability_description_manipulator

    override val isBase = false

    override val priority
        get() = PRIORITY

    override val maxUsages = 1

    override val isActivateable: Boolean
        get() {
            return super.isActivateable
                    && Game.getInstance().phase == Game.PHASE_POINT
                    && !player.isHandFull
                    && Game.getInstance().points >= COST
        }

    override fun shouldActivate(checkpoint: AI.Checkpoint): Boolean {
        if (checkpoint == AI.Checkpoint.END_POINT && !player.hand.has(Card.Suit.CLUBS)) {
            val card = player.hand.minOrNull()
            if (card != null && card.baseValue <= 3) {
                return true
            }
        }
        return false
    }

    override fun activate() {
        if (isActivateable) {
            super.activate()
            // todo: something about abilities that activate and resolve: may prevent AI from checking abilities again
            resolve()
        }
    }

    override fun resolve(): Boolean {
        if (activated) {
            logActivation()

            Game.getInstance().changePoints(-COST)

            // draw 1 card
            player.draw(1, Game.Log.TAG_ABILITY)

//            clearSelection()

            if (!gameScreen.isAutoplay) {

                val handFragment = gameScreen.supportFragmentManager.findFragmentByTag(
                    GameScreen.HAND_FRAGMENT_TAG) as HandFragment

                handFragment.notifyDraw(1)
                handFragment.scrollToEnd()

                gameScreen.updateInfo()
                gameScreen.updateDiscardPile()

                gameScreen.lockForAbility(false, "Select a card to place on the top of the deck.") {
                    selection = gameScreen.selectedCards[0]
                    player.hand.remove(selection!!)
                    selection!!.resetModifiers()
                    Game.getInstance().deck.add(0, selection!!)
                    Game.getInstance().log("${player.name} placed a card on the top of the deck", Game.Log.TAG_ABILITY)
                    gameScreen.updateDiscardPile()
                    handFragment.removeSelections()
                    gameScreen.lockForAbility(true, "", null)
                    gameScreen.deactivateAbilitySelection()
                }

                gameScreen.activateAbilitySelection(1, true, Card.MAX_VALUE, EnumSet.allOf(Card.Suit::class.java))
            } else {
                makeSelection()
                player.hand.remove(selection!!)
                selection!!.resetModifiers()
                Game.getInstance().deck.add(0, selection!!)
                Game.getInstance().log("${player.name} placed a card on the top of the deck", Game.Log.TAG_ABILITY)
            }

            return super.resolve()
        }
        return false
    }

    override fun deactivate() {
        if (activated) {
            if (!gameScreen.isAutoplay) {
                clearSelection()
            }
            super.deactivate()
        }
    }

    override fun makeSelection() {
        if (activated) {
            val cards = CardGroup()
            cards.addAll(player.hand.filter { it.suit != Card.Suit.CLUBS })
            // assertion: cards is not empty, and selection is not null
            selection = cards.minOrNull()
        }
    }

    override val isSelectionMade
        get() = selection != null

    override fun clearSelection() {}

    companion object {
        private const val COST = 3
        const val PRIORITY = CombinerAbility.PRIORITY + 1
        private const val serialVersionUID = 1L
    }
}