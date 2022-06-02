/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game

import java.io.Serializable
import java.util.*

/**
 * Represents a list of cards and contains useful methods for operations and calculations
 */
class CardGroup(private val maxSize: Int = Integer.MAX_VALUE) : Comparable<CardGroup>, Serializable, Iterable<Card> {

    val cards: ArrayList<Card> = ArrayList()

    /**
     * @return true if the Group contains 0 Cards
     */
    val isEmpty: Boolean
        get() {
            return cards.isEmpty()
        }

    val size
        get() = cards.size

    val average: Double
        get() = valueSum().toDouble() / size

    /**
     * attempts to add a card to the group
     * @param card the card to add
     * @return true if the card was added
     */
    fun add(card: Card): Boolean {
        return if (size < maxSize) {
            cards.add(card)
        } else false
    }

    /**
     * attempts to remove a card from the group
     * @param card the card to remove
     * @return true if the group contained the card and was removed
     */
    fun remove(card: Card): Boolean {
        return cards.remove(card)
    }

    /**
     * removes the Card at a given index
     * @param index the index of the Card to remove
     * @return the Card that was removed
     */
    fun remove(index: Int): Card {
        return cards.removeAt(index)
    }

    override fun toString(): String {
        val result = StringBuilder()
        for (i in cards.indices) {
            result.append(cards[i]).append(" ")
        }
        return result.toString()
    }

    operator fun get(index: Int): Card {
        return cards[index]
    }

    fun toArrayList(): ArrayList<Card> {
        return cards
    }

    /**
     * removes all Cards from the group
     */
    fun clear() {
        cards.clear()
    }

    fun valueSum(): Int {
        var sum = 0
        for (card in cards) {
            sum += card.value
        }
        return sum
    }

    fun costSum(): Int {
        var sum = 0
        for (card in cards) {
            sum += card.cost
        }
        return sum
    }

    /**
     *
     * @return the Card with the greatest value in the group
     */
    fun highestCard() = cards.maxOrNull()

    fun highestValue() = highestCard()?.value ?: 0

    /**
     * mixes the order of the Cards
     */
    fun shuffle() {
        cards.shuffle()
    }

    /**
     * @return the sum of the rank values of all the cards in the group
     */
    fun rankValueSum(): Int {
        var sum = 0
        for (card in cards) {
            sum += card.rankValue()
        }
        return sum
    }

    override fun compareTo(other: CardGroup): Int {
        val value = valueSum() - other.valueSum()
        return if (value != 0) {
            value
        } else rankValueSum() - other.rankValueSum()
    }

    /**
     * removes the intersection of this Group and another
     * @param c the Group of Cards to remove
     */
    fun removeAll(c: CardGroup) {
        cards.removeAll(c.toArrayList())
    }

    /**
     * adds all cards in the given group to this one
     * or until the max is reached
     * @param cg the cards to add
     */
    fun addAll(cg: CardGroup) {
        addAll(cg.toArrayList())
    }

    fun addAll(cardList: List<Card>) {
        for (card in cardList) {
            if (!add(card)) {
                return
            }
        }
    }

    fun indexOf(card: Card): Int {
        return cards.indexOf(card)
    }

    fun add(index: Int, card: Card): Boolean {
        if (size < maxSize) {
            cards.add(index, card)
            return true
        }
        return false
    }

    /**
     * adds all cards in a CardGroup to this one a a given index
     * @param index the index to add the cards
     * @param cardGroup the cards to add
     */
    fun addAll(index: Int, cardGroup: CardGroup) {
        addAll(index, cardGroup.toArrayList())
    }

    /**
     * adds all cards in a List to this one a a given index
     * @param index the index to add the cards
     * @param cardList the cards to add
     */
    fun addAll(index: Int, cardList: List<Card>) {
        for (card in cardList) {
            if (!add(index, card)) {
                return
            }
        }
    }

    fun has(suit: Card.Suit, number: Int = 1): Boolean = has(EnumSet.of(suit), number)

    fun has(suits: EnumSet<Card.Suit>, number: Int = 1): Boolean {
        var count = 0
        for (card in cards) {
            if (suits.contains(card.suit)) {
                if (++count == number) {
                    return true
                }
            }
        }
        return false
    }

    fun removeAll(suit: Card.Suit) {
        for (i in cards.size - 1 downTo 0) {
            if (cards[i].suit == suit) {
                cards.removeAt(i)
            }
        }
    }

    override fun iterator(): Iterator<Card> = cards.iterator()

    /**
     * This Comparator compares the values of cardgroups
     * and then compares the sizes with the smallest sizes being the greatest
     */
    object ValueSizeComparator : Comparator<CardGroup> {
        override fun compare(cg1: CardGroup, cg2: CardGroup): Int {
            val value = cg1.valueSum() - cg2.valueSum()
            return if (value != 0) {
                value
            } else cg2.size - cg1.size
        }
    }

    object CostSizeComparator : Comparator<CardGroup> {
        override fun compare(cg1: CardGroup, cg2: CardGroup): Int {
            val value = cg1.costSum() - cg2.costSum()
            return if (value != 0) {
                value
            } else cg2.size - cg1.size
        }
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
