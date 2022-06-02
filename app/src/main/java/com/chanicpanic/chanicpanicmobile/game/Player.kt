/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game

import android.annotation.SuppressLint
import com.chanicpanic.chanicpanicmobile.game.abilities.*
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap

@SuppressLint("UseSparseArrays")
/**
 * Represents a Player
 * Contains a Hand and a Board.
 */
class Player(val startingTurn: Int) : Serializable, Comparable<Player> {

    private var _data = HashMap<Int, Int>()

    val data: HashMap<Int, Int>
        get() {
            // _data may be null following deserialization
            @Suppress("SENSELESS_COMPARISON")
            if (_data == null) {
                _data = HashMap()
            }
            return _data
        }

    /**
     * the hand of this player
     */
    var hand: CardGroup
        private set

    /**
     * the player's board
     */
    val board: Board

    /**
     * the effective turn of this player
     * it begins equal to turn, but may change as players are eliminated
     */
    var turn: Int
        private set

    var presence: Int
        private set

    /**
     * a list of this player's special abilities
     */
    private val specialAbilities = ArrayList<Ability>()

    /**
     * a list of this player's base abilities
     */
    private val baseAbilities = ArrayList<Ability>()

    val fullHand
        get() = data.getOrPut(KEY_FULL_HAND) { FULL_HAND }

    val fullBoardRow
        get() = data.getOrPut(KEY_BOARD_FULL_ROW) { Board.FULL_ROW }

    /**
     * whether this player is panicked
     */
    var isPanicked: Boolean = false
        private set

    @Transient
    var ai: AI? = null
        private set

    /**
     * a set of the starting turns of players that have attacked this player in between turns
     */
    private val attackedBy = HashSet<Int>(2)

    /**
     * the mode(s) that this player can be played
     */
    var mode: EnumSet<Mode>

    /**
     * the display name of this player
     */
    var name: String

    /**
     * when true, cards cannot be played
     */
    @Transient
    var isPlayLocked: Boolean = false
        private set

    /**
     *
     * @return a list of all of this player's abilities
     */
    val abilities: List<Ability>
        get() {
            val abilityList = ArrayList<Ability>()
            abilityList.addAll(specialAbilities)
            abilityList.addAll(baseAbilities)
            return abilityList
        }

    /**
     * @return whether this player has an AI attached
     */
    val isAIAttached: Boolean
        get() = ai != null

    /**
     * @return whether this player's hand is full
     */
    val isHandFull: Boolean
        get() = hand.size == fullHand

    init {
        presence = 0
        turn = startingTurn
        hand = CardGroup(FULL_HAND)
        board = Board(startingTurn)
        name = "Player " + (startingTurn + 1)
        mode = EnumSet.allOf(Mode::class.java)
    }

    /**
     * starts this player's turn for the given round
     */
    fun startTurn() {
        // reset abilities
        for (ability in abilities) {
            if (ability is ActiveAbility) {
                ability.resetUsages()
            }
        }

        // clear attacks
        attackedBy.clear()

        // clear panic
        isPanicked = false

        // clear playLock
        isPlayLocked = false
    }

    fun calculatePresence() {
        val gain = 2 * board.collapse.size
        presence += gain
        Game.getInstance().log("$name gained $gain Presence")
        Game.getInstance().log("Total Presence: $presence")
    }

    /**
     * sets the playLock variable
     */
    fun lockPlay() {
        isPlayLocked = true
    }

    /**
     * sets the panicked variable
     */
    fun panic() {
        isPanicked = true
    }

    /**
     *
     * @param player
     * @return whether this player can be attacked by the given player
     */
    fun isAttackableBy(player: Player) =
            Game.getInstance().round >= Game.ATTACK_ROUND
                    && !isPanicked
                    && !Game.getInstance().onSameTeam(this, player)
                    && (attackedBy.size < 2 || attackedBy.contains(player.startingTurn))

