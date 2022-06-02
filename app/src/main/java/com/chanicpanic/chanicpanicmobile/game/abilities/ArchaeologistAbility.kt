/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.gamescreen.HandFragment
import com.chanicpanic.chanicpanicmobile.game.*

/**
 * Archaeologist
 * Once per turn: Pay 4 points: Declare a suit; Discard the top card of the deck until a card of the declared suit is discarded or the deck runs out; Add the top card of the discard pile to your hand.
 *
 * Special
 * Active
 * Resolvable
 * Selection Necessary
 */
class ArchaeologistAbility(gameScreen: GameScreen, player: Player) : ActiveAbility(gameScreen, player), SelectionNecessary {

    private var selection: Card.Suit? = null

    override val nameID
        get() = R.string.ability_name_archaeologist

    override val descriptionID
        get() = R.string.ability_description_archaeologist

    override val isBase = false

    // before Hopeful (Once per turn comes first)
    // after Loaded
    override val priority
        get() = PRIORITY

    override val maxUsages = 1

    override val isActivateable: Boolean
        get() {
            return super.isActivateable
                    && Game.getInstance().phase == Game.PHASE_POINT
                    && Game.getInstance().points >= COST
                    && !player.isHandFull
        }

    override fun shouldActivate(checkpoint: AI.Checkpoint): Boolean {
        var hearts = 0
        var diamonds = 0
        var spades = 0
        var clubs = 0
        var stars = 0

        // tally up cards by suit in hand
        for (card in player.hand) {
            when (card.suit) {
                Card.Suit.HEARTS -> hearts++
                Card.Suit.DIAMONDS -> diamonds++
                Card.Suit.SPADES -> spades++
                Card.Suit.CLUBS -> clubs++
                Card.Suit.STARS -> stars++
            }
        }

        val counts = arrayOf(hearts, diamonds, spades, clubs)

        // find the suit with the lowest count
        // invalidate utility cards whose count + board presence exceeds a full row
        var index = 0
        var min = Integer.MAX_VALUE
        for (i in counts.indices) {
            val count = counts[i]
            if (count < min) {
                if (i < 3) {
                    if (player.board.rowSize(Card.Suit.values()[i]) + count >= Board.FULL_ROW) {
                        continue
                    }
                }
                min = count
                index = i
            }
        }

        // only activate if going for a suit not held in hand
        if (min > 0) {
            return false
        }

        selection = Card.Suit.values()[index]

        return true
    }

    override fun activate() {
        if (isActivateable) {
            super.activate()

            if (!gameScreen.isAutoplay) {
                selection = Card.Suit.HEARTS

                (GameScreen.DialogBuilder(gameScreen))
                        .setTitle("Declare a Suit")
                        .setPositiveButton("Confirm") { resolve() }
                        .setNegativeButton("Cancel") { clearSelection() }
                        .setOnDismissListener { gameScreen.setAbilityDisplay(null) }
                        .setCancelable(false)
                        .setFullScreen(false)
                        .setRadioGroup(Card.Suit.getSuitNames(gameScreen, false)) { _, checkedId -> selection = Card.Suit.values()[checkedId] }
                        .show()
            }

        }
    }

    override fun resolve(): Boolean {
        if (activated) {
            logActivation()

            Game.getInstance().changePoints(-COST)

            Game.getInstance().log("${player.name} declared ${Card.Suit.getName(selection!!, gameScreen)}", Game.Log.TAG_ABILITY)

            val discards = CardGroup()
            var discard: Card?
            var out: Boolean
            do {
                out = Game.getInstance().deck.size == 1
                discard = Game.getInstance().draw()
                discards.add(discard)
                Game.getInstance().discard(discard)
            } while (discard!!.suit != selection && !out)

            Game.getInstance().log("$discards ${if (discards.size > 1) "were" else "was"} ${gameScreen.getString(R.string.discarded)}", Game.Log.TAG_ABILITY.or(Game.Log.TAG_DISCARD))

            player.hand.add(discard)
            Game.getInstance().discardPile.remove(discard)

            Game.getInstance().log("${player.name} added $discard to hand", Game.Log.TAG_ABILITY)

            clearSelection()

            if (!gameScreen.isAutoplay) {
                gameScreen.updateInfo()
                gameScreen.updateDiscardPile()

                // show draw
                val handFragment = gameScreen.supportFragmentManager.findFragmentByTag(
                    GameScreen.HAND_FRAGMENT_TAG) as HandFragment
                handFragment.notifyDraw(1)
                handFragment.updateSelectables()
                handFragment.scrollToEnd()
            }

            return super.resolve()
        }
        return false
    }

    override fun deactivate() {
        if (activated) {
            super.deactivate()
        }
    }

    override fun makeSelection() {

    }

    override val isSelectionMade: Boolean
        get() {
            return selection != null
        }

    override fun clearSelection() {
        selection = null
    }

    companion object {
        const val PRIORITY = SellerAbility.PRIORITY + 1
        private const val COST = 4
        private const val serialVersionUID = 1L
    }
}