//@
package xyz.hyperreal.yaml


object Main extends App {

  try {
    val tree =
      new YamlParser().parse(
        """
          |plain:
          |  This unquoted scalar
          |  spans many lines.
          |
          |quoted: "So does this
          |  quoted scalar.\n"
        """.stripMargin
      )

    println( tree )
    println( new Evaluator().eval(tree)/*.asInstanceOf[List[String]].head map (_.toInt)*/ )
  } catch {
    case e: Exception => println( e.getMessage )
  }

}