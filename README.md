# Assignment 2: Reversi

In a pure functional language, such as Haskell, the code is a pure (no side-effects) description of a program which is
then run in a runtime environment.

As Scala is mixed paradigm, we sometimes find ourselves "pushing mutation to the edges", so that the core parts of your code
can be pure. For instance, "effect types" (types that describe a program that has input and output effects) may have an 
`unsafeRunSync` or `unsafeRunAsync` method which is not pure. You then have a pure program in most of the code, with a 
single call to an `unsafeRun` method in the `main` method - the mutation has been pushed to the edges of the program.

We're not going to go quite that far - this is only your second assignment in the unit. However, we are going to try
to separate our functional code from our imperative code somewhat. And we're going to try to make the "important"
code functional (and more testable).

In this case, we're going to use functional programming to write a little classical AI for the game of Reversi. 
The AI will be able to look ahead a variable number of moves to pick the best move to take.
The AI code (code in `Reversi.scala`) should be pure and functional.

The UI has been written for you (in `App.scala`), and does include some mutation.

## Reversi

[Reversi](https://en.wikipedia.org/wiki/Reversi) is a traditional game played on an 8x8 board.

There are two players, black and white, who take turns placing disks on the board.

The first two moves for each player involve placing a disk in one of the four central squares of the board. For these two moves,
no captures are made. (We're doing the original 1883 rules.) Programming hint: after these moves, there are four pieces on the board.

For all subsequent moves, a move is valid if it captures at least one piece. (See the examples on the wikipedia page, or try playing it online.)
Note that captures can be horizontal, vertical, or diagonal, and a move might capture pieces on multiple axes.

If a player has no valid moves, their turn is skipped.

If neither player has a valid move, the game ends and the player with the most pieces on the board wins.

Note: You are *not writing this for humans to play*.

## Writing the simulation

You are writing a model of the game in which the computer will play itself. A widget on the UI will let us alter the "lookahead" setting for the AI.

* If Lookahead is 0, it will play a random valid move
* If Lookahead is 1, it will play the move that leaves it with the most pieces after playing it.
* If Lookahead is 2, it will play the move that leaves it with the most pieces after its opponent has played the best move with Lookahead of 1
* If Lookahead is 3, it will play the move that leaves it with the most pieces after its opponent has played the best move with Lookahead of 2
* etc.

(Though you'll notice the mark scheme only goes so far as to try lookahead of 0, 1, and 2.)

The UI will ask your code to play a turn about once per second. 

There is also a history of moves. Clicking in this history of moves will ask your simulation to go back in time as if that move had just been played.
You will notice that the UI wants to keep an immutable `Seq[GameState]` rather than just a game state, for this purpose.

## Running the code

This repository is set up so that the code can be run either using `scala-cli` or `sbt`

In other words,

* `scala-cli run .` will run the app
* So will `sbt run`
* `scala-cli test .` will run the tests
* So will `sbt test`

Please note that you may get a warning from the JavaFX toolkit when it starts up:

```
WARNING: Unsupported JavaFX configuration: classes were loaded from 'unnamed module @2484f53f'
```

This is normal. The reason it appears is because JavaFX (the UI kit used behind the scenes) is a Java module, but when loaded via ScalaFX, the program runs in the "unnamed module". It still works.


