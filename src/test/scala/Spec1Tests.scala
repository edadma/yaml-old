package xyz.hyperreal.yaml

import java.time.LocalTime

import org.scalatest._
import prop.PropertyChecks


// http://yaml.org/spec/1.0/
class Spec1Tests extends FreeSpec with PropertyChecks with Matchers {

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

}