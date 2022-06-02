/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.gamescreen.HandFragment
import com.chanicpanic.chanicpanicmobile.game.*
import com.chanicpanic.chanicpanicmobile.gamescreen.CardGroupView
import java.util.*

/**
 * Purifier
 * Once per turn: Discard all the cards in your hand: Draw 1 card for each discarded; Shuffle the discard pile into the deck.
 *
 * Special
 * Active
 * Resolvable
 */
class PurifierAbility(gameScreen: GameScreen, player: Player) : ActiveAbility(gameScreen, player) {

    override val nameID
        get() = R.string.ability_name_purifier

    override val descriptionID
        get() = R.string.ability_description_purifier

    override val isBase = false

    override val priority
        get() = PRIORITY

    override val maxUsages = 1

    override val isActivateable
        get() = super.isActivateable
                && Game.getInstance().phase >= Game.PHASE_POINT
                && !player.hand.isEmpty


    override fun shouldActivate(checkpoint: AI.Checkpoint): Boolean {
        // can't play more than 1 card, and average value of hand is less than 5
        // cannot be able to play a club, and cannot have more than 1
        return player.playableCards.size < 2 && !player.playableCards.has(EnumSet.of(Card.Suit.CLUBS)) && !player.hand.has(EnumSet.of(Card.Suit.CLUBS), 2) && player.hand.average <= MAX_HAND_AVERAGE
    }

    override fun activate() {
        if (isActivateable) {
            super.activate()
            resolve()
        }
    }

    override fun resolve(): Boolean {
        if (activated) {
            logActivation()

            val discards = CardGroup()
            discards.addAll(player.hand)
            player.hand.clear()
            Game.getInstance().discard(discards)

            Game.getInstance().log("${player.name} ${gameScreen.getString(R.string.discarded)} $discards", Game.Log.TAG_ABILITY.or(Game.Log.TAG_DISCARD))

            player.draw(discards.size, Game.Log.TAG_ABILITY)
            Game.getInstance().shuffle()

            if (!gameScreen.isAutoplay) {
                // show discard
                (gameScreen.findViewById(R.id.hand) as CardGroupView).cardViewAdapter.selectAll()
                // show draw
                val handFragment = gameScreen.supportFragmentManager.findFragmentByTag(
                    GameScreen.HAND_FRAGMENT_TAG) as HandFragment

                handFragment.removeSelections()
                gameScreen.updateDiscardPile()

                handFragment.notifyDraw(discards.size)
                handFragment.scrollToEnd()

                gameScreen.setAbilityDisplay(null)
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


    companion object {
        const val PRIORITY = PacifistAbility.PRIORITY + 1
        private const val MAX_HAND_AVERAGE = 4
        private const val serialVersionUID = 1L
    }
}