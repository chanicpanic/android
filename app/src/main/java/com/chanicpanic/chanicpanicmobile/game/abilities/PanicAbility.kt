/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.game.*

/**
 * Panic:
 * Once per game: Discard all Spades you own and end your turn: All Spades controlled by your next opponent are discarded; You may not be attacked until the end of your next turn.
 *
 * Base
 * Active
 * Resolvable
 */
class PanicAbility(gameScreen: GameScreen, player: Player) : ActiveAbility(gameScreen, player) {

    override val nameID
        get() = R.string.ability_name_panic

    override val descriptionID
        get() = R.string.ability_description_panic

    override val isBase = true

    override val priority
        get() = PRIORITY

    override val maxUsages = 1

    override val isActivateable: Boolean
        get() {
            return super.isActivateable
                    && Game.getInstance().phase >= Game.PHASE_POINT
        }

    override fun shouldActivate(checkpoint: AI.Checkpoint): Boolean {
        if (checkpoint == AI.Checkpoint.END_TURN)
        // only activate at end of turn
        {
            // only if could be attacked before next turn
            if (Game.getInstance().round >= Game.ATTACK_ROUND || Game.getInstance().round == Game.ATTACK_ROUND - 1 && player.turn > 0) {
                val board = player.board
                val health = board.getRow(Card.Suit.HEARTS).valueSum()
                val defense = board.getRow(Card.Suit.DIAMONDS).valueSum() + health

                val nextOpponent = Game.getInstance().opponentsOf(player.turn)[0]
                val opponentSpades = nextOpponent.board.getRow(Card.Suit.SPADES).valueSum()

                // activate if defenses are insufficient
                return (defense - opponentSpades - PROJECTED_SPADES <= 0) || (Game.getInstance().round >= Game.BYPASS_ROUND && health - (opponentSpades + PROJECTED_SPADES) / 2 <= 0)
            }
        }
        return false
    }

    override fun activate() {
        if (isActivateable) {
            if (!gameScreen.isAutoplay) {
                (GameScreen.DialogBuilder(gameScreen))
                        .setTitle(gameScreen.getString(R.string.dialog_panic_title))
                        .setMessage(gameScreen.getString(R.string.dialog_panic_message))
                        .setPositiveButton(gameScreen.getString(R.string.yes)) { super.activate(); resolve() }
                        .setNegativeButton(gameScreen.getString(R.string.no), null)
                        .setOnDismissListener { gameScreen.setAbilityDisplay(null) }
                        .setFullScreen(false)
                        .setCancelable(true)
                        .show()
            } else {
                super.activate()
                resolve()
            }
        }
    }

    override fun resolve(): Boolean {
        if (activated) {

            logActivation()

            player.panic()

            val discards = CardGroup()

            // discard spades in hand
            val hand = player.hand
            for (i in hand.size - 1 downTo 0) {
                val card = hand[i]
                if (card.suit == Card.Suit.SPADES) {
                    discards.add(card)
                    player.discard(card)
                }
            }

            // discard spades on board
            var spades = player.board.getRow(Card.Suit.SPADES)
            for (i in spades.size - 1 downTo 0) {
                val card = spades[i]
                discards.add(card)
                player.board.discard(spades[i])
            }


            if (!discards.isEmpty) {
                Game.getInstance().log("${player.name} ${gameScreen.getString(R.string.discarded)} $discards", Game.Log.TAG_ABILITY.or(Game.Log.TAG_DISCARD))
                discards.clear()
            }


            // discard next opponent's spades on board
            val nextOpponent = Game.getInstance().opponentsOf(player.turn)[0]
            spades = nextOpponent.board.getRow(Card.Suit.SPADES)
            for (i in spades.size - 1 downTo 0) {
                val card = spades[i]
                discards.add(card)
                nextOpponent.board.discard(spades[i])
            }

            if (!discards.isEmpty) {
                Game.getInstance().log("${nextOpponent.name} ${gameScreen.getString(R.string.discarded)} $discards", Game.Log.TAG_ABILITY.or(Game.Log.TAG_DISCARD))
            }

            super.resolve()

            // end turn
            if (gameScreen.isAutoplay) {
                throw EndTurnException()
            } else {
                gameScreen.endTurn()
            }

            return true
        }
        return false
    }

    override fun deactivate() {
        if (activated) {
            super.deactivate()
        }
    }

    override fun resetUsages() {}

    companion object {
        private const val PROJECTED_SPADES = 8
        const val PRIORITY = -2
        private const val serialVersionUID = 1L
    }
}