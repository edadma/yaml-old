//@
package xyz.hyperreal.yaml

import org.scalatest._
import prop.PropertyChecks


class Tests extends FreeSpec with PropertyChecks with Matchers {

  "numeric" in {
    read(
      """
        |- 123
        |- 123asdf
        |- 123 asdf
      """.stripMargin
    ) shouldBe List( 123, "123asdf", "123 asdf" )
  }

}

