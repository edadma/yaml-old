//@
package xyz.hyperreal.yaml

import java.time.{LocalDate, LocalTime, ZonedDateTime}

import util.parsing.input._
import util.parsing.combinator.PackratParsers
import util.parsing.combinator.syntactical.StandardTokenParsers
import util.parsing.input.CharArrayReader.EofCh
import xyz.hyperreal.indentation_lexical._


object YamlLexical {
  val INTERPOLATION_REGEX = """\$(?:([a-zA-Z_]+\d*)|\{([^}]+)\}|\$)"""r
  val INTERPOLATED_REGEX = """[\ue000-\ue002]([^\ue000-\ue002]+)"""r
  val INTERPOLATION_DELIMITER = '\ue000'
  val INTERPOLATION_LITERAL = '\ue000'
  val INTERPOLATION_VARIABLE = '\ue001'
  val INTERPOLATION_EXPRESSION = '\ue002'

  val FLOAT_REGEX = """([-+]?(?:\d+)?\.\d+(?:[Ee][-+]?\d+)?|[-+]?\d+\.\d+[Ee][-+]?\d+|[-+]?\.inf|\.NaN)"""r
  val DEC_REGEX = """([-+]?(?:0|[123456789]\d*))"""r
  val HEX_REGEX = """([-+]?0[xX](?:\d|[abcdefABCDEF])+)"""r
  val OCT_REGEX = """([-+]?0[oO][01234567]+)"""r
  val DATE_REGEX = """(\d+-\d\d-\d\d)"""r
  val TIMESTAMP_REGEX = """(\d+-\d\d-\d\d[Tt]\d\d:\d\d:\d\d(?:\.\d+)?(?:Z|[+-]\d\d:\d\d))"""r
  val TIME_REGEX = """([012]\d:[012345]\d:[012345]\d(?:\.\d+)?)"""r
}

class YamlLexical extends IndentationLexical(false, true, List("{", "["), List("}", "]"), "#", "/*", "*/") {

  import YamlLexical._

  override def token: Parser[Token] = anchorToken | aliasToken | scalarToken | super.token

  override def identChar = letter | elem('_') // | elem('$')

  override def whitespace: Parser[Any] = rep[Any](
    whitespaceChar
      | '/' ~ '*' ~ comment
      | '#' ~ rep(chrExcept(EofCh, '\n'))
      | '/' ~ '*' ~ failure("unclosed comment")
  )

  case class DecLit( chars: String ) extends Token
  case class HexLit( chars: String ) extends Token
  case class OctLit( chars: String ) extends Token
  case class DateLit( chars: String ) extends Token
  case class TimestampLit( chars: String ) extends Token
  case class TimeLit( chars: String ) extends Token
  case class Anchor( chars: String ) extends Token
  case class Alias( chars: String ) extends Token

  private def anchorToken: Parser[Token] =
    '&' ~> rep1(elem("anchor", c => c.isLetterOrDigit)) ^^ (l => Anchor( l.mkString ))

  private def aliasToken: Parser[Token] =
    '*' ~> rep1(elem("anchor", c => c.isLetterOrDigit)) ^^ (l => Alias( l.mkString ))

  private def scalarToken: Parser[Token] =
//    ('\'' ~ '\'' ~ '\'') ~> rep(guard(not('\'' ~ '\'' ~ '\'')) ~> elem("", ch => true)) <~ ('\'' ~ '\'' ~ '\'') ^^
//      {l => StringLit( l mkString )} |
//    ('"' ~ '"' ~ '"') ~> rep(guard(not('"' ~ '"' ~ '"')) ~> elem("", ch => true)) <~ ('"' ~ '"' ~ '"') ^^
//      {l => StringLit( interpolate(l mkString, false) )} |
    '\'' ~> rep(
      ('\'' ~ '\'' ^^^ "''") |
        (guard(not('\'')) ~> (('\\' ~ '\'' ^^^ "\\'") | elem("", ch => true)))) <~ '\'' ^^
      {l => StringLit( escape(l mkString) )} |
    '"' ~> rep(guard(not('"')) ~> (('\\' ~ '"' ^^^ "\\\"") | elem("", ch => true))) <~ '"' ^^
      {l => StringLit( interpolate(l mkString, true) )} |
    text ^^ { l =>
      l.mkString.trim match {
        case FLOAT_REGEX( n ) => NumericLit( n )
        case DEC_REGEX( n ) => DecLit( n )
        case HEX_REGEX( n ) => HexLit( n )
        case OCT_REGEX( n ) => OctLit( n )
        case DATE_REGEX( d ) => DateLit( d )
        case TIMESTAMP_REGEX( d ) => TimestampLit( d )
        case TIME_REGEX( t ) => TimeLit( t )
        case s => StringLit( s )
      }
    }

