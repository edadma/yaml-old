//@
package xyz.hyperreal.yaml

import java.time.{LocalDate, ZonedDateTime}

import org.scalatest._
import prop.PropertyChecks


class Tests extends FreeSpec with PropertyChecks with Matchers {

  "date" in {
    read(
      """
        |- 2018-06-11
        |- 2001-12-15T02:59:43.1Z
        |- 2001-12-14t21:59:43.10-05:00
        |- 2018-06-11 asdf
        |- 2001-12-15T02:59:43.1Z asdf
        |- 2001-12-14t21:59:43.10-05:00 asdf
      """.stripMargin
    ) shouldBe List( LocalDate.parse("2018-06-11"), ZonedDateTime.parse("2001-12-15T02:59:43.1Z"), ZonedDateTime.parse("2001-12-14t21:59:43.10-05:00"), "2018-06-11 asdf", "2001-12-15T02:59:43.1Z asdf", "2001-12-14t21:59:43.10-05:00 asdf" )
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
