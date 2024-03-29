//@
package xyz.hyperreal.yaml

import java.time.{LocalDate, LocalTime, ZonedDateTime}

import org.scalatest._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks


class Tests extends FreeSpec with ScalaCheckPropertyChecks with Matchers {

  "datetime" in {
    read(
      """
        |- 2018-06-11
        |- 2001-12-15T02:59:43.1Z
        |- 2001-12-14t21:59:43.10-05:00
        |- 21:59:43.10
        |- 2018-06-11 asdf
        |- 2001-12-15T02:59:43.1Z asdf
        |- 2001-12-14t21:59:43.10-05:00 asdf
        |- 21:59:43.10 asdf
      """.stripMargin
    ).head shouldBe List( LocalDate.parse("2018-06-11"), ZonedDateTime.parse("2001-12-15T02:59:43.1Z"), ZonedDateTime.parse("2001-12-14t21:59:43.10-05:00"), LocalTime.parse("21:59:43.10"), "2018-06-11 asdf", "2001-12-15T02:59:43.1Z asdf", "2001-12-14t21:59:43.10-05:00 asdf", "21:59:43.10 asdf" )
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
        |- -123
        |- -123asdf
        |- -123 asdf
      """.stripMargin
    ).head shouldBe List( 123.4, 0x123, 123, "123asdf", "123 asdf", -123.4, -123, "-123asdf", "-123 asdf" )
  }

}
