/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game

import android.util.SparseIntArray
import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.game.abilities.*
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * This class serves as the engine behind all "smart" actions
 * Its main functions are calculating the most efficient attack and
 * controlling the computer auto-play
 */
open class AI(protected var gameScreen: GameScreen) {

    /**
     * the player this AI is playing for
     */
    protected var player: Player? = null

    /**
     * an array to hold cards sorted by suit
     */
    protected var sortedCards = arrayOf(CardGroup(), CardGroup(), CardGroup(), CardGroup())

    /**
     * the total value of the priority that should be played
     */
    protected var priorityValue: Int = 0

    /**
     * @return the Player representing the opponent
     */
    protected val opponent: Player
        get() = Game.getInstance().getPlayer(if (Game.getInstance().turn == 0) 1 else 0)

    /**
     * This method must be called before this AI can take a turn
     *
     * @param player the player this AI plays for
     */
    fun setPlayerTo(player: Player) {
        this.player = player
    }

    /**
     * takes the turn for the given player
     *
     * @throws IllegalStateException if player is not set
     */
    open fun takeTurn() {
        try {
            if (player == null) {
                throw IllegalStateException("player must be set")
            }

            checkAbilities(Checkpoint.PRE_PLAY)

            // sort playable cards by suit
            clearGroups()
            sortCards(player!!.playableCards)

            // determine playing priority
            val priorities = determinePriority()

            // determine cards to play
            var playSelection = selectCards(priorities)

            // play based on priority until nothing can be played or priority fulfilled
            while (!playSelection.isEmpty && priorityValue > 0) {
                // play cards
                playCards(playSelection, priorities)

                // re-sort cards
                clearGroups()
                sortCards(player!!.playableCards)

                // determine cards to play
                playSelection.clear()

                // only play Utility cards
                if (player!!.playableCards.size > group(Card.Suit.CLUBS).size) {
                    playSelection = selectCards(priorities)
                }
            }

            // play all else possible
            // plays highest valued cards until only clubs can be played
            playElse()

            checkAbilities(Checkpoint.END_POINT)

            // abilities may have opened up new plays
            playElse()

            // plays clubs
            // as long as there is a club to play
            // and the club value is greater than five
            // or the hand is not full
            // or points are greater than or equal to 12 (no other card has been played this turn)
            var card = group(Card.Suit.CLUBS).highestCard()
            while (card != null && (card.value >= 6 || player!!.hand.size != player!!.data[Player.KEY_FULL_HAND] || Game.getInstance().points >= Game.STARTING_POINTS)) {
                // plays club
                playCard(card)

                checkAbilities(Checkpoint.POST_CLUB)

                clearGroups()
                sortCards(player!!.playableCards)

                // if there is still a priority, select a card to play
                if (priorityValue > 0) {
                    // selects the highest valued card
                    playSelection = selectCards(priorities, compareCosts = false)
                    if (!playSelection.isEmpty) {
                        // if the selected card's value is greater than the highest club in hand,
                        // then play it
                        // else play the club first
                        if (playSelection[0].value > group(Card.Suit.CLUBS).highestValue()) {
                            playCards(playSelection, priorities)
                            clearGroups()
                            sortCards(player!!.playableCards)
                        }
                    }
                }

                // get the next highest club
                card = group(Card.Suit.CLUBS).highestCard()
            }

            playElse()

            attack()

            checkAbilities(Checkpoint.END_TURN)
            GameScreen.saveGame(gameScreen)
            gameScreen.newTurn()
        } catch (e: EndException) {
            if (e is EndTurnException) {
                GameScreen.saveGame(gameScreen)
                gameScreen.newTurn()
            }
        }

    }

    /**
     * clears all the CardGroups used to sort suits in hand
     */
    protected fun clearGroups() {
        for (cardGroup in sortedCards) {
            cardGroup.clear()
        }
    }

    /**
     * Sorts a CardGroup into four sub groups based on suit
     *
     * @param cards the cards to sort
     */
    protected fun sortCards(cards: CardGroup) {
        for (card in cards) {
            group(card.suit).add(card)
        }
    }

    /**
     * @param suit the suit of the sorted cards to get
     * @return the CardGroup of the given suit in sortedCards
     */
    protected fun group(suit: Card.Suit): CardGroup {
        return sortedCards[suit.ordinal]
    }

