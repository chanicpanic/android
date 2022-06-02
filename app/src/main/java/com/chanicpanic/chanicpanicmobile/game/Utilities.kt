/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game

import kotlin.math.sqrt

/**
 * Contains the characters representing each Suit
 */
const val SUITS = "♥♦♠♣⭐"

/**
 * @param n integer to test
 * @return true if the given integer is prime
 */
fun isPrime(n: Int): Boolean {
    if (n < 2) {
        return false
    }
    var i = 2
    while (i <= sqrt(n.toDouble())) {
        if (n % i == 0) {
            return false
        }
        i++
    }
    return true
}

/**
 * @param suit the suit to retrieve a string of
 * @return the character representing the indicated Suit
 */
fun suitToString(suit: Card.Suit): String {
    return SUITS[suit.ordinal].toString()
}
