/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.io;

import nars.Events;
import nars.Events.ERR;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.io.narsese.InvalidInputException;
import nars.io.narsese.OldNarseseParser;
import nars.nal.Sentence;
import nars.nal.nal8.ImmediateOperation;
import nars.nal.nal8.Operator;
import nars.nal.nal8.SynchronousSentenceFunction;
import nars.nal.nal8.TermFunction;
import nars.nal.term.Term;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Collection;

/**
 * @author me
 */
public class BindJavascriptExpression implements TextReaction {

    public static final String BINDING_SYMBOL = ":=";
    private final OldNarseseParser narsese;
    private final NAR nar;
    ScriptEngine js;

    public BindJavascriptExpression(NAR nar) {
        super();
        this.nar = nar;
        this.narsese = new OldNarseseParser(nar, null /* TODO */);
    }


    @Override
    public Object react(String input) {
        if (input.contains(BINDING_SYMBOL)) {
            String[] p = input.split(BINDING_SYMBOL);
            if (p.length != 2) {
                throw new RuntimeException("Invalid syntax for expression binding");
            }

            String newOp = Operator.addPrefixIfMissing(p[0].trim());
            Operator existing = nar.memory.operator(newOp);
            if (existing != null) {
                throw new RuntimeException("Unable to bind new expression to existing Operator: " + existing);
            }

            String proc = p[1].trim();
            if (!proc.startsWith("js{")) {
                throw new RuntimeException("Unrecognized expression format: " + proc);
            }
            if (!proc.endsWith("}") && !proc.endsWith("}sentence")) {
                throw new RuntimeException("Expression must end with '}'");
            }

            boolean sentence = proc.trim().endsWith("}sentence");
            if (sentence) {
                proc = proc.replaceAll("}sentence", "}");
            }
            proc = proc.substring(3, proc.length() - 1).trim();


            if (js == null) {
                ScriptEngineManager factory = new ScriptEngineManager();
                js = factory.getEngineByName("JavaScript");
            }

            final String o = newOp.substring(1);
            String jsFunc = "function " + o + "($1, $2, $3, $4, $5, $6) { " + proc + "; }";


            return new ImmediateOperation() {

                @Override
                public void execute(Memory m) {
                    try {




                        js.eval(jsFunc);
                        js.put("memory", nar.memory);

                        if (sentence) {

                            nar.on(new SynchronousSentenceFunction(newOp) {


                                @Override
                                protected Collection<Sentence> function(Memory memory, Term[] args) {


                                    Object result = evalJS(o, args, memory);


                                    //TODO handle result being an array or collection

                                    try {
                                        throw new RuntimeException("disabled sentence parsing temporarly");
                                        //return Collections.singleton(narsese.parseSentence(new StringBuilder(result.toString())));
                                    } catch (InvalidInputException ex) {
                                        memory.emit(ERR.class, ex.toString());
                                        if (Global.DEBUG)
                                            ex.printStackTrace();
                                    }

                                    return null;
                                }

                            });


                        } else {

                            nar.on(new TermFunction(newOp) {


                                @Override
                                public Term function(Term[] args) {
                                    Object result = evalJS(o, args, getMemory());

                                    try {
                                        return narsese.parseTerm(result.toString());
                                    } catch (InvalidInputException ex) {
                                        getMemory().emit(ERR.class, ex.toString());
                                        if (Global.DEBUG)
                                            ex.printStackTrace();
                                    }

                                    return null;
                                }

                            });


                        }

                        nar.memory.emit(Events.OUT.class, "Bound: " + jsFunc);

                    } catch (ScriptException ex) {
                        throw new RuntimeException(ex.toString(), ex);
                    }

                }
            };

        }
        return null;
    }

    protected <X> X evalJS(String o, Term[] args, final Memory memory) {


        StringBuilder argsToParameters = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            argsToParameters.append("'").append(args[i].toString()).append("'");
            if (args.length - 1 != i)
                argsToParameters.append(",");
        }


        Object result = null;
        try {

            //bind dynamic variables with values as they are at the time the operation is invoked
            js.put("$SELF", memory.getSelf());

            result = js.eval(o + "(" + argsToParameters + ")");

        } catch (ScriptException ex) {
            throw new RuntimeException("Exception in executing " + this + ": " + ex.toString(), ex);
        }
        return (X) result;
    }

}
