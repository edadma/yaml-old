package xyz.hyperreal.yaml

import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.Position
import util.parsing.combinator.PackratParsers
import util.parsing.combinator.syntactical.StandardTokenParsers
import util.parsing.input.CharArrayReader.EofCh
import util.parsing.input.{CharSequenceReader, Positional, Reader}

import xyz.hyperreal.indentation_lexical._


object Interpolation {
  val INTERPOLATION_PATTERN = """\$(?:([a-zA-Z_]+\d*)|\{([^}]+)\}|\$)"""r
  val INTERPOLATED_PATTERN = """[\ue000-\ue002]([^\ue000-\ue002]+)"""r
  val INTERPOLATION_DELIMITER = '\ue000'
  val INTERPOLATION_LITERAL = '\ue000'
  val INTERPOLATION_VARIABLE = '\ue001'
  val INTERPOLATION_EXPRESSION = '\ue002'
}

class YamlLexical extends IndentationLexical(false, true, List("{", "[", "("), List("}", "]", ")"), ";;", "/*", "*/") {

  import Interpolation._

  override def token: Parser[Token] = /*regexToken |*/ stringToken | decimalToken | super.token

  override def identChar = letter | elem('_') // | elem('$')

  override def whitespace: Parser[Any] = rep[Any](
    whitespaceChar
      | '/' ~ '*' ~ comment
      | ';' ~ ';' ~ rep(chrExcept(EofCh, '\n'))
      | '/' ~ '*' ~ failure("unclosed comment")
  )

//  case class RegexLit( chars: String ) extends Token

//  private def regexToken: Parser[Token] =
//    '`' ~> rep(guard(not('`')) ~> (('\\' ~ '`' ^^^ "\\`") | elem("", ch => true))) <~ '`' ^^
//      {l => RegexLit( l mkString )}

  private def stringToken: Parser[Token] =
    ('\'' ~ '\'' ~ '\'') ~> rep(guard(not('\'' ~ '\'' ~ '\'')) ~> elem("", ch => true)) <~ ('\'' ~ '\'' ~ '\'') ^^
      {l => StringLit( l mkString )} |
      ('"' ~ '"' ~ '"') ~> rep(guard(not('"' ~ '"' ~ '"')) ~> elem("", ch => true)) <~ ('"' ~ '"' ~ '"') ^^
        {l => StringLit( interpolate(l mkString, false) )} |
      '\'' ~> rep(guard(not('\'')) ~> (('\\' ~ '\'' ^^^ "\\'") | elem("", ch => true))) <~ '\'' ^^
        {l => StringLit( escape(l mkString) )} |
      '"' ~> rep(guard(not('"')) ~> (('\\' ~ '"' ^^^ "\\\"") | elem("", ch => true))) <~ '"' ^^
        {l => StringLit( interpolate(l mkString, true) )}

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

//  reserved += (
//  )

//  delimiters += (
//  )
}

class YamlParser extends StandardTokenParsers with Parsers {

  import Interpolation._

  override val lexical = new YamlLexical

  def parse[T]( grammar: Parser[T], r: Reader[Char] ) = phrase(grammar)(lexical.read(r))

  def parseFromSource[T]( src: io.Source, grammar: Parser[T] ) = parseFromString(src.mkString, grammar)

  def parseFromString[T]( src: String, grammar: Parser[T] ) = {
    parse(grammar, new CharSequenceReader(src)) match {
      case Success(tree, _) => tree
      case NoSuccess(error, rest) => problem(rest.pos, error)
    }
  }

  import lexical.{Newline, Indent, Dedent/*, RegexLit*/}

//  def regexLit: Parser[String] =
//    elem("regex literal", _.isInstanceOf[RegexLit]) ^^ (_.chars)

  def pos = positioned( success(new Positional{}) ) ^^ { _.pos }

  def nl = rep1(Newline)

  def onl = rep(Newline)

  def number: Parser[Number] =
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

  def yaml =
    map |
    list |
    primitive

  def map =
    rep1sep(primitive ~ colon ~ value, nl)

  def list =
    rep1sep(dash ~ value, nl)

  def value =
    primitive | Indent ~ yaml ~ Dedent

  def primitive =
    string |
    number |
    text

}