    fun isBypassableBy(player: Player) =
            isAttackableBy(player)
                    && Game.getInstance().round >= Game.BYPASS_ROUND
                    && board.rowSize(Card.Suit.DIAMONDS) > 0
                    && (!hasSpecialAbility(LifelineAbility::class.java) || board.rowSize(Card.Suit.HEARTS) != 1)


    /**
     * adds this player to the given player's attackedBy set
     * @param player the player that is being attacked
     */
    fun attack(player: Player) {
        player.attackedBy.add(startingTurn)
    }

    /**
     * reduces the effective turn of this player
     */
    fun decrementTurn() {
        turn--
    }

    /**
     * precondition: card is playable and in hand;
     * plays a card and makes all necessary updates to fields
     * @param card the card to be played
     * @return the index of the card played
     */
    fun playCard(card: Card): Int {
        val index = hand.toArrayList().indexOf(card)
        if (index > -1 && card.isPlayable(this, Game.getInstance().points, Game.getInstance().phase, Game.getInstance().lastClub)) {
            hand.remove(index)


            // if card is a club
            if (card.suit == Card.Suit.CLUBS) {
                Game.getInstance().log("$name played $card", Game.Log.TAG_PLAY)

                // remove points and activate club restrictions
                Game.getInstance().lastClub = card.value + if (hasSpecialAbility(ResourcefulAbility::class.java)) 2 else 0
                Game.getInstance().points = 0
                Game.getInstance().phase = Game.PHASE_CLUB
                // draw the appropriate number of cards
                // value 1-5: draw two cards
                // value 6-10: draw one card
                if (10 - card.baseValue >= 5 || hasSpecialAbility(StandardizationAbility::class.java)) {
                    draw(2)
                } else {
                    draw(1)
                }
                // discard the club
                Game.getInstance().discard(card)
            } else
            // card is heart, diamond, or spade
            {
                if (Game.getInstance().phase == Game.PHASE_CLUB) {
                    if (hasSpecialAbility(ClubMemberAbility::class.java)) {
                        Game.getInstance().lastClub = Game.getInstance().lastClub - card.value
                    } else {
                        Game.getInstance().lastClub = 0 // no more cards can be played this turn
                    }
                } else {
                    Game.getInstance().changePoints(-card.cost) // decrement points
                }

                card.resetModifiers()

                if ((card.suit == Card.Suit.HEARTS && hasSpecialAbility(HealthyAbility::class.java)) ||
                        (card.suit == Card.Suit.DIAMONDS && hasSpecialAbility(ToughnessAbility::class.java)) ||
                        (card.suit == Card.Suit.SPADES && hasSpecialAbility(WarriorAbility::class.java))) {
                    card.addValueShift(1)
                }

                board.play(card) // add card to board

                Game.getInstance().log("$name played $card", Game.Log.TAG_PLAY)
            }

        }
        return index
    }

    /**
     * draws n cards
     * @param n the number of cards to draw
     */
    @JvmOverloads
    fun draw(n: Int, @Game.Log.LogTag tag: Int = 0) {
        for (i in 1..n) {
            val card = Game.getInstance().draw(false)
            if (!hand.add(card))
            // exit if draw fails: full hand
            {
                Game.getInstance().log(name + " drew " + (i - 1) + if (i - 1 == 1) " card" else " cards", Game.Log.TAG_DRAW.or(tag))
                return
            }
            Game.getInstance().confirmDraw()
            card.resetModifiers()
            if (hasSpecialAbility(HagglerAbility::class.java)) {
                card.addCostShift(-1)
            }
        }
        Game.getInstance().log(name + " drew " + n + if (n == 1) " card" else " cards", Game.Log.TAG_DRAW.or(tag))
    }

    /**
     * returns a CardGroup containing the playable cards in hand
     * @return the cards in hand that can be played
     */
    val playableCards: CardGroup
        get() {
            val cards = CardGroup()
            for (card in hand) {
                if (card.isPlayable(this, Game.getInstance().points, Game.getInstance().phase, Game.getInstance().lastClub)) {
                    cards.add(card)
                }
            }
            return cards
        }

