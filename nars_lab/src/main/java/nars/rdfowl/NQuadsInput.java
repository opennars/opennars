package nars.rdfowl;

import automenta.vivisect.Video;
import nars.Global;
import nars.NAR;
import nars.gui.NARSwing;
import nars.nal.nal1.Inheritance;
import nars.nal.nal1.Negation;
import nars.nal.nal2.Similarity;
import nars.nal.nal4.Product;
import nars.nal.nal5.Equivalence;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.nar.Default;
import nars.narsese.InvalidInputException;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by me on 6/4/15.
 */
abstract public class NQuadsInput {


    private final static String RDF_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    private static String parentTagName = null;

    private final NAR nar;

    final float beliefConfidence;
    private boolean includeDataType = false;

    public NQuadsInput(NAR n, String nqLoc, float beliefConfidence) throws Exception {
        this.nar = n;
        this.beliefConfidence = beliefConfidence;
        input(new File(nqLoc));
    }



    final static Pattern nQuads = Pattern.compile("((?:\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"(?:@\\w+(?:-\\w+)?|\\^\\^<[^>]+>)?)|<[^>]+>|\\_\\:\\w+|\\.)");


    /**
     * These parsing rules were devised by physically looking at the OWL file
     * and figuring out what goes where. This should by no means be considered a
     * generalized way to parse OWL files.
     *
     * Parsing rules:
     *
     * owl:Class@rdf:ID = entity (1), type=Wine optional:
     * owl:Class/rdfs:subClassOf@rdf:resource = entity (2), type=Wine (2) --
     * parent --> (1) if owl:Class/rdfs:subClassOf has no attributes, ignore if
     * no owl:Class/rdfs:subClassOf entity, ignore it
     * owl:Class/owl:Restriction/owl:onProperty@rdf:resource related to
     * owl:Class/owl:Restriction/owl:hasValue@rdf:resource
     *
     * Region@rdf:ID = entity, type=Region optional:
     * Region/locatedIn@rdf:resource=entity (2), type=Region (2) -- parent --
     * (1) owl:Class/rdfs:subClassOf/owl:Restriction - ignore
     *
     * WineBody@rdf:ID = entity, type=WineBody WineColor@rdf:ID = entity,
     * type=WineColor WineFlavor@rdf:ID = entity, type=WineFlavor
     * WineSugar@rdf:ID = entity, type=WineSugar Winery@rdf:ID = entity,
     * type=Winery WineGrape@rdf:ID = entity, type=WineGrape
     *
     * Else if no namespace, this must be a wine itself, capture as entity:
     * ?@rdf:ID = entity, type=Wine all subtags are relations: tagname =
     * relation_name tag@rdf:resource = target entity
     */
    public void input(File f) throws Exception {

        List<String> items = new ArrayList(4);

        new Scanner(f).useDelimiter("\n").forEachRemaining(s -> {
            Matcher m = nQuads.matcher(s);
            int count = 0;
            items.clear();
            while(m.find()) {
                String t = s.substring(m.start(), m.end());
                items.add(t);
            }

            if (items.size() >= 3) {

                Atom subj = resource(items.get(0));
                Atom pred = resource(items.get(1));
                Term obj = resourceOrValue(items.get(2));
                if (subj != null && obj != null && pred != null) {
                    if (!subj.equals(obj))  { //avoid equal subj & obj, if only namespace differs
                        try {
                            input(subj, pred, obj);
                        }
                        catch (InvalidInputException iie) {
                            System.err.println(iie);
                            //iie.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public Atom resource(String s) {
        if (s.startsWith("<") && s.endsWith(">")) {
            s = s.substring(1, s.length() - 1);

            if (s.contains("#")) {
                String[] a = s.split("#");
                String p = a[0]; //TODO handle the namespace
                s = a[1];
            }
            else {
                String[] a = s.split("/");
                if (a.length == 0) return null;
                s = a[a.length - 1];
            }


            return Atom.the(s, true);
        }
        else
            return null;
    }
    public Term resourceOrValue(String s) {
        Atom a = resource(s);
        if (a == null) {
            //...
        }
        return a;
    }

    protected abstract void believe(Compound assertion);

    abstract public static class TagProcessor {

        abstract protected void execute(XMLStreamReader parser);
    }


    static public Term atom(String uri) {
        int lastSlash = uri.lastIndexOf('/');
        if (lastSlash!=-1)
            uri = uri.substring(lastSlash + 1);

        switch(uri) {
            case "owl#Thing": uri = "thing"; break;
        }

        return Atom.the(uri);
    }

//
//    //TODO make this abstract for inserting the fact in different ways
//    protected void inputClass(Term clas) {
//        inputClassBelief(clas);
//    }

//    private void inputClassBelief(Term clas) {
//        nar.believe(isAClass(clas));
//    }

//    public static Term isAClass(Term clas) {
//        return Instance.make(clas, owlClass);
//    }

    public static final Atom owlClass = Atom.the("Class");
    static final Atom parentOf = Atom.the("parentOf");
    static final Atom type = Atom.the("type");
    static final Atom subClassOf = Atom.the("subClassOf");
    static final Atom subPropertyOf = Atom.the("subPropertyOf");
    static final Atom equivalentClass = Atom.the("equivalentClass");
    static final Atom equivalentProperty = Atom.the("equivalentProperty");
    static final Atom inverseOf = Atom.the("inverseOf");
    static final Atom disjointWith = Atom.the("disjointWith");
    static final Atom domain = Atom.the("domain");
    static final Atom range = Atom.the("range");
    static final Atom sameAs = Atom.the("sameAs");
    static final Atom dataTypeProperty = Atom.the("DatatypeProperty");

    /**
     * Saves the relation into the database. Both entities must exist if the
     * relation is to be saved. Takes care of updating relation_types as well.
     *
     */
    private void input(final Atom subject, final Atom predicate, final Term object) {

        //http://www.w3.org/TR/owl-ref/

        Compound belief = null;

        if (predicate.equals(parentOf) || predicate.equals(type)
                ||predicate.equals(subClassOf)||predicate.equals(subPropertyOf)) {

            if (object.equals(owlClass)) {
                return;
            }

            if (!includeDataType) {
                if (object.equals(dataTypeProperty)) {
                    return;
                }
            }

            belief = (Inheritance.make(subject, object));

        }
        else if (predicate.equals(equivalentClass)) {
            belief = (Equivalence.make(subject, object));
        }
        else if (predicate.equals(sameAs)) {
            belief = (Similarity.make(subject, object));
            //belief = (Equivalence.make(subject, object));
        }
        else if (predicate.equals(domain)) {
            // PROPERTY domain CLASS
            //<PROPERTY($subj, $obj) ==> <$subj {-- CLASS>>.

            belief = nar.term(
                //"<" + subject + "($subj,$obj) ==> <$subj {-- " + object + ">>"
                    "(" + subject + "($subj,$obj) && <$subj {-- " + object + ">)"
            );
            //System.err.println(belief);
        }
        else if (predicate.equals(range)) {
            // PROPERTY range CLASS
            //<PROPERTY($subj, $obj) ==> <$obj {-- CLASS>>.
            belief = nar.term(
                    //"<" + subject + "($subj,$obj) ==> <$obj {-- " + object + ">>"
                    "(" + subject + "($subj,$obj) && <$obj {-- " + object + ">)"
            );

        }
        else if (predicate.equals(equivalentProperty)) {
            belief = (Equivalence.make(subject, object));
        }
        else if (predicate.equals(inverseOf)) {

            //TODO: PREDSUBJ(#subj, #obj) <=> PREDOBJ(#obj, #subj)
        }
        else if (predicate.equals(disjointWith)) {
            //System.out.println(subject + " " + predicate + " " + object);
            belief = (Compound)Negation.make(Similarity.make(subject, object));
        }
        else {
            //System.out.println(subject + " " + predicate + " " + object);
            belief = (Operation.make(predicate,
                    Product.make(subject, object)));
        }

        if (belief!=null) {
            believe(belief);
        }

    }



    // ======== String manipulation methods ========
    /**
     * Format the XML tag. Takes as input the QName of the tag, and formats it
     * to a namespace:tagname format.
     *
     * @param qname the QName for the tag.
     * @return the formatted QName for the tag.
     */
    private String formatTag(QName qname) {
        String prefix = qname.getPrefix();
        String suffix = qname.getLocalPart();

        suffix = suffix.replace("http://dbpedia.org/ontology/", "");

        if (prefix == null || prefix.length() == 0) {
            return suffix;
        } else {
            return prefix + ":" + suffix;
        }
    }

    /**
     * Split up Uppercase Camelcased names (like Java classnames or C++ variable
     * names) into English phrases by splitting wherever there is a transition
     * from lowercase to uppercase.
     *
     * @param name the input camel cased name.
     * @return the "english" name.
     */
    private String getEnglishName(String name) {
        StringBuilder englishNameBuilder = new StringBuilder();
        char[] namechars = name.toCharArray();
        for (int i = 0; i < namechars.length; i++) {
            if (i > 0 && Character.isUpperCase(namechars[i])
                    && Character.isLowerCase(namechars[i - 1])) {
                englishNameBuilder.append(' ');
            }
            englishNameBuilder.append(namechars[i]);
        }
        return englishNameBuilder.toString();
    }

    public static void main(String[] args) throws Exception {
        Default d = new Default(1000,16,3).setInternalExperience(null).level(7);
        //Solid d = new Solid(32, 4096,1,3,1,2);
        d.setInternalExperience(null).level(7);
        d.inputsMaxPerCycle.set(256);
        d.setTermLinkBagSize(64);



        NAR n = new NAR(d);
        //n.input("schizo(I)!"); //needs nal8



        new NQuadsInput(n, "/home/me/Downloads/dbpedia.n4", 0.94f /* conf */) {

            @Override
            protected void believe(Compound assertion) {
                float freq = 1.0f;

                //insert with zero priority to bypass main memory go directly to subconcepts
                n.believe(0.01f, Global.DEFAULT_JUDGMENT_DURABILITY, assertion, Tense.Eternal, freq, beliefConfidence);
            }
        };


        //new TextOutput(n, System.out).setShowStamp(false).setOutputPriorityMin(0.25f);

        n.runWhileInputting(1);
        n.frame(1); //one more to be sure



        Video.themeInvert();
        new NARSwing(n);

/*
        //n.frame(100);
        n.believe(0.75f, 0.8f, n.term("<Spacecraft <-> PopulatedPlace>!"),
                Tense.Eternal, 1.0f, 0.95f);
        n.believe(0.75f, 0.8f, n.term("(&&,SpaceMission,Work). :|:"),
                Tense.Eternal, 1.0f, 0.95f);
        //n.frame(5000);
*/
        //n.run();
    }
}

