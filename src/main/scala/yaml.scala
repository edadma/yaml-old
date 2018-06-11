package xyz.hyperreal

import scala.util.parsing.input.Position


package object yaml {

  def read( src: String ): Any = new Evaluator().eval( new YamlParser().parse(src) )

  def read( src: io.Source ): Any = new Evaluator().eval( new YamlParser().parse(src) )

  def problem( pos: Position, error: String ) =
    if (pos eq null)
      sys.error( error )
    else
      sys.error( pos.line + ": " + error + "\n" + pos.longString )

}