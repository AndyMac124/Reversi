## Reversi

[Reversi](https://en.wikipedia.org/wiki/Reversi) is a traditional game played on an 8x8 board.

There are two players, black and white, who take turns placing disks on the board.

The first two moves for each player involve placing a disk in one of the four central squares of the board. For these two moves,
no captures are made. (We're doing the original 1883 rules.) Programming hint: after these moves, there are four pieces on the board.

For all subsequent moves, a move is valid if it captures at least one piece. (See the examples on the wikipedia page, or try playing it online.)
Note that captures can be horizontal, vertical, or diagonal, and a move might capture pieces on multiple axes.

If a player has no valid moves, their turn is skipped.

If neither player has a valid move, the game ends and the player with the most pieces on the board wins.

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