    /**
     * Determines whether the AI should prioritize the playing of hearts, hearts
     * and diamonds, spades, or none.
     * Also sets a priority for total value of cards that should be played
     *
     * @return a list of Suits that should be played
     */
    protected open fun determinePriority(): EnumSet<Card.Suit> {
        // priority is always hearts and diamonds for turns 1-2
        // priority value is the number of points for the turn +1
        // priority is defense if multiple opponents exist
        if (Game.getInstance().round < Game.ATTACK_ROUND || Game.getInstance().opponentsOf(player!!.turn).size > 1) {
            priorityValue = 13
            return EnumSet.of(Card.Suit.HEARTS, Card.Suit.DIAMONDS)
        } else {
            val opponent = opponent

            // calculates sums for attack and defense
            val playerStats = calculateStats(player!!)
            val opponentStats = calculateStats(opponent)

            val spadeSumHand = playerStats.get(KEY_ATTACK_POWER) - playerStats.get(KEY_WEAPONS)

            // attacking is allowed turns 3+

            // assertion: this is always true
            //if (Game.getInstance().round >= Game.ATTACK_ROUND) {

            // shield bypassing is allowed turns 4+
            if (Game.getInstance().round >= Game.BYPASS_ROUND) {
                // priority is spades if kill is possible with shield bypass
                // priority value is as many spades as possible
                if (opponentStats.get(KEY_HEALTH) <= playerStats.get(KEY_ATTACK_POWER) / 2) {
                    priorityValue = spadeSumHand
                    return EnumSet.of(Card.Suit.SPADES)
                }
            }

            // priority is hearts if the opponent can shield bypass and
            // has the potential to kill the next turn
            // priority value is
            //todo: fix this logic for more players maybe
            if (player!!.turn == Game.getInstance().playerCount - 1 || Game
                            .getInstance().round >= Game.BYPASS_ROUND) {
                if (playerStats.get(KEY_HEALTH) <= opponentStats.get(KEY_ATTACK_POWER) / 2) {
                    if (player!!.board.rowSize(Card.Suit.DIAMONDS) > 0) {
                        priorityValue = opponentStats.get(KEY_ATTACK_POWER) / 2
                        return EnumSet.of(Card.Suit.HEARTS)
                    }
                }
            }
            //}

            // priority is spades if kill is possible
            // priority values is as many spades as possible
            if (opponentStats.get(KEY_DEFENSE) <= playerStats.get(KEY_ATTACK_POWER)) {
                priorityValue = spadeSumHand
                return EnumSet.of(Card.Suit.SPADES)
            }

            // priority is hearts and diamonds if opponent has the potential to
            // kill the next turn
            // priority value is the difference btwn human attack power
            // and defense plus one to survive
            if (playerStats.get(KEY_DEFENSE) <= opponentStats.get(KEY_ATTACK_POWER)) {
                priorityValue = opponentStats.get(KEY_ATTACK_POWER) - playerStats.get(KEY_DEFENSE) + 1
                return EnumSet.of(Card.Suit.HEARTS, Card.Suit.DIAMONDS)
            }

        }

        // else no priority
        priorityValue = 0
        return EnumSet.noneOf(Card.Suit::class.java)
    }

    /**
     * selects cards to play based on priority
     *
     * @return CardGroup containing cards to play
     */
    protected open fun selectCards(priorities: EnumSet<Card.Suit>, compareCosts: Boolean = true): CardGroup {
        // determine playable combos
        var combos: MutableList<CardGroup> = determineCombos(priorities)
        val tempCombos = sortCombos(combos)

        // combos is empty if no combos fulfilled priority
        // tempCombos holds combos that did not meet priority
        if (combos.isEmpty() && tempCombos.isNotEmpty()) {
            combos = tempCombos
        }

        // return highest valued combo
        return if (combos.isNotEmpty()) {
            Collections.max(combos, if (compareCosts) CardGroup.CostSizeComparator else CardGroup.ValueSizeComparator)
        } else CardGroup()
    }

    /**
     * removes unplayable combos from the list and adds combos that do not fulfill the priority
     * to a list to return
     *
     * @param combos the list of combos to sort
     * @return a list of combos that are playable and to not satisfy the priority value
     */
    protected fun sortCombos(combos: MutableList<CardGroup>): MutableList<CardGroup> {
        val tempCombos = ArrayList<CardGroup>()
        for (i in combos.indices.reversed()) {

            // if combo is playable
            // assertion: if club is active, all combos will consist of one card
            if (combos[i].costSum() <= Game.getInstance().points || Game.getInstance().phase == Game.PHASE_CLUB) {
                if (combos[i].valueSum() < priorityValue) {
                    tempCombos.add(combos.removeAt(i)) // combo does not fulfill priority
                }
            } else {
                combos.removeAt(i)
            }
        }

        return tempCombos

    }

