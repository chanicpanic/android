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
 * Maximizer
 * Pay 3 points: Return a card you control to your hand: Play a card of the same suit for free that costs up to 3 points more.
 *
 * Special
 * Active
 * Resolvable
 * SelectionNecessary
 */
class MaximizerAbility(gameScreen: GameScreen, player: Player) : ActiveAbility(gameScreen, player), SelectionNecessary {

    var selection: Card? = null

    private var aiSelection: Pair<Card, Card>? = null

    private var stage = 0

    override val nameID
        get() = R.string.ability_name_maximizer

    override val descriptionID
        get() = R.string.ability_description_maximizer

    override val isBase = false

    override val priority
        get() = PRIORITY

    override val maxUsages = NO_MAX_USAGES

    override val isActivateable: Boolean
        get() {
            return super.isActivateable
                    && Game.getInstance().phase == Game.PHASE_POINT
                    && Game.getInstance().points >= COST
                    && !player.isHandFull
                    && !player.board.isEmpty
        }

    override fun shouldActivate(checkpoint: AI.Checkpoint): Boolean {
        if (checkpoint == AI.Checkpoint.END_POINT) {
            val utilities = arrayOf(CardGroup(), CardGroup(), CardGroup())
            for (card in player.hand) {
                if (Card.UTILITY_CARDS.contains(card.suit)) {
                    utilities[card.suit.ordinal].add(card)
                }
            }

            var best = 0
            for (suit in Card.UTILITY_CARDS) {
                val sortedHand = utilities[suit.ordinal].sorted()
                val sortedBoard = player.board.getRow(suit).sorted()
                val mod = if (player.hasSpecialAbility(HagglerAbility::class.java)) 1 else 0
                for (boardCard in sortedBoard) {
                    for (handCard in sortedHand) {
                        var diff = handCard.cost - (boardCard.cost - mod)
                        if (diff in 1..3) {
                            diff += player.board.rowSize(suit)
                            if (diff > best) {
                                best = diff
                                aiSelection = Pair(boardCard, handCard)
                            }
                        }
                    }
                }
            }
        }
        return isSelectionMade
    }

    override fun activate() {
        if (isActivateable) {
            super.activate()
            if (!gameScreen.isAutoplay) {
                stage = 0
                gameScreen.activateAbilitySelection(1, true, Card.MAX_VALUE, Card.UTILITY_CARDS, false)
            }
        }
    }

    override fun resolve(): Boolean {
        if (activated) {
            logActivation()

            stage = 1

            if (!gameScreen.isAutoplay) {

                if (!isSelectionMade) {
                    selection = gameScreen.selectedCards[0]
                }

                Game.getInstance().changePoints(-COST)
                player.board.remove(selection!!)
                player.hand.add(selection!!)
                Game.getInstance().log("${player.name} returned $selection to hand", Game.Log.TAG_ABILITY)

                gameScreen.updateInfo()
                gameScreen.updateSpinner()

                val handFragment = gameScreen.supportFragmentManager.findFragmentByTag(
                    GameScreen.HAND_FRAGMENT_TAG) as HandFragment
                handFragment.notifyDraw(1)
                handFragment.scrollToEnd()
                ((gameScreen.pager.adapter!! as GameScreen.BoardPagerAdapter).getFragment(gameScreen.boardShown) as BoardFragment).update()

                gameScreen.lockForAbility(false, "Select a card to play") {
                    selection = gameScreen.selectedCards[0]
                    player.hand.remove(selection!!)
                    selection!!.resetModifiers()
                    player.board.play(selection!!)
                    Game.getInstance().log("${player.name} played $selection", Game.Log.TAG_ABILITY.or(Game.Log.TAG_PLAY))

                    val selectedCard =
                        CardView(
                            gameScreen
                        )
                    selectedCard.card = selection

                    // update the hand on screen
                    // todo: review this
                    handFragment.adapter.clearSelection()
                    handFragment.adapter.toggleSelection(selectedCard)
                    handFragment.removeSelections()

                    // update board
                    val boardFragment = (gameScreen.pager.adapter as GameScreen.BoardPagerAdapter).getFragment(Game
                            .getInstance().turn) as BoardFragment

                    boardFragment.update()

                    clearSelection()

                    gameScreen.updateSpinner()

                    gameScreen.lockForAbility(true, "", null)
                    gameScreen.deactivateAbilitySelection()

                    stage = 0
                }

                gameScreen.deactivateAbilitySelection() // because of board selections
                gameScreen.activateAbilitySelection(1, true, selection!!.cost + 3, EnumSet.of(selection!!.suit))
            } else {
                Game.getInstance().changePoints(-COST)
                player.board.remove(aiSelection!!.first)
                player.hand.add(aiSelection!!.first)
                Game.getInstance().log("${player.name} returned ${aiSelection!!.first} to hand", Game.Log.TAG_ABILITY)

                player.hand.remove(aiSelection!!.second)
                aiSelection!!.second.resetModifiers()
                player.board.play(aiSelection!!.second)
                Game.getInstance().log("${player.name} played ${aiSelection!!.second}", Game.Log.TAG_ABILITY.or(Game.Log.TAG_PLAY))
                return super.resolve()
            }

            // always return false because resolution is not complete
            super.resolve()
        }
        return false
    }

    override fun deactivate() {
        if (activated) {
            clearSelection()
            if (!gameScreen.isAutoplay) {
                if (stage == 0) {
                    gameScreen.deactivateAbilitySelection()
                }
            }
            super.deactivate()
        }
    }

    override fun makeSelection() {}

    override val isSelectionMade
        get() = (selection != null && !gameScreen.isAutoplay) || (aiSelection != null && gameScreen.isAutoplay)

    override fun clearSelection() {
        selection = null; aiSelection = null
    }

    companion object {
        const val COST = 3
        const val PRIORITY = PlaguedAbility.PRIORITY + 1
        private const val serialVersionUID = 1L
    }
}