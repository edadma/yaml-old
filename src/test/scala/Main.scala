package xyz.hyperreal.yaml


object Main extends App {

  new YamlParser().parseFromString(
    """
      |"asdf":
      |  - "eyt"
      |"dfhg": "zxcv"
    """.stripMargin
  )

}