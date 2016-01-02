/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.solution;

import objenome.Phenotainer;
import objenome.goal.Between;
import objenome.solution.dependency.DecideImplementationClass;

/** stores a double value between 0...N which is used to select equally
 *  from the list of N classes in its creator Multiclass
 */
public class SetImplementationClass implements SetNumericValue {
    public final DecideImplementationClass multiclass;
    double value;

    public SetImplementationClass(DecideImplementationClass multiclass, @Between(min=0,max=1) double normalizedValue) {
        value = normalizedValue;
        this.multiclass = multiclass;
    }

    public Class getValue() {
        int num = multiclass.size();
        int which = (int) (value * multiclass.size());
        if (which == num) {
            which = num - 1;
        }
        return multiclass.implementors.get(which);
    }

    
    @Override public void apply(Phenotainer c) { 
        c.remove(multiclass.abstractClass);
        c.use(multiclass.abstractClass, getValue());
    }

    @Override
    public String toString() {
        return "ClassSelect(" + multiclass +") => " + getValue();
    }

    @Override
    public String key() {
        //Path?
        return "ClassSelect("+multiclass.abstractClass+')';
    }

    @Override
    public Double getMin() {
        return 0.0d;
    }

    @Override
    public Double getMax() {
        return 1.0d;
    }

    @Override
    public Number getNumber() {
        return value;
    }

    @Override
    public void setValue(double d) {
        value = d;
    }

    @Override
    public void mutate() {
        setValue( Math.random() * (getMax() - getMin()) + getMin() );
    }    
}
