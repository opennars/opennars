package nars.analyze.cfg.method;

import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InvokeInstruction;

/**
 * Created by me on 1/15/15.
 */
public class CGMethodCall extends CGMethod {

    public final CGMethod method;
    private final String key;
    public Instruction at;

    public CGMethodCall(CGMethod method, InvokeInstruction at) {
        super(method.jc, method.mg, method.ii);
        this.method = method;
        this.at = at;
        this.className = method.className;
        this.methodName = method.methodName;
        this.argumentTypes = method.argumentTypes;
        this.key = method.key() + "|" + at.toString();
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return key;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public boolean equals(Object obj) {
        return key.equals(((CGMethod)obj).key());
    }


    @Override
    protected void pre() {
        //nothing
    }

    @Override
    protected void post() {
        this.throwing = method.throwing;
        this.returnType = method.returnType;
    }
}

