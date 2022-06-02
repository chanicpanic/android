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
 * Pacifist
 * Thrice per turn: Discard a Spade: Draw 1 card.
 *
 * Special
 * Active
 * Resolvable
 * Selection Necessary
 */
class PacifistAbility(gameScreen: GameScreen, player: Player) : ActiveAbility(gameScreen, player), SelectionNecessary {

    private var selection: Card? = null

    override val nameID
        get() = R.string.ability_name_pacifist

    override val descriptionID
        get() = R.string.ability_description_pacifist

    override val isBase = false

    override val priority
        get() = PRIORITY

    override val maxUsages = 3

    override val isActivateable
        get() = super.isActivateable
                && Game.getInstance().phase >= Game.PHASE_POINT
                && player.hand.has(EnumSet.of(Card.Suit.SPADES))

    override fun shouldActivate(checkpoint: AI.Checkpoint) = Game.getInstance().round < Game.ATTACK_ROUND

    override fun activate() {
        if (isActivateable) {
            super.activate()

            if (!gameScreen.isAutoplay) {
                gameScreen.activateAbilitySelection(1, true, Game.MAX_VALUE, EnumSet.of(Card.Suit.SPADES))
            }
        }
    }

    override fun resolve(): Boolean {
        if (activated) {
            logActivation()

            if (!isSelectionMade) {
                selection = gameScreen.selectedCards[0]
            }

            player.discard(selection!!)
            Game.getInstance().log("${player.name} ${gameScreen.getString(R.string.discarded)} $selection", Game.Log.TAG_ABILITY.or(Game.Log.TAG_DISCARD))

            clearSelection()
            gameScreen.clearSelections()

            player.draw(1, Game.Log.TAG_ABILITY)

            if (!gameScreen.isAutoplay) {
                // show discard
                // show draw
                val handFragment = gameScreen.supportFragmentManager.findFragmentByTag(
                    GameScreen.HAND_FRAGMENT_TAG) as HandFragment

                handFragment.removeSelections()
                gameScreen.updateDiscardPile()

                handFragment.notifyDraw(1)
                handFragment.scrollToEnd()
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
        val spades = CardGroup()
        spades.addAll(player.hand.filter { it.suit == Card.Suit.SPADES })
        selection = spades.minOrNull()
    }

    override val isSelectionMade: Boolean
        get() {
            return selection != null
        }

    override fun clearSelection() {
        selection = null
    }

    companion object {
        const val PRIORITY = TradeMasterAbility.PRIORITY + 1
        private const val serialVersionUID = 1L
    }
}