package nars;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import nars.java.AtomObject;
import nars.nal.Compounds;
import nars.nal.nal7.CyclesInterval;
import nars.nal.nal8.Operator;
import nars.task.MutableTask;
import nars.term.Term;
import nars.term.Terms;
import nars.term.atom.Atom;
import nars.term.compile.TermIndex;
import nars.term.compound.Compound;
import nars.term.match.VarPattern;
import nars.term.variable.Variable;
import nars.truth.Truth;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

import static nars.Op.*;

/**
 * core utility class for:
    --building any type of value, either programmatically or parsed from string input
       (which can be constructed in a static context)
    --??
 */
public abstract class $  {

    public final static TermIndex terms = new TermIndex.UncachedTermIndex();


    public static final org.slf4j.Logger logger = LoggerFactory.getLogger($.class);

    public static final <T extends Term> T $(String term) {
        return (T)Narsese.the().term(term);
        //        try { }
        //        catch (InvalidInputException e) { }
    }

    public static final <C extends Compound> MutableTask $(String term, char punc) {
        C t = Narsese.the().term(term);
        if (t == null) return null;
        return new MutableTask(t)
                .punctuation(punc)
                .eternal();
                //.normalized();
    }

    public static <O> AtomObject<O> ref(String term, O instance) {
        return new AtomObject(term, instance);
    }

    public static Atom the(String id) {
        return Atom.the(id);
    }

    public static Atom[] the(String... id) {
        int l = id.length;
        Atom[] x = new Atom[l];
        for (int i = 0; i < l; i++)
            x[i] = Atom.the(id[i]);
        return x;
    }


    public static Atom the(int i) {
        return Atom.the(i);
    }

    /**
     * Op.INHERITANCE from 2 Terms: subj --> pred
     *  returns a Term if the two inputs are equal to each other
     */
    public static <T extends Term> T inh(Term subj, Term pred) {

//        if ((predicate instanceof Operator) && if (subject instanceof Product))
//            return new GenericCompound(Op.INHERITANCE, (Operator)predicate, (Product)subject);
//        else

        return (T) the(INHERIT, subj, pred);
    }


    public static <T extends Term> T inh(String subj, String pred) {
        return inh((Term)$(subj), $(pred));
    }


    public static Term sim(Term subj, Term pred) {
        return the(SIMILAR, subj, pred);
    }

    public static Compound oper(String operator, String... args) {
        return oper(Operator.the(operator), $.p(args));
    }


    public static Compound oper(Operator opTerm, Term... arg) {
        return oper(opTerm, $.p(arg));
    }

    public static Compound oper(Atom opTerm, Compound args) {
        return oper(new Operator(opTerm), args);
    }

    public static Compound oper(Operator opTerm, Compound arg) {
        return (Compound) the(
                INHERIT,
                arg == null ? Compounds.Empty : arg,
                opTerm
        );
    }


    public static Term impl(Term a, Term b) {
        return the(IMPLICATION, a, b);
    }

    public static Term neg(Term x) {
        return the(NEGATE, x);
    }

    public static CyclesInterval cycles(int numCycles) {
        return CyclesInterval.make(numCycles);
    }
    public static <T extends Term> Compound<T> p(Collection<? super T> t) {
        return $.p(t.toArray((T[]) new Term[t.size()]));
    }

    public static Compound p(Term... t) {
        if (t == null)
            return Compounds.Empty;

        int l = t.length;
        if (l == 0) //length 0 product are allowd and shared
            return Compounds.Empty;

        return (Compound) the(PRODUCT, t);
    }

    /** creates from a sublist of a list */
    static Compound p(List<Term> l, int from, int to) {
        Term[] x = new Term[to - from];

        for (int j = 0, i = from; i < to; i++)
            x[j++] = l.get(i);

        return $.p(x);
    }

    public static Compound<Atom> p(String... t) {
        return $.p($.the(t));
    }

    public static Variable v(Op type, String s) {
        return v(type.ch, s);
    }


    public static Variable varDep(int i) {
        return v(VAR_DEP, i);
    }

    public static Variable varDep(String s) {
        return v(VAR_DEP, s);
    }

    public static Variable varIndep(int i) {
        return v(VAR_INDEP, i);
    }

    public static Variable varIndep(String s) {
        return v(VAR_INDEP, s);
    }

    public static Variable varQuery(int i) {
        return v(VAR_QUERY, i);
    }

    public static Variable varQuery(String s) {
        return v(VAR_QUERY, s);
    }

    public static Variable varPattern(int i) {
        return v(VAR_PATTERN, i);
    }

    public static Variable varPattern(String s) {
        return v(VAR_PATTERN, s);
    }

    /**
     * Try to make a new compound from two components. Called by the logic rules.
     * <p>
     *  A {-- B becomes {A} --> B
     * @param subj The first component
     * @param pred The second component
     * @return A compound generated or null
     */
    public static Compound inst(Term subj, Term pred) {
        return (Compound) $.inh($.sete(subj), pred);
    }


    /**
     * Try to make a new compound from two components. Called by the logic rules.
     * <p>
     *  A {-] B becomes {A} --> [B]
     * @param subject The first component
     * @param predicate The second component
     * @return A compound generated or null
     */
    public static final Compound instprop(Term subject, Term predicate) {
        return (Compound) $.inh( $.sete(subject), $.seti(predicate));
    }

//    public static Term term(final Op op, final Term... args) {
//        return Terms.term(op, args);
//    }

    public static MutableTask belief(Compound term, Truth copyFrom) {
        return belief(term, copyFrom.getFrequency(), copyFrom.getConfidence());
    }

    public static MutableTask belief(Compound term, float freq, float conf) {
        return new MutableTask(term).belief().truth(freq, conf);
    }

