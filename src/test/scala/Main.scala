//@
package xyz.hyperreal.yaml


object Main extends App {

  try {
    val tree =
      new YamlParser().parse(
        """
          |a:
          |  c:""".stripMargin
      )

    println( tree )
    println( new Evaluator(Nil).eval(tree) )
  } catch {
    case e: Exception => println( e.getMessage )
  }

}