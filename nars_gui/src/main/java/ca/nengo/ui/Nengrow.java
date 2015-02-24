package ca.nengo.ui;


import org.simplericity.macify.eawt.Application;
import org.simplericity.macify.eawt.DefaultApplication;

abstract public class Nengrow extends AbstractNengo {

    public Nengrow() {
        this(new DefaultApplication());
    }
    public Nengrow(Application app) {
        super();
        setApplication(app);
    }

    @Override
    protected void initialize() {
        super.initialize();

        add(getUniverse());

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    abstract public void init() throws Exception;

/*    public static void main(String[] args) {
        new Nengrow();
    }*/
}
