//@
package xyz.hyperreal.yaml


object Main extends App {

  try {
    val tree =
      new YamlParser().parse(
        """
          |{asdf, zxcv, asdf}
        """.stripMargin
      )

    println( tree )
    println( new Evaluator(Nil).eval(tree)/*.asInstanceOf[List[String]].head map (_.toInt)*/ )
  } catch {
    case e: Exception => println( e.getMessage )
  }

}