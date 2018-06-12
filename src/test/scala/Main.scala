package xyz.hyperreal.yaml


object Main extends App {

  val tree =
    new YamlParser().parse(
      """
        |- &n 123
        |- *n
      """.stripMargin
  )

  println( tree )
  println( new Evaluator().eval(tree) )

}
