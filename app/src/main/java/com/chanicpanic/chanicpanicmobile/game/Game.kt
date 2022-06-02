/*
 * Copyright (c) chanicpanic 2022
 */

package com.chanicpanic.chanicpanicmobile.game

import android.widget.Toast
import androidx.annotation.IntDef
import androidx.fragment.app.FragmentTransaction
import com.chanicpanic.chanicpanicmobile.R
import com.chanicpanic.chanicpanicmobile.gamescreen.GameScreen
import com.chanicpanic.chanicpanicmobile.gamescreen.HandFragment
import com.chanicpanic.chanicpanicmobile.game.abilities.*
import java.io.Serializable
import java.util.*
import kotlin.math.ceil

/**
 * This singleton class contains all essential data, objects, and methods to control a game of Chanic Panic
 */
class Game private constructor() : Serializable {

    /**
     * The number of players
     */
    var startingPlayerCount: Int = 0

    /**
     * The number of teams
     */
    var teams: Int = 0

    /**
     * The number of special abilities owned by each player
     */
    var abilities: Int = 0

    /**
     * The current round
     * increments by 1 at the start each of the first player's turns
     */
    var round: Int = 0
        private set

    /**
     * The current turn
     * Indicates which player is active
     * Ranges from 0 to playerCount - 1
     */
    var turn: Int = 0
        private set

    /**
     * The number of points left
     * Becomes 0 at the start of the Club Phase
     */
    @Transient
    var points: Int = 0

    /**
     * The current phase of the turn
     * See PHASE constants
     */
    @Transient
    private var _phase: Int = 0

    var phase
        get() = _phase
        set(value) {
            if (value == PHASE_CLUB && _phase != PHASE_CLUB) {
                log("Club Phase", Log.TAG_PHASE)
            }
            _phase = value
        }

    /**
     * The value of the last Club played
     * Becomes 0 when a non-Club is played after a CLub
     */
    @Transient
    var lastClub: Int = 0

    /**
     * booleans representing whether the base abilities are active
     */
    var isTraderActive: Boolean = false
    var isAllyActive: Boolean = false
    var isPanicActive: Boolean = false

    /**
     * whether or not presence is active
     */
    var isPresenceActive: Boolean = false

    /**
     * Represents the deck
     */
    var deck = CardGroup()
        private set

    /**
     * Represents the discard pile
     */
    val discardPile = CardGroup()

    /**
     * A list of all the active players
     */
    private val players = ArrayList<Player>()

    /**
     * A list of all the eliminated players
     * This list and the players list will never hold the same player
     */
     val eliminatedPlayers = ArrayList<Player>()

    /**
     * whether or not this instance is deserialized
     */
    /**
     * @return whether or not this instance has been deserialized
     */
    @Transient
    var isDeserialized = false
        private set

    /**
     * This list holds lists of strings.
     * Those sublists each hold the log of 1 turn of play
     */
    private val gameLog: MutableList<MutableList<Log.LogInfo>> = ArrayList()

    val details: MutableList<String> = ArrayList()

    /**
     * This array holds the indices of each color used by each team
     * Ex. The value at teamColorIndices[0] holds the index of the color in GameScreen.TEAM_COLORS
     */
    var teamColorIndices: IntArray? = null

    /**
     * The maximum number of turns that will be kept in the game log
     */
    @Transient
    private var loglimit = LOG_NO_LIMIT

    var playerCount: Int
        get() = players.size
        set(playerCount) {
            this.startingPlayerCount = playerCount
        }

    val board: Board
        get() = getBoard(turn)

    /**
     *
     * @return the full game log
     */
    val fullLog: List<Log.LogInfo>
        get() {
            val full = ArrayList<Log.LogInfo>()
            for (list in gameLog) {
                full.addAll(list)
                full.add(Log.LogInfo("", Log.TAG_DEFAULT, -1))
            }
            return full
        }

    /**
     *
     * @return a reference to the current turn's player
     */
    val player: Player
        get() = getPlayer(turn)

    /**
     *
     * @return whether the game is over
     */
    val isGame: Boolean
        get() = players.size == teammatesOf(0).size + 1 || _isGame

    private var _isGame = false

