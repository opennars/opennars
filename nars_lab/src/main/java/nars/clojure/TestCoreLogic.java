package nars.clojure;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import static java.lang.System.*;

/**
 * Created by me on 2/6/15.
 */
public class TestCoreLogic {

    //http://stackoverflow.com/a/23555959

    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("clojure.java.api.Clojure");

        IFn plus = Clojure.var("clojure.core", "+");
        out.println(plus.invoke(1, 2));

        IFn require = Clojure.var("clojure.core", "require");
        //require.invoke(Clojure.read("clojure.set"));
        require.invoke(Clojure.read("clojure.logic"));

        IFn map = Clojure.var("clojure.core", "map");
        IFn inc = Clojure.var("clojure.core", "inc");
        out.println( map.invoke(inc, Clojure.read("[1 2 3]")) );

        IFn printLength = Clojure.var("clojure.core", "*print-length*");
        IFn deref = Clojure.var("clojure.core", "deref");
        out.println(deref.invoke(printLength));


    }

}
