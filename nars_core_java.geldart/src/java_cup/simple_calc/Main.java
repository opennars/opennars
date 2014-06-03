// Driver for parser

package java_cup.simple_calc;

import java_cup.simple_calc.parser;
import java_cup.runtime.Symbol;

class Main {

  static boolean do_debug_parse = false;

  static public void main(String[] args) throws java.io.IOException {

      /* create a parsing object */
      parser parser_obj = new parser(new scanner());

      /* open input files, etc. here */
      Symbol parse_tree = null;

      try {
        if (do_debug_parse)
          parse_tree = parser_obj.debug_parse();
        else
          parse_tree = parser_obj.parse();
      } catch (Exception e) {
        /* do cleanup here -- possibly rethrow e */
      } finally {
	/* do close out here */
      }
  }
}