    private var _result = Result()

    val result: Result
        get() {
            @Suppress("SENSELESS_COMPARISON")
            // _result may be null due to deserialization
            if (_result == null) {
                _result = Result()
            }
            return _result
        }

    // tracks the number of emojis given this turn
    var emojis = 0
        private set

    fun changePoints(p: Int) {
        points += p
    }

    fun getBoard(board: Int): Board {
        return getPlayer(board).board
    }

    /**
     *
     * @param team the team number
     * @return the color index of the team
     */
    fun getTeamColorIndex(team: Int): Int {
        return teamColorIndices!![team]
    }

    /**
     * puts an index for a given team
     * @param team the team number
     * @param index the index value to put
     */
    fun setTeamColorIndex(team: Int, index: Int) {
        teamColorIndices!![team] = index
    }

    /**
     * Adds an event to the game log
     * @param event the event to add
     */
    @JvmOverloads
    fun log(event: String, @Log.LogTag tag: Int = Log.TAG_DEFAULT) {

        gameLog.last().add(Log.getLogInfo(event, tag))

        if (tag == Log.TAG_EMOJI) {
            emojis++
        }
    }

    /**
     * sets the max number of turns to keep in the log
     * @param logLimit the max number of turns to keep in the log
     */
    fun setLogLimit(logLimit: Int) {
        this.loglimit = logLimit
    }

    /**
     * starts a log for a new turn
     * also trims the log to the log limit
     */
    fun newLog() {
        gameLog.add(ArrayList())
        if (loglimit != LOG_NO_LIMIT) {
            val limit = if (loglimit == LOG_LAST_ROUND) playerCount else loglimit
            while (gameLog.size > limit + 1) {
                gameLog.removeAt(0)
            }
        }
    }

    /**
     * sets fields for the start of a new turn
     * covers the Draw and Standby Phases
     */
    fun nextTurn() {
        turn = (turn + 1) % playerCount
        round += if (turn == 0) 1 else 0
        emojis = 0
    }

