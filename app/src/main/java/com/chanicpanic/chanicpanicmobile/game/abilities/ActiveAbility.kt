/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game.abilities

import androidx.annotation.CallSuper
import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.game.AI
import com.chanicpanic.chanicpanicmobile.game.Game
import com.chanicpanic.chanicpanicmobile.game.Player

abstract class ActiveAbility(gameScreen: GameScreen, player: Player) : Ability(gameScreen, player), Resolvable {
    private var usages = 0

    var activated = false
        private set

    final override val isActive = true

    override fun logActivation() {
        Game.getInstance().log("${player.name} ${gameScreen.getString(R.string.activated)} $name", Game.Log.TAG_ABILITY)
    }

    open fun resetUsages() {
        usages = 0
    }

    open val isActivateable
        get() = maxUsages == NO_MAX_USAGES || usages < maxUsages

    @CallSuper
    open fun activate() {
        activated = true
    }

    @CallSuper
    open fun deactivate() {
        activated = false
    }

    @CallSuper
    override fun resolve(): Boolean {
        usages++
        deactivate()
        return true
    }

    override val isResolvable
        get() = isActivateable

    abstract val maxUsages: Int

    abstract fun shouldActivate(checkpoint: AI.Checkpoint): Boolean

    companion object {
        const val NO_MAX_USAGES = -1
        private const val serialVersionUID = 1L
    }
}
