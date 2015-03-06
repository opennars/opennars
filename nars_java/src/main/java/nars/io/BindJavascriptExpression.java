/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.io;

import nars.core.Events;
import nars.core.Events.ERR;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.io.narsese.InvalidInputException;
import nars.io.narsese.Narsese;
import nars.logic.entity.Sentence;
import nars.logic.entity.Term;
import nars.logic.nal8.ImmediateOperation;
import nars.logic.nal8.Operator;
import nars.logic.nal8.SynchronousSentenceFunction;
import nars.logic.nal8.SynchronousTermFunction;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Collection;
import java.util.Collections;

/**
 * @author me
 */
public class BindJavascriptExpression implements TextReaction {

    public static final String BINDING_SYMBOL = ":=";
    private final Narsese narsese;
    private final NAR nar;
    ScriptEngine js;

    public BindJavascriptExpression(NAR nar) {
        super();
        this.nar = nar;
        this.narsese = new Narsese(nar);
    }


    @Override
    public Object react(String input) {
        if (input.contains(BINDING_SYMBOL)) {
            String[] p = input.split(BINDING_SYMBOL);
            if (p.length != 2) {
                throw new RuntimeException("Invalid syntax for expression binding");
            }

            String newOp = Operator.addPrefixIfMissing(p[0].trim());
            Operator existing = nar.memory.getOperator(newOp);
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

                            nar.addPlugin(new SynchronousSentenceFunction(newOp) {


                                @Override
                                protected Collection<Sentence> function(Memory memory, Term[] args) {



                                    Object result = evalJS(o, args, memory);


                                    //TODO handle result being an array or collection

                                    try {
                                        return Collections.singleton(narsese.parseSentence(new StringBuilder(result.toString())));
                                    } catch (InvalidInputException ex) {
                                        memory.emit(ERR.class, ex.toString());
                                        if (Parameters.DEBUG)
                                            ex.printStackTrace();
                                    }

                                    return null;
                                }

                            });


                        } else {

                            nar.addPlugin(new SynchronousTermFunction(newOp) {


                                @Override
                                public Term function(Memory memory, Term[] args) {
                                    Object result = evalJS(o, args, memory);

                                    try {
                                        return narsese.parseTerm(result.toString());
                                    } catch (InvalidInputException ex) {
                                        memory.emit(ERR.class, ex.toString());
                                        if (Parameters.DEBUG)
                                            ex.printStackTrace();
                                    }

                                    return null;
                                }

                                @Override
                                protected Term getRange() {
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
