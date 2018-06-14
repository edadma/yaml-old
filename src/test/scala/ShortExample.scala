//import xyz.hyperreal.yaml._
//
//
//object ShortExample extends App {
//
//  val result =
//    read(
//      """
//        |---
//        |hr:
//        |  - Mark McGwire
//        |  # Following node labeled SS
//        |  - &SS Sammy Sosa
//        |rbi:
//        |  - *SS # Subsequent occurrence
//        |  - Ken Griffey
//      """.stripMargin
//    )
//
//  println( result )
//
//}