  private def text: Parser[List[Elem]] =
    guard(
      not(
        elem('{') |
          '[' |
          '-' ~ '-' ~ '-' |
          '.' ~ '.' ~ '.' |
          (elem('&') | '*') ~ elem("letterordigit", _.isLetterOrDigit)
      )) ~> rep1(guard(not(elem(']') | '}' | ',' ~ ' ' | ',' ~ '\n' | ':' ~ ' ' | ':' ~ '\n' | ':' ~ '#' | '-' ~ ' ' | '-' ~ '\n' | '-' ~ '#' | '\n')) ~> elem("", ch => true))

  private def escape( s: String) = {
    val buf = new StringBuilder

    def chr( r: Reader[Char] ) {
      if (!r.atEnd) {
        if (r.first == '\\') {
          if (r.rest.atEnd)
            sys.error( "unexpected end of string" )//todo: nicer error reporting; not easy - will have to return a special "error" object

          if (r.rest.first == 'u') {
            var u = r.rest.rest

            def nextc =
              if (u.atEnd)
                sys.error( "unexpected end of string inside unicode sequence" )
              else {
                val res = u.first

                u = u.rest
                res
              }

            buf append Integer.valueOf( new String(Array(nextc, nextc, nextc, nextc)), 16 ).toChar
            chr( u )
          } else if (r.rest.first == 'x') {
            var u = r.rest.rest

            def nextc =
              if (u.atEnd)
                sys.error( "unexpected end of string inside hex sequence" )
              else {
                val res = u.first

                u = u.rest
                res
              }

            buf append Integer.valueOf( new String(Array(nextc, nextc)), 16 ).toChar
            chr( u )
          } else {
            r.rest.first match {
              case '\\' => buf += '\\'
              case '\'' => buf += '\''
              case '"' => buf += '"'
              case '$' => buf += '$'
              case '/' => buf += '/'
              case 'b' => buf += '\b'
              case 'f' => buf += '\f'
              case 'n' => buf += '\n'
              case 'r' => buf += '\r'
              case 't' => buf += '\t'
              case c => buf ++= s"\\$c"
            }

            chr( r.rest.rest )
          }
        } else if (r.first == '\'' && !r.rest.atEnd && r.rest.first == '\'') {
          buf append '\''
          chr( r.rest.rest )
        } else {
          buf append r.first
          chr( r.rest )
        }
      }
    }

    chr( new CharSequenceReader(s) )
    buf.toString()
  }

  private def interpolate( s: String, handleEscape: Boolean ): String = {
    val buf = new StringBuilder
    var last = 0
    var nonliteral = false

    def append( code: Char, s: String ) {
      buf += code
      buf append s
    }

    def literal( s: String ) = append( INTERPOLATION_LITERAL, if (handleEscape) escape(s) else s )

    for (m <- INTERPOLATION_REGEX.findAllMatchIn( s )) {
      if (m.start > last)
        literal( s.substring(last, m.start) )

      m.matched.charAt( 1 ) match {
        case '$' => literal( "$" )
        case '{' => append(INTERPOLATION_EXPRESSION, m.group(2))
        case _ => append(INTERPOLATION_VARIABLE, m.group(1))
      }

      nonliteral = true
      last = m.end
    }

    if (last < s.length)
      literal( s.substring(last) )

    if (!nonliteral)
      buf.deleteCharAt( 0 )

    buf.toString
  }

  reserved += (
    "true", "false"
  )

  delimiters += (
    "{", "[", ",", "]", "}",
    ":", "-", ": ", "- ", "--- ", "---", "..."
  )
}

class YamlParser extends StandardTokenParsers with PackratParsers {

  override val lexical = new YamlLexical

  def parse( r: Reader[Char] ): AST =
    phrase(yaml)(lexical.read(r)) match {
      case Success(tree, _) => tree
      case NoSuccess(error, rest) => problem( rest.pos, error )
    }

  def parse( src: String ): AST = parse( new CharSequenceReader(src) )

  def parse( src: io.Source ): AST = parse( new PagedSeqReader(PagedSeq.fromSource(src)) )

  import lexical.{Newline, Indent, Dedent, DecLit, HexLit, OctLit, DateLit, TimestampLit, TimeLit, Anchor, Alias}

  lazy val decLit: PackratParser[Number] =
    elem("dec literal", _.isInstanceOf[DecLit]) ^^ (_.chars.toInt.asInstanceOf[Number])

  lazy val hexLit: PackratParser[Number] =
    elem("hex literal", _.isInstanceOf[HexLit]) ^^ { n =>
      val (offset, sign) =
        n.chars.charAt(0) match {
          case '-' => (3, -1)
          case '+' => (3, 1)
          case _ => (2, 1)
        }

      (Integer.parseInt( n.chars.substring(offset), 16 )*sign).asInstanceOf[Number]
    }

  lazy val octLit: PackratParser[Number] =
    elem("octal literal", _.isInstanceOf[OctLit]) ^^ { n =>
      val (offset, sign) =
        n.chars.charAt(0) match {
          case '-' => (3, -1)
          case '+' => (3, 1)
          case _ => (2, 1)
        }

      (Integer.parseInt( n.chars.substring(offset), 8 )*sign).asInstanceOf[Number]
    }

