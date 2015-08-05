package nars.johkra;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * User: Johannes Krampf <johkra@gmail.com>
 * Date: 06.02.11
 */

public final class Term {
    private String pred;
    private List<Term> args;

    public Term(String s, List<Term> args) throws ParseException {
        if ((s == null) || (s.length() == 0)) {
            throw (new ParseException("Predicate mustn't be empty or null", -1));
        }
        Map.Entry<String, List<String>> parts = null;
        if (args == null) {
            parts = Util.splitInfix(s);
        }
        if (args != null) {
            pred = s;
            this.args = args;
        } else if (parts != null) {
            this.pred = parts.getKey();
            this.args = new ArrayList<>();
            for (String str : parts.getValue()) {
                this.args.add(new Term(str, null));
            }
        } else if (s.charAt(s.length() - 1) == ']') {
            List<String> flds = Util.split(s.substring(1, s.length() - 1), ",", true);
            List<String> flds2 = Util.split(s.substring(1, s.length() - 1), "|", true);
            if (flds2.size() > 1) {
                this.pred = ".";
                this.args = new ArrayList<>();
                for (String str : flds2) {
                    this.args.add(new Term(str, null));
                }
            } else {
                Term l = new Term(".", null);
                for (int i = flds.size() - 1; i >= 0; i--) {
                    List<Term> temp = new ArrayList<>();
                    temp.add(new Term(flds.get(i), null));
                    temp.add(l);
                    l = new Term(".", temp);
                }
                this.pred = l.pred;
                this.args = l.args;
            }
        } else if (s.charAt(s.length() - 1) == ')') {
            List<String> flds = Util.split(s, "(", false);
            if (flds.size() != 2) {
                throw new ParseException("Syntax error in term: '" + s + '\'', -1);
            }
            this.pred = flds.get(0);
            this.args = new ArrayList<>();
            for (String str : Util.split(flds.get(1).substring(0, flds.get(1).length() - 1), ",", true)) {
                this.args.add(new Term(str, null));
            }
        } else {
            this.pred = s;
            this.args = new ArrayList<>();
        }

    }

    private Term() {
    }


    public String getPred() {
        return pred;
    }

    public List<Term> getArgs() {
        return args;
    }

    public Term clone() {
        Term clone = new Term();
        clone.pred = pred;
        clone.args = new ArrayList<>(args);
        return clone;
    }

    @Override
    public String toString() {
        if (this.getPred().equals(".")) {
            if (this.getArgs().size() == 0) {
                return "[]";
            }
            Term nxt = this.getArgs().get(1);
            if (nxt.getPred().equals(".") && (nxt.getArgs().size() == 0)) {
                return "[" + this.getArgs().get(0) + ']';
            } else if (nxt.getPred().equals(".")) {
                return "[" + this.getArgs().get(0) + ',' + nxt.toString().substring(1, nxt.toString().length() - 1) + ']';
            } else {
                return "[" + this.getArgs().get(0) + '|' + nxt + ']';
            }
        } else if (this.getArgs().size() > 0) {
            String argsString = "";
            if (args != null) {
                argsString = Collections.singletonList(args).toString();
                argsString = argsString.substring(1, argsString.length() - 1);
            }
            return '<' + this.getPred() + '(' + argsString + ")>";
        }
        return this.getPred();
    }
}
