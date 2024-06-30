package cosc250.reversi

import scala.annotation.tailrec
import scala.collection.immutable.Queue
import scala.util.Random

enum Player:
    case Black
    case White

/** The board size is always 8 by 8 */
val boardSize = 8

/** A location on the board. Zero-indexed */
type Location = (Int, Int)

/**
 * The state of the board
 * @param lastMove - the location of the last move
 * @param board - maps the locations of pieces on the board (note that if a piece has not been played in a square, it won't be in the map)
 * @param turn - whose turn it is next
 */
case class GameState(lastMove:Option[(Location, Player)], board:Map[Location, Player], turn:Player) {
    /** Simply a list of all location on the board */
    private val allLocations: List[Location] = (0 to 7).flatMap {
        x => (0 to 7).map { y => (x, y) }
    }.toList

    /** Determines the winner or draw */
    val determineWinner: List[Player] =
        if (blackPieces < whitePieces)
            List(Player.White)
        else if (whitePieces < blackPieces)
            List(Player.Black)
        else
            List(Player.Black, Player.White)

    /** A list of possible starting locations */
    private val startLocations = List((3, 3), (3, 4), (4, 3), (4, 4))

    /** Checks if all first moves are complete */
    val firstMovesComplete: Boolean = startLocations.forall(board.contains)

    /** Determines the opponent of this turn */
    private val opponent: Player = if (turn == Player.Black) Player.White else Player.Black

    /** A list of all 8 directions from a location */
    private val directions = List((0, 1), (0, -1), (-1, 0), (1, 0), (-1, -1), (-1, 1), (1, -1), (1, 1))
    
    /**
     * For passing on a move
     * @return The same GameState but with the opponents turn
     */
    def pass: GameState =
        println(s"Player $turn passed")
        GameState(lastMove, board, opponent)

    /**
     * @return an Int of the number of black pieces
     */
    def blackPieces: Int = board.count { case (_, player) => player == Player.Black }

    /**
     * @return an Int of the number of white pieces
     */
    def whitePieces: Int = board.count { case (_, player) => player == Player.White }

    /**
     * @return Boolean of whether the game is over
     */
    def gameOver: Boolean = firstMovesComplete && !anyValidMove(turn) && !anyValidMove(opponent)

    /**
     * Whether a particular move is valid
     * @param loc Location to check
     * @return Boolean of whether the location is valid
     */
    def isValidMove(loc: Location): Boolean = 
        !board.contains(loc) 
          && enemyNeighbours(loc).nonEmpty 
          && checkForEnemySandwich(loc)

    /**
     * A list of valid moves for the current board
     * @return A list of all valid moves for the current GameState
     */
    def validMovesList(): List[Location] = allLocations.filter(isValidMove)
    
    /**
     * Returns a list of positions on the board containing the opponent
     * @return A list of all enemy positions on the current board
     */
    def allEnemyPositions(): List[Location] =
        board.filter { case (_, player) => player == opponent }.keys.toList

    /**
     * Returns a boolean if the opponent is opposite a given location
     * @param loc Location to check
     * @return List of all neighbour enemies
     */
    def enemyNeighbours(loc: Location): List[Location] =
        allEnemyPositions().filter(surroundingLocations(loc).filter(allLocations.contains).contains)

    /**
     * To check if there is any valid move for a given player
     * @param player Player to check
     * @return Boolean of whether the player has any possible moves
     */
    def anyValidMove(player: Player): Boolean =
        allLocations.exists(loc => !board.contains(loc) && isValidMove(loc))

    /**
     * Checks for an enemy sandwich (two player pieces with at least one enemy between them and no spaces)
     * @param loc Location to check
     * @return Boolean whether there is a sandwich in any direction
     */
    def checkForEnemySandwich(loc: Location): Boolean =
        directions.exists(dir => checkDirectionForSandwich(loc, dir).nonEmpty)
    
    /**
     * Checks a single direction as far as possible for placement criteria 
     * i.e. an opponent, followed by 0+ more opponents 
     * and ending with a player square and no empty squares between.
     * @param loc the initial location to start the check from
     * @param dir the direction we are going int (x,y) adjustment to make
     * @return a list of all squares meeting the rules for placement
     */
    def checkDirectionForSandwich(loc: Location, dir: (Int, Int)): List[Location] =
        val (x, y) = dir // The x and y change for the direction we are checking
        /**
         * Finds all positions to flip
         * @param pos List of opponent positions/player positions meeting the criteria
         * @param x The x coordinated adjustment to make each time
         * @param y The y coordinated adjustment to make each time
         * @return a list of all positions to be changed to the attacking player 
         *         or returns and empty list if the direction was invalid
         */
        @tailrec
        def adjust(pos: List[Location], x: Int, y: Int): List[Location] = {
            val newLoc = (pos.head._1 + x, pos.head._2 + y)
            if (board.contains(newLoc)) {
                if (board(newLoc) == opponent) {
                    adjust(newLoc :: pos, x, y)
                } else if (board(newLoc) == turn) {
                    newLoc :: pos
                } else {
                    List()
                }
            } else {
                List()
            }
        }
        // Getting the next square in the direction
        val (nextX, nextY) = (loc._1 + x, loc._2 + y)
        // Confirming it is an opponent square
        if (board.contains((nextX, nextY)) && board(nextX, nextY) == opponent) {
            adjust(List(loc), x, y) // Calling the tailrec function
        } else {
            List() // Returning empty list, direction not valid
        }

    /**
     * Returns a list of Locations around the given Location
     * @param loc Central location to work off
     * @return A list of all Locations around the loc
     */
    def surroundingLocations(loc: Location): List[Location] =
        directions.map { case (dX, dY) =>
            (loc._1 + dX, loc._2 + dY)
        }

    /**
     * Called from play outside this class to make a move based on the lookAhead
     * @param lookAhead Number of moves to look ahead to determine the best move
     * @return A new GameState for the UI
     */
    def move(lookAhead: Int): GameState =
        if (firstMovesComplete) {
            val depth = checkRemainingMoves(lookAhead) // Adjusts lookAhead if board is getting full
            if (!anyValidMove(turn)) return pass
            depth match {
                case 0 =>
                    randomValidMove()
                case 1 =>
                    playerBestMove()
                case _ =>
                    searchAhead(depth)
            }
        } else {
            placeStartingPiece
        }
    
    /**
     * Picks a random first move
     * @return the new GameState
     */
    def placeStartingPiece: GameState =
        val remainingOptions = Random.shuffle(startLocations).filter(loc => !board.contains(loc))
        if remainingOptions.nonEmpty then
            GameState(Some((remainingOptions.head, turn)), board.updated(remainingOptions.head, turn), opponent)
        else
            println("Woops, startOptions shouldn't be empty yet")
            this
            
    /**
     * Used in the move method to ensure lookAhead doesn't go beyond board capacity
     * @param lookAhead The Int number of steps requested to look ahead.
     * @return An Int of either the requested amount or max left on the board
     */
    def checkRemainingMoves(lookAhead: Int): Int =
        val spacesLeft = 64 - whitePieces - blackPieces
        if (spacesLeft > lookAhead) lookAhead else spacesLeft

    /**
     * For selecting just a random valid move
     * @return the new GameState
     */
    def randomValidMove(): GameState =
        returnGameState(Random.shuffle(validMovesList()).head)

    /**
     * For just looking at the next move
     * @return new GameState of the players best next move
     */
    def playerBestMove(): GameState =
        val (chosenSquare, changedSquares) = pickBestMove()
        GameState(Some((chosenSquare, turn)), getUpdatedBoard(changedSquares), opponent)
    
    /**
     * For choosing the next move that takes the most opponent pieces
     * @return A tuple with the best location to move to and the locations changed with that move
     */
    def pickBestMove(): (Location, List[Location]) =
        // Each valid move mapped to the squares it will change
        val options = validMovesList().map(loc => squaresToChange(loc)).toMap
        // Take the move that changes the most squares
        val bestMove = options.maxBy { case (_, locList) => locList.length }
        // Returning the location and all positions it changes
        (bestMove._1, bestMove._2)

    /**
     * Creates a new game for exploration purposes only
     * @param gameState A GameState to build off
     * @param loc The new location for the theoretical move
     * @return The new (But hypothetical) GameState
     */
    def dummyState(gameState: GameState, loc: Location): GameState = {
        val newBoard = gameState.getUpdatedBoard(gameState.squaresToChange(loc)._2)
        GameState(Some(loc, gameState.turn), newBoard, gameState.opponent)
    }

    /**
     * Method to search at certain number of plays ahead
     * @param depth number of plays ahead to search
     * @return new GameState with the best play found
     */
    def searchAhead(depth: Int): GameState = {
        // Building a Map of each valid location and the GameState in response
        val initialMap = validMovesList().map { loc =>
            (loc, dummyState(this, loc))
        }.toMap

        /**
         * A method to recursively explore the best moves
         * @param cDepth current depth we are searching
         * @param subState current state of the hypothetical game
         * @param subScore current score based on the explored moves
         * @param p Int to determine the player or opponent using mod 2
         * @return and Int of the end scored based on the explored moves
         */
        @tailrec
        def getNextRound(cDepth: Int, subState: GameState, subScore: Int, p: Int): Int =
            if (cDepth <= depth) {
                // If this hypothetical turn needs to pass
                if (!subState.anyValidMove(subState.turn)) {
                    getNextRound(cDepth + 1, subState, subScore, p + 1)
                } else {
                    // Creating the next state with the best move
                    val nextSubState = dummyState(subState, subState.pickBestMove()._1)
                    // s for brevity on the score from this move
                    // The minus 1 is because squares to change length includes the end of the
                    // sandwich which is actually an unchanged square.
                    val s = subState.squaresToChange(subState.pickBestMove()._1)._2.length - 1
                    // new depth
                    val nDepth = cDepth + 1 
                    // balance is either added or subtract depending on player or opponent
                    val balance = if (p % 2 == 1) subScore + s else subScore - s
                    getNextRound(nDepth, nextSubState, balance, p + 1)
                }
            } else {
                subScore
            }
        /* 
        Taking all valid moves and exploring them in the tailrec function with
        loc being the main first move by the player and the tailrec function returning the end score difference
         */
        val scores = initialMap.map { case (loc, possibleState) =>
            // First getting the changed squares based off the first move (loc)
            val baseScore = if (turn == Player.Black)
                possibleState.blackPieces - blackPieces
            else
                possibleState.whitePieces - whitePieces
            (loc, getNextRound(2, possibleState, baseScore, 2))
            }
        returnGameState(scores.maxBy(_._2)._1)
    }

    /**
     * Based on a given move, will calculate all squares to be changed
     * @param loc Location the player is moving to
     * @return a list of squares to flip from opponent to player
     */
    def squaresToChange(loc: Location): (Location, List[Location]) =
        val changedSquares = directions.map { direction =>
            checkDirectionForSandwich(loc, direction)
        }
        (loc, changedSquares.flatten)

    /**
     * Returns a new Map of the updated pieces based on a given list of locations
     * @param changedSquares A list of squares on the board to change
     * @return An updated Map for the new game board
     */
    def getUpdatedBoard(changedSquares: List[Location]): Map[Location, Player] =
        changedSquares.foldLeft(board) { case (b, l) =>
            b.updated(l, turn)
        }
        
    /**
     * Generates the new GameState from a given location
     * @param move The location the player is moving to
     * @return the new GameState
     */
    def returnGameState(move: Location): GameState =
        val (chosenSquare, changeList) = squaresToChange(move)
        GameState(Some((chosenSquare, turn)), getUpdatedBoard(changeList), opponent)
}

/** New Game */
object GameState {
    def newGame: GameState = GameState(None, Map.empty, Player.Black)
}

/** A game is a sequence of game-states (so it remembers past moves). The most recent move is at the end. */
type Game = Seq[GameState]

/** Creates a new game, containing just the start game state */
def newGame:Seq[GameState] = Seq(GameState.newGame)

/** Called by the UI on each animation tick to make your AI play the game */
def play(state:Seq[GameState], lookAhead:Int):Seq[GameState] =
    if (!state.last.gameOver) {
        state :+ state.last.move(lookAhead)
    } else {
        val winner = state.last.determineWinner
        if (winner.length == 1) {
            println(s" ${winner.head} is the winner!")
        } else {
            println("It's a draw!")
        }
        state
    }

/** Called by the UI when the user clicks back in the game history */
def rewindTo(state:Seq[GameState], move:Int):Seq[GameState] =
    state.dropRight(state.length - move - 1)