    /**
     * determines combos consisting of the suits in the list
     *
     * @return an ArrayList of combos (CardGroups)
     */
    protected open fun determineCombos(suits: EnumSet<Card.Suit>): ArrayList<CardGroup> {
        val combos = ArrayList<CardGroup>()
        val board = player!!.board

        for (suit in suits) {
            // size is 1 if it is the club phase and the board row is not full, otherwise it is
            // the number of open spots on the board
            val size = if (Game.getInstance().phase == Game.PHASE_CLUB)
                1 - board.rowSize(suit) / Board.FULL_ROW
            else
                Board.FULL_ROW - board.rowSize(suit)
            combos.addAll(combosOf(group(suit).toArrayList(), size))
        }

        // if priority is hearts and diamonds, get combos of both
        if ((suits.contains(Card.Suit.HEARTS) && suits.contains(Card.Suit.DIAMONDS) && (Game
                        .getInstance().phase != Game.PHASE_CLUB))) {
            combos.addAll(combosOf(group(Card.Suit.HEARTS).toArrayList(), group(Card.Suit
                    .DIAMONDS).toArrayList(), 1, 1))
        }
        return combos
    }

    /**
     * plays the cards from the CardGroup;
     * decrements priority value if a card of a priority suit is played
     *
     * @param cards the cards to play
     */
    protected fun playCards(cards: CardGroup, priorities: EnumSet<Card.Suit>) {
        for (card in cards) {
            playCard(card)

            if (priorities.contains(card.suit))
            // decrement priority value
            {
                priorityValue -= card.value
            }
        }
    }

    /**
     * delegates to player.playCard(card)
     * may also have statements to animate the screen
     *
     * @param card the card to play
     */
    protected fun playCard(card: Card?) {
        player!!.playCard(card!!)
    }

    /**
     * plays all other cards possible;
     * plays highest valued cards until only clubs can be played
     */
    protected open fun playElse() {
        // sort playable cards
        clearGroups()
        sortCards(player!!.playableCards)

        // play as long as non-clubs can be played
        while (player!!.playableCards.size > group(Card.Suit.CLUBS).size) {
            val playables = player!!.playableCards

            // remove clubs
            for (i in playables.size - 1 downTo 0) {
                if (playables[i].suit === Card.Suit.CLUBS) {
                    playables.remove(i)
                }
            }

            // play highest valued card and re-sort
            if (!playables.isEmpty) {
                playCard(playables.highestCard())
            }

            clearGroups()
            sortCards(player!!.playableCards)
        }
    }

    /**
     * runs the attack process of a player
     *
     * @throws EndGameException if AI wins the game
     */
    @Throws(EndGameException::class)
    protected open fun attack() {
        if ((Game.getInstance().round >= Game.ATTACK_ROUND && !player!!.board.getRow(Card.Suit.SPADES).isEmpty))
        // if can attack and have spades
        {
            // sort cards in hand
            sortCards(player!!.hand)

            // select attack target
            val target = selectAttackTarget()


            if (target != null) {
                val opponent = target.target

                // select best attack
                val attack = target.attack
                //selectAttack(opponent);

                val opponentStats = calculateStats(opponent)

                if (attack != null)
                // if there is an acceptable combo
                {
                    if ((attack.isKill || // if kill

                                    !((opponentStats.get(KEY_HEALTH) <= 10 && (opponentStats.get(KEY_SHIELDS) > opponentStats.get(KEY_HEALTH)))) || // do not attack if the opponent's

                                    // health is weak and the shields are greater: build for bypass
                                    player!!.board.rowSize(Card.Suit.SPADES) == Board.FULL_ROW))
                    // attack
                    // if spades are full
                    {
                        player!!.attack(opponent)

                        var board = player!!.board

                        for (card in attack.spadesUsed) {
                            // update data: discard the selected spades on the board
                            board.discard(card)
                            //Game.discard(card);
                        }

                        // get the board that is on screen
                        board = opponent.board

                        // discard diamonds destroyed
                        for (card in attack.diamondsDestroyed) {
                            board.discard(card)
                        }

                        // discard hearts destroyed
                        for (card in attack.heartsDestroyed) {
                            board.discard(card)
                        }

                        Game.getInstance().log(player!!.name + " " + (if (attack.isBypass)
                            gameScreen.getString(R.string.bypassed)
                        else
                            gameScreen.getString(R.string.attacked)) + " " + opponent.name + " " + gameScreen.getString(R.string.with) + " " +
                                attack.spadesUsed, Game.Log.TAG_ATTACK)

                        if (attack.diamondsDestroyed.size + attack.heartsDestroyed.size > 0) {
                            Game.getInstance().log(player!!.name + " " + gameScreen.getString(R.string.destroyed) + " " + attack
                                    .diamondsDestroyed + attack.heartsDestroyed, Game.Log.TAG_ATTACK)
                        }

                        if (attack.isKill) {
                            Game.getInstance().eliminate(opponent.turn, attack.isBypass)
                            gameScreen.pager.adapter!!.notifyDataSetChanged()
                            if (Game.getInstance().isGame) {
                                gameScreen.endGame()
                                throw EndGameException()
                            }

                        } else {
                            // attempt to play more (likely spades)
                            playElse()

                            // attack again maybe
                            attack()
                        }
                    }
                }
            }

        }
    }

