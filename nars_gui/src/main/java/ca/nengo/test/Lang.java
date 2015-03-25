package ca.nengo.test;


import ca.nengo.model.SimulationException;
import ca.nengo.ui.lib.world.*;
import ca.nengo.ui.model.plot.AbstractWidget;
import ca.nengo.util.ScriptGenException;
import nars.NAR;
import nars.io.narsese.NarseseParser;
import nars.nal.entity.Term;
import nars.prototype.Default;
import org.parboiled.Node;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.ParsingResult;
import org.piccolo2d.util.PBounds;

import java.awt.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import static org.parboiled.support.ParseTreeUtils.printNodeTree;

/**
 * autocompletion and structural editing of narsese
 */

/*
now, to try displaing this. Would be nice to keep this core independent of nengo stuff, but java lacks
 multiple inheritance, what now?
 */

//i need @BuildParseTree, but grappa wont play with this class, because its not a top level class?





public class Lang {


    public NAR nar = new NAR(new Default());
    public NarseseParser p;// = Parser.newParser(nar);
    int debugIndent = 0;
    public Match root;
    TestCharMesh.CharMesh mesh;

    public Lang(){
        p = NarseseParser.newParser(nar);
    }

    public void update(TestCharMesh.CharMesh mesh)
    {
        this.mesh = mesh;
        //delete root;
        root = text2match(mesh.asString());
        //add root to the panel
    }

    private void debug(String s)
    {
        for (int i = 0; i < debugIndent; i++)
            System.out.print(" ");
        System.out.println(s);
    }


    public class Match extends AbstractWidget{
        public Node node;

        Color bgColor = new Color(0,40,40);

        @Override
        protected void paint(ca.nengo.ui.lib.world.PaintContext paintContext, double width, double height) {
            Graphics2D g = paintContext.getGraphics();

            final int iw = (int) width;
            final int ih = (int) height;
            g.setPaint(bgColor);
            g.fillRect(0, 0, iw, ih);
         }

        @Override
        public void run(float startTime, float endTime) throws SimulationException {
        }

        @Override
        public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
            return null;
        }


        public Match(Node n){
            super("match", 1,1);
            this.node = n;
            debug(" new " + this);
            int margin = 5;
            PBounds startBounds = ((TestCharMesh.SmartChar)mesh.get(node.getStartIndex())).getBounds();
            PBounds endBounds   = ((TestCharMesh.SmartChar)mesh.get(node.getEndIndex())).getBounds();
            double x = startBounds.getX() - margin;
            double y = startBounds.getY() - margin;
            double w = endBounds.getWidth() +  endBounds.getX() - x + margin * 2;
            double h = startBounds.getHeight() + margin * 2;

            setBounds(0,0,w,h);
            move(x, y);
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
        l.text2match(input);
    }

    public Match text2match(String text)
    {

        ParseRunner rpr = new RecoveringParseRunner(p.Input());
        ParsingResult r = rpr.run(text);
        /*r.getValueStack().iterator().forEachRemaining(x -> {
            System.out.println("  " + x.getClass() + ' ' + x)});*/


        p.printDebugResultInfo(r);


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
        return w;
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
