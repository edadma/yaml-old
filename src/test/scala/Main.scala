//@
package xyz.hyperreal.yaml


object Main extends App {

  try {
    val tree =
      new YamlParser().parse(
        """
          |? - Detroit Tigers
          |  - Chicago cubs
          |:
          |  - 2001-07-23
          |
          |? [ New York Yankees,
          |    Atlanta Braves ]
          |: [ 2001-07-02, 2001-08-12,
          |    2001-08-14 ]
        """.stripMargin
      )

    println( tree )
    println( new Evaluator().eval(tree) )
  } catch {
    case e: Exception => println( e.getMessage )
  }

}