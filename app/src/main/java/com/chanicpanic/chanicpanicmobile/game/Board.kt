/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game

import java.io.Serializable

/**
 * Represents a Board
 * Contains 3 rows of cards, one for each Suit of Utility Cards
 */
class Board(val player: Int) : Serializable {

    val rows = arrayOf(CardGroup(FULL_ROW), CardGroup(FULL_ROW), CardGroup(FULL_ROW))

    /**
     * returns a CardGroup of all the cards on the board
     */
    val collapse: CardGroup
        get() {
            val cards = CardGroup()
            cards.addAll(rows[0])
            cards.addAll(rows[1])
            cards.addAll(rows[2])
            return cards
        }

    /**
     * @param suit the suit of the row to examine
     * @return the number of cards in the row of the given Suit
     */
    fun rowSize(suit: Card.Suit): Int {
        return rows[suit.ordinal].size
    }

    /**
     * adds a Card to the appropriate row of the board
     *
     * @param card the Card to add
     * @return true if the card is added
     */
    fun play(card: Card): Boolean {
        return rows[card.suit.ordinal].add(card)
    }

    fun discard(card: Card) {
        remove(card)
        Game.getInstance().discard(card)
    }

    fun remove(card: Card) {
        rows[card.suit.ordinal].remove(card)
    }

    /**
     * @param suit the Suit of the row to obtain
     * @return a CardGroup representing the row of the indicated Suit
     */
    fun getRow(suit: Card.Suit): CardGroup {
        return rows[suit.ordinal]
    }

    fun clear() {
        for (row in rows) {
            row.clear()
        }
    }

    val isEmpty
        get() = rows[0].isEmpty && rows[1].isEmpty && rows[2].isEmpty

    override fun toString(): String {
        val suits = Card.Suit.values()
        val result = StringBuilder()
        if (player == Game.getInstance().player.startingTurn) {
            for (i in 2 downTo 0) {
                result.append(suitToString(suits[i])).append(toString(rows[i])).append(if (i > 0) "\n" else "")
            }
        } else {
            for (i in 0..2) {
                result.append(suitToString(suits[i])).append(toString(rows[i])).append(if (i < 2) "\n" else "")
            }
        }
        return result.toString()
    }

    companion object {
        private const val serialVersionUID = 1L

        const val FULL_ROW = 3

        fun toString(group: CardGroup): String {
            val result = StringBuilder()
            for (i in 0 until group.size) {
                result.append(" ").append(group[i].baseValue)
            }
            return result.toString()
        }
    }
}