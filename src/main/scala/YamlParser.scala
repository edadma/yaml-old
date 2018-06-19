//@
package xyz.hyperreal.yaml

import java.time.format.DateTimeFormatter
import java.time._

import util.parsing.input._
import util.parsing.combinator.PackratParsers
import util.parsing.combinator.syntactical.StandardTokenParsers
import util.parsing.input.CharArrayReader.EofCh
import xyz.hyperreal.indentation_lexical._


object YamlLexical {
  val NEWLINE_REGEX = """[ \t]*\n[ \t]*"""r
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

  case class TextLit( chars: String ) extends Token
  case class Anchor( chars: String ) extends Token
  case class Alias( chars: String ) extends Token

  private def anchorToken: Parser[Token] =
    '&' ~> rep1(elem("anchor", c => c.isLetterOrDigit)) ^^ (l => Anchor( l.mkString ))

  private def aliasToken: Parser[Token] =
    '*' ~> rep1(elem("anchor", c => c.isLetterOrDigit)) ^^ (l => Alias( l.mkString ))

  private def scalarToken: Parser[Token] =
    '\'' ~> rep(
      ('\'' ~ '\'' ^^^ "''") |
        (guard(not('\'')) ~> elem("", ch => true))) <~ '\'' ^^
      {l => StringLit( quoteEscape(newlines(l mkString)) )} |
    '"' ~> rep(guard(not('"')) ~> (('\\' ~ '"' ^^^ "\\\"") | elem("", ch => true))) <~ '"' ^^
      {l => StringLit( escape(newlines(l mkString)) )} |
    text ^^ (l => TextLit( l.mkString.trim ))

  private def quoteEscape( s: String ) = s.replace( "''", "'" )

  private def newlines( s: String ) = NEWLINE_REGEX.replaceAllIn( s, " " )

  private def text: Parser[List[Elem]] =
    guard(
      not(
        elem('{') |
          '!' |
          '|' |
          '>' |
          '[' |
          '?'|
          '-' ~ '-' ~ '-' |
          '.' ~ '.' ~ '.'
      )) ~>
      rep1(guard(not(
        elem(']') |
          '}' |
          ',' ~ ' ' |
          ',' ~ '\n' |
          ':' ~ ' ' |
          ':' ~ '\n' |
          '-' ~ ' ' |
          '-' ~ '\n' |
//          ' ' ~ '#' |
          '\n')) ~> elem("", ch => true))

  private def escape( s: String ) = {
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
              case 'a' => buf += '\u0007'
              case 'e' => buf += '\u001b'
              case 'b' => buf += '\b'
              case 'f' => buf += '\f'
              case 'n' => buf += '\n'
              case 'r' => buf += '\r'
              case 't' => buf += '\t'
              case 'v' => buf += '\u000b'
              case 'N' => buf += '\u0085'
              case 'L' => buf += '\u2028'
              case 'P' => buf += '\u2029'
              case '\\' => buf += '\\'
              case '_' => buf += '\u00A0'
              case '"' => buf += '"'
              case '/' => buf += '/'
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

  delimiters += (
    "{", "[", ",", "]", "}", "|", ">", "|-", ">-", "!", "!!",
    ":", "-", ": ", "- ", "? ", "?", "--- ", "---", "..."
  )
}

object YamlParser {
  val FLOAT_REGEX = """([-+]?(?:\d+)?\.\d+(?:[Ee][-+]?\d+)?|[-+]?\d+\.\d+[Ee][-+]?\d+|[-+]?\.inf|\.NaN)"""r
  val DEC_REGEX = """([-+]?(?:0|[123456789]\d*))"""r
  val HEX_REGEX = """(0x(?:\d|[abcdefABCDEF])+)"""r
  val OCT_REGEX = """(0o[01234567]+)"""r
  val DATE_REGEX = """(\d+-\d\d-\d\d)"""r
  val TIMESTAMP_REGEX = """(\d+-\d\d-\d\d[Tt]\d\d:\d\d:\d\d(?:\.\d*)?(?:Z|[+-]\d\d:\d\d))"""r
  val TIME_REGEX = """([012]\d:[012345]\d:[012345]\d(?:\.\d+)?)"""r
  val SPACED_DATETIME_REGEX = """(\d+-\d\d-\d\d\s+\d\d:\d\d:\d\d(?:\.\d*)?)"""r
  val SPACED_TIMESTAMP_REGEX = """(\d+-\d\d-\d\d\s+\d\d:\d\d:\d\d(?:\.\d*)?)\s+(Z|[+-]\d(?:\d(?::?\d\d(?::?\d\d)?)?)?)"""r
  val INT_REGEX = """([+-]\d+)"""r
  val SPACED_FORMATTER = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss[.SS]" )
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

  import lexical.{Newline, Indent, Dedent, TextLit, Anchor, Alias}

  lazy val textLit: PackratParser[String] =
    elem("text literal", _.isInstanceOf[TextLit]) ^^ (_.chars)

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
    pairs ^^ (p => MapAST( None, p )) |
    listValues ^^ (l => ListAST( None, l )) |
    flowValue |
    multiline

  lazy val colon: PackratParser[_] =
    ": " | ":" //~ guard(Indent | Newline))

  lazy val dash: PackratParser[_] =
    "- " | "-"

  lazy val question: PackratParser[_] =
    "? " | "?"

  lazy val pairs: PackratParser[List[(ValueAST, ValueAST)]] =
    rep1(pair <~ nl)

