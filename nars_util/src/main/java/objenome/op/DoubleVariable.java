package objenome.op;

/** optimized variation of Variable for double values, avoids some tests
 * TODO make a superclass abstract to avoid any Double boxing

 * */
public class DoubleVariable extends Variable<Double> {

    public DoubleVariable(String name) {
        super(name, Double.class);
    }

    @Override
    public void setValue(final Double value) {
        //skip superclass's type checking
        this.value = value;
    }

}
