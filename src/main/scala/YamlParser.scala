//@
package xyz.hyperreal.yaml

import scala.util.parsing.combinator.Parsers
import util.parsing.input._
import util.parsing.combinator.PackratParsers
import util.parsing.combinator.syntactical.StandardTokenParsers
import util.parsing.input.CharArrayReader.EofCh
import xyz.hyperreal.indentation_lexical._


object Interpolation {
  val INTERPOLATION_PATTERN = """\$(?:([a-zA-Z_]+\d*)|\{([^}]+)\}|\$)"""r
  val INTERPOLATED_PATTERN = """[\ue000-\ue002]([^\ue000-\ue002]+)"""r
  val INTERPOLATION_DELIMITER = '\ue000'
  val INTERPOLATION_LITERAL = '\ue000'
  val INTERPOLATION_VARIABLE = '\ue001'
  val INTERPOLATION_EXPRESSION = '\ue002'
}

class YamlLexical extends IndentationLexical(false, true, List("{", "["), List("}", "]"), "#", "/*", "*/") {

  import Interpolation._

  override def token: Parser[Token] = /*regexToken |*/ decimalToken | stringToken | super.token

  override def identChar = letter | elem('_') // | elem('$')

  override def whitespace: Parser[Any] = rep[Any](
    whitespaceChar
      | '/' ~ '*' ~ comment
      | '#' ~ rep(chrExcept(EofCh, '\n'))
      | '/' ~ '*' ~ failure("unclosed comment")
  )

//  case class RegexLit( chars: String ) extends Token

//  private def regexToken: Parser[Token] =
//    '`' ~> rep(guard(not('`')) ~> (('\\' ~ '`' ^^^ "\\`") | elem("", ch => true))) <~ '`' ^^
//      {l => RegexLit( l mkString )}

  private def stringToken: Parser[Token] =
//    ('\'' ~ '\'' ~ '\'') ~> rep(guard(not('\'' ~ '\'' ~ '\'')) ~> elem("", ch => true)) <~ ('\'' ~ '\'' ~ '\'') ^^
//      {l => StringLit( l mkString )} |
//    ('"' ~ '"' ~ '"') ~> rep(guard(not('"' ~ '"' ~ '"')) ~> elem("", ch => true)) <~ ('"' ~ '"' ~ '"') ^^
//      {l => StringLit( interpolate(l mkString, false) )} |
    '\'' ~> rep(guard(not('\'')) ~> (('\\' ~ '\'' ^^^ "\\'") | elem("", ch => true))) <~ '\'' ^^
      {l => StringLit( escape(l mkString) )} |
    '"' ~> rep(guard(not('"')) ~> (('\\' ~ '"' ^^^ "\\\"") | elem("", ch => true))) <~ '"' ^^
      {l => StringLit( interpolate(l mkString, true) )} |
    text ^^ {
      l => StringLit( l.mkString.trim )}

