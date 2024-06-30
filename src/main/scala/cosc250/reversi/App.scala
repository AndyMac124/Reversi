// Don't delete the comment below -- it is a Scala-CLI "using" directive to instruct it to use ScalaFX UI framework
//> using dep org.scalafx::scalafx::20.0.0-R31
//> using jvm "17"

package cosc250.reversi

import scalafx.Includes._
import scalafx.application.JFXApp3
import scalafx.scene.{Scene, Group}
import scalafx.scene.paint.Color
import scalafx.scene.shape.*
import scalafx.scene.layout.*
import scalafx.scene.control.*
import scalafx.geometry.*
import scalafx.collections.*
import scalafx.animation.*
import scalafx.beans.property.*

object App extends JFXApp3 {

    override def start() = {
        stage = new JFXApp3.PrimaryStage {
            title.value = "Reversi"
            width = 480
            height = 600
            scene = new Scene {
                content = new UI().subscene                    
            }
        }
    }

}

class UI() {

    // A constant for how bix the squares are
    val squareSize = 40

    /** Represents a square on the board. */
    class Square(col:Int, row:Int) {

        private val square = new Rectangle {
            width = squareSize
            height = squareSize
            fill = if (col + row) % 2 == 0 then Color.hsb(114, 0.85, 0.33) else Color.hsb(114, 0.65, 0.43)
        }

        val ui = new Group {
            translateX = col * squareSize
            translateY = row * squareSize
            children = Seq(
                square
            )
        }

        def clear():Unit = {
            ui.children = Seq(square)
        }

        def place(p:Player) = {
            ui.children = Seq(
                square,
                new Circle {
                    centerX = squareSize / 2
                    centerY = squareSize / 2
                    radius = squareSize / 3
                    fill = if p == Player.White then Color.White else Color.Black
                }
            )
        }
    }

    private val squares = for y <- 0 until boardSize yield
        for x <- 0 until boardSize yield 
            new Square(x, y)


    var game = newGame

    private def showGameState(g:GameState):Unit = {
        for 
            y <- 0 until boardSize
            x <- 0 until boardSize
        do
            if g.board.contains((x, y)) then
                squares(y)(x).place(g.board((x, y)))
            else 
                squares(y)(x).clear()
    }

    private def showGame(gs:Seq[GameState]):Unit = {
        showGameState(gs.last)
        history.items = ObservableBuffer((for 
            (g, i) <- gs.zipWithIndex 
            (p, loc) <- g.lastMove
        yield
            (i, loc, p)
        )*)
    }


    val history = new ListView[(Int, Player, Location)] {
        orientation = Orientation.Vertical
        cellFactory = (cell, data) => {
            val (i, p, (x, y)) = data
            val col = "abcdefgh"(x)
            cell.text = s"$i. $col${y+1}"
            cell.textFill = if p == Player.Black then Color.Black else Color.White
            cell.onMouseClicked = { _ =>
                lastTime = System.nanoTime()
                game = rewindTo(game, i)
                showGame(game)                
            }

            cell.style = 
                if i % 2 == 0 then "-fx-background-color: #1D7A12;"
                else  "-fx-background-color: #1A6E10;"
        }
    }

    private def step():Unit = {
        println(s"Lookahead ${lookAhead.value.value}")
        game = play(game, lookAhead.value.value)
        showGame(game)
    }

    private var lastTime = 0L
    private val playing = BooleanProperty(false)

    private val lookAhead = new Spinner[Int](0, 5, 0)

    AnimationTimer({ now =>
        if playing.value && (now > lastTime + 1e9) then 
            lastTime = now
            step()
    }).start()

    private val startStop = new Button {
        text <== (for p <- playing yield if p then "Stop" else "Start")
        onAction = { _ => playing.value = !playing.value }
    }

    val subscene = new HBox(
        new VBox(
            new Group(squares.flatten.map(_.ui).toSeq*),
            new HBox(5, startStop, Label("Lookahead"), lookAhead)
        ),
        history
    )



}