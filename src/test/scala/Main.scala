package xyz.hyperreal.yaml


object Main extends App {

  val tree =
    read( //io.Source.fromFile("test1.yml") )
      """
        |asdf:
        |  - 123
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
