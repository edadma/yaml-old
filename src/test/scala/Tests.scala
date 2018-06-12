//@
package xyz.hyperreal.yaml

import java.time.LocalDate

import org.scalatest._
import prop.PropertyChecks


class Tests extends FreeSpec with PropertyChecks with Matchers {

  "date" in {
    read(
      """
        |- 2018-06-11
      """.stripMargin
    ) shouldBe List( LocalDate.parse("2018-06-11") )
  }

  "numeric" in {
    read(
      """
        |- 123.4
        |- 0x123
        |- 123
        |- 123asdf
        |- 123 asdf
        |- -123.4
        |- -0x123
        |- -123
        |- -123asdf
        |- -123 asdf
      """.stripMargin
    ) shouldBe List( 123.4, 0x123, 123, "123asdf", "123 asdf", -123.4, -0x123, -123, "-123asdf", "-123 asdf" )
  }

}

