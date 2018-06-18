//@
package xyz.hyperreal.yaml


object Main extends App {

  try {
    val tree =
      new YamlParser().parse(
        """
          |[1, 2]: zxcv
        """.stripMargin
      )

    println( tree )
    println( new Evaluator(Nil).eval(tree)/*.asInstanceOf[List[String]].head map (_.toInt)*/ )
  } catch {
    case e: Exception => println( e.getMessage )
  }

}