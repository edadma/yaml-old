package xyz.hyperreal.yaml

import scala.collection.mutable


class Evaluator {

  val anchors = new mutable.HashMap[String, Any]

  def reset: Unit = {
    anchors.clear
  }

  def eval( ast: AST ): Any =
    ast match {
      case SourceAST( documents ) =>
        for (d <- documents)
          yield {
            reset
            eval( d )
          }
      case p: PrimitiveAST => p.v
      case MapAST( pairs ) => pairs map {case PairAST(k, v) => (eval(k), eval(v))} toMap
      case ListAST( elements ) => elements map eval
    }

}