    /**
     * initializes fields for a new turn
     * returns true if an EndGameException was not thrown
     */
    fun startTurn(gameScreen: GameScreen) : Boolean{

        fun checkPresence() {
            if (phase == PHASE_STANDBY && isPresenceActive && round > 1) {
                player.calculatePresence()
                var presence = player.presence
                if (startingPlayerCount != teams) {
                    val teammates = teammatesOf(player.turn)
                    for (player in teammates) {
                        presence += player.presence
                    }
                    presence /= teammates.size + 1
                }
                if (presence >= 200) {
                    _isGame = true
                    result.isGame = true
                    result.victoryCondition = Result.VICTORY_PRESENCE
                    result.winner = player
                    throw EndGameException()
                }
                if (!gameScreen.isAutoplay) {
                    Toast.makeText(gameScreen, "+${2 * player.board.collapse.size} Presence", Toast.LENGTH_SHORT).show()
                }
                if (!gameScreen.isAutoplay) {
                    gameScreen.updateSpinner()
                }
            }
        }


        try {
            if (turn == 0) {
                log(gameScreen.getString(R.string.round) + " " + round, Log.TAG_ROUND)
                log("")
            }
            log(gameScreen.getString(R.string.turn_log) + " " + (turn + 1))

            _phase = PHASE_DRAW
            points = STARTING_POINTS
            lastClub = 0

            if (!gameScreen.isAutoplay) {
                gameScreen.updateInfo()
            }

            player.startTurn()

            val options = player.indexOfSpecialAbility(OptionsAbility::class.java)
            if (options > -1 && round > 1) {
                val ability = player.getSpecialAbilities()[options] as OptionsAbility
                if (!gameScreen.isAutoplay) {
                    GameScreen.DialogBuilder(gameScreen)
                            .setTitle("Would you like to activate Options?")
                            .setMessage("Your Draw Phase will be skipped and you will gain 6 points during your Standby Phase.\n\n"
                                    + "Your hand is: " + player.hand)
                            .setCancelable(false)
                            .setFullScreen(false)
                            .setPositiveButton("Yes") {
                                try {
                                    ability.resolve()

                                    checkPresence()

                                    // check standby phase abilities
                                    checkAbilities()

                                    _phase = PHASE_POINT
                                    log("Point Phase", Log.TAG_PHASE)

                                    gameScreen.updateInfo()
                                } catch (e: EndGameException) {
                                    gameScreen.endGame()
                                }
                            }
                            .setNegativeButton("No") {
                                try {
                                    log("Draw Phase", Log.TAG_PHASE)
                                    // draw cards
                                    val fulfilled = player.indexOfSpecialAbility(FulfilledAbility::class.java)
                                    if (fulfilled > -1) {
                                        (player.getSpecialAbilities()[fulfilled] as FulfilledAbility).resolve()
                                    } else if (round == 1) {
                                        if (player.hand.isEmpty) {
                                            player.draw(5)
                                            val ft = gameScreen.supportFragmentManager.beginTransaction()
                                            ft.replace(R.id.frameHand, HandFragment.newInstance(turn),
                                                    GameScreen.HAND_FRAGMENT_TAG)
                                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                                    .commitNow()
                                        } else {
                                            player.draw(5)
                                            // show draw
                                            val handFragment = gameScreen.supportFragmentManager.findFragmentByTag(
                                                GameScreen.HAND_FRAGMENT_TAG) as HandFragment
                                            handFragment.notifyDraw(1)
                                            handFragment.scrollToEnd()
                                        }

                                    } else {


                                        if (player.hand.isEmpty) {
                                            player.draw(1)
                                            val ft = gameScreen.supportFragmentManager.beginTransaction()
                                            ft.replace(R.id.frameHand, HandFragment.newInstance(turn),
                                                    GameScreen.HAND_FRAGMENT_TAG)
                                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                                    .commitNow()
                                        } else {
                                            player.draw(1)
                                            // show draw
                                            val handFragment = gameScreen.supportFragmentManager.findFragmentByTag(
                                                GameScreen.HAND_FRAGMENT_TAG) as HandFragment
                                            handFragment.notifyDraw(1)
                                            handFragment.scrollToEnd()
                                        }

                                    }

                                    // check draw phase abilities
                                    checkAbilities()

                                    _phase = PHASE_STANDBY
                                    log("Standby Phase", Log.TAG_PHASE)

                                    checkPresence()

                                    // check standby phase abilities
                                    checkAbilities()

                                    _phase = PHASE_POINT
                                    log("Point Phase", Log.TAG_PHASE)

                                    gameScreen.updateInfo()
                                } catch (e: EndGameException) {
                                    gameScreen.endGame()
                                }
                            }
                            .show()
                } else if (ability.shouldActivate(AI.Checkpoint.PRE_PLAY)) {
                    ability.resolve()

                    checkPresence()

                    // check standby phase abilities
                    checkAbilities()

                    _phase = PHASE_POINT
                    log("Point Phase", Log.TAG_PHASE)
                } else {
                    log("Draw Phase", Log.TAG_PHASE)
                    // draw cards
                    val fulfilled = player.indexOfSpecialAbility(FulfilledAbility::class.java)
                    when {
                        fulfilled > -1 -> (player.getSpecialAbilities()[fulfilled] as FulfilledAbility).resolve()
                        round == 1 -> player.draw(5)
                        else -> player.draw(1)
                    }

                    // check draw phase abilities
                    checkAbilities()

                    _phase = PHASE_STANDBY
                    log("Standby Phase", Log.TAG_PHASE)

                    // check standby phase abilities
                    checkAbilities()

                    _phase = PHASE_POINT
                    log("Point Phase", Log.TAG_PHASE)
                }

            } else {
                log("Draw Phase", Log.TAG_PHASE)
                // draw cards
                val fulfilled = player.indexOfSpecialAbility(FulfilledAbility::class.java)
                if (fulfilled > -1 && round > 1) {
                    (player.getSpecialAbilities()[fulfilled] as FulfilledAbility).resolve()
                } else if (round == 1) {
                    player.draw(5)
                } else {
                    player.draw(1)
                }

                // check draw phase abilities
                checkAbilities()

                _phase = PHASE_STANDBY
                log("Standby Phase", Log.TAG_PHASE)

                checkPresence()

                // check standby phase abilities
                checkAbilities()

                _phase = PHASE_POINT
                log("Point Phase", Log.TAG_PHASE)

                if (!gameScreen.isAutoplay) {
                    gameScreen.updateInfo()
                }
            }
        } catch (e: EndGameException) {
            gameScreen.endGame()
            return false
        }
        return true
    }

