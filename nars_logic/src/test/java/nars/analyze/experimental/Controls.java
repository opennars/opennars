package nars.analyze.experimental;

import objenome.op.DoubleVariable;
import objenome.op.Variable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds a set of parameters which can be controlled:
 *   --
 */
public class Controls {

    private final List<Variable> variables = new ArrayList();

    public Controls() {
    }


    public Controls(Object... components) {
        for (Object o : components)
            reflect(o);
    }

    /** adds all meters which exist as fields of a given object (via reflection) */
    public void reflect(Object obj) {
        Class c = obj.getClass();
        Class variable = DoubleVariable.class;
        for (Field f : c.getFields()) {

//System.out.println("field: " + f.getType() + " " + f.isAccessible() + " " + Meter.class.isAssignableFrom( f.getType() ));

            if ( variable.isAssignableFrom( f.getType() ) ) {
                Variable m = null;
                try {
                    m = (Variable)f.get(obj);
                } catch (IllegalAccessException e) {
                    //TODO ignore or handle errors?
                }
                if (m!=null)
                    add(m);
            }
        }
    }

    public Variable add(Variable v) {
        variables.add(v);
        return v;
    }

    public List<Variable> getVariables() {
        return variables;
    }
}
