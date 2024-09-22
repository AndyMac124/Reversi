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


