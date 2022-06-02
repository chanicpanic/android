/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.game.Player

/**
 * Club Member
 * When you play a Club: You may play any number of cards whose total value is less than or equal to the value of the Club.
 *
 * Special
 * Passive
 * Continuous
 */
class ClubMemberAbility(gameScreen: GameScreen, player: Player) : PassiveAbility(gameScreen, player), Continuous {

    override val nameID
        get() = R.string.ability_name_club_member

    override val descriptionID
        get() = R.string.ability_description_club_member

    override val isBase = false


    companion object {
        private const val serialVersionUID = 1L
    }
}