    /**
     * This method checks a player's passive abilities to see if they need to be resolved
     * and resolves then as needed.
     */
    private fun checkAbilities() {
        // check in order from highest priority to lowest
        val abilities = ArrayList<Ability>()
        abilities.addAll(player.getSpecialAbilities())
        Collections.sort(abilities, Collections.reverseOrder(Resolvable.AbilityPriorityComparator()))

        for (ability in abilities) {
            if (ability.isPassive && ability is Resolvable) {
                (ability as Resolvable).resolve()
            }
        }
    }

    /**
     * This returns the top card of the deck and may confirms the draw
     * @param  confirm whether or not this draw is confirmed
     * @return the top card to the deck
     */
    // todo handle case where deck is empty (currently impossible)
    fun draw(confirm: Boolean = true): Card {
        val card = deck[0]
        if (confirm) {
            confirmDraw()
        }
        return card
    }

    /**
     * This confirms the draw.
     * This is where the card is removed from the deck and the deck may be shuffled
     * Do not call this if draw(true) was called
     */
    fun confirmDraw() {
        deck.remove(0)
        if (deck.isEmpty) {
            shuffle()
        }
    }

    /**
     * Shuffle the discard pile into the deck
     */
    fun shuffle() {
        if (!discardPile.isEmpty) {
            deck.addAll(discardPile)
            discardPile.clear()
            deck.shuffle()
        }
    }

    /**
     * Adds a card to the top of the discard pile
     * @param card the card to be discarded
     */
    fun discard(card: Card) {
        card.resetModifiers()
        discardPile.add(0, card)
    }

    /**
     * adds a cardgroup to the top of the discard pile
     * @param cardGroup cards to be discarded
     */
    fun discard(cardGroup: CardGroup) {
        for (card in cardGroup) {
            card.resetModifiers()
        }
        discardPile.addAll(0, cardGroup)
    }

    /**
     * initializes players and teamColorIndices
     */
    fun loadPlayers() {
        players.clear()
        for (i in 0 until startingPlayerCount) {
            players.add(Player(i))
        }
        teamColorIndices = IntArray(teams)
        for (i in 0 until teams) {
            teamColorIndices!![i] = i
        }
    }

    /**
     * initializes variables for a new game
     */
    fun initialize() {
        round = 0
        turn = startingPlayerCount - 1
        points = 12
        lastClub = 0
        _phase = 2

        discardPile.clear()
        deck.clear()
        gameLog.clear()
        gameLog.add(ArrayList())
        details.clear()

        deck = Card.generateDecks(ceil(startingPlayerCount / 2.0).toInt())
        deck.shuffle()
    }

    /**
     *
     * @param player the turn of the player to get
     * @return a reference to the player of the give turn
     */
    fun getPlayer(player: Int): Player {
        return players[player]
    }

    /**
     *
     * @return A list of all the active players in the game
     */
    fun getPlayers(): List<Player> {
        return players
    }

    /**
     * determines whether the turn player and the player with the given turn are on the same team
     * @param effectiveTurn the turn of the player to compare with
     * @return whether or not the turn player and the player with the given turn are on the same team
     */
    fun onSameTeam(effectiveTurn: Int): Boolean {
        return onSameTeam(getPlayer(effectiveTurn).startingTurn, player.startingTurn)
    }

    /**
     *
     * @param startingTurn1 the starting turn of a player
     * @param startingTurn2 the starting turn of another player
     * @return whether the 2 referenced players are on the same team
     */
    private fun onSameTeam(startingTurn1: Int, startingTurn2: Int): Boolean {
        return startingTurn1 % teams == startingTurn2 % teams
    }

    /**
     *
     * @param player the player to compare with
     * @return whether or not the turn player and the given player are on the same team
     */
    fun onSameTeam(player: Player): Boolean {
        return onSameTeam(this.player, player)
    }

