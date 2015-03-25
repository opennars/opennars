package ca.nengo.test;


import nars.NAR;
import nars.io.narsese.NarseseParser;
import nars.nal.entity.Term;
import nars.prototype.Default;
import org.parboiled.Node;
import org.parboiled.Parboiled;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.ParsingResult;
import java.lang.reflect.Type;
import java.util.ArrayList;


import static org.parboiled.support.ParseTreeUtils.printNodeTree;

/**
 * autocompletion and structural editing of narsese
 */

/*
now, to try displaing this. Would be nice to keep this core independent of nengo stuff, but java lacks
 multiple inheritance, what now?
 */

//i need @BuildParseTree, but grappa wont play with this class, because its not a top level class?
@BuildParseTree
public class Parser extends NarseseParser {
    protected Parser(){super();}
};


public class Lang {


    public NAR nar = new NAR(new Default());
    public Parser p;// = Parser.newParser(nar);
    int debugIndent = 0;


    public Lang(){
        p = Parboiled.createParser(Parser.class);
        p.memory = nar.memory;
    }

    private void debug(String s)
    {
        for (int i = 0; i < debugIndent; i++)
            System.out.print(" ");
        System.out.println(s);
    }


    public class Match {
        public Node node;
        public Match(Node n){
            this.node = n;
            debug(" new " + this);
        }
        public void print(){
            debug(""+this);
        }

        public Match node2widget(Node n) {
            Object v = n.getValue();
            if (v == null)
                return null;
            Type t = v.getClass();
            debug(" " + n.getMatcher().getLabel() + "  " + t);
            if (t == Term.class)
                return new Word(n);
            else if (t == Float.class)
                return new Number(n);
            else if (t == String.class)
                return null;
            else if (t == Character.class)
                return null;
            else if (n.getMatcher().getLabel() == "s")
                return null;
            else if (n.getMatcher().getLabel() == "EOI")
                return null;
            else if (n.getMatcher().getLabel() == "zeroOrMore")
                return new ListMatch(n);
            else if (n.getMatcher().getLabel() == "oneOrMore")
                return new ListMatch(n);
            else if (n.getMatcher().getLabel() == "sequence")
                return children2one(n);
            else if (n.getMatcher().getLabel() == "firstOf")
                return children2one(n);
            else
                return new Syntaxed(n);
        }

        public Match children2one(Node n)
        {
            ArrayList<Match> items = children2list(n);
            if(items.size()==1)
                return items.get(0);
            else if(items.size()==0)
                return null;
            else
                throw new RuntimeException("ewwww " + n + ", " + items.size() + ", " +n.getChildren());
        }

        public ArrayList<Match> children2list(Node n) {
            ArrayList<Match> items = new ArrayList<Match>();
            debugIndent++;
            for (Object o : n.getChildren()) {

                Node i = (Node) o;
                debug(" doing " + i);
                Match w = (Match) node2widget(i);
                debug(" done " + i);
                debug(" continuing with " + this);
                if (w != null)
                    items.add(w);
            }
            debugIndent--;
            return items;
        }


    };
    public class MatchWithChildren extends Match {
        public ArrayList<Match> items;// = new ArrayList<Match>();

        public MatchWithChildren(Node n){
            super(n);
            items = children2list(n);
        }
        public void print(){
            debug(""+this+" with items: ");
            debugIndent++;
            for (Match w:items)
                w.print();

            debugIndent--;
        }

    }

    public class ListMatch extends MatchWithChildren {
        public ListMatch(Node n){
            super(n);
        }
    };

    public class Syntaxed extends MatchWithChildren {
        public Syntaxed(Node n){
            super(n);
        }
    };
    public class Word extends Match {
        public Word(Node n){super(n);}
    };
    public class Number extends Match {
        public Number(Node n){super(n);}
        public float getValue(){
            return (float)node.getValue();
        }
    };



    public static void main(String[] args) {
        String input;
        //input = "<<(*,$a,$b,$c) --> Nadd> ==> <(*,$c,$a) --> NbiggerOrEqual>>.";
        //input = "<{light} --> [on]>.";
        //input = "(--,<goal --> reached>).";
        //input = "<(*,{tom},{sky}) --> likes>.";
        input = "<neutralization --> reaction>. <neutralization --> reaction>?";

        Lang l = new Lang();
        l.text2widget(input);
    }

    public void text2widget(String text)
    {

        ParseRunner rpr = new RecoveringParseRunner(p.Input());
        ParsingResult r = rpr.run(text);
        /*r.getValueStack().iterator().forEachRemaining(x -> {
            System.out.println("  " + x.getClass() + ' ' + x)});*/





        System.out.println("valid? " + (r.matched && (r.parseErrors.isEmpty())) );
        r.getValueStack().iterator().forEachRemaining(x -> System.out.println("  " + x.getClass() + ' ' + x));

        for (Object e : r.parseErrors) {
            if (e instanceof InvalidInputError) {
                InvalidInputError iie = (InvalidInputError) e;
                System.err.println(e);
                if (iie.getErrorMessage()!=null)
                    System.err.println(iie.getErrorMessage());
                for (MatcherPath m : iie.getFailedMatchers()) {
                    System.err.println("  ?-> " + m);
                }
                System.err.println(" at: " + iie.getStartIndex() + " to " + iie.getEndIndex());
            }
            else {
                System.err.println(e);
            }

        }

        System.out.println(printNodeTree(r));







        Node root = r.parseTreeRoot;
        //Object x = root.getValue();
        //System.out.println("  " + x.getClass() + ' ' + x);
        System.out.println();
        System.out.println(" " + root);
        System.out.println();
        Match w = new ListMatch((Node)root.getChildren().get(1));
        System.out.println();
        System.out.println(" " + root);
        System.out.println();
        w.print();
    }
}




//lets not do this now
    /*
    private static class Sym {
        public static enum Param {LIST_ELEM_TYPE, SRSTR};
        public static class Params extends HashMap<Param, Object>{};
        public static Params newParams(){
            return new Params();
        }

        Map<Param, Object> params; // like the type of items of a List or min/max items
        Type type;
        public Sym(Class type, String name, Map<Param, Object> params){
            this.type = type;
        }
    };

    private Object some(Object sym){
        Sym.Params params = Sym.newParams();
        params.put(Sym.Param.LIST_ELEM_TYPE, sym);
        return new Sym(ListMatch.class, "", params);

    }
    */


        /*
    //private class Sequence extends ImmutableList<Object> {}; // can contain String, Sym,
    private class Choices extends ArrayList<ImmutableList<Object>> {};
    private class Grammar extends HashMap<Object, Choices> {};
    private Grammar g;
    public Lang(){
        g = new Grammar();
        g.put(Task.class, new Choices());
        g.get(Task.class).add(l().add(
                BudgetValue.class).add(
                Sentence.class).build());
    }
    private ImmutableList.Builder<Object> l(){
        return new ImmutableList.Builder<Object>();
    }
        */
