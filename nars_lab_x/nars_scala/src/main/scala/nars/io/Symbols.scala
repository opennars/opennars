package nars.io

//remove if not needed
import scala.collection.JavaConversions._

object Symbols {

  val JUDGMENT = '.'

  val QUESTION = '?'

  val VAR_INDEPENDENT = '$'

  val VAR_DEPENDENT = '#'

  val VAR_QUERY = '?'

  val BUDGET_VALUE_MARK = '$'

  val TRUTH_VALUE_MARK = '%'

  val VALUE_SEPARATOR = ';'

  val COMPOUND_TERM_OPENER = '('

  val COMPOUND_TERM_CLOSER = ')'

  val STATEMENT_OPENER = '<'

  val STATEMENT_CLOSER = '>'

  val SET_EXT_OPENER = '{'

  val SET_EXT_CLOSER = '}'

  val SET_INT_OPENER = '['

  val SET_INT_CLOSER = ']'

  val ARGUMENT_SEPARATOR = ','

  val IMAGE_PLACE_HOLDER = '_'

  val INTERSECTION_EXT_OPERATOR = "&"

  val INTERSECTION_INT_OPERATOR = "|"

  val DIFFERENCE_EXT_OPERATOR = "-"

  val DIFFERENCE_INT_OPERATOR = "~"

  val PRODUCT_OPERATOR = "*"

  val IMAGE_EXT_OPERATOR = "/"

  val IMAGE_INT_OPERATOR = "\\"

  val NEGATION_OPERATOR = "--"

  val DISJUNCTION_OPERATOR = "||"

  val CONJUNCTION_OPERATOR = "&&"

  val INHERITANCE_RELATION = "-->"

  val SIMILARITY_RELATION = "<->"

  val INSTANCE_RELATION = "{--"

  val PROPERTY_RELATION = "--]"

  val INSTANCE_PROPERTY_RELATION = "{-]"

  val IMPLICATION_RELATION = "==>"

  val EQUIVALENCE_RELATION = "<=>"

  val INPUT_LINE = "IN"

  val OUTPUT_LINE = "OUT"

  val PREFIX_MARK = ':'

  val RESET_MARK = '*'

  val COMMENT_MARK = '/'

  val STAMP_OPENER = '{'

  val STAMP_CLOSER = '}'

  val STAMP_SEPARATOR = ';'

  val STAMP_STARTER = ':'

  val TO_COMPONENT_1 = " @("

  val TO_COMPONENT_2 = ")_ "

  val TO_COMPOUND_1 = " _@("

  val TO_COMPOUND_2 = ") "
}
