package nars.jprolog;

import nars.jprolog.lang.*;

import java.io.*;
import java.util.HashMap;

import static nars.jprolog.PrologMain.parseAtomicGoal;

public class Repl {

    public static void main(String[] args) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        for (String file : args) {
            if (file.equals(".")) {
                System.exit(0);
            }
            try {
                procFile(new FileInputStream(file), null);
            } catch (FileNotFoundException e) {
                System.err.println("File '" + file + "' not found.");
            }
        }
        procFile(System.in, "? ");
    }

    public static void procFile(InputStream file, String prompt) throws ClassNotFoundException, IllegalAccessException, InstantiationException {

        String cp = "nars.jprolog.builtin";

        PrologClassLoader pcl = new PrologClassLoader(PrologMain.class.getClassLoader());

        Class clazz = pcl.loadPredicateClass(cp,
                "initialization",
                2,
                true);
        Term arg1 = Prolog.Nil;
        arg1 = new ListTerm(SymbolTerm.makeSymbol("user"), arg1);
        arg1 = new ListTerm(SymbolTerm.makeSymbol(cp), arg1);
        //	    arg1 = new ListTerm(SymbolTerm.makeSymbol("jp.ac.kobe_u.cs.prolog.compiler.pl2am"), arg1);
        //	    arg1 = new ListTerm(SymbolTerm.makeSymbol("jp.ac.kobe_u.cs.prolog.compiler.am2j"), arg1);

        Term arg2 = parseAtomicGoal("goal");

        Term[] args = {arg1, arg2};
        Predicate code = (Predicate) (clazz.newInstance());
        PrologControl p = new PrologControl(pcl);
        p.setPredicate(code, args);
        for (boolean r = p.call(); r; r = p.redo()) {
        }


        HashMap env = new HashMap<String, String>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(file));
            if (prompt != null) {
                System.out.print(prompt);
                System.out.flush();
            }
            String line = in.readLine();
            while (line != null) {
                line = line.replaceAll("#.*", "").replace(" is ", "*is*").replace(" ", "");
                if (line.equals("")) {
                    break;
                }
                char last = line.charAt(line.length() - 1);
                char punc = '.';
                if (last == '?' || last == '.') {
                    punc = last;
                    line = line.substring(0, line.length() - 1);
                }
                if (prompt != null) {
                    System.out.print(prompt);
                    System.out.flush();
                }

                line = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
