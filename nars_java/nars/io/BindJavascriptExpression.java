/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.io;

import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import nars.core.Memory;
import nars.entity.Task;
import nars.io.Output.EXE;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;

/**
 *
 * @author me
 */
public class BindJavascriptExpression implements TextReaction {
    
    ScriptEngine js;
    
    public static final String BINDING_SYMBOL = ":=";
    private final Memory memory;

    public BindJavascriptExpression(Memory memory) {
        super();
        this.memory = memory;
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
                
                memory.addOperator(new Operator(newOp) {
                    @Override protected List<Task> execute(Operation operation, Term[] args, Memory memory) {
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
                            throw new RuntimeException("Exception in executing " + operation + ": " + ex.toString(), ex);
                        }
                        
                        return null;
                    }                    
                });
                
                memory.emit(EXE.class, "Bound: "+ jsFunc);
            } catch (ScriptException ex) {
                throw new RuntimeException(ex.toString(), ex);
            }
            
        
        
            return true;
            
            
        }
        return null;
    }
    
}
