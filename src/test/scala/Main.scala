//@
package xyz.hyperreal.yaml


object Main extends App {

  try {
    val tree =
      new YamlParser().parse(
        """
          |---
          |hr:
          |  - Mark McGwire
          |  # Following node labeled SS
          |  - &SS Sammy Sosa
          |rbi:
          |  - *SS # Subsequent occurrence
          |  - Ken Griffey
        """.stripMargin
      )

    println( tree )
    println( new Evaluator().eval(tree) )
  } catch {
    case e: Exception => println( e.getMessage )
  }

}