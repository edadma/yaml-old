//@
package xyz.hyperreal.yaml


object Main extends App {

  try {
    val u = "\\u263A"
    val x = "\\x0d\\x0a"
    val d = "\\-"
    val tree =
      new YamlParser().parse(
        """
          |"\u005Cu263A"
        """.stripMargin
      )

    println( tree )
    println( new Evaluator().eval(tree).asInstanceOf[List[String]].head map (_.toInt) )
  } catch {
    case e: Exception => println( e.getMessage )
  }

}