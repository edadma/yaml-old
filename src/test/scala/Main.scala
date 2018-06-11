package xyz.hyperreal.yaml


object Main extends App {

  val tree =
    new YamlParser().parse(
      """
        |asdf:
        |  - 123asdf
        |  - tryu
        |  - oiuh: true
        |    iut: gfd
        |  - 345.2
        |dfhg: zxcv
        |poiu:
        |  iuy: 1
        |  gf: 2
      """.stripMargin
  )

  println( tree )

}
