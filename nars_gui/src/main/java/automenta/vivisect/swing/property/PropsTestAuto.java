package automenta.vivisect.swing.property;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Date;

public class PropsTestAuto implements PropertyChangeListener {

    @Property
    byte x = 23;

    @Property
    long y = 10;

    @Property
    Rectangle r = new Rectangle(10, 20);

    @Property(category = "Advanced stuff!", name = "A file", editable = false)
    File f = new File("/home/zp/Destop/x.tdt");

    @Property(name = "This is the date", description = "Enter any date you want")
    Date date = new Date();

    float d = 10.0f;

    String s = "sdfsdf";

    @Property
    Line2D.Double line = new Line2D.Double(10, 10, 11, 11);

    public float getD() {
        return d;
    }

    public void setD(float d) {
        this.d = d;
    }

    
    public static void main(String[] args) throws Exception {
        PropsTestAuto pt = new PropsTestAuto();
        PropertyUtils.editProperties(null, pt, true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println(evt);
    }
}
