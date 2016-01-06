//package nars.nlp.gf;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.google.common.collect.Lists;
//import nars.NAR;
//import nars.NARStream;
//import nars.io.nlp.Twenglish;
//import nars.nal.nal1.Inheritance;
//import nars.nal.nal2.Instance;
//import nars.nal.nal2.Similarity;
//import nars.nal.nal3.SetExt;
//import nars.nal.nal4.Product;
//import nars.nal.nal5.Implication;
//import nars.nal.nal7.TemporalRules;
//import nars.nal.nal7.Tense;
//
//import nars.nar.experimental.Solid;
//import nars.term.Atom;
//import nars.term.Term;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by me on 8/9/15.
// */
//public class NARgf extends GrammaticalFrameworkClient {
//
//    //final String DefaultGrammar = "Phrasebook";
//    //final String DefaultLang = "Eng";
//    //final String DefaultCat = "Message";
//
//    final String DefaultGrammar = "AppEng";
//    final String DefaultLang = "";
//    final String DefaultCat = "Phrase";
//
//
//    public final NAR nar;
//
//    public NARgf(NAR n) {
//        super();
//        this.nar = n;
//    }
//
//    public void inputNatural(String text) {
//        final String input = text;
//
//        String cat = DefaultCat;
////        char lastChar = text.charAt(text.length()-1);
////        switch (lastChar) {
////            case '?': cat = "Question"; break;
////            case '!': cat = "Message"; break;
////            case '.': cat = "Message"; break;
////        }
////        if (cat!=null)
////            text = text.substring(0, text.length()-1);
////        else {
////            cat = "Message";
////        }
//
//
//
//        {
//            JsonNode x = tree(DefaultGrammar, DefaultLang, text, cat);
//            if (x != null && x.size() > 0) {
//                JsonNode trees = x.get(0).get("trees");
//                if (trees != null) {
//                    onParse(input, trees);
//                    return;
//                }
//            }
//        }
//
//        {
//            JsonNode y = complete(DefaultGrammar, DefaultLang, text, cat, 8);
//            if (y != null && y.size() > 0) {
//                JsonNode completions = y.get(0).get("completions");
//                if (completions!=null) {
//                    onCompletion(input, completions);
//                }
//            }
//        }
//
//
//    }
//
//    private void onCompletion(String input, JsonNode completions) {
//        //System.out.println("(&/, " + input + ", ... " + ", (&|, " + completions + " ) )");
//        List<Term> ss = new ArrayList();
//        for (int i = 0; i < completions.size(); i++) {
//            String s = completions.get(i).asText();
//            ss.add(Atom.the(s));
//        }
//
//        nar.believe(
//                Implication.make(
//                        sentence(tokenize(input)),
//                        w((SetExt)SetExt.make(ss)),
//                        TemporalRules.ORDER_FORWARD
//                )
//        );
//    }
//
//    private Term sentence(List<Term> tokens) {
//        //return Sequence.makeForward(tokens);
//        return Product.make(tokens);
//    }
//
//    public static Term w(String word) {
//        return w(Atom.the(word));
//    }
//    public static Term w(Atom word) {
//        return word; //w((SetExt)SetExt.make(word));
//    }
//
//    public static Term w(SetExt words) {
//        return words; //Inheritance.make(words, Atom.the("W"));
//    }
//
//    public Term termize(GfTree t) {
//        return termize(t.mRoot);
//    }
//
//    private Term termize(GfFun f) {
//        Atom subj = Atom.the(f.getName());
//        if (f.hasArgs()) {
//
//            List<Term> args = Lists.transform(f.getArgs(), a -> termize(a));
//            if (args.size() == 1) {
//                return Instance.make(args.get(0), subj);
//            }
//            else {
//                return Operation.make(
//                        subj,
//                        Product.make(args)
//                );
//            }
//        }
//        else {
//            return subj;
//        }
//    }
//
//    private void onParse(String input, JsonNode trees) {
//        List<Term> ttt = new ArrayList();
//        for (int i = 0; i < trees.size(); i++) {
//            String t = trees.get(i).asText();
//            GfTree tr = new GfTree(t);
//            Term tt = termize(tr);
//            ttt.add(tt);
//            //System.out.println(tt);
//        }
//
//        nar.believe(
//                //getHeardOperation(tt),
//                getInputSimilarity(input, SetExt.make(ttt)),
//                Tense.Present, 1f, 0.9f );
//
//        //System.out.println("(" + input + " <-> " + trees + ")");
//    }
//
//    private Similarity getInputSimilarity(String input, Term tt) {
//        List<Term> tokens = tokenize(input);;
//        return Similarity.make(
//                sentence(tokens),
//                tt
//        );
//    }
//
//    private List<Term> tokenize(String input) {
//        /*return Lists.transform(Twenglish.tokenize(input),
//                        t -> Instance.make(t, Atom.the("W")));*/
//        return Lists.transform(Twenglish.tokenize(input),
//                        t -> t);
//    }
//
//    private Inheritance getHeardOperation(Term tt) {
//        return Inheritance.make( Product.make(tt, Atom.the("input")), Atom.the("say") );
//    }
//
//
//    public static void main(String[] arg) {
//
//        NAR n = new NAR(
//                //new Equalized(2000, 96, 8).setCyclesPerFrame(16)
//                new Solid(1, 4000, 1, 3, 1, 8).setInternalExperience(null)
//        );
//
//
//        //new NARSwing(n);
//                //NARfx.window(n);
//
//
//
//        NARStream s = new NARStream(n);
//        s.stdout();
//
//        NARgf g = new NARgf(n);
//
//        for (int i = 0; i < 1; i++) {
//            n.input("<<{(#a, #b)} --> Is> <-> <#a <-> #b>>.");
//
//            g.inputNatural("you are English");
//            //g.inputNatural("I am one");
//
//            g.inputNatural("hello");
//            g.inputNatural("what is your ");
//            g.inputNatural("what is your name");
//
//            g.inputNatural("what ");
//            g.inputNatural("what is ");
//            g.inputNatural("I am "); //I must be capitalized
//
//            g.inputNatural("this ");
//            g.inputNatural("Norwegian ");
//
//            g.inputNatural("this good tea is very warm");
//            g.inputNatural("don't sit");
//            g.inputNatural("what time is it");
//            g.inputNatural("how old are we");
//            g.inputNatural("how far is the cheapest airport from the most popular Belgian restaurant by ferry");
//            g.inputNatural("where do you play");
//            g.inputNatural("a toilet is closed");
//            g.inputNatural("you don't understand");
//            n.frame(4);
//
//        }
//
//        n.frame(100);
//
//    }
// }
