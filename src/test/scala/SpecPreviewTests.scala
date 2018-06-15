package xyz.hyperreal.yaml

import java.time.{LocalDate, LocalTime, ZonedDateTime}

import org.scalatest._
import prop.PropertyChecks


// http://yaml.org/spec/1.2/spec.html
class SpecPreviewTests extends FreeSpec with PropertyChecks with Matchers {

	"Example 2.1.  Sequence of Scalars" in {
    read(
      """
        |- Mark McGwire
        |- Sammy Sosa
        |- Ken Griffey
      """.stripMargin
    ).head shouldBe List( "Mark McGwire", "Sammy Sosa", "Ken Griffey" )
	}

  "Example 2.2.  Mapping of Scalars to Scalars" in {
    read(
      """
        |hr:  65
        |avg: 0.278
        |rbi: 147
      """.stripMargin
    ).head shouldBe Map( "hr" -> 65, "avg" -> 0.278, "rbi" -> 147 )
  }

  "Example 2.3.  Mapping of Scalars to Sequences" in {
    read(
      """
        |american:
        |  - Boston Red Sox
        |  - Detroit Tigers
        |  - New York Yankees
        |national:
        |  - New York Mets
        |  - Chicago Cubs
        |  - Atlanta Braves
      """.stripMargin
    ).head shouldBe Map( "american" -> List( "Boston Red Sox", "Detroit Tigers", "New York Yankees" ), "national" -> List( "New York Mets", "Chicago Cubs", "Atlanta Braves" ) )
  }

  "Example 2.4.  Sequence of Mappings" in {
    read(
      """
        |-
        |  name: Mark McGwire
        |  hr:  65
        |  avg: 0.278
        |-
        |  name: Sammy Sosa
        |  hr:  63
        |  avg: 0.288
      """.stripMargin
    ).head shouldBe List( Map("name" -> "Mark McGwire", "hr" -> 65, "avg" -> 0.278), Map("name" -> "Sammy Sosa", "hr" -> 63, "avg" -> 0.288) )
  }

  "Example 2.5. Sequence of Sequences" in {
    read(
      """
        |- [name        , hr, avg  ]
        |- [Mark McGwire, 65, 0.278]
        |- [Sammy Sosa  , 63, 0.288]
      """.stripMargin
    ).head shouldBe List( List("name", "hr", "avg"), List("Mark McGwire", 65, 0.278), List("Sammy Sosa", 63, 0.288) )
  }

  "Example 2.6. Mapping of Mappings" in {
    read(
      """
        |Mark McGwire: {hr: 65, avg: 0.278}
        |Sammy Sosa: {
        |    hr: 63,
        |    avg: 0.288
        |  }
      """.stripMargin
    ).head shouldBe Map( "Mark McGwire" -> Map("hr" -> 65, "avg" -> 0.278), "Sammy Sosa" -> Map("hr" -> 63, "avg" -> 0.288) )
  }

  "Example 2.7.  Two Documents in a Stream" in {
    read(
      """
        |# Ranking of 1998 home runs
        |---
        |- Mark McGwire
        |- Sammy Sosa
        |- Ken Griffey
        |
        |# Team ranking
        |---
        |- Chicago Cubs
        |- St Louis Cardinals
      """.stripMargin
    ) shouldBe List( List("Mark McGwire", "Sammy Sosa", "Ken Griffey"), List("Chicago Cubs", "St Louis Cardinals") )
  }

  "Example 2.8.  Play by Play Feed from a Game" in {
    read(
      """
        |---
        |time: 20:03:20
        |player: Sammy Sosa
        |action: strike (miss)
        |...
        |---
        |time: 20:03:47
        |player: Sammy Sosa
        |action: grand slam
        |...
      """.stripMargin
    ) shouldBe List( Map("time" -> LocalTime.parse("20:03:20"), "player" -> "Sammy Sosa", "action" -> "strike (miss)"), Map("time" -> LocalTime.parse("20:03:47"), "player" -> "Sammy Sosa", "action" -> "grand slam") )
  }

  "Example 2.9.  Single Document with Two Comments" in {
    read(
      """
        |---
        |hr: # 1998 hr ranking
        |  - Mark McGwire
        |  - Sammy Sosa
        |rbi:
        |  # 1998 rbi ranking
        |  - Sammy Sosa
        |  - Ken Griffey
      """.stripMargin
    ).head shouldBe Map( "hr" -> List("Mark McGwire", "Sammy Sosa"), "rbi" -> List("Sammy Sosa", "Ken Griffey") )
  }