    /**
     *
     * @param player1 a player
     * @param player2 another player
     * @return whether the 2 referenced players are on the same team
     */
    fun onSameTeam(player1: Player, player2: Player): Boolean {
        return player1.startingTurn % teams == player2.startingTurn % teams
    }

    /**
     * eliminates the player with the given effective turn from the game
     * @param player the effective turn of the player to eliminate
     */
    fun eliminate(player: Int, bypass: Boolean) {
        if (startingPlayerCount != teams) {
            val teammates = teammatesOf(player)
            if (teammates.isNotEmpty()) {
                var presence = 0
                for (p in teammates) {
                    presence += p.presence
                }
                presence /= teammates.size
                if (presence >= 200) {
                    _isGame = true
                    result.isGame = true
                    result.victoryCondition = Result.VICTORY_PRESENCE
                    result.winner = this.player
                }
            }
        }

        val eliminated = players.removeAt(player)
        // change effective turns of other players
        var i = player
        val z = players.size
        while (i < z) {
            players[i].decrementTurn()
            i++
        }
        if (player < turn) {
            turn--
        }
        eliminated.eliminate()
        eliminatedPlayers.add(eliminated)

        if (isGame) {
            result.isGame = true
            result.victoryCondition = if (bypass) Result.VICTORY_ELIMINATION_BYPASS else Result.VICTORY_ELIMINATION
            result.winner = this.player
        }
    }

    /**
     *
     * @param player the effective turn of the player to analyze
     * @return a list of the teammates of the referenced player
     */
    fun teammatesOf(player: Int): List<Player> {
        return relatedPlayers(player, true)
    }

    /**
     *
     * @param player the effective turn of the player to analyze
     * @return a list of the opponents of the referenced player
     */
    fun opponentsOf(player: Int): List<Player> {
        return relatedPlayers(player, false)
    }

    /**
     *
     * @param player the effective turn of the player to analyze
     * @param teammates whether this will return teammates or opponents
     * @return a list of the teammates or the opponents of the referenced player
     */
    private fun relatedPlayers(player: Int, teammates: Boolean): List<Player> {
        val relatedPlayers = ArrayList<Player>()
        var otherPlayer = player
        var i = 0
        val otherPlayers = players.size - 1
        while (i < otherPlayers) {
            otherPlayer = (otherPlayer + 1) % (otherPlayers + 1)
            if (onSameTeam(getPlayer(player), getPlayer(otherPlayer)) == teammates) {
                relatedPlayers.add(getPlayer(otherPlayer))
            }
            i++
        }
        return relatedPlayers
    }

