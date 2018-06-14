//@
package xyz.hyperreal.yaml


object Main extends App {

  try {
    val tree =
      new YamlParser().parse(
        """
          |# ASCII Art
          |--- |
          |  \//||\/||
          |  // ||  ||__
        """.stripMargin
      )

    println( tree )
    println( new Evaluator().eval(tree) )
  } catch {
    case e: Exception => println( e.getMessage )
  }

}