  """Example 2.10.  Node for “Sammy Sosa” appears twice in this document""" in {
    read(
      """
        |---
        |hr:
        |  - Mark McGwire
        |  # Following node labeled SS
        |  - &SS Sammy Sosa
        |rbi:
        |  - *SS # Subsequent occurrence
        |  - Ken Griffey
      """.stripMargin
    ).head shouldBe Map( "hr" -> List("Mark McGwire", "Sammy Sosa"), "rbi" -> List("Sammy Sosa", "Ken Griffey") )
  }

  "Example 2.11. Mapping between Sequences" in {
    read(
      """
        |? - Detroit Tigers
        |  - Chicago cubs
        |:
        |  - 2001-07-23
        |
        |? [ New York Yankees,
        |    Atlanta Braves ]
        |: [ 2001-07-02, 2001-08-12,
        |    2001-08-14 ]
      """.stripMargin
    ).head shouldBe
      Map(
        List("Detroit Tigers", "Chicago cubs") ->
          List(LocalDate.parse("2001-07-23")),
        List("New York Yankees", "Atlanta Braves") ->
          List(LocalDate.parse("2001-07-02"), LocalDate.parse("2001-08-12"),
            LocalDate.parse("2001-08-14")))
  }

  "Example 2.12. Compact Nested Mapping" in {
    read(
      """
        |---
        |# Products purchased
        |- item    : Super Hoop
        |  quantity: 1
        |- item    : Basketball
        |  quantity: 4
        |- item    : Big Shoes
        |  quantity: 1
      """.stripMargin
    ).head shouldBe List( Map("item" -> "Super Hoop", "quantity" -> 1), Map("item" -> "Basketball", "quantity" -> 4), Map("item" -> "Big Shoes", "quantity" -> 1) )
  }

  "Example 2.13.  In literals, newlines are preserved" in {
    read(
      """
        |# ASCII Art
        |--- |
        |  \//||\/||
        |  // ||  ||__
      """.stripMargin
    ).head shouldBe
      """|\//||\/||
         |// ||  ||__
         |""".stripMargin
  }

  "Example 2.14.  In the folded scalars, newlines become spaces" in {
    read(
      """
        |--- >
        |  Mark McGwire's
        |  year was crippled
        |  by a knee injury.
      """.stripMargin
    ).head shouldBe "Mark McGwire's year was crippled by a knee injury.\n"
  }

  "Example 2.16.  Indentation determines scope" in {
    read(
      """
        |name: Mark McGwire
        |accomplishment: >
        |  Mark set a major league
        |  home run record in 1998.
        |stats: |
        |  65 Home Runs
        |  0.278 Batting Average
      """.stripMargin
    ).head shouldBe
      Map(
        "name" -> "Mark McGwire",
        "accomplishment" -> "Mark set a major league home run record in 1998.\n",
        "stats" -> "65 Home Runs\n0.278 Batting Average\n"
      )
  }

  "Example 2.17. Quoted Scalars" in {
    read(
      """
        |unicode: "Sosa did fine.\u005Cu263A"
        |control: "\b1998\t1999\t2000\n"
        |hex esc: "\x0d\x0a is \r\n"
        |
        |single: '"Howdy!" he cried.'
        |quoted: ' # Not a ''comment''.'
        |tie-fighter: '|\-*-/|'
      """.stripMargin
    ).head shouldBe Map(
      "unicode" -> "Sosa did fine.\u263A",
      "control" -> "\b1998\t1999\t2000\n",
      "hex esc" -> "\u000d\u000a is \r\n",
      "single" -> "\"Howdy!\" he cried.",
      "quoted" -> " # Not a 'comment'.",
      "tie-fighter" -> "|\\-*-/|"
    )
  }

  "Example 2.19. Integers" in {
    read(
      """
        |canonical: 12345
        |decimal: +12345
        |octal: 0o14
        |hexadecimal: 0xC
      """.stripMargin
    ).head shouldBe Map( "canonical" -> 12345, "decimal" -> 12345, "octal" -> 12, "hexadecimal" -> 12 )
  }

  "Example 2.20. Floating Point" in {
    read(
      """
        |canonical: 1.23015e+3
        |exponential: 12.3015e+02
        |fixed: 1230.15
        |negative infinity: -.inf
        |not a number: .NaN
      """.stripMargin
    ).head.asInstanceOf[Map[_, _]] map {
      case (k, v) => (k, if (v.asInstanceOf[Double].toString == "NaN") "NaN" else v)
    } shouldBe Map( "canonical" -> 1.23015e+3, "exponential" -> 12.3015e+02, "fixed" -> 1230.15, "negative infinity" -> Double.NegativeInfinity, "not a number" -> "NaN" )
  }

