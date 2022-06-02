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
 * Combiner
 * Discard 2 Clubs: Draw 3 cards; Your Last Club is treated as the total value of the discarded Clubs.
 *
 * Special
 * Active
 * Resolvable
 * Selection Necessary
 */
class CombinerAbility(gameScreen: GameScreen, player: Player) : ActiveAbility(gameScreen, player), SelectionNecessary {

    private val selections = CardGroup(2)

    override val nameID
        get() = R.string.ability_name_combiner

    override val descriptionID
        get() = R.string.ability_description_combiner

    override val isBase = false

    override val priority
        get() = PRIORITY

    override val maxUsages = 2

    override val isActivateable
        get() = super.isActivateable
                && Game.getInstance().phase >= Game.PHASE_POINT
                && player.hand.has(EnumSet.of(Card.Suit.CLUBS), 2)

    override fun shouldActivate(checkpoint: AI.Checkpoint): Boolean {
        if (checkpoint != AI.Checkpoint.PRE_PLAY) {
            makeSelection()
            return isSelectionMade
        }
        return false
    }

    override fun activate() {
        if (isActivateable) {
            super.activate()
            if (!gameScreen.isAutoplay) {
                gameScreen.activateAbilitySelection(2, true, Game.MAX_VALUE, EnumSet.of(Card.Suit.CLUBS))
            }
        }
    }

    override fun resolve(): Boolean {
        if (activated) {
            logActivation()

            if (!isSelectionMade) {
                selections.addAll(gameScreen.selectedCards)
            }

            player.discard(selections[0])
            player.discard(selections[1])
            Game.getInstance().log("${player.name} ${gameScreen.getString(R.string.discarded)} $selections", Game.Log.TAG_ABILITY.or(Game.Log.TAG_DISCARD))

            player.draw(3, Game.Log.TAG_ABILITY)

            Game.getInstance().phase = Game.PHASE_CLUB
            Game.getInstance().lastClub = selections.valueSum()

            clearSelection()
            gameScreen.clearSelections()

            if (!gameScreen.isAutoplay) {
                gameScreen.updateInfo()

                // show draw
                // todo test ability with 8 cards in hand
                val handFragment = gameScreen.supportFragmentManager.findFragmentByTag(
                    GameScreen.HAND_FRAGMENT_TAG) as HandFragment

                handFragment.removeSelections()
                gameScreen.updateDiscardPile()


                handFragment.notifyDraw(3)
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
        var combos = AI.combosOfSize(player.hand.filter { it.suit == Card.Suit.CLUBS }, 2)
        combos = combos.filter { it.valueSum() <= 10 }

        if (combos.isNotEmpty()) {
            selections.addAll(combos.maxOrNull()!!)
        }
    }

    override val isSelectionMade: Boolean
        get() {
            return selections.size == 2
        }

    override fun clearSelection() {
        selections.clear()
    }

    companion object {
        const val PRIORITY = PurifierAbility.PRIORITY + 1
        private const val serialVersionUID = 1L
    }
}