    val playableAverage: Int
        get() {
            val cards = playableCards
            return cards.valueSum() / cards.size
        }

    /**
     * discards the given card from the hand
     * @param card the card to discarded
     * @return whether the card was discarded
     */
    fun discard(card: Card): Boolean {
        val removed = hand.remove(card)
        if (removed) {
            Game.getInstance().discard(card)
        }
        return removed
    }

    /**
     * adds an ability for this player
     * @param ability the ability to add
     */
    fun addAbility(ability: Ability) {
        if (ability.isBase) {
            baseAbilities.add(ability)
        } else {
            specialAbilities.add(ability)
            if (ability is LargeCapacityAbility) {
                data[KEY_FULL_HAND] = LargeCapacityAbility.FULL
                hand = CardGroup(LargeCapacityAbility.FULL)
            }
        }
    }

    /**
     *
     * @return a list of this player's special abilities
     */
    fun getSpecialAbilities(): List<Ability> {
        return specialAbilities
    }

    /**
     *
     * @return a list of this player's base abilities
     */
    fun getBaseAbilities(): List<Ability> {
        return baseAbilities
    }

    /**
     * attaches an AI to this player
     * @param ai the AI to attack
     */
    fun attachAI(ai: AI) {
        ai.setPlayerTo(this)
        this.ai = ai
    }

    /**
     * detaches the AI from this player
     */
    fun detachAI() {
        ai = null
    }

    /**
     * delegates to the attached AI
     */
    fun takeTurn() {
        ai!!.takeTurn()
    }

    /**
     * eliminates the player from the game
     */
    fun eliminate() {
        val discards = CardGroup()

        // discard all cards on board and in hand
        val boardSuits = EnumSet.of(Card.Suit.HEARTS, Card.Suit.DIAMONDS, Card.Suit.SPADES)
        for (suit in boardSuits) {
            discards.addAll(board.getRow(suit))
            board.getRow(suit).clear()
        }

        discards.addAll(hand)
        hand.clear()

        Game.getInstance().discard(discards)

        Game.getInstance().log("$name was eliminated", Game.Log.TAG_ATTACK)
        if (!discards.isEmpty) {
            Game.getInstance().log(discards.toString() + " " + (if (discards.size > 1) "were" else "was") + " " + "discarded", Game.Log.TAG_DISCARD)
        }
    }

    /**
     * returns the index of the ability of the given class in the ability list
     * if the player has it
     * @param clazz the class of the ability to search for
     * @return the index of the ability in the list, -1 if not found
     */
    fun <T : Ability> indexOfSpecialAbility(clazz: Class<T>): Int {
        for (i in getSpecialAbilities().indices) {
            if (clazz.isInstance(specialAbilities[i])) {
                return i
            }
        }
        return -1
    }

    /**
     *
     * @param clazz the ability class to look for
     * @return whether this player has an ability of the given class
     */
    fun <T : Ability> hasSpecialAbility(clazz: Class<T>): Boolean {
        return indexOfSpecialAbility(clazz) > -1
    }

    /**
     * resets this player for a new game
     */
    fun reset() {
        detachAI()
        this.turn = startingTurn
        board.clear()

        // todo check this for play again issues
        hand = CardGroup(FULL_HAND)
        baseAbilities.clear()
        specialAbilities.clear()
        presence = 0
    }

    override fun compareTo(other: Player): Int {
        return startingTurn - other.startingTurn
    }

    enum class Mode {
        HUMAN, CPU
    }

    companion object {
        private const val serialVersionUID = 1L

        // keys for the data field
        const val KEY_FULL_HAND = 0
        const val KEY_BOARD_FULL_ROW = 1

        /**
         * the maximum number of cards a player can hold
         */
        const val FULL_HAND = 8
    }
}