  "Example 2.21. Miscellaneous" in {
    read(
      """
        |null:
        |booleans: [ true, false ]
        |string: '012345'
      """.stripMargin
    ).head shouldBe Map( (null, null), "booleans" -> List(true, false), "string" -> "012345" )
  }

  "Example 2.22. Timestamps" in {
    read(
      """
        |canonical: 2001-12-15T02:59:43.1Z
        |iso8601: 2001-12-14t21:59:43.10-05:00
        |spaced: 2001-12-14 21:59:43.10 -5
        |date: 2002-12-14
      """.stripMargin
    ).head shouldBe
      Map(
        "canonical" -> ZonedDateTime.parse( "2001-12-15T02:59:43.1Z" ),
        "iso8601" -> ZonedDateTime.parse( "2001-12-14t21:59:43.10-05:00" ),
        "spaced" -> ZonedDateTime.parse( "2001-12-14T21:59:43.10-05:00" ),
        "date" -> LocalDate.parse( "2002-12-14" )
        )
  }

  "Example 2.27. Invoice" in {
    val bill_to =
      Map(
        "given" -> "Chris",
        "family" -> "Dumars",
        "address" ->
          Map(
            "lines" -> "458 Walkman Dr.\nSuite #292\n",
            "city" -> "Royal Oak",
            "state" -> "MI",
            "postal" -> 48046
          )
      )

    read(
      """
        |--- #!<tag:clarkevans.com,2002:invoice>
        |invoice: 34843
        |date   : 2001-01-23
        |bill-to: &id001
        |    given  : Chris
        |    family : Dumars
        |    address:
        |        lines: |
        |            458 Walkman Dr.
        |            Suite #292
        |        city    : Royal Oak
        |        state   : MI
        |        postal  : 48046
        |ship-to: *id001
        |product:
        |    - sku         : BL394D
        |      quantity    : 4
        |      description : Basketball
        |      price       : 450.00
        |    - sku         : BL4438H
        |      quantity    : 1
        |      description : Super Hoop
        |      price       : 2392.00
        |tax  : 251.42
        |total: 4443.52
        |comments:
        |    Late afternoon is best.
        |    Backup contact is Nancy
        |    Billsmer @ 338-4338.
      """.stripMargin
    ).head shouldBe
      Map(
        "invoice" -> 34843,
        "date" -> LocalDate.parse("2001-01-23"),
        "bill-to" -> bill_to,
        "ship-to" -> bill_to,
        "product" ->
          List(
            Map(
              "sku" -> "BL394D",
              "quantity" -> 4,
              "description" -> "Basketball",
              "price" -> 450.00
            ),
            Map(
              "sku" -> "BL4438H",
              "quantity" -> 1,
              "description" -> "Super Hoop",
              "price" -> 2392.00
            )
          ),
        "tax" -> 251.42,
        "total" -> 4443.52,
        "comments" -> "Late afternoon is best. Backup contact is Nancy Billsmer @ 338-4338."
      )
  }

  "Example 2.28. Log File" in {
    read(
      """
        |---
        |Time: 2001-11-23 15:01:42 -5
        |User: ed
        |Warning:
        |  This is an error message
        |  for the log file
        |---
        |Time: 2001-11-23 15:02:31 -5
        |User: ed
        |Warning:
        |  A slightly different error
        |  message.
        |---
        |Date: 2001-11-23 15:03:17 -5
        |User: ed
        |Fatal:
        |  Unknown variable "bar"
        |Stack:
        |  - file: TopClass.py
        |    line: 23
        |    code: |
        |      x = MoreObject("345\n")
        |  - file: MoreClass.py
        |    line: 58
        |    code: |-
        |      foo = bar
      """.stripMargin
    ) shouldBe
      List(
        Map(
          "Time" -> ZonedDateTime.parse( "2001-11-23T15:01:42-05:00" ),
          "User" -> "ed",
          "Warning" -> "This is an error message for the log file"
        ),
        Map(
          "Time" -> ZonedDateTime.parse( "2001-11-23T15:02:31-05:00" ),
          "User" -> "ed",
          "Warning" -> "A slightly different error message."
        ),
        Map(
          "Date" -> ZonedDateTime.parse( "2001-11-23T15:03:17-05:00" ),
          "User" -> "ed",
          "Fatal" -> "Unknown variable \"bar\"",
          "Stack" ->
            List(
              Map(
                "file" -> "TopClass.py",
                "line" -> 23,
                "code" -> "x = MoreObject(\"345\\n\")\n"
              ),
              Map(
                "file" -> "MoreClass.py",
                "line" -> 58,
                "code" -> "foo = bar"
              )
            )
        )
      )
  }

}