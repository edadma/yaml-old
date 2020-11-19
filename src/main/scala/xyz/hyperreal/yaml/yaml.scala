package xyz.hyperreal

import scala.util.parsing.input.Position

package object yaml {

  def read(src: String, options: String*): Seq[Any] =
    new Evaluator(options)
      .eval(new YamlParser().parse(src))
      .asInstanceOf[List[Any]]

  def read(src: io.Source, options: String*): Seq[Any] =
    new Evaluator(options)
      .eval(new YamlParser().parse(src))
      .asInstanceOf[List[Any]]

  private[yaml] def problem(pos: Position, error: String): Nothing =
    if (pos eq null)
      sys.error(error)
    else
      sys.error(s"${pos.line}: $error\n${pos.longString}")

}
