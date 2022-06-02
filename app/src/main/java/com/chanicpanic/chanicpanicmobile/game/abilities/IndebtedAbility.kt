/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.gamescreen.BoardFragment
import com.chanicpanic.chanicpanicmobile.gamescreen.HandFragment
import com.chanicpanic.chanicpanicmobile.game.*
import com.chanicpanic.chanicpanicmobile.gamescreen.CardView
import java.util.*

/**
 * Indebted
 * Once per turn: Pay 4 points: Play a Utility Card for free; You may not play any more cards this turn.
 *
 * Special
 * Active
 * Resolvable
 * Selection Necessary
 */
class IndebtedAbility(gameScreen: GameScreen, player: Player) : ActiveAbility(gameScreen, player), SelectionNecessary {

    private var selection: Card? = null

    override val nameID
        get() = R.string.ability_name_indebted

    override val descriptionID
        get() = R.string.ability_description_indebted

    override val isBase = false

    override val priority
        get() = PRIORITY

    override val maxUsages = 1

    override val isActivateable: Boolean
        get() {
            val utilityCards = CardGroup()
            utilityCards.addAll(player.hand.filter { Card.UTILITY_CARDS.contains(it.suit) })

            val hasPlayable = (utilityCards.has(Card.Suit.HEARTS) && player.board.rowSize(Card.Suit.HEARTS) < Board.FULL_ROW)
                    || (utilityCards.has(Card.Suit.DIAMONDS) && player.board.rowSize(Card.Suit.DIAMONDS) < Board.FULL_ROW)
                    || (utilityCards.has(Card.Suit.SPADES) && player.board.rowSize(Card.Suit.SPADES) < Board.FULL_ROW && Game.getInstance().round >= Game.ATTACK_ROUND)

            return super.isActivateable
                    && Game.getInstance().phase == Game.PHASE_POINT
                    && Game.getInstance().points >= COST
                    && hasPlayable
        }

    override fun shouldActivate(checkpoint: AI.Checkpoint): Boolean {
        if (checkpoint == AI.Checkpoint.END_POINT && player.playableCards.isEmpty) {
            val utilityCards = CardGroup()
            utilityCards.addAll(player.hand.filter { Card.UTILITY_CARDS.contains(it.suit) })
            if (utilityCards.has(Card.Suit.HEARTS) && player.board.rowSize(Card.Suit.HEARTS) < Board.FULL_ROW) {
                selection = player.hand.filter { it.suit == Card.Suit.HEARTS && it.cost > COST }.maxOrNull()
                return isSelectionMade
            }
            if (utilityCards.has(Card.Suit.DIAMONDS) && player.board.rowSize(Card.Suit.DIAMONDS) < Board.FULL_ROW) {
                selection = player.hand.filter { it.suit == Card.Suit.DIAMONDS && it.cost > COST }.maxOrNull()
                return isSelectionMade
            }
            if (utilityCards.has(Card.Suit.SPADES) && player.board.rowSize(Card.Suit.SPADES) < Board.FULL_ROW && Game.getInstance().round >= Game.ATTACK_ROUND) {
                selection = player.hand.filter { it.suit == Card.Suit.HEARTS && it.cost > COST }.maxOrNull()
                return isSelectionMade
            }
        }
        return false
    }

    override fun activate() {
        if (isActivateable) {
            super.activate()
            if (!gameScreen.isAutoplay) {

                val suits = EnumSet.noneOf(Card.Suit::class.java)

                if (player.board.rowSize(Card.Suit.HEARTS) < Board.FULL_ROW) {
                    suits.add(Card.Suit.HEARTS)
                }
                if (player.board.rowSize(Card.Suit.DIAMONDS) < Board.FULL_ROW) {
                    suits.add(Card.Suit.DIAMONDS)
                }
                if (player.board.rowSize(Card.Suit.SPADES) < Board.FULL_ROW && Game.getInstance().round >= Game.ATTACK_ROUND) {
                    suits.add(Card.Suit.SPADES)
                }

                gameScreen.activateAbilitySelection(1, true, Card.MAX_VALUE, suits)
            }
        }
    }

    override fun resolve(): Boolean {
        if (activated) {
            logActivation()

            Game.getInstance().changePoints(-COST)

            if (!isSelectionMade) {
                selection = gameScreen.selectedCards[0]
            }

            player.hand.remove(selection!!)
            player.board.play(selection!!)
            Game.getInstance().log("${player.name} played $selection", Game.Log.TAG_ABILITY.or(Game.Log.TAG_PLAY))

            if (!gameScreen.isAutoplay) {

                gameScreen.updateInfo()

                val selectedCard =
                    CardView(
                        gameScreen
                    )
                selectedCard.card = selection

                // update the hand on screen
                val hand = gameScreen.supportFragmentManager.findFragmentByTag(GameScreen.HAND_FRAGMENT_TAG) as HandFragment
                // todo review
                hand.adapter.clearSelection()
                hand.adapter.toggleSelection(selectedCard)
                hand.removeSelections()

                // update board
                val boardFragment = (gameScreen.pager.adapter as GameScreen.BoardPagerAdapter).getFragment(Game
                        .getInstance().turn) as BoardFragment
                boardFragment.play(selectedCard.card.suit)

                // change card position to the Board
                selectedCard.setPosition(CardView.POSITION_BOARD, Game.getInstance().turn)

                gameScreen.updateSpinner()
            }

            player.lockPlay()

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

    override fun makeSelection() {}

    override val isSelectionMade
        get() = selection != null

    override fun clearSelection() {
        selection = null
    }

    companion object {
        private const val COST = 4
        const val PRIORITY = ManipulatorAbility.PRIORITY + 1
        private const val serialVersionUID = 1L
    }
}