//@
package xyz.hyperreal.yaml


object Main extends App {

  try {
    val tree =
      new YamlParser().parse(
        """
          |2001-11-23 15:01:42.10 Z
        """.stripMargin
      )

    println( tree )
    println( new Evaluator().eval(tree) )
  } catch {
    case e: Exception => println( e.getMessage )
  }

}