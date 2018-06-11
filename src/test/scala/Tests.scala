//@
package xyz.hyperreal.yaml

import org.scalatest._
import prop.PropertyChecks


class Tests extends FreeSpec with PropertyChecks with Matchers {

  "numeric" in {
    read(
      """
        |- 0x123
        |- 123
        |- 123asdf
        |- 123 asdf
        |- -0x123
        |- -123
        |- -123asdf
        |- -123 asdf
      """.stripMargin
    ) shouldBe List( 0x123, 123, "123asdf", "123 asdf", -0x123, -123, "-123asdf", "-123 asdf" )
  }

}

