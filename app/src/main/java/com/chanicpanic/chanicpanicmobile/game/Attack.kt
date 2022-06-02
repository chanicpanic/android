/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game

/**
 * This class represents an Attack
 * An Attack object is immutable.
 */
class Attack(defenderHearts: CardGroup,
             defenderDiamonds: CardGroup,
             spadesUsed: CardGroup,
             diamondsDestroyed: CardGroup,
             heartsDestroyed: CardGroup,
             val isBypass: Boolean,
             power: Double)
    : Comparable<Attack> {

    val spadesUsed = CardGroup()
    val diamondsDestroyed = CardGroup()
    val heartsDestroyed = CardGroup()
    val attackPower: Int
    val damage: Int
    val efficiency: Int
    val isKill: Boolean

    val rating: Int
        get() = damage + efficiency

    init {
        this.spadesUsed.addAll(spadesUsed)
        this.diamondsDestroyed.addAll(diamondsDestroyed)
        this.heartsDestroyed.addAll(heartsDestroyed)
        val precisePower = this.spadesUsed.valueSum() * power
        attackPower = precisePower.toInt()
        damage = this.diamondsDestroyed.valueSum() + this.heartsDestroyed.valueSum()
        efficiency = if (precisePower > 0 && damage > 0) ((damage / precisePower * 100 + 10 * (10 - (attackPower - damage))) / 2).toInt() else 0

        isKill = if (defenderHearts.size - heartsDestroyed.size == 0) // if there are no hearts left
        {
            if (defenderHearts.size > 0) // if there were hearts
            {
                true
            } else // if all diamonds were destroyed and damage carried over, or it was a bypass with damage
            {
                attackPower > damage && defenderDiamonds.size - diamondsDestroyed.size == 0 || isBypass && attackPower > 0
            }
        } else {
            false
        }
    }

    override fun compareTo(other: Attack): Int {
        if (isKill == other.isKill) {
            return rating - other.rating
        }
        return if (isKill) 1 else -1
    }
}
