package xyz.hyperreal.yaml

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
    ) shouldBe List( "Mark McGwire", "Sammy Sosa", "Ken Griffey" )
	}

  "Mapping of scalars to scalars" in {
    read(
      """
        |hr:  65
        |avg: 0.278
        |rbi: 147
      """.stripMargin
    ) shouldBe Map( "hr" -> 65, "avg" -> 0.278, "rbi" -> 147 )
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
    ) shouldBe Map( "american" -> List( "Boston Red Sox", "Detroit Tigers", "New York Yankees" ), "national" -> List( "New York Mets", "Chicago Cubs", "Atlanta Braves" ) )
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
    ) shouldBe List( Map( "name" -> "Mark McGwire", "hr" -> 65, "avg" -> 0.278 ), Map( "name" -> "Sammy Sosa", "hr" -> 63, "avg" -> 0.288 ) )
  }

}