  lazy val dateLit: PackratParser[LocalDate] =
    elem("date literal", _.isInstanceOf[DateLit]) ^^ (d => LocalDate.parse(d.chars))

  lazy val timestampLit: PackratParser[ZonedDateTime] =
    elem("timestamp literal", _.isInstanceOf[TimestampLit]) ^^ (d => ZonedDateTime.parse(d.chars))

  lazy val timeLit: PackratParser[LocalTime] =
    elem("time literal", _.isInstanceOf[TimeLit]) ^^ (t => LocalTime.parse(t.chars))

  lazy val anchor: PackratParser[String] =
    elem("anchor", _.isInstanceOf[Anchor]) ^^ (_.chars)

  lazy val alias: PackratParser[String] =
    elem("alias", _.isInstanceOf[Alias]) ^^ (_.chars)

  lazy val pos: PackratParser[Position] = positioned( success(new Positional{}) ) ^^ { _.pos }

  lazy val nl: PackratParser[_] = rep1(Newline)

  lazy val onl: PackratParser[_] = rep(Newline)

  lazy val dashes: PackratParser[_] =
    "--- " | "---" ~ guard(Newline)

  lazy val yaml: PackratParser[AST] =
    onl ~> rep1(opt(dashes) ~> onl ~> document <~ opt("...") <~ onl) ^^ SourceAST

  lazy val document: PackratParser[ValueAST] =
    container | flowValue

  lazy val container: PackratParser[ContainerAST] =
    map | list

  lazy val colon: PackratParser[_] =
    ": " | (":" ~ guard(Indent | Newline))

  lazy val dash: PackratParser[_] =
    "- " | ("-" ~ guard(Indent | Newline))

  lazy val map: PackratParser[ContainerAST] =
    opt(anchor) ~ rep1(pair <~ nl) ^^ {
      case a ~ m => MapAST( a, m )
    }

  lazy val pair: PackratParser[PairAST] =
    opt(primitive) ~ colon ~ opt(value) ^^ {
      case k ~ _ ~ v => PairAST( ornull(k), ornull(v) ) }

  lazy val value: PackratParser[ValueAST] =
    primitive | Indent ~> container <~ Dedent | flowContainer

  def ornull( a: Option[ValueAST] ) =
    a match {
      case None => NullAST( None )
      case Some( v ) => v
    }

  lazy val list: PackratParser[ContainerAST] =
    opt(anchor) ~ rep1(dash ~> opt(listValue) <~ nl) ^^ {
      case a ~ l => ListAST( a, l map ornull )
    }

  val listValue: PackratParser[ValueAST] =
    pair ~ (Indent ~> map <~ Dedent) ^^ {
      case p ~ MapAST( _, ps ) => MapAST( None, p :: ps ) } |
    pair ^^ (p => MapAST( None, List(p) )) |
    value

  lazy val flowContainer: PackratParser[ContainerAST] =
    flowMap | flowList

  lazy val flowMap: PackratParser[ContainerAST] =
    opt(anchor) ~ ("{" ~> repsep(flowPair, ",") <~ "}") ^^ {
      case a ~ l => MapAST( a, l )
    }

  lazy val flowPair: PackratParser[PairAST] =
    opt(flowValue) ~ colon ~ opt(flowValue) ^^ {
      case k ~ _ ~ v => PairAST( ornull(k), ornull(v) )
    }

  lazy val flowList: PackratParser[ContainerAST] =
    opt(anchor) ~ ("[" ~> repsep(opt(flowValue), ",") <~ "]") ^^ {
      case a ~ l => ListAST( a, l map ornull )
    }

  lazy val flowValue: PackratParser[ValueAST] =
    primitive | flowContainer

  lazy val primitive: PackratParser[PrimitiveAST] =
    opt(anchor) <~ "true" ^^ { a => BooleanAST( a, true ) } |
    opt(anchor) <~ "false" ^^ { a => BooleanAST( a, false ) } |
    opt(anchor) ~ stringLit ^^ { case a ~ s => StringAST( a, s ) } |
    opt(anchor) ~ decLit ^^ { case a ~ n => NumberAST( a, n ) } |
    opt(anchor) ~ hexLit ^^ { case a ~ n => NumberAST( a, n ) } |
    opt(anchor) ~ octLit ^^ { case a ~ n => NumberAST( a, n ) } |
    opt(anchor) ~ numericLit ^^ {
      case a ~ (".inf"|"+.inf") => NumberAST( a, Double.PositiveInfinity )
      case a ~ "-.inf" => NumberAST( a, Double.NegativeInfinity )
      case a ~ ".NaN" => NumberAST( a, Double.NaN )
      case a ~ n => NumberAST( a, n.toDouble ) } |
    opt(anchor) ~ dateLit ^^ { case a ~ d => DateAST( a, d ) } |
    opt(anchor) ~ timestampLit ^^ { case a ~ t => TimestampAST( a, t ) } |
    opt(anchor) ~ timeLit ^^ { case a ~ t => TimeAST( a, t ) } |
    pos ~ alias ^^ {
      case p ~ a => AliasAST( p, a ) }

}