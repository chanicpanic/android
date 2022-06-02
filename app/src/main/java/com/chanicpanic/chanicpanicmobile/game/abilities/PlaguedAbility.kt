/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.gamescreen.BoardFragment
import com.chanicpanic.chanicpanicmobile.gamescreen.HandFragment
import com.chanicpanic.chanicpanicmobile.game.*
import java.util.*

/**
 * Plagued
 * Once per turn: Pay 4 points: Discard 1 card: All cards on a board with a base value equal to the discarded card\'s base value are discarded.
 *
 * Special
 * Active
 * Resolvable
 * Selection Necessary
 */
class PlaguedAbility(gameScreen: GameScreen, player: Player) : ActiveAbility(gameScreen, player), SelectionNecessary {

    var selection: Card? = null

    override val nameID
        get() = R.string.ability_name_plagued

    override val descriptionID
        get() = R.string.ability_description_plagued

    override val isBase = false

    override val priority
        get() = PRIORITY

    override val maxUsages = 1

    override val isActivateable: Boolean
        get() {
            return super.isActivateable
                    && Game.getInstance().phase == Game.PHASE_POINT
                    && Game.getInstance().points >= COST
                    && !player.hand.isEmpty
        }

    override fun shouldActivate(checkpoint: AI.Checkpoint): Boolean {
        if (checkpoint == AI.Checkpoint.END_POINT) {
            val potentials = Array(Card.MAX_VALUE) { 0 }
            values@ for (value in MIN_VALUE..Card.MAX_VALUE) {
                if (player.hand.any { it.value == value }) {
                    for (player in Game.getInstance().getPlayers()) {
                        val board = player.board
                        for (row in board.rows) {
                            for (card in row) {
                                if (card.value == value) {
                                    if (player == this.player) {
                                        potentials[value - 1] = 0
                                        continue@values
                                    } else if (Game.getInstance().onSameTeam(player, this.player)) {
                                        potentials[value - 1]--
                                    } else {
                                        potentials[value - 1]++
                                    }
                                }
                            }
                        }
                    }
                }
            }

            var greatest = 1
            for (value in potentials.indices) {
                if (potentials[value] >= greatest) {
                    greatest = potentials[value]
                    selection = player.hand.filter { it.value == value + 1 }.minOrNull()
                }
            }

            return isSelectionMade
        }
        return false
    }

    override fun activate() {
        if (isActivateable) {
            super.activate()
            if (!gameScreen.isAutoplay) {
                gameScreen.activateAbilitySelection(1, true, Card.MAX_VALUE, EnumSet.allOf(Card.Suit::class.java))
            }
        }
    }

    override fun resolve(): Boolean {
        if (activated) {
            logActivation()

            if (!isSelectionMade) {
                selection = gameScreen.selectedCards[0]
            }

            Game.getInstance().changePoints(-COST)
            player.discard(selection!!)
            Game.getInstance().log("${player.name} ${gameScreen.getString(R.string.discarded)} $selection", Game.Log.TAG_ABILITY.or(Game.Log.TAG_DISCARD))

            if (!gameScreen.isAutoplay) {
                val handFragment = gameScreen.supportFragmentManager.findFragmentByTag(
                    GameScreen.HAND_FRAGMENT_TAG) as HandFragment
                handFragment.removeSelections()
                gameScreen.updateInfo()
                gameScreen.updateDiscardPile()
            }

            val discards = CardGroup()

            for (i in 0 until Game.getInstance().playerCount) {
                val board = Game.getInstance().getBoard(i)
                for (cardGroup in board.rows) {
                    for (j in cardGroup.size - 1 downTo 0) {
                        if (cardGroup[j].baseValue == selection!!.baseValue) {
                            discards.add(cardGroup.remove(j))
                        }
                    }
                }
            }

            Game.getInstance().discard(discards)
            if (!discards.isEmpty) {
                Game.getInstance().log("$discards ${if (discards.size == 1) "was" else "were"} discarded", Game.Log.TAG_ABILITY.or(Game.Log.TAG_DISCARD))
            }

            if (!gameScreen.isAutoplay) {
                for (i in 0 until Game.getInstance().playerCount) {
                    ((gameScreen.pager.adapter as GameScreen.BoardPagerAdapter).getFragment(i) as BoardFragment?)?.update()
                }
                gameScreen.updateDiscardPile()
            }

            return super.resolve()
        }
        return false
    }

    override fun deactivate() {
        if (activated) {
            clearSelection()
            if (!gameScreen.isAutoplay) {
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
        const val COST = 4
        private const val MIN_VALUE = 4
        const val PRIORITY = IndebtedAbility.PRIORITY + 1
        private const val serialVersionUID = 1L
    }
}