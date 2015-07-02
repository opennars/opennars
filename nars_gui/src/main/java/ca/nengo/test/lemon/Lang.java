package ca.nengo.test.lemon;


import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import nars.NAR;
import nars.narsese.NarseseParser;
import nars.nar.Default;

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

    public class Feedback {
        String message = "Hi, are you a bridge?";
        int start;
        int end;
        public Feedback(){}
    }
    public class Error extends Feedback{
        public Error(String message){
            this.message = message;
            this.start = -1;
        };
        public Error(String message, int start, int end){
            this.message = message;
            this.start = start;
            this.end = end;
        };
    }

    //public class xxx extends PNode..

//    public class Match { // turn these into piccolo pnodes
//        public Node ast;
//        Color bgColor = new Color(255,0,255);
//        PBounds bounds;
//        int level;
//        int start = -1; // -1 indicates that it isnt related to any glyph in particular
//        int end = -1;
//        ArrayList<Feedback> feedback = new ArrayList<Feedback>();
//
//        public void accept(MatchVisitor visitor, boolean depth_first){
//            visitor.visit(this);
//        }
//
//        public Match(Node n){
//            for (TreeNode<Node<Object>> p = n; p != null; p = p.getParent())
//                this.level++;
//            this.ast = n;
//            if (ast != null)
//            {
//                start = n.getStartIndex();
//                end = n.getEndIndex();
//            }
//            debug(" new " + this);
//        }
//
//        public Match collapsed(){
//            return this;
//        }
//
//        public void print(){
//            String r = start + "," + end + " "+getClass().getSimpleName();
//            if (ast != null)
//                r += ", value: " + ast.getValue() + ", matcher: " + ast.getMatcher();
//            debug(r);
//        }
//
//        public Match node2widget(Node n) {
//
//            Object v = n.getValue();
//            if (v == null)
//                return null;
//            Type t = v.getClass();
//            debug(" " + n.getMatcher().getLabel() + "  " + t);
//            if (t == Float.class)
//                return new Number(n);
//            else if (t == String.class)
//                return new Word(n);
//            else if (t == Character.class)
//                return null;
//            else if (n.getMatcher().getLabel() == "s")
//                return null;
//            else if (n.getMatcher().getLabel() == "EOI")
//                return null;
//            else if (n.getMatcher().getLabel() == "zeroOrMore")
//                return new ListMatch(n);
//            else if (n.getMatcher().getLabel() == "oneOrMore")
//                return new ListMatch(n);
//            //else if (n.getMatcher().getLabel() == "sequence")
//              //  return children2one(n);
//            else if (n.getMatcher().getLabel() == "firstOf")
//                return children2one(n);
//            else
//                return new Syntaxed(n);
//        }
//
//        public Match children2one(Node n)
//        {
//            ArrayList<Match> items = children2list(n);
//            if(items.size()==1)
//                return items.get(0);
//            else if(items.size()==0)
//                return null;
//            else
//                 throw new RuntimeException("ewwww " + n + ", children:" +n.getChildren() + ", items.size:" + items.size() + ", items: " + items);
//        }
//
//        public ArrayList<Match> children2list(Node n) {
//            ArrayList<Match> items = new ArrayList<Match>();
//            debugIndent++;
//            for (Object o : n.getChildren()) {
//
//                Node i = (Node) o;
//                debug(" doing " + i + " with m " + i.getMatcher());
//                //if (i.getMatcher().
//                {
//                    Match w = (Match) node2widget(i);
//                    if (w != null)
//                        items.add(w);
//                    debug(" done " + i);
//                }
//                debug(" continuing with " + this);
//            }
//            debugIndent--;
//            return items;
//        }
//
//
//    };
//    public class MatchWithChildren extends Match {
//        public ArrayList<Match> items = new ArrayList<Match>();
//
//        public void accept(MatchVisitor visitor, boolean depth_first){
//            if (!depth_first)
//                super.accept(visitor, depth_first);
//            for (Match m:items){
//                m.accept(visitor, depth_first);
//            }
//            if (depth_first)
//                super.accept(visitor, depth_first);
//        }
//
//        public MatchWithChildren(Node n){
//            super(n);
//            if (n != null)
//                items = children2list(n);
//        }
//
//        public Match collapsed(){
//            ArrayList<Match> new_items = new ArrayList<Match>();
//            for (Match i:items) {
//                Match c = i.collapsed();
//                if (i.start == start && i.end == end) { //? re -1's?
//                    return c;
//                } else {
//                    if (i.start != i.end)
//                        new_items.add(c);
//                }
//            }
//            return (getClass() == ListMatch.class) ?
//                    new ListMatch(ast, new_items) :
//                    new Syntaxed(ast, new_items);
//        }
//
//        public void print(){
//            super.print();
//            debugIndent++;
//            for (Match w:items)
//                w.print();
//            debugIndent--;
//        }
//
//        public MatchWithChildren(Node n, ArrayList<Match> items) {
//            super(n);
//            this.items = items;
//        }
//
//    }
//
//    public class ListMatch extends MatchWithChildren {
//        public ListMatch(Node n){
//            super(n);
//        }
//        public ListMatch(Node n, ArrayList<Match> items){
//            super(n, items);
//        }
//    };
//
//    public class Syntaxed extends MatchWithChildren {
//        public Syntaxed(Node n){
//            super(n);
//        }
//        public Syntaxed(Node n, ArrayList<Match> items){
//            super(n, items);
//        }
//
//    };
//    public class Word extends Match {
//        public Word(Node n){super(n);}
//    };
//    public class Number extends Match {
//        public Number(Node n){super(n);}
//        public float getFloat(){
//            return (float)ast.getValue();
//        }
//    };
//
//
//    public static void main(String[] args) {
//        String input;
//        //input = "<<(*,$a,$b,$c) --> Nadd> ==> <(*,$c,$a) --> NbiggerOrEqual>>.";
//        //input = "<{light} --> [on]>.";
//        //input = "(--,<goal --> reached>).";
//        //input = "<(*,{tom},{sky}) --> likes>.";
//        input = "<neutralization --> reaction>. <neutralization --> reaction>?";
//
//        Lang l = new Lang();
//        l.text2match(input);
//    }
//
//    public Match text2match(String text)
//    {
//
//        ParseRunner rpr = new ListeningParseRunner(p.Input());
//
//        System.out.println("parsing:\"" + text + "\"");
//
//        Match w = new ListMatch(null);;
//
//        try {
//
//            ParsingResult r = rpr.run(text);
//            //p.printDebugResultInfo(r);
//
//            Node root = r.getParseTree();
//            //Object x = root.getValue();
//            //System.out.println("  " + x.getClass() + ' ' + x);
//            System.out.println();
//            System.out.println("getParseTree(): " + root);
//            System.out.println();
//            Node n =  (Node)root.getChildren().get(1);
//            System.out.println("Node n: " + n);
//            w = new ListMatch(n);
//            System.out.println();
//        }
//        catch (ParserRuntimeException e) {
//            e.printStackTrace();
//            System.out.println("so yeah, that didnt parse.");
//            w.feedback.add(new Error(e.toString()));
//        }
//
//        System.out.println();
//
//        if (!rpr.getParseErrors().isEmpty()){
//            System.out.println("errors.");
//            for (Object e : rpr.getParseErrors()) {
//
//                if (e instanceof InvalidInputError) {
//                    InvalidInputError iie = (InvalidInputError) e;
//                    String msg = (iie.getClass().getSimpleName() + ": " + iie.getErrorMessage() + "\n");
//                    msg += (" at: " + iie.getStartIndex() + " to " + iie.getEndIndex()  + "\n");
//                    for (MatcherPath m : iie.getFailedMatchers())
//                        msg += ("  ?-> " + m + '\n');
//                    w.feedback.add(new Error(msg, iie.getStartIndex(),iie.getEndIndex()));
//                    System.out.println(msg);
//                } else {
//                    w.feedback.add(new Error(e.toString()));
//                    System.out.println(e.toString());
//                }
//            }
//        }
//        System.out.println();
//        System.out.println("Match w: " + w);
//        System.out.println();
//        w.print();
//        System.out.println();
//        System.out.println("collapse:");
//        System.out.println();
//        w = w.collapsed();
//        System.out.println();
//        System.out.println("collapsed:");
//        w.print();
//        System.out.println();
//        System.out.println("done text2match.");
//        System.out.println();
//
//        return w;
//    }
}


