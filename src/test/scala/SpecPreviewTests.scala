package xyz.hyperreal.yaml

import java.time.{LocalDate, LocalTime}

import org.scalatest._
import prop.PropertyChecks


// http://yaml.org/spec/1.2/spec.html
class SpecPreviewTests extends FreeSpec with PropertyChecks with Matchers {

	"Sequence of scalars" in {
    read(
      """
        |- Mark McGwire
        |- Sammy Sosa
        |- Ken Griffey
      """.stripMargin
    ).head shouldBe List( "Mark McGwire", "Sammy Sosa", "Ken Griffey" )
	}

  "Mapping of scalars to scalars" in {
    read(
      """
        |hr:  65
        |avg: 0.278
        |rbi: 147
      """.stripMargin
    ).head shouldBe Map( "hr" -> 65, "avg" -> 0.278, "rbi" -> 147 )
  }

  "Mapping of scalars to sequences" in {
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

  "Sequence of mappings" in {
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

  "Sequence of sequences" in {
    read(
      """
        |- [name        , hr, avg  ]
        |- [Mark McGwire, 65, 0.278]
        |- [Sammy Sosa  , 63, 0.288]
      """.stripMargin
    ).head shouldBe List( List("name", "hr", "avg"), List("Mark McGwire", 65, 0.278), List("Sammy Sosa", 63, 0.288) )
  }

  "Mapping of mappings" in {
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

  "Two Documents in a Stream" in {
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

  "Play by Play Feed from a Game" in {
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

  "Single Document with Two Comments" in {
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

  """Node for “Sammy Sosa” appears twice in this document""" in {
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

  "Compact Nested Mapping" in {
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

  "In literals, newlines are preserved" in {
    read(
      """
        |# ASCII Art
        |--- |
        |  \//||\/||
        |  // ||  ||__
      """.stripMargin
    ).head shouldBe
      """
        |\//||\/||
        |// ||  ||__
      """.trim.stripMargin
  }

  "In the folded scalars, newlines become spaces" in {
    read(
      """
        |--- >
        |  Mark McGwire's
        |  year was crippled
        |  by a knee injury.
      """.stripMargin
    ).head shouldBe "Mark McGwire's year was crippled by a knee injury."
  }

  "Indentation determines scope" in {
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
        "accomplishment" -> "Mark set a major league home run record in 1998.",
        "stats" -> "65 Home Runs\n0.278 Batting Average"
      )
  }

  "Quoted Scalars" in {
    read(
      """
        |unicode: "Sosa did fine.\u263A"
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

  "Integers" in {
    read(
      """
        |canonical: 12345
        |decimal: +12345
        |octal: 0o14
        |hexadecimal: 0xC
      """.stripMargin
    ).head shouldBe Map( "canonical" -> 12345, "decimal" -> 12345, "octal" -> 12, "hexadecimal" -> 12 )
  }

  "Floating Point" in {
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

  "Miscellaneous" in {
    read(
      """
        |null:
        |booleans: [ true, false ]
        |string: '012345'
      """.stripMargin
    ).head shouldBe Map( (null, null), "booleans" -> List(true, false), "string" -> "012345" )
  }

  "Invoice" in {
    val bill_to =
      Map(
        "given" -> "Chris",
        "family" -> "Dumars",
        "address" ->
          Map(
            "lines" -> "458 Walkman Dr.\nSuite #292",
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

//  "Example 2.28. Log File" in {
//    read(
//      """
//        |---
//        |Time: 2001-11-23 15:01:42 -5
//        |User: ed
//        |Warning:
//        |  This is an error message
//        |  for the log file
//        |---
//        |Time: 2001-11-23 15:02:31 -5
//        |User: ed
//        |Warning:
//        |  A slightly different error
//        |  message.
//        |---
//        |Date: 2001-11-23 15:03:17 -5
//        |User: ed
//        |Fatal:
//        |  Unknown variable "bar"
//        |Stack:
//        |  - file: TopClass.py
//        |    line: 23
//        |    code: |
//        |      x = MoreObject("345\n")
//        |  - file: MoreClass.py
//        |    line: 58
//        |    code: |-
//        |      foo = bar
//      """.stripMargin
//    )
//  }

}