  private def text: Parser[List[Elem]] =
    guard(not(elem('{') | '[' | ('t' ~ 'r' ~ 'u' ~ 'e' | 'f' ~ 'a' ~ 'l' ~ 's' ~ 'e' | 'n' ~ 'u' ~ 'l' ~ 'l') ~ (',' ~ ' '|',' ~ '\n'|':' ~ ' '|':' ~ '\n'|':' ~ '#'|'-' ~ ' '|'-' ~ '\n'|'-' ~ '#'|'\n'))) ~> rep1(guard(not(elem(']')|'}'|',' ~ ' '|',' ~ '\n'|':' ~ ' '|':' ~ '\n'|':' ~ '#'|'-' ~ ' '|'-' ~ '\n'|'-' ~ '#'|'\n')) ~> elem("", ch => true))

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
          } else {
            buf.append(
              Map (
                '\\' -> '\\', '\'' -> '\'', '"' -> '"', '$' -> '$', '/' -> '/', 'b' -> '\b', 'f' -> '\f', 'n' -> '\n', 'r' -> '\r', 't' -> '\t'
              ).get(r.rest.first) match {
                case Some( c ) => c
                case _ => sys.error( "illegal escape character " + r.rest.first )
              } )

            chr( r.rest.rest )
          }
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

    for (m <- INTERPOLATION_PATTERN.findAllMatchIn( s )) {
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

  private def decimalToken: Parser[Token] =
    digits ~ '.' ~ digits ~ optExponent ^^ { case intPart ~ _ ~ fracPart ~ exp => NumericLit(intPart + '.' + fracPart + exp) } |
      '.' ~ digits ~ optExponent ^^ { case _ ~ fracPart ~ exp => NumericLit('.' + fracPart + exp) } |
      digits ~ optExponent ^^ { case intPart ~ exp => NumericLit(intPart + exp) }

  private def digits = rep1(digit) ^^ (_ mkString)

  private def chr( c: Char ) = elem("", ch => ch == c)

  private def exponent = (chr('e') | 'E') ~ opt(chr('+') | '-') ~ digits ^^ {
    case e ~ None ~ exp => List(e, exp) mkString
    case e ~ Some(s) ~ exp => List(e, s, exp) mkString
  }

  private def optExponent = opt(exponent) ^^ {
    case None => ""
    case Some(e) => e
  }

  reserved += (
    "true", "false", "null"
  )

  delimiters += (
    "{", "[", ",", "]", "}",
    ":", "-", ": ", "- "
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

  import lexical.{Newline, Indent, Dedent}

  lazy val pos: PackratParser[Position] = positioned( success(new Positional{}) ) ^^ { _.pos }

  lazy val nl: PackratParser[_] = rep1(Newline)

  lazy val onl: PackratParser[_] = rep(Newline)

  lazy val number: PackratParser[Number] =
    numericLit ^^
      (n =>
        if (n startsWith "0x") {
          val num = BigInt( n substring 2, 16 )

          if (num.isValidInt)
            num.intValue.asInstanceOf[Number]
          else
            num
        } else if (n matches ".*[.eE].*")
          n.toDouble.asInstanceOf[Number]
        else {
          val bi = BigInt( n )

          if (bi.isValidInt)
            bi.intValue.asInstanceOf[Number]
          else
            bi
        } )

  lazy val yaml: PackratParser[AST] =
    onl ~> document <~ onl

  lazy val document: PackratParser[AST] =
    container | flowValue

  lazy val container: PackratParser[ContainerAST] =
    map | list

  lazy val colon: PackratParser[_] =
    ": " | (":" ~ guard(Indent))

  lazy val dash: PackratParser[_] =
    "- " | ("-" ~ guard(Indent))

  lazy val map: PackratParser[ContainerAST] =
    rep1(pair <~ nl) ^^ MapAST

  lazy val pair: PackratParser[PairAST] =
    primitive ~ colon ~ anyValue ^^ {
      case k ~ _ ~ v => PairAST( k, v ) }

  lazy val anyValue: PackratParser[AST] =
    value |
    flowContainer

  lazy val list: PackratParser[ContainerAST] =
    rep1(dash ~> listValue <~ nl) ^^ ListAST

  val listValue: PackratParser[AST] =
    pair ~ (Indent ~> map <~ Dedent) ^^ {
      case p ~ MapAST( ps ) => MapAST( p :: ps ) } |
    pair ^^ (p => MapAST( List(p) )) |
    anyValue

  lazy val value: PackratParser[AST] =
    primitive | Indent ~> container <~ Dedent

  lazy val flowContainer: PackratParser[ContainerAST] =
    flowMap | flowList

  lazy val flowMap: PackratParser[ContainerAST] =
    "{" ~> repsep(flowPair, ",") <~ "}" ^^ MapAST

  lazy val flowPair: PackratParser[PairAST] =
    flowValue ~ colon ~ flowValue ^^ {
      case k ~ _ ~ v => PairAST( k, v )
    }

  lazy val flowList: PackratParser[ContainerAST] =
    "[" ~> repsep(flowValue, ",") <~ "]" ^^ ListAST

  lazy val flowValue: PackratParser[AST] =
    primitive | flowContainer

  lazy val primitive: PackratParser[AST] =
    "true" ^^^ BooleanAST( true ) |
    "false" ^^^ BooleanAST( true ) |
    "null" ^^^ NullAST |
    stringLit ^^ StringAST |
    number ^^ NumberAST

}