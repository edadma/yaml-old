//@
package xyz.hyperreal.yaml


object Main extends App {

  try {
    val tree =
      new YamlParser().parse(
      """
        |canonical: 1.23015e+3
        |exponential: 12.3015e+02
        |fixed: 1230.15
        |negative infinity: -.inf
        |not a number: .NaN
      """.stripMargin
      )

    println( tree )
    println( new Evaluator().eval(tree) )
  } catch {
    case e: Exception => println( e.getMessage )
  }

}