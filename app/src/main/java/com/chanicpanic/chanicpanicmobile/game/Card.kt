/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game

import android.content.Context
import com.chanicpanic.chanicpanicmobile.R
import java.io.Serializable
import java.util.*

/**
 * Represents a Card with a Suit and a value
 * A card is immutable, and only one Card should exist with any given combination of fields
 */
class Card private constructor(val suit: Suit, val baseValue: Int, private val deckId: Int) : Comparable<Card>, Serializable {

    private var valueShift = 0

    private var valueMultiplier = 1

    private var costShift = 0

    private var costMultiplier = 1

    fun addValueShift(shift: Int) {
        valueShift += shift
    }

    fun resetValueModifiers() {
        valueShift = 0; valueMultiplier = 1
    }

    fun addCostShift(shift: Int) {
        costShift += shift
    }

    fun resetCostModifiers() {
        costShift = 0; costMultiplier = 1
    }

    fun resetModifiers() {
        resetValueModifiers(); resetCostModifiers()
    }

    val value
        get() = baseValue * valueMultiplier + valueShift

    val cost
        get() = if (UTILITY_CARDS.contains(suit)) baseValue * costMultiplier + costShift else 0

    /**
     * @return a value that factors in the card's suit
     */
    fun rankValue(): Int {
        return baseValue + (4 - suit.ordinal)
    }

    /**
     * first compares values
     * if they are equal, rank values are compared such that
     * the order is hearts, diamonds, spades, clubs
     * @param other the card to compare to
     * @return an int > 0 if this card is greater than the other, < 0 if less, = 0 if equal
     */
    override fun compareTo(other: Card): Int {
        if (baseValue != other.baseValue) {
            return baseValue - other.baseValue
        }
        val comparison = rankValue() - other.rankValue()
        return if (comparison != 0) {
            comparison
        } else deckId - other.deckId
    }

    private inner class CardComparatorRankValue : Comparator<Card> {
        override fun compare(c1: Card, c2: Card): Int {
            return c1.rankValue() - c2.rankValue()
        }
    }

    /**
     * As all Cards are unique, this should only return true if the cards have the same reference
     * @param other the Object to test for equality
     * @return true if the object is a Card with the same Suit and value and deckId
     */
    override fun equals(other: Any?): Boolean {
        if (other is Card) {
            val card = other as Card?
            return suit == card!!.suit && baseValue == card.baseValue && deckId == card.deckId
        }
        return false
    }

    override fun toString(): String {
        return suitToString(suit) + baseValue
    }

    fun cardViewText(): String {
        return suitToString(suit) + "\n" + value
    }

    /**
     *
     * @param player the player who owns this card
     * @param points the points left
     * @param phase the current phase
     * @param lastClub the last club played
     * @return true if the card can be played with the given parameters
     */
    fun isPlayable(player: Player, points: Int, phase: Int, lastClub: Int): Boolean {
        if (player.isPlayLocked) {
            return false
        }
        if (phase == Game.PHASE_CLUB) {
            if (baseValue > lastClub) {
                return false
            }
        } else if (UTILITY_CARDS.contains(suit))
        // if Point Phase and Utility card
        {
            if (cost > points) {
                return false
            }
        }
        if (suit != Suit.CLUBS && player.board.rowSize(suit) == Board.FULL_ROW) {
            return false
        }
        if (suit == Suit.SPADES && Game.getInstance().round < Game.ATTACK_ROUND) {
            return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = suit.hashCode()
        result = 31 * result + value
        result = 31 * result + deckId
        return result
    }

    /**
     * Represents the possible suits of a card
     */
    enum class Suit {
        HEARTS, DIAMONDS, SPADES, CLUBS, STARS;


        companion object {

            fun getName(suit: Suit, context: Context): String {
                return context.getString(getNameId(suit))
            }

            fun getNameId(suit: Suit): Int {
                return when (suit) {
                    HEARTS -> R.string.hearts
                    DIAMONDS -> R.string.diamonds
                    SPADES -> R.string.spades
                    CLUBS -> R.string.clubs
                    STARS -> R.string.stars
                }
            }

            fun getSuitNames(context: Context, stars: Boolean): Array<String?> {
                val names = arrayOfNulls<String>(if (stars) 5 else 4)

                for (i in names.indices) {
                    names[i] = getName(values()[i], context)
                }

                return names
            }
        }
    }

    companion object {
        private const val serialVersionUID = 1L

        const val MAX_VALUE = 10

        val UTILITY_CARDS: EnumSet<Suit> = EnumSet.of(Suit.HEARTS, Suit.DIAMONDS, Suit.SPADES)
        val RESOURCE_CARDS: EnumSet<Suit> = EnumSet.of(Suit.CLUBS, Suit.STARS)

        /**
         * This package-private method generates cards for a game.
         * This should only be called by the Game class
         * @param decks the number of decks to generate
         * @return a CardGroup containing a set of unique Cards to use in the game
         */
        internal fun generateDecks(decks: Int): CardGroup {
            val cards = CardGroup()
            val suits = Suit.values()
            for (i in 0 until decks) {
                for (j in 1..MAX_VALUE) {
                    for (k in 0..3) {
                        cards.add(Card(suits[k], j, i))
                    }
                }
            }
            return cards
        }
    }
}