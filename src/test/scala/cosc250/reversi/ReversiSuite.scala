// Don't delete the comment below -- it is a Scala-CLI "using" directive to instruct it to use the munit test framework
//> using test.dep org.scalameta::munit::0.7.29

package cosc250.reversi

/**
  * Tests
  */
class ReversiSuite extends munit.FunSuite {

  test("Counts pieces") {
    assertEquals(GameState(None, Map((3, 3) -> Player.Black, (3, 4) -> Player.Black), Player.White).blackPieces, 2)
    assertEquals(GameState(None, Map((3, 3) -> Player.White, (3, 4) -> Player.White), Player.White).whitePieces, 2)
    assertEquals(GameState(None, Map((4, 3) -> Player.White, (3, 4) -> Player.White), Player.Black).blackPieces, 0)
  }

  test("Should detect if a player has any valid moves") {
    // Both players have moves
    val game = GameState(None, Map((4, 3) -> Player.Black, (3, 4) -> Player.White, (3, 3) -> Player.Black, (4, 4) -> Player.White), Player.Black)
    assertEquals(game.anyValidMove(Player.Black), true)
    assertEquals(game.anyValidMove(Player.White), true)
    // No valid moves
    val game2 = GameState(None, Map((4, 3) -> Player.Black, (3, 4) -> Player.Black, (3, 3) -> Player.Black, (4, 4) -> Player.Black), Player.Black)
    assertEquals(game2.anyValidMove(Player.Black), false)
    assertEquals(game2.anyValidMove(Player.White), false)
  }

  test("Should detect if game over") {
    // Game should be over
    val gameIsOver = GameState(None, Map((4, 3) -> Player.Black, (3, 4) -> Player.Black, (3, 3) -> Player.Black, (4, 4) -> Player.Black), Player.Black)
    assertEquals(gameIsOver.gameOver, true)
    // Black can play
    val gameIsNotOver = GameState(None, Map((4, 3) -> Player.Black, (3, 4) -> Player.White, (3, 3) -> Player.Black, (4, 4) -> Player.White), Player.Black)
    assertEquals(gameIsNotOver.gameOver, false)
    // Player must pass but opponent can play, game not over
    val passButNotOver = GameState(None, Map((4, 3) -> Player.Black, (3, 4) -> Player.White, (3, 3) -> Player.Black,
      (4, 4) -> Player.White, (4, 2) -> Player.Black, (3, 2) -> Player.Black,
      (2, 3) -> Player.Black, (5, 3) -> Player.Black, (2, 4) -> Player.Black, (2, 4) -> Player.Black), Player.Black)
    assertEquals(passButNotOver.gameOver, false)
  }

  test("Should be able to detect if a move is valid") {
    val game = GameState(None, Map((4, 3) -> Player.White, (3, 4) -> Player.White, (3,3) -> Player.Black, (4,4) -> Player.Black), Player.Black)
    assertEquals(game.isValidMove(4, 2), true)
    assertEquals(game.isValidMove(6,6), false)
    assertEquals(game.isValidMove(3,2), false)
    val game2 = GameState(None, Map((3, 1) -> Player.White, (3, 2) -> Player.Black, (3, 3) -> Player.Black, (3, 4) -> Player.Black, (3, 6) -> Player.Black, (2, 3) -> Player.White), Player.White)
    assertEquals(game2.isValidMove(4, 4), false)
    assertEquals(game2.isValidMove(4, 3), true)
  }

  test("Should pass if can't make a move") {
    // Basic game, Black can play
    val game = GameState(None, Map((4, 3) -> Player.Black, (3, 4) -> Player.Black, (3, 3) -> Player.Black, (4, 4) -> Player.Black), Player.Black)
    assertEquals(game.validMovesList().isEmpty, true)
    // White can play, confirmation for the below
    val notPass = GameState(None, Map((2, 2) -> Player.Black, (3, 2) -> Player.Black, (4, 2) -> Player.Black,
      (5, 2) -> Player.Black, (3, 3) -> Player.White, (4, 3) -> Player.White,
      (2, 4) -> Player.Black, (3, 4) -> Player.Black, (4, 4) -> Player.Black,
      (5, 4) -> Player.Black), Player.White)
    assertEquals(notPass.anyValidMove(Player.White), true)
    // Player Black must pass but opponent can play
    val passButNotOver = GameState(None, Map((2, 2) -> Player.Black, (3, 2) -> Player.Black, (4, 2) -> Player.Black,
      (5, 2) -> Player.Black, (3, 3) -> Player.White, (4, 3) -> Player.White,
      (2, 4) -> Player.Black, (3, 4) -> Player.Black, (4, 4) -> Player.Black,
      (5, 4) -> Player.Black), Player.Black)
    assertEquals(passButNotOver.anyValidMove(Player.Black), false)
  }

  test("Should find locations of enemies in surrounding squares") {
    val game = GameState(None, Map((3, 2) -> Player.Black, (3, 4) -> Player.Black, (2, 3) -> Player.Black, (5, 5) -> Player.Black), Player.White)
    assertEquals(game.enemyNeighbours(3, 3), List((3, 2), (3, 4), (2, 3)))
  }

  test("Should be able to detect if a location creates an enemy sandwich") {
    val game = GameState(None, Map((3, 2) -> Player.Black, (3, 3) -> Player.White), Player.Black)
    assertEquals(game.checkForEnemySandwich(3, 4), (true))
  }

  test("Should pass if can't make a move") {
    val game = GameState(None, Map((4, 3) -> Player.Black, (3, 4) -> Player.Black, (3, 3) -> Player.Black, (4, 4) -> Player.Black), Player.Black)
    assertEquals(game.validMovesList().isEmpty, true)
  }

  test("Should be able to determine if all first moves have been completed") {
    val gameTrue = GameState(None, Map((4, 3) -> Player.White, (3, 4) -> Player.White, (3,3) -> Player.Black, (4,4) -> Player.Black), Player.Black)
    assertEquals(gameTrue.firstMovesComplete, true)
    val gameFalse = GameState(None, Map((4, 3) -> Player.White, (3, 4) -> Player.White, (3, 3) -> Player.Black), Player.Black)
    assertEquals(gameFalse.firstMovesComplete, false)
  }

  test("Should be able to look two moves ahead") {
    // Very messy setup but there is a trap where black must take a worse first move for a better outcome
    /*
          1 2 3 4 5 6
        0       W
        1       B
        2     W W
        3   W W W
        4     W W
        5       W
        6
        7
     */
    val game = GameState(None, Map((3, 4) -> Player.White, (3, 3) -> Player.White, (4, 3) -> Player.White,
      (4, 4) -> Player.White, (4, 5) -> Player.White, (4, 2) -> Player.White, (4, 1) -> Player.Black,
      (4, 0) -> Player.White, (3, 2) -> Player.White, (2, 3) -> Player.White), Player.Black)
    val expectedBestMove: Location = (1, 4)
    val resultGameState = game.move(2)
    assertEquals(resultGameState.lastMove, Some((expectedBestMove, Player.Black)))
  }
}