    /**
     * @return the best opponent to attack
     */
    protected open fun selectAttackTarget(): AttackTarget? {
        val targets = ArrayList<AttackTarget>()
        val opponents = Game.getInstance().opponentsOf(player!!.turn)
        for (opponent in opponents) {
            if (opponent.isAttackableBy(player!!)) {
                val target = AttackTarget(opponent, selectAttack(opponent))
                if (target.attack != null) {
                    if (target.attack.isKill) {
                        return target
                    }
                    targets.add(target)
                }
            }
        }
        return if (targets.isNotEmpty()) {
            Collections.max(targets)
        } else null
    }

    /**
     * generates multiple stats for a player
     *
     * @param p the player to get stats for
     * @return a SparseIntArray containing all stats with keys
     */
    protected fun calculateStats(p: Player): SparseIntArray {
        val sums = SparseIntArray()
        sums.put(KEY_HEALTH, p.board.getRow(Card.Suit.HEARTS).valueSum())
        sums.put(KEY_SHIELDS, p.board.getRow(Card.Suit.DIAMONDS).valueSum())
        sums.put(KEY_WEAPONS, p.board.getRow(Card.Suit.SPADES).valueSum())
        sums.put(KEY_DEFENSE, sums.get(KEY_HEALTH) + sums.get(KEY_SHIELDS))
        sums.put(KEY_ATTACK_POWER, sums.get(KEY_WEAPONS) + if (player!!.turn == p.turn)
            max(group(Card.Suit.SPADES).valueSum(), Game.getInstance().points)
        else
            Game.STARTING_POINTS)
        return sums
    }


    /**
     * @param opponent the player to attack
     * @return the best attack combo to use on the opponent
     */
    protected open fun selectAttack(opponent: Player): Attack? {
        // generate all combos of spades on board
        val attackCombos = allCombosOf(player!!.board.getRow(Card.Suit.SPADES)
                .toArrayList())

        // create list to track attack stats
        val attacks = ArrayList<Attack>()

        for (combo in attackCombos) {
            // get the stats of the attack of each combo
            val attack = calculateAttack(combo, player!!, opponent, false)

            // if kills...
            if (attack.isKill) {
                return attack
            }
            if (opponent.isBypassableBy(player!!)) {
                val bypassAttack = calculateAttack(combo, player!!, opponent, true)
                if (bypassAttack.isKill) {
                    return bypassAttack
                }
            }

            // add stats to list
            if (attack.efficiency >= MINIMUM_EFFICIENCY) {
                attacks.add(attack)
            }
        }

        // find best Attack
        if (attacks.isNotEmpty()) {
            val bestAttack = Collections.max(attacks)
            // check that it has the min efficiency
            if (bestAttack.efficiency >= MINIMUM_EFFICIENCY) {
                return bestAttack
            }
        }
        return null
    }

