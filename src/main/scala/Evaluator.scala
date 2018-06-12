package xyz.hyperreal.yaml


class Evaluator {

  def eval( ast: AST ): Any =
    ast match {
      case p: PrimitiveAST => p.v
      case MapAST( pairs ) => pairs map {case PairAST(k, v) => (eval(k), eval(v))} toMap
      case ListAST( elements ) => elements map eval
    }

}