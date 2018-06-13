package xyz.hyperreal.yaml


object Main extends App {

  val tree =
    new YamlParser().parse(
      """
        |- 1
        |-
        |- 3
      """.stripMargin
  )

  println( tree )
  println( new Evaluator().eval(tree) )

}
