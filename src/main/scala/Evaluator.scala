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
      case AliasAST( pos, name ) =>
        anchors get name match {
          case None => problem( pos, s"anchor not found: $name" )
          case Some( v ) => v
        }
      case p: PrimitiveAST =>
        if (p.anchor isDefined)
          anchors(p.anchor.get) = p.v

        p.v
      case MapAST( anchor, pairs ) =>
        val map = pairs map {case (k, v) => (eval(k), eval(v))} toMap

        if (anchor isDefined)
          anchors(anchor get) = map

        map
      case ListAST( anchor, elements ) =>
        val list = elements map eval

        if (anchor isDefined)
          anchors(anchor get) = list

        list
    }

}