    public static MutableTask goal(Compound term, float freq, float conf) {
        return new MutableTask(term).goal().truth(freq, conf);
    }

    public static Term implAfter(Term condition, Term consequence) {
        return the(IMPLICATION_AFTER, condition, consequence);
    }

    public static Compound sete(Collection<Term> t) {
        return $.sete(array(t));
    }

   private static Term[] array(Collection<Term> t) {
        return t.toArray(new Term[t.size()]);
    }

    public static Compound seti(Collection<Term> t) {
        return $.seti(array(t));
    }

    public static Compound sete(Term... t) {
        return (Compound) the(SET_EXT, t);
    }

    /** shorthand for extensional set */
    public static Compound s(Term... t) {
        return sete(t);
    }

    public static Compound seti(Term... t) {
        return (Compound) the(SET_INT, t);
    }

    /**
     * Try to make a new compound from two components. Called by the logic rules.
     * <p>
     *  A --] B becomes A --> [B]
     * @param subject The first component
     * @param predicate The second component
     * @return A compound generated or null
     */
    public static Term property(Term subject, Term predicate) {
        return inh(subject, $.seti(predicate));
    }

    public static Variable v(char ch, String name) {

//        if (name.length() < 3) {
//            int digit = Texts.i(name, -1);
//            if (digit != -1) {
//                Op op = Variable.typeIndex(ch);
//                return Variable.the(op, digit);
//            }
//        }

        switch (ch) {
            case Symbols.VAR_DEPENDENT:
                return new Variable.VarDep(name);
            case Symbols.VAR_INDEPENDENT:
                return new Variable.VarIndep(name);
            case Symbols.VAR_QUERY:
                return new Variable.VarQuery(name);
            case Symbols.VAR_PATTERN:
                return new VarPattern(name);
            default:
                throw new RuntimeException("invalid variable type: " + ch);
        }

    }

    public static Variable v(Op type, int counter) {
        if (counter < Variable.MAX_VARIABLE_CACHED_PER_TYPE) {
            Variable[] vct = Variable.varCache[Variable.typeIndex(type)];
            Variable existing = vct[counter];
            return existing != null ? existing : (vct[counter] = Variable._the(type, counter));
        }

        return v(type.ch, String.valueOf(counter));
    }

    public static Term conj(Term... a) {
        return the(CONJUNCTION, a);
    }

    public static Term disj(Term... a) {
        return the(DISJUNCTION, a);
    }

    static {
//        // assume SLF4J is bound to logback in the current environment
//        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
//
//        try {
//            JoranConfigurator configurator = new JoranConfigurator();
//            configurator.setContext(context);
//            // Call context.reset() to clear any previous configuration, e.g. default
//            // configuration. For multi-step configuration, omit calling context.reset().
//            context.reset();
//            //configurator.doConfigure(args[0]);
//        } catch (Exception je) {
//            // StatusPrinter will handle this
//        }
//        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
//
//        Logger logger = LoggerFactory.getLogger($.class);
//        logger.info("Entering application.");
//
//
//
//        logger.info("Exiting application.");
//
//        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
//        // print logback's internal status
//        StatusPrinter.print(lc);
//
//        // assume SLF4J is bound to logback-classic in the current environment
//        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
//        loggerContext.start();
//        //loggerContext.stop();
    }

    public static final Logger logRoot;

    /** NALogging non-axiomatic logging encoder. log events expressed in NAL terms */
    public static final PatternLayoutEncoder logEncoder;

    static {
        Thread.currentThread().setName("$");

        //http://logback.qos.ch/manual/layouts.html

        logRoot = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        LoggerContext loggerContext = logRoot.getLoggerContext();
        // we are not interested in auto-configuration
        loggerContext.reset();

        logEncoder = new PatternLayoutEncoder();
        logEncoder.setContext(loggerContext);
        logEncoder.setPattern("\\( %highlight(%level),%green(%thread),%yellow(%logger{0}) \\): \"%message\".%n");
        logEncoder.start();


        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(loggerContext);
        appender.setEncoder(logEncoder);
        appender.start();
        logRoot.addAppender(appender);

//        rootLogger.debug("Message 1");
//        rootLogger.info("Message 1");
//        rootLogger.warn("Message 2");
//        rootLogger.error("Message 2");
    }
    public static void main(String[] args) {

    }

    public static Term equiv(Term subject, Term pred) {
        return the(EQUIV, subject, pred);
    }
    public static Term equivAfter(Term subject, Term pred) {
        return the(EQUIV_AFTER, subject, pred);
    }
    public static Term equivWhen(Term subject, Term pred) {
        return the(EQUIV_WHEN, subject, pred);
    }

    public static Term diffInt(Term a, Term b) {
        return the(DIFF_INT, a, b);
    }

    public static Term diffExt(Term a, Term b) {
        return the(DIFF_EXT, a, b);
    }

    public static Term imageExt(Term... x) {
        return the(IMAGE_EXT, x);
    }
    public static Term imageInt(Term... x) {
        return the(IMAGE_INT, x);
    }
    public static Term sect(Term... x) {
        return the(INTERSECT_EXT, x);
    }
    public static Term sectInt(Term... x) {
        return the(INTERSECT_INT, x);
    }

    public static Operator operator(String name) {
        return new Operator($.the(name));
    }


    public static Term the(Op op, Term... subterms) {
        return terms.the(op, subterms);
    }
    public static Term the(Op op, Term[] subterms, int relation) {
        return terms.the(op, subterms, relation);
    }
    public static Term the(Op op, Collection<Term> subterms, int relation) {
        return terms.the(op, Terms.toArray(subterms), relation);
    }

}
