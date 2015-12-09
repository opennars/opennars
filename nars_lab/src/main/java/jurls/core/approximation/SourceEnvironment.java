/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

/**
 *
 * @author thorsten2
 */
public class SourceEnvironment {

    private int nextVariable = 1;
    private final StringBuilder stringBuilder = new StringBuilder();

    public String allocateVariable() {
        String v = "x" + nextVariable++;
        append("double ").append(v).append(";").nl();
        return v;
    }

    public SourceEnvironment assign(String var) {
        stringBuilder.append(var).append(" = ");

        return this;
    }

    public SourceEnvironment additiveAssign(String var) {
        stringBuilder.append(var).append(" += ");

        return this;
    }
    public SourceEnvironment append(String s) {
        stringBuilder.append(s);

        return this;
    }

    public SourceEnvironment append(int i) {
        stringBuilder.append(i);

        return this;
    }

    public void nl() {
        stringBuilder.append('\n');
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
