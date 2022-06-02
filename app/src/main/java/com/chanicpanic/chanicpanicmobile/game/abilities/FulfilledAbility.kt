/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import androidx.fragment.app.FragmentTransaction
import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.gamescreen.HandFragment
import com.chanicpanic.chanicpanicmobile.game.Game
import com.chanicpanic.chanicpanicmobile.game.Player

/**
 * Fulfilled
 * During your Draw Phase: If you hold 5 or more cards, do not draw; Otherwise, draw until you hold 5 cards.
 *
 * Special
 * Passive
 * Explicitly Checked
 */
class FulfilledAbility(gameScreen: GameScreen, player: Player) : PassiveAbility(gameScreen, player), ExplicitlyChecked {

    override val nameID
        get() = R.string.ability_name_fulfilled

    override val descriptionID
        get() = R.string.ability_description_fulfilled

    override val isBase = false

    override fun resolve(): Boolean {
        if (Game.getInstance().round > 1) {
            logActivation()
            if (player.hand.size < 5) {
                val cards = 5 - player.hand.size
                player.draw(cards, Game.Log.TAG_ABILITY)
                if (!gameScreen.isAutoplay) {

                    if (player.hand.isEmpty) {

                        val ft = gameScreen.supportFragmentManager.beginTransaction()
                        ft.replace(R.id.frameHand, HandFragment.newInstance(Game.getInstance().turn),
                                GameScreen.HAND_FRAGMENT_TAG)
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                .commitNow()
                    } else {

                        // show draw
                        val handFragment = gameScreen.supportFragmentManager.findFragmentByTag(
                            GameScreen.HAND_FRAGMENT_TAG) as HandFragment?
                        if (handFragment != null) {
                            handFragment.notifyDraw(cards)
                            handFragment.scrollToEnd()
                        }
                    }
                }
            }
            return true
        }
        return false
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}