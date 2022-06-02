/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.game.EndException
import com.chanicpanic.chanicpanicmobile.game.Player
import java.io.Serializable
import java.util.*

/**
 * This abstract class is the superclass of all abilities
 *
 * All abilities have the following attributes:
 *
 * All abilities are either Resolvable or Continuous
 *
 * Resolvable abilities have a one time or temporary effect
 *
 * Continuous abilities are always in effect and cannot be considered as completed/resolved
 *
 * All abilities are either Active or Passive
 *
 * Active abilities are Resolvable
 * They require a choice by the player to activate
 * They may have a cost, or a limited number of usages.
 *
 * Passive abilities may be Resolvable or Continuous
 * Their effects are activated automatically
 *
 * All abilities are either Base or Special
 *
 * Base abilities are owned by every player
 *
 * Special abilities are unique to a player.
 */
abstract class Ability(@Transient var gameScreen: GameScreen, @Transient var player: Player) : Serializable {
    val name: String
        get() = gameScreen.getString(nameID)

    val description: String
        get() = gameScreen.getString(descriptionID)

    val isSpecial
        get() = !isBase

    val isPassive
        get() = !isActive

    fun load(gameScreen: GameScreen, player: Player) {
        this.gameScreen = gameScreen
        this.player = player
    }

    override fun toString(): String {
        return "$name\n$description"
    }

    protected abstract val nameID: Int

    protected abstract val descriptionID: Int

    abstract val isBase: Boolean

    abstract val isActive: Boolean

    protected abstract fun logActivation()

    companion object {
        private const val serialVersionUID = 1L
    }
}

/**
 * This interface defines an ability that is resolvable.
 */
interface Resolvable {

    fun resolve(): Boolean

    val isResolvable: Boolean

    val priority: Int

    class AbilityPriorityComparator : Comparator<Ability> {
        override fun compare(o1: Ability, o2: Ability): Int {
            if (o1 is Resolvable && o2 is Resolvable) {
                val r1 = o1 as Resolvable
                val r2 = o2 as Resolvable

                return r1.priority - r2.priority
            }
            return if (o1 is Resolvable) 1 else -1
        }
    }
}

/**
 * This interface defines an ability that requires a selection by the player
 */
interface SelectionNecessary {

    fun makeSelection()

    val isSelectionMade: Boolean

    fun clearSelection()
}

/**
 * marker interface for Continuous Abilities
 */
interface Continuous


/**
 * This interface marks an ability that is activated once per turn
 * (with the possible exception of turn 1).
 *
 * The Game explicitly checks if a player has this ability.
 *
 */
interface ExplicitlyChecked {
    fun resolve(): Boolean
}

/**
 * This unchecked Exception should be thrown by any ability whose effect ends the turn of the player
 * in order to end the turn and interrupt any other actions.
 *
 * It is guaranteed to be caught.
 */
class EndTurnException : EndException()