/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.io;

import nars.config.Parameters;
import nars.io.Output.ERR;
import nars.language.Term;
import nars.operator.Operator;
import nars.operator.SynchronousFunctionOperator;
import nars.storage.Memory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author me
 */
public class BindJavascriptExpression implements TextReaction {
    
    ScriptEngine js;
    
    public static final String BINDING_SYMBOL = ":=";
    private final Memory memory;
    private final Narsese narsese;

    public BindJavascriptExpression(Memory memory) {
        super();
        this.memory = memory;
        this.narsese = new Narsese(memory);
    }
    
    
    @Override
    public Object react(String input) {
        if (input.contains(BINDING_SYMBOL)) {
            String[] p = input.split(BINDING_SYMBOL);
            if (p.length!=2) {
                throw new RuntimeException("Invalid syntax for expression binding");
            }
            
            String newOp = Operator.addPrefixIfMissing(p[0].trim());
            Operator existing = memory.getOperator(newOp);
            if (existing!=null) {
                throw new RuntimeException("Unable to bind new expression to existing Operator: " + existing);
            }
            
            String proc = p[1].trim();
            if (!proc.startsWith("js{")) {
                throw new RuntimeException("Unrecognized expression format: " + proc);
            }
            if (!proc.endsWith("}")) {
                throw new RuntimeException("Expression must end with '}'");
            }
            proc = proc.substring(3, proc.length()-1).trim();
            
            
            if (js == null) {
                ScriptEngineManager factory = new ScriptEngineManager();
                js = factory.getEngineByName("JavaScript");
            }


            
            final String o = newOp.substring(1);
            try {
                
                String jsFunc = "function " + o + "($1, $2, $3, $4, $5, $6) { " + proc + "; }";
                
                js.eval(jsFunc);
                js.put("memory", memory);
                
                memory.addOperator(new SynchronousFunctionOperator(newOp) {
                    

                    @Override
                    protected Term function(Memory memory, Term[] args) {
                       StringBuilder argsToParameters = new StringBuilder();
                        for (int i = 0; i < args.length; i++) {
                            argsToParameters.append("'").append(args[i].toString()).append("'");
                            if (args.length-1 != i) 
                                argsToParameters.append(",");
                        }
                        
                        
                        Object result = null;
                        try {
                            result = js.eval(o + "(" + argsToParameters + ")");
                        } catch (ScriptException ex) {
                            throw new RuntimeException("Exception in executing " + this + ": " + ex.toString(), ex);
                        }
                        
                        
                        try {
                            return narsese.parseTerm(result.toString());
                        } catch (Narsese.InvalidInputException ex) {
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
                
                //memory.emit(OUT.class, "Bound: "+ jsFunc);
            } catch (ScriptException ex) {
                throw new RuntimeException(ex.toString(), ex);
            }
            
        
        
            return true;
            
            
        }
        return null;
    }
    
}
