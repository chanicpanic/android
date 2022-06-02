/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.gamescreen.HandFragment
import com.chanicpanic.chanicpanicmobile.game.AI
import com.chanicpanic.chanicpanicmobile.game.Card
import com.chanicpanic.chanicpanicmobile.game.Game
import com.chanicpanic.chanicpanicmobile.game.Player
import java.util.*

/**
 * Gambler
 * Twice per turn: Discard 1 card: Discard the top card of the deck; If the deck card has a lower value than the card from your hand, draw 2 cards.
 *
 * Special
 * Active
 * Resolvable
 * Selection Necessary
 */
class GamblerAbility(gameScreen: GameScreen, player: Player) : ActiveAbility(gameScreen, player), SelectionNecessary {

    private var selection: Card? = null

    override val nameID
        get() = R.string.ability_name_gambler

    override val descriptionID
        get() = R.string.ability_description_gambler

    override val isBase = false

    override val priority
        get() = PRIORITY

    override val maxUsages = 2

    override val isActivateable: Boolean
        get() {
            return super.isActivateable
                    && Game.getInstance().phase >= Game.PHASE_POINT
                    && !player.hand.isEmpty
        }

    override fun shouldActivate(checkpoint: AI.Checkpoint) = false

    override fun activate() {
        if (isActivateable) {
            super.activate()
            if (!gameScreen.isAutoplay) {
                gameScreen.activateAbilitySelection(1, true, Game.MAX_VALUE, EnumSet.allOf(Card.Suit::class.java))
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

            val top = Game.getInstance().draw()
            Game.getInstance().discard(top)
            Game.getInstance().log("$top was ${gameScreen.getString(R.string.discarded)}", Game.Log.TAG_ABILITY.or(Game.Log.TAG_DISCARD))

            if (!gameScreen.isAutoplay) {
                (gameScreen.supportFragmentManager.findFragmentByTag(GameScreen.HAND_FRAGMENT_TAG) as HandFragment).removeSelections()
                gameScreen.updateDiscardPile()
            }

            if (top.value < selection!!.value) {
                player.draw(2, Game.Log.TAG_ABILITY)

                if (!gameScreen.isAutoplay) {
                    val handFragment = gameScreen.supportFragmentManager.findFragmentByTag(
                        GameScreen.HAND_FRAGMENT_TAG) as HandFragment
                    handFragment.notifyDraw(2)
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
            return selection != null
        }

    override fun clearSelection() {
        selection = null
    }

    companion object {
        const val PRIORITY = TraderAbility.PRIORITY + 1
        private const val serialVersionUID = 1L
    }
}