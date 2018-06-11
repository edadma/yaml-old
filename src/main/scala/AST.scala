package xyz.hyperreal.yaml


trait AST

trait PrimitiveAST extends AST
case class BooleanAST( b: Boolean ) extends PrimitiveAST
case class StringAST( s: String ) extends PrimitiveAST
case class NumberAST( n: Number ) extends PrimitiveAST
case object NullAST extends PrimitiveAST

trait ContainerAST extends AST
case class MapAST( pairs: List[PairAST] ) extends ContainerAST
case class ListAST( elements: List[AST] ) extends ContainerAST

case class PairAST( key: AST, value: AST ) extends AST
