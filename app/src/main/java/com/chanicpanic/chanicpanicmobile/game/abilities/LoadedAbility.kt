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
 * Loaded
 * Pay 12 points: Draw until you have a full hand; You may not play any more cards this turn.
 *
 * Special
 * Active
 * Resolvable
 */
class LoadedAbility(gameScreen: GameScreen, player: Player) : ActiveAbility(gameScreen, player) {

    override val nameID
        get() = R.string.ability_name_loaded

    override val descriptionID
        get() = R.string.ability_description_loaded

    override val isBase = false

    // comes before Hopeful (other End-point ability)
    // before Archaeologist
    override val priority
        get() = PRIORITY

    override val maxUsages = NO_MAX_USAGES

    override val isActivateable: Boolean
        get() {
            return super.isActivateable
                    && Game.getInstance().phase == Game.PHASE_POINT
                    && Game.getInstance().points >= COST
                    && !player.isHandFull
        }

    override fun shouldActivate(checkpoint: AI.Checkpoint) = (checkpoint == AI.Checkpoint.PRE_PLAY && player.playableCards.average <= MAX_PLAYABLE_AVERAGE && !player.hand.has(EnumSet.of(Card.Suit.CLUBS)))
            || (checkpoint == AI.Checkpoint.END_POINT && !player.hand.has(EnumSet.of(Card.Suit.CLUBS)))

    override fun activate() {
        if (isActivateable) {
            super.activate()
            resolve()
        }
    }

    override fun resolve(): Boolean {
        if (activated) {
            logActivation()

            Game.getInstance().changePoints(-COST)

            val cards = Player.FULL_HAND - player.hand.size
            player.draw(cards, Game.Log.TAG_ABILITY)
            player.lockPlay()

            if (!gameScreen.isAutoplay) {
                // show draw
                val handFragment = gameScreen.supportFragmentManager.findFragmentByTag(
                    GameScreen.HAND_FRAGMENT_TAG) as HandFragment
                handFragment.notifyDraw(cards)
                handFragment.updateSelectables()
                handFragment.scrollToEnd()

                // update info
                gameScreen.updateInfo()

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
        const val PRIORITY = ArchaeologistAbility.PRIORITY + 1
        private const val COST = 12
        private const val MAX_PLAYABLE_AVERAGE = 4
        private const val serialVersionUID = 1L
    }
}