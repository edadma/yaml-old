package xyz.hyperreal

import scala.util.parsing.input.Position


package object yaml {

  def read( src: String ) = new Evaluator().eval( new YamlParser().parse(src) ).asInstanceOf[List[Any]]

  def read( src: io.Source ) = new Evaluator().eval( new YamlParser().parse(src) ).asInstanceOf[List[Any]]

  def problem( pos: Position, error: String ) =
    if (pos eq null)
      sys.error( error )
    else
      sys.error( pos.line + ": " + error + "\n" + pos.longString )

}