package xyz.hyperreal.yaml

import java.time.{LocalDate, ZonedDateTime}


trait AST

trait PrimitiveAST extends AST {
  val v: Any
}

case class BooleanAST( v: Boolean ) extends PrimitiveAST
case class StringAST( v: String ) extends PrimitiveAST
case class NumberAST( v: Number ) extends PrimitiveAST
case object NullAST extends PrimitiveAST { val v = null }
case class DateAST( v: LocalDate ) extends PrimitiveAST
case class TimestampAST( v: ZonedDateTime ) extends PrimitiveAST

trait ContainerAST extends AST
case class MapAST( pairs: List[PairAST] ) extends ContainerAST
case class ListAST( elements: List[AST] ) extends ContainerAST

case class PairAST( key: AST, value: AST ) extends AST