  lazy val pair: PackratParser[(ValueAST, ValueAST)] =
    flowValue ~ colon ~ opt(value) ^^ {
      case k ~ _ ~ v => (k, ornull(v)) } |
    complexKey ~ opt(nl ~ colon ~ opt(value)) ^^ {
      case k ~ (None|Some(_ ~  _ ~ None )) => (k, NullAST)
      case k ~ Some(_ ~  _ ~ Some(v) ) => (k, v)
    }

  lazy val complexKey: PackratParser[ValueAST] =
    question ~> dash ~> opt(listValue) ~ opt(Indent ~> listValues <~ Dedent) ^^ {
      case v ~ None => ListAST( None, List(ornull(v)) )
      case v ~ Some( vs ) => ListAST( None, ornull(v) :: vs )
    } |
    question ~> value

  lazy val container: PackratParser[ContainerAST] =
    map | list

  lazy val map: PackratParser[MapAST] =
    opt(anchor) ~ (Indent ~> pairs <~ Dedent) ^^ {
      case a ~ p => MapAST( a, p )
    }

  lazy val list: PackratParser[ListAST] =
    opt(anchor) ~ (Indent ~> listValues <~ Dedent) ^^ {
      case a ~ p => ListAST( a, p )
    }

  lazy val listValues: PackratParser[List[ValueAST]] =
    rep1(dash ~> opt(listValue) <~ nl) ^^ (l => l map ornull)

  val listValue: PackratParser[ValueAST] =
    pair ~ (Indent ~> pairs <~ Dedent) ^^ {
      case p ~ ps => MapAST( None, p :: ps ) } |
    pair ^^ (p => MapAST( None, List(p) )) |
    value

  lazy val value: PackratParser[ValueAST] =
    primitive | container | flowContainer | multiline

  lazy val multiline: PackratParser[ValueAST] =
    opt(anchor) ~ ("|"|"|-") ~ (Indent ~> rep1(textLit <~ nl) <~ Dedent) ^^ {
      case a ~ "|" ~ l => StringAST( a, l mkString ("", "\n", "\n") )
      case a ~ _ ~ l => StringAST( a, l mkString "\n" ) } |
    opt(anchor) ~ opt(">"|">-") ~ (Indent ~> rep1(textLit <~ nl) <~ Dedent) ^^ {
      case a ~ Some( ">" ) ~ l => StringAST( a, l mkString ("", " ", "\n") )
      case a ~ _ ~ l => StringAST( a, l mkString " " ) }

  def ornull( a: Option[ValueAST] ) =
    a match {
      case None => NullAST
      case Some( v ) => v
    }

  lazy val flowContainer: PackratParser[ContainerAST] =
    flowMap | flowList

  lazy val flowMap: PackratParser[ContainerAST] =
    opt(anchor) ~ ("{" ~> repsep(flowPair, ",") <~ "}") ^^ {
      case a ~ l => MapAST( a, l )
    }

  lazy val flowPair: PackratParser[(ValueAST, ValueAST)] =
    flowValue ~ opt(colon ~ opt(flowValue)) ^^ {
      case k ~ None => (k, NullAST)
      case k ~ Some( _ ~ v ) => (k, ornull(v))
    }

  lazy val flowList: PackratParser[ContainerAST] =
    opt(anchor) ~ ("[" ~> repsep(opt(flowValue), ",") <~ "]") ^^ {
      case a ~ l => ListAST( a, l map ornull )
    }

  lazy val flowValue: PackratParser[ValueAST] =
    primitive | flowContainer

  import YamlParser._

  lazy val primitive: PackratParser[PrimitiveAST] =
    opt(anchor) ~ stringLit ^^ { case a ~ s => StringAST( a, s ) } |
    opt(anchor) ~ textLit ^^ {
      case _ ~ ("null"|"NULL"|"Null"|"~") => NullAST
      case a ~ ("true"|"True"|"TRUE") => BooleanAST( a, true )
      case a ~ ("false"|"False"|"FALSE") => BooleanAST( a, false )
      case a ~ (".inf"|"+.inf"|".Inf"|"+.Inf"|".INF"|"+.INF") => NumberAST( a, Double.PositiveInfinity )
      case a ~ ("-.inf"|"-.Inf"|"-.INF") => NumberAST( a, Double.NegativeInfinity )
      case a ~ (".NaN"|".nan"|".NAN") => NumberAST( a, Double.NaN )
      case a ~ FLOAT_REGEX( n ) => NumberAST( a, n.toDouble )
      case a ~ DEC_REGEX( n ) => NumberAST( a, n.toInt )
      case a ~ OCT_REGEX( n ) =>
        val (offset, sign) =
          n.charAt(0) match {
            case '-' => (3, -1)
            case '+' => (3, 1)
            case _ => (2, 1)
          }

        NumberAST( a, Integer.parseInt( n.substring(offset), 8 )*sign )
      case a ~ HEX_REGEX( n ) =>
        val (offset, sign) =
          n.charAt(0) match {case '-' => (3, -1)
            case '+' => (3, 1)
            case _ => (2, 1)
          }

        NumberAST( a, Integer.parseInt( n.substring(offset), 16 )*sign )
      case a ~ DATE_REGEX( d ) => DateAST( a, LocalDate.parse(d) )
      case a ~ TIME_REGEX( t ) => TimeAST( a, LocalTime.parse(t) )
      case a ~ TIMESTAMP_REGEX( t ) => TimestampAST( a, ZonedDateTime.parse(t) )
      case a ~ SPACED_TIMESTAMP_REGEX( d, tz ) =>
        val date = LocalDateTime.parse( d, SPACED_FORMATTER )

        TimestampAST( a, date.atOffset(ZoneOffset.of(tz)).toZonedDateTime )
      case a ~ s => StringAST( a, s ) } |
    pos ~ alias ^^ {
      case p ~ a => AliasAST( p, a ) }

}