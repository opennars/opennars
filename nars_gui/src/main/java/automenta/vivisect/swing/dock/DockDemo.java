package automenta.vivisect.swing.dock;

import automenta.vivisect.swing.NWindow;
import javax.swing.JButton;
import javax.swing.JFrame;


/**
 *
 * @author me
 * @see https://java.net/projects/raven/sources/svn/show/trunk/proj/RavenDocking/src/com/kitfox/docking/test?rev=116
 * @see https://java.net/projects/raven/sources/svn/content/trunk/proj/RavenDocking/src/com/kitfox/docking/test/DockingTestFrame2.java?rev=116
 */
public class DockDemo extends DockingRegionRoot {

    int index;

    public DockDemo() {
        super();
        
        DockingContent cont = new DockingContent("uid" + index, "Component " + (index++), new JButton("x"));
        
        DockingContent cont2 = new DockingContent("uid" + index, "Component " + (index++), new JButton("y"));
        
        getDockingRoot().addDockContent(cont);
        getDockingRoot().addDockContent(cont2);
        
    }

    
    
    public static void main(String[] args)     {
        NWindow w = new NWindow("Dock Test", new DockDemo());
        
        w.setSize(640, 480);
        w.setVisible(true);
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


}
