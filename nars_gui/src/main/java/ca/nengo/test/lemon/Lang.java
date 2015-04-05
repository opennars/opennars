package ca.nengo.test.lemon;


import ca.nengo.model.SimulationException;
import ca.nengo.ui.model.plot.AbstractWidget;
import ca.nengo.util.ScriptGenException;
import nars.NAR;
import nars.io.narsese.NarseseParser;
import nars.prototype.Default;
import org.parboiled.Node;
import org.parboiled.errors.ParserRuntimeException;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.ParsingResult;
import org.parboiled.trees.TreeNode;
import org.piccolo2d.util.PBounds;

import java.awt.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * autocompletion and structural editing of narsese
 */


public class Lang {

    public static abstract class MatchVisitor{
        public abstract void visit(Match m);
    }

    public NAR nar = new NAR(new Default());
    public NarseseParser p;// = Parser.newParser(nar);
    int debugIndent = 0;
    //public Match root;
    //TestLines.Lines lines;

    public Lang(){
        p = NarseseParser.newParser(nar);
    }

    private void debug(String s)
    {
        for (int i = 0; i < debugIndent; i++)
            System.out.print(" ");
        System.out.println(s);
    }


    public class Match {
        public Node node;
        Color bgColor = new Color(255,0,255);
        PBounds bounds;
        int level;

        public void accept(MatchVisitor visitor, boolean depth_first){
            visitor.visit(this);
        }

        public Match(Node n){
            for (TreeNode<Node<Object>> p = n; p != null; p = p.getParent())
                this.level++;
            this.node = n;
            debug(" new " + this);
        }

        public Match collapsed(){
            return this;
        }

        public void print(){
            debug(this.node.getStartIndex() + "," + this.node.getEndIndex() + " "+this.getClass().getSimpleName() + ", value: " + this.node.getValue() + ", matcher: " + this.node.getMatcher());
        }

        public Match node2widget(Node n) {
            Object v = n.getValue();
            if (v == null)
                return null;
            Type t = v.getClass();
            debug(" " + n.getMatcher().getLabel() + "  " + t);
            if (t == Float.class)
                return new Number(n);
            else if (t == String.class)
                return new Word(n);
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
            //else if (n.getMatcher().getLabel() == "sequence")
              //  return children2one(n);
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
                 throw new RuntimeException("ewwww " + n + ", children:" +n.getChildren() + ", items.size:" + items.size() + ", items: " + items);
        }

        public ArrayList<Match> children2list(Node n) {
            ArrayList<Match> items = new ArrayList<Match>();
            debugIndent++;
            for (Object o : n.getChildren()) {

                Node i = (Node) o;
                debug(" doing " + i + " with m " + i.getMatcher());
                //if (i.getMatcher().
                {
                    Match w = (Match) node2widget(i);
                    if (w != null)
                        items.add(w);
                    debug(" done " + i);
                }
                debug(" continuing with " + this);
            }
            debugIndent--;
            return items;
        }


    };
    public class MatchWithChildren extends Match {
        public ArrayList<Match> items;

        public void accept(MatchVisitor visitor, boolean depth_first){
            if (!depth_first)
                super.accept(visitor, depth_first);
            for (Match m:items){
                m.accept(visitor, depth_first);
            }
            if (depth_first)
                super.accept(visitor, depth_first);
        }

        public MatchWithChildren(Node n){
            super(n);
            items = children2list(n);
        }

        public Match collapsed(){
            ArrayList<Match> new_items = new ArrayList<Match>();
            for (Match i:items) {
                Match c = i.collapsed();
                if (i.node.getStartIndex() == this.node.getStartIndex() && i.node.getEndIndex() == this.node.getEndIndex()) {
                    return c;
                } else {
                    if (i.node.getStartIndex() != i.node.getEndIndex())
                        new_items.add(c);
                }
            }
            return (getClass() == ListMatch.class) ? new ListMatch(node, new_items) : new Syntaxed(node, new_items);
        }

        public void print(){
            super.print();
            debugIndent++;
            for (Match w:items)
                w.print();
            debugIndent--;
        }

        public MatchWithChildren(Node n, ArrayList<Match> items) {
            super(n);
            this.items = items;
        }

    }

    public class ListMatch extends MatchWithChildren {
        public ListMatch(Node n){
            super(n);
        }
        public ListMatch(Node n, ArrayList<Match> items){
            super(n, items);
        }
    };

    public class Syntaxed extends MatchWithChildren {
        public Syntaxed(Node n){
            super(n);
        }
        public Syntaxed(Node n, ArrayList<Match> items){
            super(n, items);
        }

    };
    public class Word extends Match {
        public Word(Node n){super(n);}
    };
    public class Number extends Match {
        public Number(Node n){super(n);}
        public float getFloat(){
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

        System.out.println("parsing:\"" + text + "\"");

        Match w = null;

        try {

            ParsingResult r = rpr.run(text);
            p.printDebugResultInfo(r);

            Node root = r.getParseTree();
            //Object x = root.getValue();
            //System.out.println("  " + x.getClass() + ' ' + x);
            System.out.println();
            System.out.println("getParseTree(): " + root);
            System.out.println();
            w = new ListMatch((Node) root.getChildren().get(1));
            System.out.println();
            System.out.println("Match w: " + w);
            System.out.println();
            w.print();
            System.out.println();
            System.out.println("collapse:");
            System.out.println();
            w = w.collapsed();
            System.out.println();
            System.out.println("collapsed:");
            w.print();
        }
        catch (ParserRuntimeException e){
            e.printStackTrace();}
        return w;
    }
}