    /**
     * loads the abilities for each player
     * @param gameScreen reference to pass to Ability constructor
     */
    fun loadAbilities(gameScreen: GameScreen) //throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
    {

        val abilityClasses = ArrayList<Class<*>>()
        Collections.addAll(abilityClasses, *SPECIAL_ABILITIES)

//        players[0].addAbility(DirectHitterAbility(gameScreen, players[0]))
//        players[0].addAbility(LifelineAbility(gameScreen, players[0]))
//        players[0].addAbility(TradeMasterAbility(gameScreen, players[0]))

        val random = Random()
        for (player in players) {

            if (isTraderActive) {
                player.addAbility(TraderAbility(gameScreen, player))
            }
            if (isAllyActive) {
                player.addAbility(AllyAbility(gameScreen, player))
            }
            if (isPanicActive) {
                player.addAbility(PanicAbility(gameScreen, player))
            }
            try {
                for (i in 0 until abilities) {
                    player.addAbility(abilityClasses.removeAt(random.nextInt(abilityClasses.size)).getConstructor(
                        GameScreen::class.java, Player::class.java).newInstance(gameScreen, player) as Ability)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * loads the abilities with the gamescreen and player references
     * called when this instance has been deserialized
     * @param gameScreen a reference to give to abilities
     */
    fun loadFromDeserialization(gameScreen: GameScreen) {
        for (player in players) {
            for (ability in player.abilities) {
                ability.load(gameScreen, player)
            }
        }
    }

    /**
     * resets the instance after the game has ended
     */
    fun reset() {
        _isGame = false
        isDeserialized = false
        players.addAll(eliminatedPlayers)
        eliminatedPlayers.clear()
        players.sort()
        for (player in players) {
            player.reset()
        }
    }

    /**
     * clears this instance of the deserialized marking
     */
    fun clear() {
        isDeserialized = false
    }

    data class Result(var isGame: Boolean = false, var victoryCondition: Int = VICTORY_UNDETERMINED, var winner: Player? = null) : Serializable {
        companion object {
            const val VICTORY_UNDETERMINED = -1
            const val VICTORY_ELIMINATION = 0
            const val VICTORY_ELIMINATION_BYPASS = 1
            const val VICTORY_PRESENCE = 2
            const val VICTORY_CLAIMED = 3
            private const val serialVersionUID = 1L
        }
    }

    object Log {

        fun getLogInfo(message: String, @LogTag tags: Int): LogInfo {

            var detail: String? = null

            if (tags.and(TAG_ABILITY) == TAG_ABILITY) {

                var a = message.substringBefore("activated")
                a = a.trim()

                if (a == getInstance().player.name) {
                    a = message.substringAfter("activated")
                    a = a.trim()
                }

                for (ability in getInstance().player.abilities) {
                    if (ability.name == a) {
                        detail = ability.description
                    }
                }

            }

            var a = message.substringBeforeLast("with")
            if (tags.and(TAG_ATTACK) == TAG_ATTACK) {
                if (message.contains("attacked")) {


                    a = a.substringAfter("attacked")
                    a = a.trim()
                    //a = message.split(" ")

                    var playerIndex = 0
                    for (player in getInstance().players) {
                        if (player.name == a) {
                            playerIndex = player.turn
                        }
                    }

                    val player = getInstance().getPlayer(playerIndex)

                    detail = """
                            |${player.name}
                            |
                            |Hand: ${player.hand.size}
                            |
                            |${player.board}
                            """.trimMargin()

                    detail += """
                        |
                        |
                        |${getInstance().player.name}
                        |
                        |${buildPlayerDetails()}
                        """.trimMargin()


                } else if (message.contains("bypassed")) {

                    a = a.substringAfter("bypassed")
                    a = a.trim()
                    //a = message.split(" ")

                    var playerIndex = 0
                    for (player in getInstance().players) {
                        if (player.name == a) {
                            playerIndex = player.turn
                        }
                    }

                    val player = getInstance().getPlayer(playerIndex)

                    detail = """
                            |${player.name}
                            |
                            |Hand: ${player.hand.size}
                            |${if (getInstance().isPresenceActive) "Presence: ${player.presence}" else ""}
                            |
                            |${player.board}
                            """.trimMargin()

                    detail += """
                        |
                        |
                        |${getInstance().player.name}
                        |
                        |${buildPlayerDetails()}
                        """.trimMargin()
                }
            }

            if (detail == null && message.contains(getInstance().player.name)) {
                if (tags.and(TAG_ATTACK) != TAG_ATTACK) { // including eliminated could cause crash
                    detail = buildPlayerDetails()
                }
            }


            var index = getInstance().details.indexOf(detail)

            if (index == -1) {
                if (detail != null) {
                    getInstance().details.add(detail)
                    index = getInstance().details.size - 1
                }
            }

            return LogInfo(message, tags, index)
        }

        //todo: check this for no presence
        private fun buildPlayerDetails(): String {
            val player = getInstance().player
            return """
                |Hand: ${player.hand.size}
                |${if (getInstance().isPresenceActive) "Presence: ${player.presence}" else ""}
                |
                |${player.board}
                """.trimMargin()
        }

        // holds info about a log item
        data class LogInfo(val message: String, @LogTag val tags: Int, val detailIndex: Int) : Serializable {
            companion object {
                private const val serialVersionUID = 1L
            }
        }

        const val TAG_DEFAULT = 0x1
        const val TAG_ROUND = 1 shl 1
        const val TAG_PHASE = 1 shl 2
        const val TAG_DRAW = 1 shl 3
        const val TAG_PLAY = 1 shl 4
        const val TAG_DISCARD = 1 shl 5
        const val TAG_ABILITY = 1 shl 6
        const val TAG_ATTACK = 1 shl 7
        const val TAG_EMOJI = 0x100

        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
        @IntDef(flag = true,
                value = [
                    TAG_DEFAULT,
                    TAG_ROUND,
                    TAG_PHASE,
                    TAG_DRAW,
                    TAG_PLAY,
                    TAG_DISCARD,
                    TAG_ABILITY,
                    TAG_ATTACK])
        annotation class LogTag

    }

    companion object {
        private const val serialVersionUID = 1L

        /**
         * This is the single instance of this class
         */
        private var INSTANCE: Game = Game()

        @JvmStatic
        fun getInstance(): Game {
            return INSTANCE
        }

        /**
         * Represents the Draw Phase
         */
        const val PHASE_DRAW = 0

        /**
         * Represents the Standby Phase
         */
        const val PHASE_STANDBY = 1

        /**
         * Represents the Point Phase
         */
        const val PHASE_POINT = 2

        /**
         * Represents the Club Phase
         */
        const val PHASE_CLUB = 3

        /**
         * the first round spades may be played and players may attack
         */
        const val ATTACK_ROUND = 3

        /**
         * the first round players may Shield Bypass
         */
        const val BYPASS_ROUND = 4

        /**
         * Holds the String representations of each phase
         */
        @JvmField
        val PHASES = arrayOf("Draw", "Standby", "Point", "Club")

        /**
         * The maximum value of a card
         */
        const val MAX_VALUE = 10

        /**
         * The number of points a player starts each turn with
         */
        const val STARTING_POINTS = 12

        /**
         * An array of all the base abilities
         */
        val BASE_ABILITIES = arrayOf<Class<*>>(
                TraderAbility::class.java,
                AllyAbility::class.java,
                PanicAbility::class.java)

        /**
         * An array of all the special abilities
         */
        private val SPECIAL_ABILITIES = arrayOf<Class<*>>(
                ArchaeologistAbility::class.java,
                AssetsAbility::class.java,
                BlessedAbility::class.java,
                ClubMemberAbility::class.java,
                CombinerAbility::class.java,
                DirectHitterAbility::class.java,
                FulfilledAbility::class.java,
                GamblerAbility::class.java,
                HagglerAbility::class.java,
                HealthyAbility::class.java,
                HopefulAbility::class.java,
                IndebtedAbility::class.java,
                InvestorAbility::class.java,
                LargeCapacityAbility::class.java,
                LifelineAbility::class.java,
                LoadedAbility::class.java,
                ManipulatorAbility::class.java,
                MaximizerAbility::class.java,
                OptionsAbility::class.java,
                PacifistAbility::class.java,
                PlaguedAbility::class.java,
                PurifierAbility::class.java,
                ResourcefulAbility::class.java,
                SeerAbility::class.java,
                SellerAbility::class.java,
                StandardizationAbility::class.java,
                ToughnessAbility::class.java,
                TradeMasterAbility::class.java,
                WarriorAbility::class.java,
                WealthAbility::class.java)

        @JvmField
        val ABILITIES_ID_NAMES = arrayOf(
                "trader",
                "ally",
                "panic",
                "archaeologist",
                "assets",
                "blessed",
                "club_member",
                "combiner",
                "direct_hitter",
                "fulfilled",
                "gambler",
                "haggler",
                "healthy",
                "hopeful",
                "indebted",
                "investor",
                "large_capacity",
                "lifeline",
                "loaded",
                "manipulator",
                "maximizer",
                "options",
                "pacifist",
                "plagued",
                "purifier",
                "resourceful",
                "seer",
                "seller",
                "standardization",
                "toughness",
                "trade_master",
                "warrior",
                "wealth")

        /**
         * Represents no log limit
         */
        const val LOG_NO_LIMIT = -1

        /**
         * Represents only keeping the last round's worth of turns in the log
         */
        const val LOG_LAST_ROUND = -2

        /**
         * sets the instance to an instance loaded
         * @param game the instance loaded
         */
        @JvmStatic
        fun loadFromDeserialization(game: Game) {
            INSTANCE = game
            INSTANCE.isDeserialized = true
        }
    }
}
