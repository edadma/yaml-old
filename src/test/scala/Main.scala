package xyz.hyperreal.yaml


object Main extends App {

  val tree =
    new YamlParser().parse(
      """
        |--- 123
      """.stripMargin
  )

  println( tree )

}