    /**
     * checks all player's abilities to activate/resolve them in order
     * of greatest priority to least
     *
     * @throws EndTurnException if an ability effect ends the turn
     */
    protected open fun checkAbilities(checkpoint: Checkpoint) {

        var abilityResolved = false
        // make a list of abilities in order from highest priority to lowest
        val prioritizedAbilities = ArrayList<Ability>()
        prioritizedAbilities.addAll(player!!.abilities)
        Collections.sort(prioritizedAbilities, Collections.reverseOrder(Resolvable.AbilityPriorityComparator()))

        for (ability in prioritizedAbilities) {
            if (ability is ActiveAbility) {
                if (ability.isActivateable && ability.shouldActivate(checkpoint)) {
                    ability.activate()
                    if (ability is SelectionNecessary) {
                        if (!ability.isSelectionMade) {
                            ability.makeSelection()
                        }
                    }
                    if (ability.resolve()) {
                        abilityResolved = true
                    }
                }
            } else if (ability is Resolvable) {
                if (ability.isResolvable) {
                    if (ability.resolve()) {
                        abilityResolved = true
                    }
                }
            }
        }
        if (abilityResolved) {
            checkAbilities(checkpoint)
        }
    }

    /**
     * This class represents an Attack Target for the AI
     * It holds a Player and an Attack to be used on the player
     */
    protected inner class AttackTarget(val target: Player, val attack: Attack?) : Comparable<AttackTarget> {

        /**
         * Calculates the distance between the target player and the turn player
         * @return the number of turns after the turn player's turn that the target will play
         */
        private fun playerDistance(): Int {
            val distance = target.turn - player!!.turn
            return if (distance < 0) {
                target.turn + Game.getInstance().playerCount - player!!.turn
            } else distance
        }

        override fun compareTo(other: AttackTarget): Int {
            return if ((attack == null) == (other.attack == null)) {
                if (attack == null) {
                    0
                } else {
                    val i = attack.compareTo(other.attack!!)
                    if (i != 0) i else other.playerDistance() - playerDistance()
                }
            } else if (attack == null) {
                -1
            } else 1
        }
    }

    /**
     * This enum represents different points of execution in AI logic
     * It is passed to methods whose behavior changes based on the Checkpoint
     */
    enum class Checkpoint {
        PRE_PLAY, END_POINT, POST_CLUB, END_TURN
    }

