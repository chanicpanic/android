/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen.HAND_FRAGMENT_TAG
import com.chanicpanic.chanicpanicmobile.gamescreen.BoardFragment
import com.chanicpanic.chanicpanicmobile.gamescreen.HandFragment
import com.chanicpanic.chanicpanicmobile.game.*
import com.chanicpanic.chanicpanicmobile.gamescreen.CardView

/**
 * Hopeful
 * Pay 4 points: Draw 1 card; If it is a Heart, you may immediately play it for free.
 *
 * Special
 * Active
 * Resolvable
 */
class HopefulAbility(gameScreen: GameScreen, player: Player) : ActiveAbility(gameScreen, player) {

    override val nameID
        get() = R.string.ability_name_hopeful

    override val descriptionID
        get() = R.string.ability_description_hopeful

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
        }

    override fun shouldActivate(checkpoint: AI.Checkpoint) = checkpoint == AI.Checkpoint.END_POINT

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

            player.draw(1, Game.Log.TAG_ABILITY)

            if (!gameScreen.isAutoplay) {
                val handFragment = gameScreen.supportFragmentManager.findFragmentByTag(HAND_FRAGMENT_TAG) as HandFragment
                handFragment.notifyDraw(1)
                handFragment.updateSelectables()
                handFragment.scrollToEnd()

                gameScreen.updateInfo()

                gameScreen.setAbilityDisplay(null)
            }

            val card = player.hand[player.hand.size - 1]

            if (card.suit == Card.Suit.HEARTS && player.board.rowSize(Card.Suit.HEARTS) < Board.FULL_ROW && !player.isPlayLocked) {
                if (!gameScreen.isAutoplay) {
                    GameScreen.DialogBuilder(gameScreen)
                            .setTitle("Would you like to play $card?")
                            .setPositiveButton("Yes") {
                                player.hand.remove(card)
                                player.board.play(card)
                                Game.getInstance().log("${player.name} played $card", Game.Log.TAG_ABILITY.or(Game.Log.TAG_PLAY))

                                val selectedCard =
                                    CardView(
                                        gameScreen
                                    )
                                selectedCard.card = card

                                // update the hand on screen
                                val hand = gameScreen.supportFragmentManager.findFragmentByTag(HAND_FRAGMENT_TAG) as HandFragment
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
                            .setNegativeButton("No", null)
                            .setFullScreen(false)
                            .setCancelable(false)
                            .show()
                } else if (card.value > 2) {
                    player.hand.remove(card)
                    player.board.play(card)
                    Game.getInstance().log("${player.name} played $card", Game.Log.TAG_ABILITY.or(Game.Log.TAG_PLAY))
                }
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
        const val PRIORITY = MaximizerAbility.PRIORITY + 1
        private const val COST = 4
        private const val serialVersionUID = 1L
    }
}