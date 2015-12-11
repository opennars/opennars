package nars.golo;

import gololang.EvaluationEnvironment;
import org.eclipse.golo.compiler.GoloCompiler;
import org.eclipse.golo.compiler.parser.ParseException;

import java.io.StringReader;

/**
 * Created by me on 12/10/15.
 */
public class GoloTest {

    public static void main(String[] args) throws ParseException {
        EvaluationEnvironment e = new EvaluationEnvironment();
        //e.run("print(1+1)");
        System.out.println(e.asFunction("print(1+1)"));

        e.anonymousModule("import nars.term.*; let termContainer = Adapter() : extends(\"nars.term.TermContainer\")");


        Object x = new GoloCompiler().initParser(
                new StringReader("print(1+1)")).CompilationUnit();
        System.out.println(x);

    }
}

