package xyz.hyperreal.yaml


class Evaluator {

  def eval( ast: AST ): Any =
    ast match {
      case BooleanAST( b ) => b
      case StringAST( s ) => s
      case NumberAST( n ) => n
      case NullAST => null
      case MapAST( pairs ) => Map( pairs map {case PairAST(k, v) => (eval(k), eval(v))}: _* )
      case ListAST( elements ) => List( elements map eval: _* )
    }

}