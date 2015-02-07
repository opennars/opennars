package nars.johkra;

import java.util.*;

/**
 * User: Johannes Krampf <johkra@gmail.com>
 * Date: 12.02.11
 */
 final class Util {
    private Util() {}
    private static String[] infixOps = {"*is*","==","<",">","+","-","*","/"};

    public static List<String> split(String l, String sep, Boolean all) {
        int nest = 0;
        int lsep = sep.length();
        ArrayList<String> returnValue = new ArrayList<>();
        if (l.isEmpty()) {
            return returnValue;
        }
        for (int i = 0; i <= l.length() - lsep; i++) {
            String current = l.substring(i, i + lsep);
            if ((nest <= 0) && (current.equals(sep))) {
                if (all) {
                    returnValue.add(l.substring(0, i));
                    returnValue.addAll(split(l.substring(i + lsep), sep, all));
                } else {
                    returnValue.add(l.substring(0, i));
                    returnValue.add(l.substring(i + lsep));

                }
                return returnValue;
            }
            if ((current.equals("[")) || (current.equals("("))) {
                nest += 1;
            }
            if ((current.equals("]")) || (current.equals(")"))) {
                nest -= 1;
            }
        }
        returnValue.add(l);
        return returnValue;
    }
    public static List<String> getOperators() {
        return Arrays.asList(infixOps);
    }
    public static Map.Entry<String,List<String>> splitInfix(String s) {
        for (String op: infixOps) {
            List<String> p = Util.split(s, op, false);
            if (p.size() > 1) {
                return new AbstractMap.SimpleEntry<>(op, p);
            }
        }
        return null;
    }

}
