package xyz.hyperreal

import scala.util.parsing.input.Position


package object yaml {

  def problem( pos: Position, error: String ) =
    if (pos eq null)
      sys.error( error )
    else
      sys.error( pos.line + ": " + error + "\n" + pos.longString )

}