    companion object {

        /**
         * keys for stats
         */
        protected const val KEY_HEALTH = 0
        protected const val KEY_SHIELDS = 1
        protected const val KEY_WEAPONS = 2
        protected const val KEY_DEFENSE = 3
        protected const val KEY_ATTACK_POWER = 4

        /**
         * the minimum efficiency attack that the AI will make
         */
        protected const val MINIMUM_EFFICIENCY = 85

        /**
         * Generates all the possible combinations of cards of a given size from a list of cards
         *
         * @param list the list to generate combos from
         * @param size the size of each combo
         * @return a list of CardGroups representing combos (returns empty list over null)
         * @throws IllegalArgumentException if size < 1
         */
        @JvmStatic
        fun combosOfSize(list: List<Card>, size: Int): List<CardGroup> {
            if (size < 1) {
                throw IllegalArgumentException("size must be greater than 0")
            }

            // create list that will be returned
            val combos = ArrayList<CardGroup>()

            // base case #1
            when (size) {
                1 -> // each card is its own combo
                    for (card in list) {
                        val cg = CardGroup()
                        cg.add(card)
                        combos.add(cg)
                    }
                list.size -> { //base case #2
                    // the list is the only combo
                    val newCombo = CardGroup()
                    newCombo.addAll(list)
                    combos.add(newCombo)
                }
                else -> {
                    // determine how many times the following loop will run
                    val end = list.size - size

                    for (i in 0..end) {
                        // make a recursive call of the combos of the sublist of the next card to the
                        // last with a size 1 smaller
                        val tempCombos = combosOfSize(list.subList(i + 1, list.size), (size - 1))

                        // add the card at i to each tempCombo and add it to the return
                        for (tempCombo in tempCombos) {
                            val cg = CardGroup()
                            cg.add(list[i])
                            cg.addAll(tempCombo)
                            combos.add(cg)
                        }
                    }
                }
            }
            return combos
        }

        /**
         * determines the hearts and diamonds to destroy to be most efficient given spades
         *
         *
         * @param defender
         * @param attacker
         * @param bypass   whether or not this is a bypass  @return an Attack object representing the attack
         */
        @JvmStatic
        fun calculateAttack(spades: CardGroup, attacker: Player, defender: Player, bypass: Boolean): Attack {
            val hearts = defender.board.getRow(Card.Suit.HEARTS)
            val diamonds = defender.board.getRow(Card.Suit.DIAMONDS)

            // initialize variables
            val weapons = spades.valueSum()
            val shields = diamonds.valueSum()

            var diamondsDestroyed = CardGroup()
            var heartsDestroyed = CardGroup()

            var power = 1.0

            if (weapons > 0) {
                if (!bypass) {
                    if (weapons < shields)
                    // if not all diamonds are destroyed, calculate diamonds
                    {
                        diamondsDestroyed = determineGreatestCombo(diamonds, weapons)
                    } else {
                        // all diamonds are destroyed
                        diamondsDestroyed.addAll(diamonds)
                        if (weapons > shields)
                        // if attack goes through, calculate hearts
                        {
                            heartsDestroyed = determineGreatestCombo(hearts, weapons - shields)
                        }
                    }
                } else
                // bypass, calculate hearts
                {
                    power = if (attacker.hasSpecialAbility(DirectHitterAbility::class.java) && defender.board.rowSize(Card.Suit.DIAMONDS) >= 2) .75 else .5
                    heartsDestroyed = determineGreatestCombo(hearts, (weapons * power).toInt())
                }
            }

            return Attack(hearts, diamonds, spades, diamondsDestroyed, heartsDestroyed, bypass, power)
        }

        @JvmStatic
        fun buildAttack(spades: CardGroup, attacker: Player, defender: Player, bypass: Boolean, diamondsDestroyed: CardGroup, heartsDestroyed: CardGroup): Attack {
            var power = 1.0
            if (bypass) {
                power = if (attacker.hasSpecialAbility(DirectHitterAbility::class.java) && defender.board.rowSize(Card.Suit.DIAMONDS) >= 2) .75 else .5
            }
            val hearts = defender.board.getRow(Card.Suit.HEARTS)
            val diamonds = defender.board.getRow(Card.Suit.DIAMONDS)
            return Attack(hearts, diamonds, spades, diamondsDestroyed, heartsDestroyed, bypass, power)
        }

        /**
         * Determines the card combo of any size in the given CardGroup with the greatest value that
         * is not larger than the max
         *
         * @param cg  the CardGroup from which to determine the greatest combo
         * @param max the maximum value of the combo
         * @return the combo with the greatest value no greater than the max
         */
        @JvmStatic
        fun determineGreatestCombo(cg: CardGroup, max: Int): CardGroup {
            // determine all combos of list
            val combos = ArrayList<CardGroup>()
            combos.addAll(allCombosOf(cg.toArrayList()))

            // remove combos whose total values are greater than the max
            for (i in combos.indices.reversed()) {
                if (combos[i].valueSum() > max) {
                    combos.removeAt(i)
                }
            }

            //return greatest combo
            return if (combos.isNotEmpty()) {
                Collections.max(combos, CardGroup.ValueSizeComparator)
            } else CardGroup()
        }

        /**
         * @param list the list to get combos of
         * @return all combos of every size possible of a list of cards
         */
        fun allCombosOf(list: List<Card>): List<CardGroup> {
            return combosOf(list, list.size)
        }

        /**
         * @param list    the list to get combos of
         * @param maxSize the max combo size
         * @return all combos of a list of size 1 to the max size (or size of list)
         */
        @JvmStatic
        fun combosOf(list: List<Card>, maxSize: Int): List<CardGroup> {
            val max = min(maxSize, list.size)

            val combos = ArrayList<CardGroup>()

            for (i in 1..max) {
                combos.addAll(combosOfSize(list, i))
            }

            return combos
        }

        /**
         * @param list1 the first list
         * @param list2 the second list
         * @param size1 the number of cards from the first list that will be in each combo
         * @param size2 the number of cards from the second list that will be in each combo
         * @return combos of cards populated from 2 lists
         */
        @JvmStatic
        fun combosOf(list1: List<Card>, list2: List<Card>, size1: Int, size2: Int): List<CardGroup> {
            val combos = ArrayList<CardGroup>()

            // get combos of each list separately
            val combos1 = combosOfSize(list1, size1)
            val combos2 = combosOfSize(list2, size2)

            // combine those combos
            for (combo1 in combos1) {
                for (combo2 in combos2) {
                    val combo = CardGroup()
                    combo.addAll(combo1)
                    combo.addAll(combo2)
                    combos.add(combo)
                }
            }

            return combos
        }
    }
}
