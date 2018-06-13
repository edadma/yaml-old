package xyz.hyperreal.yaml


object Main extends App {

  val tree =
    new YamlParser().parse(
      """
        |---
        |-
      """.stripMargin
  )

  println( tree )
  println( new Evaluator().eval(tree) )

}