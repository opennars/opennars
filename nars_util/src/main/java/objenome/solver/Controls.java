package objenome.solver;

import objenome.op.Variable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds a set of parameters which can be controlled:
 *   --
 */
public interface Controls {


    /** adds all meters which exist as fields of a given object (via reflection) */
    static List<Variable> reflect(Class c, Object obj) {
        List<Variable> variables = new ArrayList();

        Class variable = Variable.class;
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
                    variables.add(m);
            }
        }
        return variables;
    }

}
